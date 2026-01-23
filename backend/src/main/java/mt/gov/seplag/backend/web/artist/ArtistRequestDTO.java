package mt.gov.seplag.backend.web.artist;

import jakarta.validation.constraints.NotBlank;

public record ArtistRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String name
) {}