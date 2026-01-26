package mt.gov.seplag.backend.web.album;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlbumRequestDTO(
    @NotBlank(message = "Título é obrigatório")
    String title,

    @NotNull(message = "artistId é obrigatório")
    Long artistId
) {}