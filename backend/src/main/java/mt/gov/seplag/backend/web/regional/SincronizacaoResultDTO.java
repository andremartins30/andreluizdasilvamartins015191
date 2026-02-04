package mt.gov.seplag.backend.web.regional;

public record SincronizacaoResultDTO(
        int inseridos,
        int atualizados,
        int inativados,
        String mensagem
) {}
