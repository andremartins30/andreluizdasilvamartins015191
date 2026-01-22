package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistService {

    private final ArtistRepository repository;

    public ArtistService(ArtistRepository repository) {
        this.repository = repository;
    }

    public List<Artist> listarTodos() {
        return repository.findAll();
    }

    public Artist salvar(Artist artist) {
        return repository.save(artist);
    }
}