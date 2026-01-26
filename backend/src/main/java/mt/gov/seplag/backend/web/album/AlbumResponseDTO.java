package mt.gov.seplag.backend.web.album;

public record AlbumResponseDTO(
        Long id,
        String title,
        Long artistId,
        String artistName
) {}
