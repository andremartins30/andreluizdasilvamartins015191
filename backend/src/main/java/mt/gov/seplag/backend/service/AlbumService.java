package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.web.album.AlbumResponseDTO;

import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AlbumService {

    private final AlbumRepository repository;

    public AlbumService(AlbumRepository repository) {
        this.repository = repository;
    }

    public Page<AlbumResponseDTO> listar(String artist, Pageable pageable) {
        Page<Album> albums;

        if (artist != null && !artist.isBlank()) {
            albums = repository.findByArtist_NameContainingIgnoreCase(artist, pageable);
        } else {
            albums = repository.findAll(pageable);
        }

        return albums.map(a -> new AlbumResponseDTO(
                a.getId(),
                a.getTitle(),
                a.getArtist().getName()
        ));
    }


}