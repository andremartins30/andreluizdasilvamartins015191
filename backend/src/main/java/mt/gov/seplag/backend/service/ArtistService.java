package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.web.artist.ArtistRequestDTO;
import mt.gov.seplag.backend.web.artist.ArtistResponseDTO;

import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import mt.gov.seplag.backend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import mt.gov.seplag.backend.shared.exception.ForbiddenException;

import java.util.List;

@Service
public class ArtistService {

    private final ArtistRepository repository;
    private final AlbumRepository albumRepository;
    private final UserService userService;

    public ArtistService(ArtistRepository repository, AlbumRepository albumRepository, UserService userService) {
        this.repository = repository;
        this.albumRepository = albumRepository;
        this.userService = userService;
    }

    public Page<ArtistResponseDTO> listarTodos(String name, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<Artist> artists;

        if (name != null && !name.isEmpty()) {
            artists = repository.findByUserAndNameContainingIgnoreCase(currentUser, name, pageable);
        } else {
            artists = repository.findByUser(currentUser, pageable);
        }

        return artists.map(this::toDTO);
    }

    public ArtistResponseDTO salvar(ArtistRequestDTO dto) {
        User currentUser = userService.getCurrentUser();
        
        Artist artist = new Artist();
        artist.setName(dto.getName());
        artist.setUser(currentUser);

        Artist salvo = repository.save(artist);

        return toDTO(salvo);
    }

    public ArtistResponseDTO buscarPorId(Long id) {
        User currentUser = userService.getCurrentUser();
        
        Artist artist = repository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Artista não encontrado"));

        return toDTO(artist);
    }

    public ArtistResponseDTO atualizar(Long id, ArtistRequestDTO dto) {
        User currentUser = userService.getCurrentUser();
        
        Artist artist = repository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Artista não encontrado"));

        artist.setName(dto.getName());

        Artist atualizado = repository.save(artist);

        return toDTO(atualizado);
    }

    @Transactional
    public void remover(Long id) {
        User currentUser = userService.getCurrentUser();
        
        Artist artist = repository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Artista não encontrado"));

        // Deleta todos os álbuns do artista antes de deletar o artista
        // O cascade para AlbumCover garante que as capas também sejam removidas
        albumRepository.deleteByArtist(artist);

        repository.delete(artist);
    }

    private ArtistResponseDTO toDTO(Artist artist) {
        User currentUser = userService.getCurrentUser();
        long albumsCount = albumRepository.countByUserAndArtist(currentUser, artist);
        return new ArtistResponseDTO(artist.getId(), artist.getName(), albumsCount);
    }
}