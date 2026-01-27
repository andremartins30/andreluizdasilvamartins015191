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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository repository,
                    PasswordEncoder passwordEncoder,
                    JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {

        if (repository.existsByUsername(request.username())) {
            throw new RuntimeException("Usuário já existe");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");

        repository.save(user);

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponseDTO(token);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = repository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponseDTO(token);
    }
}