package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.user.User;
import mt.gov.seplag.backend.domain.user.UserRepository;
import mt.gov.seplag.backend.security.JwtService;
import mt.gov.seplag.backend.web.auth.LoginRequestDTO;
import mt.gov.seplag.backend.web.auth.LoginResponseDTO;

import mt.gov.seplag.backend.web.auth.AuthResponseDTO;
import mt.gov.seplag.backend.web.auth.RegisterRequestDTO;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository repository, PasswordEncoder encoder, JwtService jwtService) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = repository.findByUsername(dto.username())
                .orElseThrow(() -> new RuntimeException("Usuário inválido"));

        if (!encoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponseDTO(token);
    }

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        // depois colocar persistencia
        return new AuthResponseDTO("usuario-registrado-fake-token");
    }
}