package mt.gov.seplag.backend.web.auth;

public record RegisterRequestDTO(
        String username,
        String password
) {}