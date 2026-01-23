package mt.gov.seplag.backend.controller;

import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.service.AlbumService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumService service;

    public AlbumController(AlbumService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Album> listar(
            @RequestParam(required = false) String artist,
            Pageable pageable
    ) {
        return service.listar(artist, pageable);
    }
}