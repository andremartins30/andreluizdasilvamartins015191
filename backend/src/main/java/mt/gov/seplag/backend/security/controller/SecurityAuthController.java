package mt.gov.seplag.backend.security.controller;

import mt.gov.seplag.backend.security.dto.*;
import mt.gov.seplag.backend.security.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class SecurityAuthController {

    private final JwtUtil jwtUtil;

    public SecurityAuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public TokenResponseDTO login(@RequestBody LoginRequestDTO dto) {

        // Simples por enquanto (depois ligamos em banco)
        if (!dto.username().equals("admin") || !dto.password().equals("123")) {
            throw new RuntimeException("Credenciais inválidas");
        }

        String access = jwtUtil.generateToken(dto.username());
        String refresh = jwtUtil.generateToken(dto.username());

        return new TokenResponseDTO(access, refresh);
    }

    @PostMapping("/refresh")
    public TokenResponseDTO refresh(@RequestHeader("Authorization") String refreshToken) {

        String token = refreshToken.replace("Bearer ", "");

        if (!jwtUtil.isValid(token)) {
            throw new RuntimeException("Refresh token inválido");
        }

        String username = jwtUtil.extractUsername(token);

        return new TokenResponseDTO(
                jwtUtil.generateToken(username),
                jwtUtil.generateToken(username)
        );
    }
}