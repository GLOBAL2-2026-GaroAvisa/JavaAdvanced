package br.com.GaroAvisa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.AlterarAlertasRequest;
import br.com.GaroAvisa.dto.CriarLocalRequest;
import br.com.GaroAvisa.dto.EditarLocalRequest;
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
	public PaginaResponse<LocalCardResponse> listar(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) String busca, @RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {

		return localService.listar(Long.valueOf(jwt.getSubject()), busca, pagina, tamanho);
	}

	@Operation(summary = "Cadastrar um local para o usuário logado")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LocalCardResponse criar(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid CriarLocalRequest requisicao) {

		return localService.criar(Long.valueOf(jwt.getSubject()), requisicao);
	};

	@Operation(summary = "Editar apelido, alertas e principal de um local")
	@PutMapping("/{LocalId}")
	public LocalCardResponse editar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long LocalId,
			@RequestBody @Valid EditarLocalRequest requisicao) {

		return localService.editar(Long.valueOf(jwt.getSubject()), LocalId, requisicao);
	}
	
	@Operation(summary = "Remover um local para o usuário logado")
	@DeleteMapping("/{LocalId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@AuthenticationPrincipal Jwt jwt, @PathVariable Long LocalId) {
		localService.remover(Long.valueOf(jwt.getSubject()), LocalId);

	}
	
	@Operation(summary = "Ligar ou desligar os alertas de um local")
	@PatchMapping("/{LocalId}/alertas")
	public LocalCardResponse alterarAlertas(@AuthenticationPrincipal Jwt jwt, @PathVariable Long LocalId,
			@RequestBody @Valid AlterarAlertasRequest requisicao) {

		return localService.alterarAlertas(Long.valueOf(jwt.getSubject()), LocalId, requisicao);
	}
	
}
