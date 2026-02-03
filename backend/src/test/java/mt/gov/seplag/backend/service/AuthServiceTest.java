package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.user.User;
import mt.gov.seplag.backend.domain.user.UserRepository;
import mt.gov.seplag.backend.security.service.JwtService;
import mt.gov.seplag.backend.shared.exception.BusinessException;
import mt.gov.seplag.backend.shared.exception.NotFoundException;
import mt.gov.seplag.backend.web.auth.AuthResponseDTO;
import mt.gov.seplag.backend.web.auth.LoginRequestDTO;
import mt.gov.seplag.backend.web.auth.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_DeveCriarUsuario_QuandoUsernameNaoExistir() {
        RegisterRequestDTO request = new RegisterRequestDTO("newuser", "pass123");
        User savedUser = new User();
        savedUser.setUsername("newuser");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken("newuser")).thenReturn("token");
        when(jwtService.generateRefreshToken("newuser")).thenReturn("refresh");

        AuthResponseDTO result = authService.register(request);

        assertNotNull(result);
        assertEquals("token", result.token());
        assertEquals("refresh", result.refreshToken());
    }

    @Test
    void register_DeveLancarExcecao_QuandoUsernameExistir() {
        RegisterRequestDTO request = new RegisterRequestDTO("existing", "pass");
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request));
    }

    @Test
    void login_DeveRetornarTokens_QuandoCredenciaisValidas() {
        LoginRequestDTO request = new LoginRequestDTO("user", "pass");
        User user = new User();
        user.setUsername("user");
        user.setPassword("encoded");
        
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);
        when(jwtService.generateToken("user")).thenReturn("token");
        when(jwtService.generateRefreshToken("user")).thenReturn("refresh");

        AuthResponseDTO result = authService.login(request);

        assertNotNull(result);
        assertEquals("token", result.token());
    }

    @Test
    void login_DeveLancarExcecao_QuandoUsuarioNaoExistir() {
        LoginRequestDTO request = new LoginRequestDTO("nonexistent", "pass");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.login(request));
    }

    @Test
    void refreshToken_DeveRetornarNovosTokens_QuandoTokenValido() {
        User user = new User();
        user.setUsername("user");
        
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("user")).thenReturn("new-token");
        when(jwtService.generateRefreshToken("user")).thenReturn("new-refresh");

        AuthResponseDTO result = authService.refreshToken("valid-token");

        assertNotNull(result);
        assertEquals("new-token", result.token());
    }
}
