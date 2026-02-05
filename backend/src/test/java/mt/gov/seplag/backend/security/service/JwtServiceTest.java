package mt.gov.seplag.backend.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService - Testes de Segurança JWT")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_USERNAME = "testuser";
    private static final String SECRET = "7K9mP2nQ5rS8tU1vW4xY6zA3bC5dE7fG9hJ2kL4mN6pQ8rS1tU3vW5xY7zA9bC2d";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    @DisplayName("Deve gerar access token válido com expiração de 5 minutos")
    void deveGerarAccessTokenValido() {
        // Act
        String token = jwtService.generateToken(TEST_USERNAME);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verifica estrutura JWT (header.payload.signature)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        // Verifica username
        String username = jwtService.extractUsername(token);
        assertEquals(TEST_USERNAME, username);

        // Verifica validade
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("Deve gerar refresh token válido com expiração de 7 dias")
    void deveGerarRefreshTokenValido() {
        // Act
        String refreshToken = jwtService.generateRefreshToken(TEST_USERNAME);

        // Assert
        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);

        String username = jwtService.extractUsername(refreshToken);
        assertEquals(TEST_USERNAME, username);
        assertTrue(jwtService.isTokenValid(refreshToken));
    }

    @Test
    @DisplayName("Refresh token deve ter tempo de expiração maior que access token")
    void refreshTokenDeveTermaiorExpiracao() throws InterruptedException {
        // Arrange
        String accessToken = jwtService.generateToken(TEST_USERNAME);
        Thread.sleep(10); // Pequeno delay para garantir timestamps diferentes
        String refreshToken = jwtService.generateRefreshToken(TEST_USERNAME);

        // Act
        Claims accessClaims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        Claims refreshClaims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        // Assert
        Date accessExpiration = accessClaims.getExpiration();
        Date refreshExpiration = refreshClaims.getExpiration();

        assertTrue(refreshExpiration.after(accessExpiration),
                "Refresh token deve expirar depois do access token");
    }

    @Test
    @DisplayName("Deve extrair username corretamente do token")
    void deveExtrairUsernameCorretamente() {
        // Arrange
        String token = jwtService.generateToken("usuario.teste");

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("usuario.teste", username);
    }

    @Test
    @DisplayName("Deve validar token não expirado como válido")
    void deveValidarTokenNaoExpirado() {
        // Arrange
        String token = jwtService.generateToken(TEST_USERNAME);

        // Act
        boolean isValid = jwtService.isTokenValid(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve invalidar token expirado")
    void deveInvalidarTokenExpirado() {
        // Arrange - Cria token que já nasce expirado
        String expiredToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000)) // Expirado há 5 segundos
                .signWith(key)
                .compact();

        // Act
        boolean isValid = jwtService.isTokenValid(expiredToken);

        // Assert
        assertFalse(isValid, "Token expirado deve ser inválido");
    }

    @Test
    @DisplayName("Deve invalidar token com assinatura inválida")
    void deveInvalidarTokenComAssinaturaInvalida() {
        // Arrange - Token com assinatura diferente
        Key wrongKey = Keys.hmacShaKeyFor("different-secret-key-with-minimum-length-required-256-bits".getBytes());
        String invalidToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 300000))
                .signWith(wrongKey)
                .compact();

        // Act
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Assert
        assertFalse(isValid, "Token com assinatura inválida deve ser rejeitado");
    }

    @Test
    @DisplayName("Deve invalidar token malformado")
    void deveInvalidarTokenMalformado() {
        // Arrange
        String malformedToken = "token.malformado.invalido";

        // Act
        boolean isValid = jwtService.isTokenValid(malformedToken);

        // Assert
        assertFalse(isValid, "Token malformado deve ser inválido");
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para o mesmo usuário")
    void deveGerarTokensDiferentesParaMesmoUsuario() throws InterruptedException {
        // Arrange & Act
        String token1 = jwtService.generateToken(TEST_USERNAME);
        Thread.sleep(1000); // Garante timestamps diferentes (1 segundo)
        String token2 = jwtService.generateToken(TEST_USERNAME);

        // Assert
        assertNotEquals(token1, token2, "Tokens devem ser únicos devido ao timestamp");

        // Mas ambos devem conter o mesmo username
        assertEquals(jwtService.extractUsername(token1), jwtService.extractUsername(token2));
    }

    @Test
    @DisplayName("Deve verificar que access token expira em aproximadamente 5 minutos")
    void deveVerificarExpiracaoAccessToken() {
        // Arrange
        String token = jwtService.generateToken(TEST_USERNAME);

        // Act
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        long expirationTime = claims.getExpiration().getTime();
        long issuedTime = claims.getIssuedAt().getTime();
        long diff = expirationTime - issuedTime;

        // Assert - 5 minutos = 300.000 ms (com tolerância de 1 segundo)
        long expectedExpiration = 5 * 60 * 1000; // 5 minutos
        assertTrue(Math.abs(diff - expectedExpiration) < 1000,
                "Access token deve expirar em aproximadamente 5 minutos");
    }

    @Test
    @DisplayName("Deve verificar que refresh token expira em aproximadamente 7 dias")
    void deveVerificarExpiracaoRefreshToken() {
        // Arrange
        String refreshToken = jwtService.generateRefreshToken(TEST_USERNAME);

        // Act
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        long expirationTime = claims.getExpiration().getTime();
        long issuedTime = claims.getIssuedAt().getTime();
        long diff = expirationTime - issuedTime;

        // Assert - 7 dias = 604.800.000 ms (com tolerância de 1 segundo)
        long expectedExpiration = 7 * 24 * 60 * 60 * 1000L; // 7 dias
        assertTrue(Math.abs(diff - expectedExpiration) < 1000,
                "Refresh token deve expirar em aproximadamente 7 dias");
    }

    @Test
    @DisplayName("Deve aceitar usernames com caracteres especiais")
    void deveAceitarUsernamesComCaracteresEspeciais() {
        // Arrange
        String complexUsername = "user.name+123@domain";

        // Act
        String token = jwtService.generateToken(complexUsername);
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals(complexUsername, extractedUsername);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("Deve manter consistência ao validar múltiplas vezes o mesmo token")
    void deveManterConsistenciaAoValidarMultiplasVezes() {
        // Arrange
        String token = jwtService.generateToken(TEST_USERNAME);

        // Act & Assert
        for (int i = 0; i < 5; i++) {
            assertTrue(jwtService.isTokenValid(token),
                    "Token deve permanecer válido em múltiplas validações");
            assertEquals(TEST_USERNAME, jwtService.extractUsername(token),
                    "Username deve ser extraído corretamente múltiplas vezes");
        }
    }
}
