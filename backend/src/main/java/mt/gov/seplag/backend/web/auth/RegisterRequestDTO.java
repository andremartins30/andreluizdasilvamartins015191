package mt.gov.seplag.backend.web.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, message = "Username deve ter no mínimo 3 caracteres")
        String username,

        @NotBlank(message = "Password é obrigatório")
        @Size(min = 6, message = "Password deve ter no mínimo 6 caracteres")
        String password
) {}