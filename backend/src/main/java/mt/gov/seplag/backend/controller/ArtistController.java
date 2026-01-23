package mt.gov.seplag.backend.controller;

import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.service.ArtistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {
    private final ArtistService service;

    public ArtistController(ArtistService service) {
        this.service = service;
    }

    @GetMapping
    public List<Artist> listarTodos() {
        return service.listarTodos();
    }

    @PostMapping
    public Artist criar(@RequestBody Artist artist) {
        return service.salvar(artist);
    }
}