package mt.gov.seplag.backend.security.dto;

public record TokenResponseDTO(
    String accessToken,
    String refreshToken
) {}