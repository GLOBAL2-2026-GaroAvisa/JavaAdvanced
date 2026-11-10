package br.com.GaroAvisa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.CriarLocalRequest;
import br.com.GaroAvisa.dto.LocalCardResponse;
import br.com.GaroAvisa.dto.PaginaResponse;
import br.com.GaroAvisa.service.LocalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(summary = "Cadastrar um local para o usuário logado")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocalCardResponse criar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CriarLocalRequest requisicao) {

        return localService.criar(Long.valueOf(jwt.getSubject()), requisicao);
    }
    
    
}

