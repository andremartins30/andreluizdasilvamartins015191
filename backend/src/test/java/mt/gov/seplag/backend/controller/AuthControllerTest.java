package mt.gov.seplag.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mt.gov.seplag.backend.security.service.JwtService;
import mt.gov.seplag.backend.service.AuthService;
import mt.gov.seplag.backend.shared.exception.BusinessException;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import mt.gov.seplag.backend.web.auth.AuthResponseDTO;
import mt.gov.seplag.backend.web.auth.LoginRequestDTO;
import mt.gov.seplag.backend.web.auth.RegisterRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController - Testes de Endpoints REST")
@Import(TestSecurityConfig.class) // Importa configuração sem autenticação
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService; // Necessário para o contexto Spring Security

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve registrar usuário com sucesso")
    void deveRegistrarUsuarioComSucesso() throws Exception {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("novousuario", "senha123");
        AuthResponseDTO response = new AuthResponseDTO("access-token", "refresh-token");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Usuário registrado com sucesso"))
                .andExpect(jsonPath("$.data.token").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve retornar erro quando usuário já existe")
    void deveRetornarErroQuandoUsuarioJaExiste() throws Exception {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("usuarioexistente", "senha123");

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new BusinessException("Usuário já existe"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve fazer login com sucesso")
    void deveFazerLoginComSucesso() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("usuario", "senha123");
        AuthResponseDTO response = new AuthResponseDTO("valid-access-token", "valid-refresh-token");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Usuário logado com sucesso"))
                .andExpect(jsonPath("$.data.token").value("valid-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("valid-refresh-token"));

        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar erro quando usuário não existe")
    void deveRetornarErroQuandoUsuarioNaoExiste() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("usuarioinexistente", "senha123");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new NotFoundException("Usuário não encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar erro quando senha está incorreta")
    void deveRetornarErroQuandoSenhaIncorreta() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("usuario", "senhaerrada");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Credenciais inválidas"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve renovar token com sucesso")
    @WithMockUser
    void deveRenovarTokenComSucesso() throws Exception {
        // Arrange
        String refreshToken = "valid-refresh-token";
        AuthResponseDTO response = new AuthResponseDTO("new-access-token", "new-refresh-token");

        when(authService.refreshToken(refreshToken)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Token renovado com sucesso"))
                .andExpect(jsonPath("$.data.token").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));

        verify(authService).refreshToken(refreshToken);
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve retornar erro quando header Authorization está ausente")
    @WithMockUser
    void deveRetornarErroQuandoHeaderAuthorizationAusente() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(authService, never()).refreshToken(anyString());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve retornar erro quando token não começa com Bearer")
    @WithMockUser
    void deveRetornarErroQuandoTokenNaoComecaComBearer() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .header("Authorization", "InvalidToken"))
                .andExpect(status().isInternalServerError());

        verify(authService, never()).refreshToken(anyString());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve retornar erro quando refresh token é inválido")
    @WithMockUser
    void deveRetornarErroQuandoRefreshTokenInvalido() throws Exception {
        // Arrange
        String invalidRefreshToken = "invalid-refresh-token";

        when(authService.refreshToken(invalidRefreshToken))
                .thenThrow(new RuntimeException("Token inválido"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .header("Authorization", "Bearer " + invalidRefreshToken))
                .andExpect(status().isInternalServerError());

        verify(authService).refreshToken(invalidRefreshToken);
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve validar campos obrigatórios")
    void deveValidarCamposObrigatorios() throws Exception {
        // Arrange - DTO com campos vazios
        String invalidJson = "{\"username\":\"\",\"password\":\"\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve validar campos obrigatórios")
    void deveValidarCamposObrigatoriosLogin() throws Exception {
        // Arrange - DTO com campos vazios
        String invalidJson = "{\"username\":\"\",\"password\":\"\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve aceitar Content-Type application/json")
    void deveAceitarContentTypeJson() throws Exception {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("usuario", "senha123");
        AuthResponseDTO response = new AuthResponseDTO("token", "refresh");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Deve retornar path correto no response")
    void deveRetornarPathCorretoNoResponse() throws Exception {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("usuario", "senha123");
        AuthResponseDTO response = new AuthResponseDTO("token", "refresh");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"));
    }
}
