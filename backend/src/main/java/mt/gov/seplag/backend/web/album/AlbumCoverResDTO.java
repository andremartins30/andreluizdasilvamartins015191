package mt.gov.seplag.backend.web.album;

public record AlbumCoverResDTO(
        Long albumId,
        String objectName,
        String coverUrl
) {}