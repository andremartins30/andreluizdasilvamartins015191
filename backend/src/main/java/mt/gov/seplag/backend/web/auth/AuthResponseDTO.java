package mt.gov.seplag.backend.web.auth;

public record AuthResponseDTO(
        String token,
        String refreshToken
) {}