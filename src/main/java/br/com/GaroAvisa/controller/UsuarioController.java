package br.com.GaroAvisa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.AlterarNomeRequest;
import br.com.GaroAvisa.dto.AlterarPerfilIaRequest;
import br.com.GaroAvisa.dto.UsuarioResponse;
import br.com.GaroAvisa.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios/me")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Feature 60: visualizar perfil
    @GetMapping
    public ResponseEntity<UsuarioResponse> buscar(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.buscar(usuarioId(jwt)));
    }

    // Feature 61: lápis do nome
    @PatchMapping("/nome")
    public ResponseEntity<UsuarioResponse> alterarNome(@AuthenticationPrincipal Jwt jwt,
                                                       @RequestBody @Valid AlterarNomeRequest req) {
        return ResponseEntity.ok(usuarioService.alterarNome(usuarioId(jwt), req));
    }

    // Feature 62: lápis do "Sobre mim"
    @PatchMapping("/perfil-ia")
    public ResponseEntity<UsuarioResponse> alterarPerfilIa(@AuthenticationPrincipal Jwt jwt,
                                                           @RequestBody @Valid AlterarPerfilIaRequest req) {
        return ResponseEntity.ok(usuarioService.alterarPerfilIa(usuarioId(jwt), req));
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}