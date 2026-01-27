package mt.gov.seplag.backend.web.auth;

public record LoginRequestDTO(
        String username,
        String password
) {}