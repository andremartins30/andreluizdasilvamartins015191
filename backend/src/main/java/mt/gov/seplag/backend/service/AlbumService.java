package mt.gov.seplag.backend.service;


import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.web.album.AlbumRequestDTO;
import mt.gov.seplag.backend.web.album.AlbumResponseDTO;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    public AlbumService(AlbumRepository albumRepository, ArtistRepository artistRepository) {
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
    }

    public Page<AlbumResponseDTO> listar(String artist, Pageable pageable) {
        Page<Album> albums;

        if (artist != null && !artist.isBlank()) {
            albums = albumRepository.findByArtist_NameContainingIgnoreCase(artist, pageable);
        } else {
            albums = albumRepository.findAll(pageable);
        }

        return albums.map(a -> new AlbumResponseDTO(
                a.getId(),
                a.getTitle(),
                a.getArtist().getId(),
                a.getArtist().getName()
        ));
    }

    public AlbumResponseDTO buscarPorId(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        return toResponse(album);
    }

    public AlbumResponseDTO criar(AlbumRequestDTO dto) {
        Artist artist = artistRepository.findById(dto.artistId())
                .orElseThrow(() -> new NotFoundException("Artista não encontrado"));

        Album album = new Album(dto.title(), artist);

        Album salvo = albumRepository.save(album);

        return toResponse(salvo);
    }

    public AlbumResponseDTO atualizar(Long id, AlbumRequestDTO dto) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        Artist artist = artistRepository.findById(dto.artistId())
                .orElseThrow(() -> new NotFoundException("Artista não encontrado"));

        album.setTitle(dto.title());
        album.setArtist(artist);

        Album atualizado = albumRepository.save(album);

        return toResponse(atualizado);
    }

    public void remover(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        albumRepository.delete(album);
    }

    private AlbumResponseDTO toResponse(Album album) {
        return new AlbumResponseDTO(
                album.getId(),
                album.getTitle(),
                album.getArtist().getId(),
                album.getArtist().getName()
        );
    }


}