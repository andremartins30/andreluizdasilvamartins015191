package mt.gov.seplag.backend.security.dto;

public record LoginRequestDTO(
    String username,
    String password
) {}