package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import mt.gov.seplag.backend.web.artist.ArtistResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private ArtistService artistService;

    @Test
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
    void buscarPorId_DeveLancarExcecao_QuandoNaoExistir() {
        when(artistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> artistService.buscarPorId(999L));
    }

    @Test
    void listarTodos_DeveRetornarPaginaDeArtistas() {
        Artist artist = new Artist("Test");
        Page<Artist> page = new PageImpl<>(List.of(artist));
        
        when(artistRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(albumRepository.countByArtist(any(Artist.class))).thenReturn(0L);

        Page<ArtistResponseDTO> result = artistService.listarTodos(null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void remover_DeveRemoverArtista_QuandoExistir() {
        Artist artist = new Artist("Test");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        artistService.remover(1L);

        verify(artistRepository).delete(artist);
    }
}
