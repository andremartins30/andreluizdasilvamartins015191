package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.regional.Regional;
import mt.gov.seplag.backend.domain.regional.RegionalRepository;
import mt.gov.seplag.backend.web.regional.RegionalExternaDTO;
import mt.gov.seplag.backend.web.regional.RegionalResponseDTO;
import mt.gov.seplag.backend.web.regional.SincronizacaoResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegionalService {

    private static final Logger logger = LoggerFactory.getLogger(RegionalService.class);
    private static final String API_URL = "https://integrador-argus-api.geia.vip/v1/regionais";

    private final RegionalRepository repository;
    private final RestTemplate restTemplate;

    public RegionalService(RegionalRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }


    @Transactional
    public SincronizacaoResultDTO sincronizar() {
        try {
            logger.info("Iniciando sincronização de regionais");

            // Busca regionais da API externa
            RegionalExternaDTO[] regionaisApi = restTemplate.getForObject(API_URL, RegionalExternaDTO[].class);
            
            if (regionaisApi == null || regionaisApi.length == 0) {
                logger.warn("Nenhuma regional retornada pela API");
                return new SincronizacaoResultDTO(0, 0, 0, "Nenhuma regional encontrada na API");
            }

            return sincronizarComDados(Arrays.asList(regionaisApi));

        } catch (Exception e) {
            logger.error("Erro ao sincronizar regionais", e);
            throw new RuntimeException("Erro ao sincronizar regionais: " + e.getMessage(), e);
        }
    }

    @Transactional
    public SincronizacaoResultDTO sincronizarComDados(List<RegionalExternaDTO> regionaisApiList) {
        try {
            logger.info("Sincronizando {} regionais", regionaisApiList.size());

            // Converte lista da API em Map para acesso O(1) por ID externo
            Map<Integer, RegionalExternaDTO> mapaApi = regionaisApiList.stream()
                    .collect(Collectors.toMap(RegionalExternaDTO::id, r -> r));

            // Busca APENAS regionais ativas localmente e agrupa por ID externo
            // Complexidade: O(m) onde m = registros no banco
            List<Regional> regionaisAtivas = repository.findByAtivoTrue();
            Map<Integer, Regional> mapaLocalAtivo = regionaisAtivas.stream()
                    .collect(Collectors.toMap(Regional::getIdExterno, r -> r));

            // Listas para batch de operações
            List<Regional> paraSalvar = new ArrayList<>();
            List<Regional> paraInativar = new ArrayList<>();
            Set<Integer> processados = new HashSet<>();
            int inseridos = 0;
            int atualizados = 0;
            int inativados = 0;

            // Processa regionais da API - Complexidade: O(n)
            for (RegionalExternaDTO regionalApi : regionaisApiList) {
                processados.add(regionalApi.id());
                Regional regionalLocalAtiva = mapaLocalAtivo.get(regionalApi.id());

                if (regionalLocalAtiva == null) {
                    // Caso 1: Novo no endpoint → inserir na tabela local
                    Regional novaRegional = new Regional(regionalApi.id(), regionalApi.nome());
                    paraSalvar.add(novaRegional);
                    inseridos++;
                    logger.debug("Regional {} será inserida: {}", regionalApi.id(), regionalApi.nome());
                    
                } else if (!regionalLocalAtiva.getNome().equals(regionalApi.nome())) {
                    // Caso 3: Atributo alterado → inativar registro anterior e criar novo
                    regionalLocalAtiva.setAtivo(false);
                    paraInativar.add(regionalLocalAtiva);
                    
                    Regional novaRegional = new Regional(regionalApi.id(), regionalApi.nome());
                    paraSalvar.add(novaRegional);
                    atualizados++;
                    logger.info("Regional {} alterada de '{}' para '{}'", 
                        regionalApi.id(), regionalLocalAtiva.getNome(), regionalApi.nome());
                }
                // Se existe, está ativo e nome igual → não faz nada (já sincronizado)
            }

            // Caso 2: Não disponível no endpoint → inativar na tabela local
            // Complexidade: O(m)
            for (Regional regionalLocal : regionaisAtivas) {
                if (!processados.contains(regionalLocal.getIdExterno())) {
                    regionalLocal.setAtivo(false);
                    paraInativar.add(regionalLocal);
                    inativados++;
                    logger.info("Regional {} inativada (removida da API): {}", 
                        regionalLocal.getIdExterno(), regionalLocal.getNome());
                }
            }

            // Executa todas as operações em batch - 2 operações de banco
            repository.saveAll(paraInativar);
            repository.saveAll(paraSalvar);

            String mensagem = String.format("Sincronização concluída: %d inseridos, %d atualizados, %d inativados",
                    inseridos, atualizados, inativados);
            
            logger.info(mensagem);
            return new SincronizacaoResultDTO(inseridos, atualizados, inativados, mensagem);

        } catch (Exception e) {
            logger.error("Erro ao sincronizar regionais com dados fornecidos", e);
            throw new RuntimeException("Erro ao sincronizar regionais: " + e.getMessage(), e);
        }
    }

    public List<RegionalResponseDTO> listarTodas() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RegionalResponseDTO> listarAtivas() {
        return repository.findByAtivoTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private RegionalResponseDTO toDTO(Regional regional) {
        return new RegionalResponseDTO(
                regional.getIdExterno(),
                regional.getNome(),
                regional.getAtivo()
        );
    }
}
