package br.com.GaroAvisa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.CompromissoResponse;
import br.com.GaroAvisa.dto.CriarCompromissoRequest;
import br.com.GaroAvisa.service.CompromissoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Compromissos", description = "Compromissos nos locais do usuário")
public class CompromissoController {

	private final CompromissoService compromissoService;

	@Operation(summary = "Criar um compromisso em um local do usuário logado")
	@PostMapping("/locais/{LocalId}/compromissos")
	@ResponseStatus(HttpStatus.CREATED)
	public CompromissoResponse criar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long LocalId,
			@RequestBody @Valid CriarCompromissoRequest requisicao) {

		return compromissoService.criar(Long.valueOf(jwt.getSubject()), LocalId, requisicao);
	}
}