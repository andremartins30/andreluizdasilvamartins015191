package mt.gov.seplag.backend.web.artist;

import jakarta.validation.constraints.NotBlank;

public class ArtistRequestDTO {
    @NotBlank(message = "Nome é Obrigatório!")
    private String name;

    public String getName() {
        return name;
    }
}