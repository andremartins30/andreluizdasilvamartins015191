package mt.gov.seplag.backend.controller;

import mt.gov.seplag.backend.web.artist.ArtistRequestDTO;
import mt.gov.seplag.backend.web.artist.ArtistResponseDTO;

import mt.gov.seplag.backend.service.ArtistService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "Artist", description = "Gerenciamento de artistas")
@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {
    private final ArtistService service;


    public ArtistController(ArtistService service) {
        this.service = service;
    }

    @Operation(summary = "Lista todos os artistas")
    @GetMapping
    public List<ArtistResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @Operation(summary = "Cadastra um novo artista")
    @PostMapping
    public ResponseEntity<ArtistResponseDTO> criar(@Valid @RequestBody ArtistRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
    }

    @Operation(summary = "Busca um artista por ID")
    @GetMapping("/{id}")
    public ArtistResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Atualiza artista existente")
    @PutMapping("/{id}")
    public ResponseEntity<ArtistResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ArtistRequestDTO dto
    ) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @Operation(summary = "Remove um artista por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}