package mt.gov.seplag.backend.controller;

import mt.gov.seplag.backend.web.album.AlbumResponseDTO;
import mt.gov.seplag.backend.web.album.AlbumRequestDTO;

import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.service.AlbumService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumService service;

    public AlbumController(AlbumService service) {
        this.service = service;
    }

    @Operation(summary = "Lista todos os álbuns com paginação e filtro opcional por nome do artista")
    @GetMapping
    public Page<AlbumResponseDTO> listar(
            @RequestParam(required = false) String artist,
            Pageable pageable
    ) {
        return service.listar(artist, pageable);
    }

    @Operation(summary = "Busca um álbum por ID")
    @GetMapping("/{id}")
    public AlbumResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Cadastra um novo álbum")
    @PostMapping
    public ResponseEntity<AlbumResponseDTO> criar(@Valid @RequestBody AlbumRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @Operation(summary = "Atualiza um álbum existente")
    @PutMapping("/{id}")
    public AlbumResponseDTO atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AlbumRequestDTO dto
    ) {
        return service.atualizar(id, dto);
    }

    @Operation(summary = "Remove um álbum por ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}