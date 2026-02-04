package mt.gov.seplag.backend.service;


import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import mt.gov.seplag.backend.domain.album.AlbumCoverRepository;
import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.service.storage.MinioService;
import mt.gov.seplag.backend.web.album.AlbumRequestDTO;
import mt.gov.seplag.backend.web.album.AlbumResponseDTO;
import mt.gov.seplag.backend.web.album.AlbumCoverResDTO;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import mt.gov.seplag.backend.shared.exception.BusinessException;
import mt.gov.seplag.backend.shared.exception.FileValidationException;
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
    private final AlbumNotificationService notificationService;
    private final AlbumCoverRepository albumCoverRepository;

    public AlbumService(MinioService minioService, 
                       AlbumRepository albumRepository, 
                       ArtistRepository artistRepository,
                       AlbumNotificationService notificationService,
                       AlbumCoverRepository albumCoverRepository) {
        this.minioService = minioService;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.notificationService = notificationService;
        this.albumCoverRepository = albumCoverRepository;
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

        // Enviar notificação via WebSocket
        notificationService.notifyNewAlbum(salvo.getId(), salvo.getTitle(), artist.getName());

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
        List<MultipartFile> files = List.of(file);
        return uploadCovers(albumId, files);
    }

    public AlbumCoverResDTO uploadCovers(Long albumId, List<MultipartFile> files) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        if (files == null || files.isEmpty()) {
            throw new FileValidationException("Nenhum arquivo foi enviado");
        }

        java.util.List<String> uploadedUrls = new java.util.ArrayList<>();
        String lastObjectName = null;

        for (MultipartFile file : files) {
            // Validação de arquivo vazio
            if (file.isEmpty()) {
                continue; // Pular arquivos vazios
            }

            // Validação de tipo de arquivo
            if (!List.of("image/jpeg", "image/png", "image/jpg").contains(file.getContentType())) {
                throw new FileValidationException("Formato de imagem inválido. Apenas JPEG e PNG são permitidos");
            }

            // Validação de tamanho (5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                throw new FileValidationException("Imagem excede o tamanho máximo permitido de 5MB");
            }

            // Upload da nova capa
            String objectName = minioService.upload(file, albumId);
            lastObjectName = objectName;

            // Criar registro de capa
            mt.gov.seplag.backend.domain.album.AlbumCover cover = 
                new mt.gov.seplag.backend.domain.album.AlbumCover(album, objectName);
            albumCoverRepository.save(cover);

            String url = minioService.generatePresignedUrl(objectName);
            uploadedUrls.add(url);
        }

        // Manter compatibilidade com cover_object_name (última imagem)
        if (lastObjectName != null) {
            album.setCoverObjectName(lastObjectName);
            albumRepository.save(album);
        }

        return new AlbumCoverResDTO(
                album.getId(),
                lastObjectName,
                uploadedUrls.isEmpty() ? null : uploadedUrls.get(uploadedUrls.size() - 1)
        );
    }

    public String getCoverUrl(Long id) {
        Album album = albumRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        if (album.getCoverObjectName() == null) {
            return null; // Retorna null ao invés de erro
        }

        return minioService.generatePresignedUrl(album.getCoverObjectName());   
    }

    public java.util.List<String> getAllCoverUrls(Long id) {
        Album album = albumRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        java.util.List<mt.gov.seplag.backend.domain.album.AlbumCover> covers = 
            albumCoverRepository.findByAlbumId(id);

        return covers.stream()
            .map(cover -> minioService.generatePresignedUrl(cover.getObjectName()))
            .collect(java.util.stream.Collectors.toList());
    }


}