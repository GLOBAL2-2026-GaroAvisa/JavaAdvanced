package br.com.GaroAvisa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.CadastroRequest;
import br.com.GaroAvisa.dto.LoginRequest;
import br.com.GaroAvisa.dto.TokenResponse;
import br.com.GaroAvisa.dto.UsuarioResponse;
import br.com.GaroAvisa.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Sign-up e sign-in")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Cadastrar usuário")
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody @Valid CadastroRequest requisicao) {
        return authService.cadastrar(requisicao);
    }

    @Operation(summary = "Fazer login e receber o token")
    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest requisicao) {
        return authService.login(requisicao);
    }
}