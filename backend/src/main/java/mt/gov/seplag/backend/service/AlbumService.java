package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import mt.gov.seplag.backend.domain.album.AlbumCoverRepository;
import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.domain.user.User;
import mt.gov.seplag.backend.service.storage.MinioService;
import mt.gov.seplag.backend.web.album.AlbumRequestDTO;
import mt.gov.seplag.backend.web.album.AlbumResponseDTO;
import mt.gov.seplag.backend.web.album.AlbumCoverResDTO;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import mt.gov.seplag.backend.shared.exception.BusinessException;
import mt.gov.seplag.backend.shared.exception.FileValidationException;
import mt.gov.seplag.backend.shared.exception.ForbiddenException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final MinioService minioService;
    private final AlbumNotificationService notificationService;
    private final AlbumCoverRepository albumCoverRepository;
    private final UserService userService;

    public AlbumService(MinioService minioService,
            AlbumRepository albumRepository,
            ArtistRepository artistRepository,
            AlbumNotificationService notificationService,
            AlbumCoverRepository albumCoverRepository,
            UserService userService) {
        this.minioService = minioService;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.notificationService = notificationService;
        this.albumCoverRepository = albumCoverRepository;
        this.userService = userService;
    }

    public Page<AlbumResponseDTO> listar(String artist, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<Album> albums;

        if (artist != null && !artist.isBlank()) {
            albums = albumRepository.findByUserAndArtist_NameContainingIgnoreCase(currentUser, artist, pageable);
        } else {
            albums = albumRepository.findByUser(currentUser, pageable);
        }

        return albums.map(a -> new AlbumResponseDTO(
                a.getId(),
                a.getTitle(),
                a.getArtist().getId(),
                a.getArtist().getName()));
    }

    public AlbumResponseDTO buscarPorId(Long id) {
        User currentUser = userService.getCurrentUser();
        
        Album album = albumRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        return toResponse(album);
    }

    public AlbumResponseDTO criar(AlbumRequestDTO dto) {
        User currentUser = userService.getCurrentUser();
        
        Artist artist = artistRepository.findByIdAndUser(dto.artistId(), currentUser)
                .orElseThrow(() -> new NotFoundException("Artista não encontrado ou não pertence ao usuário"));

        Album album = new Album(dto.title(), artist, currentUser);

        Album salvo = albumRepository.save(album);

        // Enviar notificação via WebSocket
        notificationService.notifyNewAlbum(salvo.getId(), salvo.getTitle(), artist.getName());

        return toResponse(salvo);
    }

    public AlbumResponseDTO atualizar(Long id, AlbumRequestDTO dto) {
        User currentUser = userService.getCurrentUser();
        
        Album album = albumRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        Artist artist = artistRepository.findByIdAndUser(dto.artistId(), currentUser)
                .orElseThrow(() -> new NotFoundException("Artista não encontrado ou não pertence ao usuário"));

        album.setTitle(dto.title());
        album.setArtist(artist);

        Album atualizado = albumRepository.save(album);

        return toResponse(atualizado);
    }

    @Transactional
    public void remover(Long id) {
        User currentUser = userService.getCurrentUser();
        
        Album album = albumRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Álbum não encontrado"));

        albumRepository.delete(album);
    }

    /**
     * Remove todos os álbuns do usuário autenticado
     * @return quantidade de álbuns removidos
     */
    @Transactional
    public long removerTodos() {
        User currentUser = userService.getCurrentUser();
        
        long count = albumRepository.countByUser(currentUser);
        
        if (count > 0) {
            albumRepository.deleteAllByUser(currentUser);
        }
        
        return count;
    }

    private AlbumResponseDTO toResponse(Album album) {
        return new AlbumResponseDTO(
                album.getId(),
                album.getTitle(),
                album.getArtist().getId(),
                album.getArtist().getName());
    }

    public AlbumCoverResDTO uploadCover(Long albumId, MultipartFile file) {
        List<MultipartFile> files = List.of(file);
        return uploadCovers(albumId, files);
    }

    public AlbumCoverResDTO uploadCovers(Long albumId, List<MultipartFile> files) {
        User currentUser = userService.getCurrentUser();
        
        Album album = albumRepository.findByIdAndUser(albumId, currentUser)
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
            mt.gov.seplag.backend.domain.album.AlbumCover cover = new mt.gov.seplag.backend.domain.album.AlbumCover(
                    album, objectName);
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
                uploadedUrls.isEmpty() ? null : uploadedUrls.get(uploadedUrls.size() - 1));
    }

    public String getCoverUrl(Long id) {
        User currentUser = userService.getCurrentUser();
        
        Album album = albumRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ForbiddenException("Álbum não encontrado ou acesso negado"));

        if (album.getCoverObjectName() == null) {
            return null;
        }

        return minioService.generatePresignedUrl(album.getCoverObjectName());
    }

    public java.util.List<String> getAllCoverUrls(Long id) {
        User currentUser = userService.getCurrentUser();
        
        Album album = albumRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ForbiddenException("Álbum não encontrado ou acesso negado"));

        java.util.List<mt.gov.seplag.backend.domain.album.AlbumCover> covers = albumCoverRepository.findByAlbumId(id);

        return covers.stream()
                .map(cover -> minioService.generatePresignedUrl(cover.getObjectName()))
                .collect(java.util.stream.Collectors.toList());
    }

    public void deleteCover(Long albumId, String objectName) {
        User currentUser = userService.getCurrentUser();
        
        Album album = albumRepository.findByIdAndUser(albumId, currentUser)
                .orElseThrow(() -> new ForbiddenException("Álbum não encontrado ou acesso negado"));

        // Buscar a capa pelo objectName
        java.util.List<mt.gov.seplag.backend.domain.album.AlbumCover> covers = albumCoverRepository
                .findByAlbumId(albumId);

        mt.gov.seplag.backend.domain.album.AlbumCover coverToDelete = covers.stream()
                .filter(cover -> cover.getObjectName().equals(objectName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Capa não encontrada"));

        // Remover do MinIO
        minioService.removeObject(objectName);

        // Remover do banco de dados
        albumCoverRepository.delete(coverToDelete);

        // Se a capa deletada era a principal, atualizar
        if (objectName.equals(album.getCoverObjectName())) {
            // Buscar a primeira capa restante (se houver)
            java.util.List<mt.gov.seplag.backend.domain.album.AlbumCover> remainingCovers = albumCoverRepository
                    .findByAlbumId(albumId);

            if (!remainingCovers.isEmpty()) {
                album.setCoverObjectName(remainingCovers.get(0).getObjectName());
            } else {
                album.setCoverObjectName(null);
            }
            albumRepository.save(album);
        }
    }

}