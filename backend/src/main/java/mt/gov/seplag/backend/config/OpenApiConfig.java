package mt.gov.seplag.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Sistema de Gerenciamento de Artistas e Álbuns - SEPLAG/MT", version = "1.0.0", description = """
                API REST para gerenciamento de artistas musicais, álbuns e capas.

                ## Funcionalidades
                - Autenticação e autorização com JWT
                - CRUD de artistas e álbuns
                - Upload e gerenciamento de capas de álbuns (MinIO S3)
                - Sincronização com API externa MusicBrainz
                - WebSocket para notificações em tempo real
                - Rate limiting para proteção de recursos
                - Health checks e métricas com Spring Actuator

                ## Endpoints de Saúde
                - `/actuator/health` - Status geral da aplicação
                - `/actuator/health/liveness` - Verificação de liveness (aplicação está viva)
                - `/actuator/health/readiness` - Verificação de readiness (aplicação está pronta)

                ## Autenticação
                A maioria dos endpoints requer autenticação via Bearer Token (JWT).
                Use o endpoint `/auth/login` para obter o token.
                """, contact = @Contact(name = "SEPLAG/MT - Secretaria de Planejamento e Gestão", email = "seplag@mt.gov.br"), license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")), servers = {
                @Server(url = "http://localhost:8080", description = "Servidor de Desenvolvimento"),
                @Server(url = "${server.url:http://localhost:8080}", description = "Servidor Configurável")
}, security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Token JWT obtido através do endpoint /auth/login")
public class OpenApiConfig {
}