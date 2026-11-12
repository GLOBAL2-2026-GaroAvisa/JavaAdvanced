package br.com.GaroAvisa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.CompromissoResponse;
import br.com.GaroAvisa.dto.CriarCompromissoRequest;
import br.com.GaroAvisa.dto.EditarCompromissoRequest;
import br.com.GaroAvisa.dto.PaginaResponse;
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

	@Operation(summary = "Próximos compromissos de todos os locais do usuário logado")
	@GetMapping("/compromissos")
	public PaginaResponse<CompromissoResponse> listarProximos(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {

		return compromissoService.listarProximos(Long.valueOf(jwt.getSubject()), pagina, tamanho);
	}

	@Operation(summary = "Próximos compromissos de um local do usuário logado")
	@GetMapping("/locais/{LocalId}/compromissos")
	public PaginaResponse<CompromissoResponse> listarProximosDoLocal(@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long LocalId,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {

		return compromissoService.listarProximosDoLocal(Long.valueOf(jwt.getSubject()), LocalId, pagina, tamanho);
	}
	
	@Operation(summary = "Editar nome, data e local de um compromisso")
	@PutMapping("/compromissos/{CompromissoId}")
	public CompromissoResponse editar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long CompromissoId,
			@RequestBody @Valid EditarCompromissoRequest requisicao) {

		return compromissoService.editar(Long.valueOf(jwt.getSubject()), CompromissoId, requisicao);
	}
}