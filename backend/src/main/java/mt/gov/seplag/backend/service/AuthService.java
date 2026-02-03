package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.user.User;
import mt.gov.seplag.backend.domain.user.UserRepository;
import mt.gov.seplag.backend.security.service.JwtService;
import mt.gov.seplag.backend.web.auth.LoginRequestDTO;

import mt.gov.seplag.backend.shared.exception.BusinessException;
import mt.gov.seplag.backend.shared.exception.NotFoundException;

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
            throw new BusinessException("Usuário já existe");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");

        repository.save(user);

        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = repository.findByUsername(request.username())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    public AuthResponseDTO refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BusinessException("Refresh token inválido ou expirado");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        String newAccessToken = jwtService.generateToken(user.getUsername());
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

        return new AuthResponseDTO(newAccessToken, newRefreshToken);
    }
}