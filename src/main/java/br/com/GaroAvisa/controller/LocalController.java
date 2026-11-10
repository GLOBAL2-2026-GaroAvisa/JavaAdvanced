package br.com.GaroAvisa.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.LocalCardResponse;
import br.com.GaroAvisa.dto.PaginaResponse;
import br.com.GaroAvisa.service.LocalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/locais")
@RequiredArgsConstructor
@Tag(name = "Locais", description = "Locais salvos pelo usuário")
public class LocalController {

    private final LocalService localService;

    @Operation(summary = "Listar e buscar os locais do usuário logado")
    @GetMapping
    public PaginaResponse<LocalCardResponse> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {

        return localService.listar(Long.valueOf(jwt.getSubject()), busca, pagina, tamanho);
    }
}