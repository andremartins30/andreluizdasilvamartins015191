package mt.gov.seplag.backend.controller;

import mt.gov.seplag.backend.web.album.AlbumResponseDTO;
import mt.gov.seplag.backend.web.album.AlbumRequestDTO;
import mt.gov.seplag.backend.web.album.AlbumCoverResDTO;
import mt.gov.seplag.backend.web.album.AlbumCoverUrlDTO;

import org.springframework.web.multipart.MultipartFile;
import mt.gov.seplag.backend.service.storage.MinioService;

import java.util.List;

import mt.gov.seplag.backend.service.AlbumService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import mt.gov.seplag.backend.shared.response.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Album", description = "Gerenciamento de álbuns")
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
            Pageable pageable) {
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
            @Valid @RequestBody AlbumRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @Operation(summary = "Remove um álbum por ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }

    @Operation(summary = "Upload da capa do álbum")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Capa enviada"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
    })
    @PostMapping("/{id}/cover")
    public ResponseEntity<ApiSuccessResponse<AlbumCoverResDTO>> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest http) {
        AlbumCoverResDTO response = service.uploadCover(id, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiSuccessResponse<>(
                        201,
                        "Capa do álbum enviada com sucesso",
                        response,
                        http.getRequestURI()));
    }

    @Operation(summary = "Upload de múltiplas capas do álbum")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Capas enviadas"),
            @ApiResponse(responseCode = "400", description = "Arquivos inválidos"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
    })
    @PostMapping("/{id}/covers")
    public ResponseEntity<ApiSuccessResponse<AlbumCoverResDTO>> uploadCovers(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            HttpServletRequest http) {
        AlbumCoverResDTO response = service.uploadCovers(id, files);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiSuccessResponse<>(
                        201,
                        files.size() + " capa(s) do álbum enviada(s) com sucesso",
                        response,
                        http.getRequestURI()));
    }

    @GetMapping("/{id}/cover-url")
    public ResponseEntity<AlbumCoverUrlDTO> getCoverUrl(@PathVariable Long id) {
        String url = service.getCoverUrl(id);
        if (url == null) {
            return ResponseEntity.ok(new AlbumCoverUrlDTO(null));
        }
        return ResponseEntity.ok(new AlbumCoverUrlDTO(url));
    }

    @Operation(summary = "Lista todas as URLs das capas do álbum")
    @GetMapping("/{id}/cover-urls")
    public ResponseEntity<java.util.List<String>> getAllCoverUrls(@PathVariable Long id) {
        java.util.List<String> urls = service.getAllCoverUrls(id);
        return ResponseEntity.ok(urls);
    }

    @Operation(summary = "Remove uma capa específica do álbum")
    @DeleteMapping("/{id}/covers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCover(
            @PathVariable Long id,
            @RequestParam String objectName) {
        service.deleteCover(id, objectName);
    }

}