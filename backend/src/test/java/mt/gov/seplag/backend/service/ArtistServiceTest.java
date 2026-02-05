package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import mt.gov.seplag.backend.web.artist.ArtistRequestDTO;
import mt.gov.seplag.backend.web.artist.ArtistResponseDTO;
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
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistService - Testes Unitários Completos")
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private ArtistService artistService;

    @Test
    @DisplayName("Deve buscar artista por ID com sucesso")
    void buscarPorId_DeveRetornarArtista_QuandoExistir() {
        Artist artist = new Artist("Test Artist");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(albumRepository.countByArtist(any(Artist.class))).thenReturn(3L);

        ArtistResponseDTO result = artistService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals("Test Artist", result.name());
        assertEquals(3L, result.albumsCount());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar artista inexistente")
    void buscarPorId_DeveLancarExcecao_QuandoNaoExistir() {
        when(artistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> artistService.buscarPorId(999L));
    }

    @Test
    @DisplayName("Deve listar todos os artistas com paginação")
    void listarTodos_DeveRetornarPaginaDeArtistas() {
        Artist artist = new Artist("Test");
        Page<Artist> page = new PageImpl<>(List.of(artist));

        when(artistRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(albumRepository.countByArtist(any(Artist.class))).thenReturn(0L);

        Page<ArtistResponseDTO> result = artistService.listarTodos(null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve remover artista e seus álbuns")
    void remover_DeveRemoverArtista_QuandoExistir() {
        Artist artist = new Artist("Test");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        artistService.remover(1L);

        verify(albumRepository).deleteByArtist(artist);
        verify(artistRepository).delete(artist);
    }

    @Test
    @DisplayName("Deve salvar novo artista com sucesso")
    void salvar_DeveCriarNovoArtista() throws Exception {
        // Arrange
        ArtistRequestDTO dto = mock(ArtistRequestDTO.class);
        when(dto.getName()).thenReturn("Novo Artista");

        Artist savedArtist = new Artist("Novo Artista");
        java.lang.reflect.Field idField = Artist.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(savedArtist, 1L);

        when(artistRepository.save(any(Artist.class))).thenReturn(savedArtist);
        when(albumRepository.countByArtist(any(Artist.class))).thenReturn(0L);

        // Act
        ArtistResponseDTO result = artistService.salvar(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Novo Artista", result.name());
        assertEquals(0L, result.albumsCount());
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    @DisplayName("Deve atualizar artista existente")
    void atualizar_DeveModificarArtista_QuandoExistir() throws Exception {
        // Arrange
        ArtistRequestDTO dto = mock(ArtistRequestDTO.class);
        when(dto.getName()).thenReturn("Nome Atualizado");

        Artist existingArtist = new Artist("Nome Antigo");
        java.lang.reflect.Field idField1 = Artist.class.getDeclaredField("id");
        idField1.setAccessible(true);
        idField1.set(existingArtist, 1L);

        Artist updatedArtist = new Artist("Nome Atualizado");
        java.lang.reflect.Field idField2 = Artist.class.getDeclaredField("id");
        idField2.setAccessible(true);
        idField2.set(updatedArtist, 1L);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(existingArtist));
        when(artistRepository.save(any(Artist.class))).thenReturn(updatedArtist);
        when(albumRepository.countByArtist(any(Artist.class))).thenReturn(2L);

        // Act
        ArtistResponseDTO result = artistService.atualizar(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals("Nome Atualizado", result.name());
        verify(artistRepository).findById(1L);
        verify(artistRepository).save(existingArtist);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar artista inexistente")
    void atualizar_DeveLancarExcecao_QuandoNaoExistir() {
        // Arrange
        ArtistRequestDTO dto = mock(ArtistRequestDTO.class);
        // Removido mock desnecessário - dto.getName() não é chamado quando artista não
        // existe

        when(artistRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> artistService.atualizar(999L, dto));

        verify(artistRepository).findById(999L);
        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    @DisplayName("Deve listar artistas filtrados por nome")
    void listarTodos_DeveFiltrarPorNome() {
        // Arrange
        Artist artist1 = new Artist("Rock Artist");
        Artist artist2 = new Artist("Rock Band");
        Page<Artist> page = new PageImpl<>(List.of(artist1, artist2));

        Pageable pageable = PageRequest.of(0, 10);

        when(artistRepository.findByNameContainingIgnoreCase("Rock", pageable))
                .thenReturn(page);
        when(albumRepository.countByArtist(any(Artist.class))).thenReturn(0L);

        // Act
        Page<ArtistResponseDTO> result = artistService.listarTodos("Rock", pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        verify(artistRepository).findByNameContainingIgnoreCase("Rock", pageable);
        verify(artistRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve listar artistas com ordenação")
    void listarTodos_DeveRespeitarOrdenacao() {
        // Arrange
        Artist artist1 = new Artist("A Artist");
        Artist artist2 = new Artist("B Artist");
        Page<Artist> page = new PageImpl<>(List.of(artist1, artist2));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        when(artistRepository.findAll(pageable)).thenReturn(page);
        when(albumRepository.countByArtist(any(Artist.class))).thenReturn(0L);

        // Act
        Page<ArtistResponseDTO> result = artistService.listarTodos(null, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        verify(artistRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há artistas")
    void listarTodos_DeveRetornarPaginaVazia() {
        // Arrange
        Page<Artist> emptyPage = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        when(artistRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<ArtistResponseDTO> result = artistService.listarTodos(null, pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Deve calcular corretamente a contagem de álbuns")
    void toDTO_DeveCalcularContagemDeAlbuns() throws Exception {
        // Arrange
        Artist artist = new Artist("Artist with Albums");
        java.lang.reflect.Field idField = Artist.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(artist, 1L);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(albumRepository.countByArtist(artist)).thenReturn(5L);

        // Act
        ArtistResponseDTO result = artistService.buscarPorId(1L);

        // Assert
        assertEquals(5L, result.albumsCount());
        verify(albumRepository).countByArtist(artist);
    }
}
