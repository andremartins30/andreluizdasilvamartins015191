package mt.gov.seplag.backend.service;

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

    public Page<Album> listar(String artist, Pageable pageable) {
        if (artist != null && !artist.isBlank()) {
            return repository.findByArtist_NameContainingIgnoreCase(artist, pageable);
        }
        return repository.findAll(pageable);
    }
}
