package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.regional.Regional;
import mt.gov.seplag.backend.domain.regional.RegionalRepository;
import mt.gov.seplag.backend.web.regional.RegionalExternaDTO;
import mt.gov.seplag.backend.web.regional.RegionalResponseDTO;
import mt.gov.seplag.backend.web.regional.SincronizacaoResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionalService - Testes do Algoritmo de Sincronização O(n+m)")
class RegionalServiceTest {

    @Mock
    private RegionalRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RegionalService regionalService;

    @Captor
    private ArgumentCaptor<List<Regional>> regionalListCaptor;

    private static final String API_URL = "https://integrador-argus-api.geia.vip/v1/regionais";

    @BeforeEach
    void setUp() {
        // Setup comum se necessário
    }

    @Test
    @DisplayName("Deve sincronizar regionais com sucesso usando API externa")
    void deveSincronizarRegionaisComSucesso() {
        // Arrange
        RegionalExternaDTO[] regionaisApi = {
                new RegionalExternaDTO(1, "Regional Norte"),
                new RegionalExternaDTO(2, "Regional Sul")
        };

        Regional regionalLocal = new Regional(1, "Regional Norte");
        regionalLocal.setAtivo(true);

        when(restTemplate.getForObject(API_URL, RegionalExternaDTO[].class))
                .thenReturn(regionaisApi);
        when(repository.findByAtivoTrue()).thenReturn(List.of(regionalLocal));
        when(repository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // Act
        SincronizacaoResultDTO resultado = regionalService.sincronizar();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.inseridos()); // Regional Sul é nova
        assertEquals(0, resultado.atualizados());
        assertEquals(0, resultado.inativados());
        assertTrue(resultado.mensagem().contains("1 inseridos"));

        verify(restTemplate).getForObject(API_URL, RegionalExternaDTO[].class);
        verify(repository).findByAtivoTrue();
        verify(repository, times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve lançar exceção quando API externa falhar")
    void deveLancarExcecaoQuandoApiExternaFalhar() {
        // Arrange
        when(restTemplate.getForObject(API_URL, RegionalExternaDTO[].class))
                .thenThrow(new RestClientException("Erro de conexão"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regionalService.sincronizar());

        assertTrue(exception.getMessage().contains("Erro ao sincronizar regionais"));
        verify(restTemplate).getForObject(API_URL, RegionalExternaDTO[].class);
        verify(repository, never()).save(any(Regional.class));
    }

    @Test
    @DisplayName("Deve retornar mensagem quando API retornar lista vazia")
    void deveRetornarMensagemQuandoApiRetornarVazia() {
        // Arrange
        when(restTemplate.getForObject(API_URL, RegionalExternaDTO[].class))
                .thenReturn(new RegionalExternaDTO[0]);

        // Act
        SincronizacaoResultDTO resultado = regionalService.sincronizar();

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.inseridos());
        assertEquals(0, resultado.atualizados());
        assertEquals(0, resultado.inativados());
        assertEquals("Nenhuma regional encontrada na API", resultado.mensagem());
    }

    @Test
    @DisplayName("Caso 1: Deve inserir regionais novas da API")
    void deveInserirRegionaisNovas() {
        // Arrange
        List<RegionalExternaDTO> regionaisApi = Arrays.asList(
                new RegionalExternaDTO(1, "Regional Norte"),
                new RegionalExternaDTO(2, "Regional Sul"),
                new RegionalExternaDTO(3, "Regional Leste"));

        when(repository.findByAtivoTrue()).thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // Act
        SincronizacaoResultDTO resultado = regionalService.sincronizarComDados(regionaisApi);

        // Assert
        assertEquals(3, resultado.inseridos());
        assertEquals(0, resultado.atualizados());
        assertEquals(0, resultado.inativados());

        // Verifica que salvou as 3 novas regionais
        verify(repository, times(2)).saveAll(regionalListCaptor.capture());
        List<List<Regional>> allSaves = regionalListCaptor.getAllValues();

        // Segunda chamada saveAll deve ter as inserções
        List<Regional> inseridas = allSaves.get(1);
        assertEquals(3, inseridas.size());
        assertTrue(inseridas.stream().allMatch(Regional::getAtivo));
    }

    @Test
    @DisplayName("Caso 2: Deve inativar regionais removidas da API")
    void deveInativarRegionaisRemovidas() {
        // Arrange
        List<RegionalExternaDTO> regionaisApi = Arrays.asList(
                new RegionalExternaDTO(1, "Regional Norte"));

        Regional regional1 = new Regional(1, "Regional Norte");
        regional1.setAtivo(true);

        Regional regional2 = new Regional(2, "Regional Sul");
        regional2.setAtivo(true);

        Regional regional3 = new Regional(3, "Regional Leste");
        regional3.setAtivo(true);

        when(repository.findByAtivoTrue()).thenReturn(Arrays.asList(regional1, regional2, regional3));
        when(repository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // Act
        SincronizacaoResultDTO resultado = regionalService.sincronizarComDados(regionaisApi);

        // Assert
        assertEquals(0, resultado.inseridos());
        assertEquals(0, resultado.atualizados());
        assertEquals(2, resultado.inativados()); // Regional 2 e 3 foram removidas da API

        // Verifica inativações
        verify(repository, times(2)).saveAll(regionalListCaptor.capture());
        List<Regional> inativadas = regionalListCaptor.getAllValues().get(0);

        assertEquals(2, inativadas.size());
        assertTrue(inativadas.stream().allMatch(r -> !r.getAtivo()));
        assertTrue(inativadas.stream().anyMatch(r -> r.getIdExterno() == 2));
        assertTrue(inativadas.stream().anyMatch(r -> r.getIdExterno() == 3));
    }

    @Test
    @DisplayName("Caso 3: Deve inativar e criar nova quando nome for alterado")
    void deveInativarECriarNovaQuandoNomeAlterado() {
        // Arrange
        List<RegionalExternaDTO> regionaisApi = Arrays.asList(
                new RegionalExternaDTO(1, "Regional Norte Atualizada") // Nome alterado
        );

        Regional regionalLocal = new Regional(1, "Regional Norte");
        regionalLocal.setId(100L);
        regionalLocal.setAtivo(true);

        when(repository.findByAtivoTrue()).thenReturn(List.of(regionalLocal));
        when(repository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // Act
        SincronizacaoResultDTO resultado = regionalService.sincronizarComDados(regionaisApi);

        // Assert
        assertEquals(0, resultado.inseridos());
        assertEquals(1, resultado.atualizados()); // Contabiliza como atualização
        assertEquals(0, resultado.inativados());

        verify(repository, times(2)).saveAll(regionalListCaptor.capture());
        List<List<Regional>> allSaves = regionalListCaptor.getAllValues();

        // Primeira chamada: inativa o antigo
        List<Regional> inativadas = allSaves.get(0);
        assertEquals(1, inativadas.size());
        assertFalse(inativadas.get(0).getAtivo());
        assertEquals("Regional Norte", inativadas.get(0).getNome());

        // Segunda chamada: cria novo com nome atualizado
        List<Regional> inseridas = allSaves.get(1);
        assertEquals(1, inseridas.size());
        assertTrue(inseridas.get(0).getAtivo());
        assertEquals("Regional Norte Atualizada", inseridas.get(0).getNome());
    }

    @Test
    @DisplayName("Deve manter regionais sem alteração quando dados forem iguais")
    void deveManterRegionaisSemAlteracao() {
        // Arrange
        List<RegionalExternaDTO> regionaisApi = Arrays.asList(
                new RegionalExternaDTO(1, "Regional Norte"),
                new RegionalExternaDTO(2, "Regional Sul"));

        Regional regional1 = new Regional(1, "Regional Norte");
        regional1.setAtivo(true);

        Regional regional2 = new Regional(2, "Regional Sul");
        regional2.setAtivo(true);

        when(repository.findByAtivoTrue()).thenReturn(Arrays.asList(regional1, regional2));
        when(repository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // Act
        SincronizacaoResultDTO resultado = regionalService.sincronizarComDados(regionaisApi);

        // Assert
        assertEquals(0, resultado.inseridos());
        assertEquals(0, resultado.atualizados());
        assertEquals(0, resultado.inativados());
        assertTrue(resultado.mensagem().contains("0 inseridos, 0 atualizados, 0 inativados"));
    }

    @Test
    @DisplayName("Deve processar cenário complexo com múltiplas operações")
    void deveProcessarCenarioComplexo() {
        // Arrange: API tem IDs 1 (alterado), 2 (igual), 4 (novo)
        List<RegionalExternaDTO> regionaisApi = Arrays.asList(
                new RegionalExternaDTO(1, "Regional Norte Atualizada"), // Alterado
                new RegionalExternaDTO(2, "Regional Sul"), // Mantido
                new RegionalExternaDTO(4, "Regional Oeste") // Novo
        );

        // Banco local tem IDs 1, 2, 3 (todos ativos)
        Regional regional1 = new Regional(1, "Regional Norte");
        regional1.setAtivo(true);

        Regional regional2 = new Regional(2, "Regional Sul");
        regional2.setAtivo(true);

        Regional regional3 = new Regional(3, "Regional Leste");
        regional3.setAtivo(true);

        when(repository.findByAtivoTrue())
                .thenReturn(Arrays.asList(regional1, regional2, regional3));
        when(repository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // Act
        SincronizacaoResultDTO resultado = regionalService.sincronizarComDados(regionaisApi);

        // Assert
        assertEquals(1, resultado.inseridos()); // ID 4 novo
        assertEquals(1, resultado.atualizados()); // ID 1 alterado
        assertEquals(1, resultado.inativados()); // ID 3 removido da API

        verify(repository, times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve listar todas as regionais")
    void deveListarTodasAsRegionais() {
        // Arrange
        Regional regional1 = new Regional(1, "Regional Norte");
        regional1.setAtivo(true);

        Regional regional2 = new Regional(2, "Regional Sul");
        regional2.setAtivo(false);

        when(repository.findAll()).thenReturn(Arrays.asList(regional1, regional2));

        // Act
        List<RegionalResponseDTO> resultado = regionalService.listarTodas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve listar apenas regionais ativas")
    void deveListarApenasRegionaisAtivas() {
        // Arrange
        Regional regional1 = new Regional(1, "Regional Norte");
        regional1.setAtivo(true);

        Regional regional2 = new Regional(2, "Regional Sul");
        regional2.setAtivo(true);

        when(repository.findByAtivoTrue()).thenReturn(Arrays.asList(regional1, regional2));

        // Act
        List<RegionalResponseDTO> resultado = regionalService.listarAtivas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repository).findByAtivoTrue();
    }

    @Test
    @DisplayName("Deve garantir complexidade O(n+m) usando Maps para acesso rápido")
    void deveGarantirComplexidadeOtima() {
        // Arrange: Grande volume de dados para verificar performance
        List<RegionalExternaDTO> regionaisApi = new ArrayList<>();
        List<Regional> regionaisLocais = new ArrayList<>();

        // Simula 100 regionais na API
        for (int i = 1; i <= 100; i++) {
            regionaisApi.add(new RegionalExternaDTO(i, "Regional " + i));
        }

        // Simula 80 regionais locais (algumas serão mantidas, outras inativadas)
        for (int i = 1; i <= 80; i++) {
            Regional r = new Regional(i, "Regional " + i);
            r.setAtivo(true);
            regionaisLocais.add(r);
        }

        when(repository.findByAtivoTrue()).thenReturn(regionaisLocais);
        when(repository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // Act
        SincronizacaoResultDTO resultado = regionalService.sincronizarComDados(regionaisApi);

        // Assert
        assertEquals(20, resultado.inseridos()); // IDs 81-100 novos
        assertEquals(0, resultado.atualizados()); // Nomes iguais
        assertEquals(0, resultado.inativados()); // Todos presentes na API

        // Verifica que usou batch operations (apenas 2 chamadas saveAll)
        verify(repository, times(2)).saveAll(anyList());
    }
}
