package mt.gov.seplag.backend.service;


import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.service.storage.MinioService;
import mt.gov.seplag.backend.web.album.AlbumRequestDTO;
import mt.gov.seplag.backend.web.album.AlbumResponseDTO;
import mt.gov.seplag.backend.web.album.AlbumCoverResDTO;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import mt.gov.seplag.backend.shared.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final MinioService minioService;

    public AlbumService(MinioService minioService, AlbumRepository albumRepository, ArtistRepository artistRepository) {
        this.minioService = minioService;
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

    public AlbumCoverResDTO uploadCover(Long albumId, MultipartFile file) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        if (file.isEmpty()) {
            throw new BusinessException("Arquivo não pode estar vazio");
        }

        if (!List.of("image/jpeg", "image/png").contains(file.getContentType())) {
            throw new BusinessException("Formato de imagem inválido");
        }

        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new BusinessException("Imagem excede o tamanho máximo permitido");
        }

        String objectName = minioService.upload(file);

        album.setCoverObjectName(objectName);
        albumRepository.save(album);

        String url = minioService.generatePresignedUrl(objectName);

        return new AlbumCoverResDTO(
                album.getId(),
                objectName,
                url
        );
    }

    public String getCoverUrl(Long id) {
        Album album = albumRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        if (album.getCoverObjectName() == null) {
            throw new NotFoundException("Álbum não possui capa cadastrada");
        }

        return minioService.generatePresignedUrl(album.getCoverObjectName());   
    }


}