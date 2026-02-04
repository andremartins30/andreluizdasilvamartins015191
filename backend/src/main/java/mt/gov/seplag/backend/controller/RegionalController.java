package mt.gov.seplag.backend.controller;

import mt.gov.seplag.backend.service.RegionalService;
import mt.gov.seplag.backend.web.regional.RegionalExternaDTO;
import mt.gov.seplag.backend.web.regional.RegionalResponseDTO;
import mt.gov.seplag.backend.web.regional.SincronizacaoResultDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Tag(name = "Regional", description = "Gerenciamento de regionais da Polícia Civil")
@RestController
@RequestMapping("/api/v1/regionais")
public class RegionalController {

    private final RegionalService service;

    public RegionalController(RegionalService service) {
        this.service = service;
    }

    @Operation(summary = "Sincroniza regionais com a API externa")
    @PostMapping("/sincronizar")
    public ResponseEntity<SincronizacaoResultDTO> sincronizar() {
        SincronizacaoResultDTO resultado = service.sincronizar();
        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Importa regionais manualmente via JSON")
    @PostMapping("/importar")
    public ResponseEntity<SincronizacaoResultDTO> importar(@RequestBody List<RegionalExternaDTO> regionais) {
        SincronizacaoResultDTO resultado = service.sincronizarComDados(regionais);
        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Lista todas as regionais (ativas e inativas)")
    @GetMapping
    public ResponseEntity<List<RegionalResponseDTO>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @Operation(summary = "Lista apenas regionais ativas")
    @GetMapping("/ativas")
    public ResponseEntity<List<RegionalResponseDTO>> listarAtivas() {
        return ResponseEntity.ok(service.listarAtivas());
    }
}
