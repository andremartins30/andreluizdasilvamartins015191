package mt.gov.seplag.backend.controller;

import mt.gov.seplag.backend.service.AuthService;
import mt.gov.seplag.backend.web.auth.LoginRequestDTO;
import mt.gov.seplag.backend.web.auth.LoginResponseDTO;
import mt.gov.seplag.backend.web.auth.RegisterRequestDTO;
import mt.gov.seplag.backend.web.auth.AuthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}