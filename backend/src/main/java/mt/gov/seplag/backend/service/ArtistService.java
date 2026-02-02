package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.web.artist.ArtistRequestDTO;
import mt.gov.seplag.backend.web.artist.ArtistResponseDTO;

import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import mt.gov.seplag.backend.shared.exception.NotFoundException;

import java.util.List;

@Service
public class ArtistService {

    private final ArtistRepository repository;
    private final AlbumRepository albumRepository;

    public ArtistService(ArtistRepository repository, AlbumRepository albumRepository) {
        this.repository = repository;
        this.albumRepository = albumRepository;
    }

    public Page<ArtistResponseDTO> listarTodos(String name, Pageable pageable) {
        Page<Artist> artists;
        
        if (name != null && !name.isEmpty()) {
            artists = repository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            artists = repository.findAll(pageable);
        }
        
        return artists.map(this::toDTO);
    }

    public ArtistResponseDTO salvar(ArtistRequestDTO dto) {
        Artist artist = new Artist();
        artist.setName(dto.getName());

        Artist salvo = repository.save(artist);

        return toDTO(salvo);
    }

    public ArtistResponseDTO buscarPorId(Long id) {
        Artist artist = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Artista não encontrado"));

        return toDTO(artist);
    }

    public ArtistResponseDTO atualizar(Long id, ArtistRequestDTO dto) {
        Artist artist = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Artista não encontrado"));

        artist.setName(dto.getName());

        Artist atualizado = repository.save(artist);

        return toDTO(atualizado);
    }

    public void remover(Long id) {
        Artist artist = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Artista não encontrado"));

        repository.delete(artist);
    }
    
    private ArtistResponseDTO toDTO(Artist artist) {
        long albumsCount = albumRepository.countByArtist(artist);
        return new ArtistResponseDTO(artist.getId(), artist.getName(), albumsCount);
    }
}