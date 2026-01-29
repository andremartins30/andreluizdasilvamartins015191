package mt.gov.seplag.backend.controller;

import mt.gov.seplag.backend.service.AuthService;
import mt.gov.seplag.backend.web.auth.LoginRequestDTO;
import mt.gov.seplag.backend.web.auth.RegisterRequestDTO;
import mt.gov.seplag.backend.web.auth.AuthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import mt.gov.seplag.backend.shared.response.ApiSuccessResponse;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "Auth", description = "Autenticação e Registro de Usuários")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registra um novo usuário")
    @PostMapping("/register")
    public ResponseEntity<ApiSuccessResponse<AuthResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request,
            HttpServletRequest http) {
        var result = authService.register(request);

        return ResponseEntity.ok(
                new ApiSuccessResponse<>(200, "Usuário registrado com sucesso", result, http.getRequestURI()));
    }

    @Operation(summary = "Realiza o login de um usuário")
    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<AuthResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest http) {
        var result = authService.login(request);

        return ResponseEntity.ok(
                new ApiSuccessResponse<>(200, "Usuário logado com sucesso", result, http.getRequestURI()));
    }
}