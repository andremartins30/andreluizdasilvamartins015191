package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.web.artist.ArtistRequestDTO;
import mt.gov.seplag.backend.web.artist.ArtistResponseDTO;

import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import org.springframework.stereotype.Service;
import mt.gov.seplag.backend.shared.exception.NotFoundException;

import java.util.List;

@Service
public class ArtistService {

    private final ArtistRepository repository;

    public ArtistService(ArtistRepository repository) {
        this.repository = repository;
    }

    public List<ArtistResponseDTO> listarTodos() {
    return repository.findAll()
        .stream()
        .map(a -> new ArtistResponseDTO(a.getId(), a.getName()))
        .toList();
    }

    public ArtistResponseDTO salvar(ArtistRequestDTO dto) {
    Artist artist = new Artist();
    artist.setName(dto.getName());

    Artist salvo = repository.save(artist);

    return new ArtistResponseDTO(salvo.getId(), salvo.getName());

    }

    public ArtistResponseDTO buscarPorId(Long id) {
    Artist artist = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Artista não encontrado"));

    return new ArtistResponseDTO(artist.getId(), artist.getName());
}
}