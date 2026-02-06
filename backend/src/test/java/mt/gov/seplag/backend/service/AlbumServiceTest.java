package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.domain.album.AlbumCover;
import mt.gov.seplag.backend.domain.album.AlbumCoverRepository;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.domain.user.User;
import mt.gov.seplag.backend.service.storage.MinioService;
import mt.gov.seplag.backend.shared.exception.FileValidationException;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import mt.gov.seplag.backend.web.album.AlbumCoverResDTO;
import mt.gov.seplag.backend.web.album.AlbumRequestDTO;
import mt.gov.seplag.backend.web.album.AlbumResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumService - Testes Unitários")
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private MinioService minioService;

    @Mock
    private AlbumNotificationService notificationService;

    @Mock
    private AlbumCoverRepository albumCoverRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AlbumService albumService;

    private Artist artist;
    private Album album;
    private User mockUser;

    @BeforeEach
    void setUp() throws Exception {
        mockUser = new User("testuser", "password123");
        java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(mockUser, 1L);
        
        artist = new Artist("Test Artist", mockUser);
        // Usando Reflection para setar ID (entidade JPA não tem setter público)
        java.lang.reflect.Field artistIdField = Artist.class.getDeclaredField("id");
        artistIdField.setAccessible(true);
        artistIdField.set(artist, 1L);

        album = new Album("Test Album", artist, mockUser);
        java.lang.reflect.Field albumIdField = Album.class.getDeclaredField("id");
        albumIdField.setAccessible(true);
        albumIdField.set(album, 1L);
        
        // Mock padrão do usuário autenticado
        when(userService.getCurrentUser()).thenReturn(mockUser);
    }

    @Test
    @DisplayName("Deve listar todos os álbuns com paginação")
    void deveListarAlbunsPaginados() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));

        when(albumRepository.findByUser(mockUser, pageable)).thenReturn(albumPage);

        // Act
        Page<AlbumResponseDTO> resultado = albumService.listar(null, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Test Album", resultado.getContent().get(0).title());
        verify(albumRepository).findByUser(mockUser, pageable);
    }

    @Test
    @DisplayName("Deve listar álbuns filtrados por nome do artista")
    void deveListarAlbunsFiltradosPorArtista() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));

        when(albumRepository.findByUserAndArtist_NameContainingIgnoreCase(mockUser, "Test", pageable))
                .thenReturn(albumPage);

        // Act
        Page<AlbumResponseDTO> resultado = albumService.listar("Test", pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(albumRepository).findByUserAndArtist_NameContainingIgnoreCase(mockUser, "Test", pageable);
        verify(albumRepository, never()).findByUser(eq(mockUser), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar álbum por ID com sucesso")
    void deveBuscarAlbumPorId() {
        // Arrange
        when(albumRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(album));

        // Act
        AlbumResponseDTO resultado = albumService.buscarPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Test Album", resultado.title());
        assertEquals("Test Artist", resultado.artistName());
        verify(albumRepository).findByIdAndUser(1L, mockUser);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao buscar álbum inexistente")
    void deveLancarExcecaoAoBuscarAlbumInexistente() {
        // Arrange
        when(albumRepository.findByIdAndUser(999L, mockUser)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> albumService.buscarPorId(999L));

        assertEquals("Álbum não encontrado", exception.getMessage());
        verify(albumRepository).findByIdAndUser(999L, mockUser);
    }

    @Test
    @DisplayName("Deve criar álbum e enviar notificação WebSocket")
    void deveCriarAlbumComNotificacao() {
        // Arrange
        AlbumRequestDTO dto = new AlbumRequestDTO("New Album", 1L);

        when(artistRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(artist));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // Act
        AlbumResponseDTO resultado = albumService.criar(dto);

        // Assert
        assertNotNull(resultado);
        assertEquals("Test Album", resultado.title());
        verify(artistRepository).findByIdAndUser(1L, mockUser);
        verify(albumRepository).save(any(Album.class));
        verify(notificationService).notifyNewAlbum(
                eq(album.getId()),
                eq(album.getTitle()),
                eq(artist.getName()));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao criar álbum com artista inexistente")
    void deveLancarExcecaoAoCriarAlbumComArtistaInexistente() {
        // Arrange
        AlbumRequestDTO dto = new AlbumRequestDTO("New Album", 999L);

        when(artistRepository.findByIdAndUser(999L, mockUser)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> albumService.criar(dto));

        assertEquals("Artista não encontrado ou não pertence ao usuário", exception.getMessage());
        verify(artistRepository).findByIdAndUser(999L, mockUser);
        verify(albumRepository, never()).save(any(Album.class));
        verify(notificationService, never()).notifyNewAlbum(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve atualizar álbum com sucesso")
    void deveAtualizarAlbum() {
        // Arrange
        AlbumRequestDTO dto = new AlbumRequestDTO("Updated Album", 1L);

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // Act
        AlbumResponseDTO resultado = albumService.atualizar(1L, dto);

        // Assert
        assertNotNull(resultado);
        verify(albumRepository).findById(1L);
        verify(artistRepository).findById(1L);
        verify(albumRepository).save(album);
    }

    @Test
    @DisplayName("Deve remover álbum com sucesso")
    void deveRemoverAlbum() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // Act
        albumService.remover(1L);

        // Assert
        verify(albumRepository).findById(1L);
        verify(albumRepository).delete(album);
    }

    @Test
    @DisplayName("Deve fazer upload de capa única com validação de tipo")
    void deveFazerUploadDeCapaUnica() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L * 1024L); // 1MB

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(minioService.upload(file, 1L)).thenReturn("test-object-name");
        when(minioService.generatePresignedUrl("test-object-name")).thenReturn("http://minio/presigned-url");
        when(albumCoverRepository.save(any(AlbumCover.class))).thenAnswer(i -> i.getArgument(0));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // Act
        AlbumCoverResDTO resultado = albumService.uploadCover(1L, file);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.albumId());
        assertEquals("test-object-name", resultado.objectName());
        assertNotNull(resultado.coverUrl());
        verify(minioService).upload(file, 1L);
        verify(albumCoverRepository).save(any(AlbumCover.class));
        verify(albumRepository).save(album);
    }

    @Test
    @DisplayName("Deve fazer upload de múltiplas capas")
    void deveFazerUploadDeMultiplasCapas() {
        // Arrange
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);

        when(file1.isEmpty()).thenReturn(false);
        when(file1.getContentType()).thenReturn("image/jpeg");
        when(file1.getSize()).thenReturn(1024L * 1024L);

        when(file2.isEmpty()).thenReturn(false);
        when(file2.getContentType()).thenReturn("image/png");
        when(file2.getSize()).thenReturn(2048L * 1024L);

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(minioService.upload(any(MultipartFile.class), eq(1L)))
                .thenReturn("object-1", "object-2");
        when(minioService.generatePresignedUrl(anyString()))
                .thenReturn("http://url1", "http://url2");
        when(albumCoverRepository.save(any(AlbumCover.class))).thenAnswer(i -> i.getArgument(0));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // Act
        AlbumCoverResDTO resultado = albumService.uploadCovers(1L, Arrays.asList(file1, file2));

        // Assert
        assertNotNull(resultado);
        assertEquals("object-2", resultado.objectName()); // Última imagem
        verify(minioService, times(2)).upload(any(MultipartFile.class), eq(1L));
        verify(albumCoverRepository, times(2)).save(any(AlbumCover.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer upload de arquivo vazio")
    void deveLancarExcecaoAoUploadDeListaVazia() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // Act & Assert
        FileValidationException exception = assertThrows(FileValidationException.class,
                () -> albumService.uploadCovers(1L, Collections.emptyList()));

        assertEquals("Nenhum arquivo foi enviado", exception.getMessage());
        verify(minioService, never()).upload(any(MultipartFile.class), anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção para tipo de arquivo inválido")
    void deveLancarExcecaoParaTipoDeArquivoInvalido() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf"); // Tipo inválido

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // Act & Assert
        FileValidationException exception = assertThrows(FileValidationException.class,
                () -> albumService.uploadCover(1L, file));

        assertEquals("Formato de imagem inválido. Apenas JPEG e PNG são permitidos",
                exception.getMessage());
        verify(minioService, never()).upload(any(MultipartFile.class), anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção para arquivo maior que 5MB")
    void deveLancarExcecaoParaArquivoMuitoGrande() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(6L * 1024L * 1024L); // 6MB

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // Act & Assert
        FileValidationException exception = assertThrows(FileValidationException.class,
                () -> albumService.uploadCover(1L, file));

        assertEquals("Imagem excede o tamanho máximo permitido de 5MB",
                exception.getMessage());
        verify(minioService, never()).upload(any(MultipartFile.class), anyLong());
    }

    @Test
    @DisplayName("Deve retornar URL da capa do álbum")
    void deveRetornarUrlDaCapa() {
        // Arrange
        album.setCoverObjectName("cover-object");
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(minioService.generatePresignedUrl("cover-object"))
                .thenReturn("http://minio/cover-url");

        // Act
        String url = albumService.getCoverUrl(1L);

        // Assert
        assertNotNull(url);
        assertEquals("http://minio/cover-url", url);
        verify(minioService).generatePresignedUrl("cover-object");
    }

    @Test
    @DisplayName("Deve retornar null quando álbum não tem capa")
    void deveRetornarNullQuandoAlbumNaoTemCapa() {
        // Arrange
        album.setCoverObjectName(null);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // Act
        String url = albumService.getCoverUrl(1L);

        // Assert
        assertNull(url);
        verify(minioService, never()).generatePresignedUrl(anyString());
    }

    @Test
    @DisplayName("Deve retornar todas as URLs de capas do álbum")
    void deveRetornarTodasAsUrlsDeCapas() {
        // Arrange
        AlbumCover cover1 = new AlbumCover(album, "object-1");
        AlbumCover cover2 = new AlbumCover(album, "object-2");

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(albumCoverRepository.findByAlbumId(1L))
                .thenReturn(Arrays.asList(cover1, cover2));
        when(minioService.generatePresignedUrl("object-1")).thenReturn("http://url1");
        when(minioService.generatePresignedUrl("object-2")).thenReturn("http://url2");

        // Act
        List<String> urls = albumService.getAllCoverUrls(1L);

        // Assert
        assertNotNull(urls);
        assertEquals(2, urls.size());
        assertTrue(urls.contains("http://url1"));
        assertTrue(urls.contains("http://url2"));
        verify(minioService, times(2)).generatePresignedUrl(anyString());
    }

    @Test
    @DisplayName("Deve deletar capa específica e atualizar capa principal")
    void deveDeletarCapaEAtualizarPrincipal() {
        // Arrange
        album.setCoverObjectName("object-1");
        AlbumCover cover1 = new AlbumCover(album, "object-1");
        AlbumCover cover2 = new AlbumCover(album, "object-2");

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(albumCoverRepository.findByAlbumId(1L))
                .thenReturn(Arrays.asList(cover1, cover2))
                .thenReturn(List.of(cover2)); // Após deletar cover1

        // Act
        albumService.deleteCover(1L, "object-1");

        // Assert
        verify(minioService).removeObject("object-1");
        verify(albumCoverRepository).delete(cover1);
        verify(albumRepository).save(album);
        // Verifica se atualizou para a próxima capa disponível
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar capa inexistente")
    void deveLancarExcecaoAoDeletarCapaInexistente() {
        // Arrange
        when(albumRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(album));
        when(albumCoverRepository.findByAlbumId(1L))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> albumService.deleteCover(1L, "non-existent"));

        assertEquals("Capa não encontrada", exception.getMessage());
        verify(minioService, never()).removeObject(anyString());
        verify(albumCoverRepository, never()).delete(any(AlbumCover.class));
    }

    @Test
    @DisplayName("Deve definir coverObjectName como null quando deletar última capa")
    void deveDefinirCoverObjectNameNullQuandoDeletarUltimaCapa() {
        // Arrange
        album.setCoverObjectName("object-1");
        AlbumCover cover1 = new AlbumCover(album, "object-1");

        when(albumRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(album));
        when(albumCoverRepository.findByAlbumId(1L))
                .thenReturn(List.of(cover1))
                .thenReturn(Collections.emptyList()); // Após deletar

        // Act
        albumService.deleteCover(1L, "object-1");

        // Assert
        verify(minioService).removeObject("object-1");
        verify(albumCoverRepository).delete(cover1);
        verify(albumRepository).save(album);
    }

    @Test
    @DisplayName("Deve remover todos os álbuns do usuário autenticado")
    void deveRemoverTodosAlbunsDoUsuario() {
        // Arrange
        when(albumRepository.countByUser(mockUser)).thenReturn(5L);

        // Act
        long count = albumService.removerTodos();

        // Assert
        assertEquals(5L, count);
        verify(albumRepository).countByUser(mockUser);
        verify(albumRepository).deleteAllByUser(mockUser);
    }

    @Test
    @DisplayName("Deve retornar 0 ao tentar remover todos quando não há álbuns")
    void deveRetornarZeroQuandoNaoHaAlbuns() {
        // Arrange
        when(albumRepository.countByUser(mockUser)).thenReturn(0L);

        // Act
        long count = albumService.removerTodos();

        // Assert
        assertEquals(0L, count);
        verify(albumRepository).countByUser(mockUser);
        verify(albumRepository, never()).deleteAllByUser(any());
    }
}
