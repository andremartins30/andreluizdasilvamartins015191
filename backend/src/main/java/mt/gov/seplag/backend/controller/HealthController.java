package mt.gov.seplag.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator")
@Tag(name = "Health & Monitoring", description = "Endpoints para monitoramento e verificação de saúde da aplicação")
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    public HealthController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check Completo", description = """
            Retorna o status de saúde geral da aplicação, incluindo verificações de:
            - Banco de dados PostgreSQL
            - Armazenamento MinIO
            - Disco
            - Ping

            Este endpoint pode ser usado por ferramentas de monitoramento e load balancers.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aplicação saudável - todos os componentes funcionando", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Health.class), examples = @ExampleObject(value = """
                    {
                      "status": "UP",
                      "components": {
                        "database": {
                          "status": "UP",
                          "details": {
                            "database": "PostgreSQL",
                            "validationQuery": "isValid()"
                          }
                        },
                        "diskSpace": {
                          "status": "UP",
                          "details": {
                            "total": 536870912000,
                            "free": 429496729600,
                            "threshold": 10485760
                          }
                        },
                        "livenessState": {
                          "status": "UP"
                        },
                        "minio": {
                          "status": "UP",
                          "details": {
                            "endpoint": "http://localhost:9000",
                            "bucket": "album-covers"
                          }
                        },
                        "ping": {
                          "status": "UP"
                        },
                        "readinessState": {
                          "status": "UP"
                        }
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "503", description = "Aplicação não saudável - um ou mais componentes falhando", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": "DOWN",
                      "components": {
                        "database": {
                          "status": "DOWN",
                          "details": {
                            "error": "Connection refused"
                          }
                        }
                      }
                    }
                    """)))
    })
    public ResponseEntity<HealthComponent> health() {
        HealthComponent health = healthEndpoint.health();
        if (health.getStatus() == Status.UP) {
            return ResponseEntity.ok(health);
        }
        return ResponseEntity.status(503).body(health);
    }

    @GetMapping("/health/liveness")
    @Operation(summary = "Liveness Probe", description = """
            Verifica se a aplicação está viva e em execução.

            Este endpoint indica se a aplicação precisa ser reiniciada.
            Se retornar DOWN, o container/aplicação deve ser reiniciado.

            Ideal para uso em Kubernetes liveness probes.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aplicação está viva", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": "UP"
                    }
                    """))),
            @ApiResponse(responseCode = "503", description = "Aplicação não está respondendo corretamente - recomendado restart", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": "DOWN"
                    }
                    """)))
    })
    public ResponseEntity<HealthComponent> liveness() {
        HealthComponent healthComponent = healthEndpoint.healthForPath("livenessState");
        if (healthComponent.getStatus() == Status.UP) {
            return ResponseEntity.ok(healthComponent);
        }
        return ResponseEntity.status(503).body(healthComponent);
    }

    @GetMapping("/health/readiness")
    @Operation(summary = "Readiness Probe", description = """
            Verifica se a aplicação está pronta para receber tráfego.

            Este endpoint indica se a aplicação está pronta para processar requisições.
            Se retornar DOWN, a aplicação não deve receber tráfego até estar UP.

            Útil durante o startup ou quando dependências externas estão indisponíveis.
            Ideal para uso em Kubernetes readiness probes e load balancers.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aplicação está pronta para receber tráfego", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": "UP"
                    }
                    """))),
            @ApiResponse(responseCode = "503", description = "Aplicação não está pronta - aguardar antes de enviar tráfego", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": "DOWN",
                      "details": {
                        "reason": "Database connection not ready"
                      }
                    }
                    """)))
    })
    public ResponseEntity<HealthComponent> readiness() {
        HealthComponent healthComponent = healthEndpoint.healthForPath("readinessState");
        if (healthComponent.getStatus() == Status.UP) {
            return ResponseEntity.ok(healthComponent);
        }
        return ResponseEntity.status(503).body(healthComponent);
    }
}
