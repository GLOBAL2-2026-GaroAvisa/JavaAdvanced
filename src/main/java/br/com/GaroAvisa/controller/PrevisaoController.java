package br.com.GaroAvisa.controller;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.PrevisaoResponse;
import br.com.GaroAvisa.service.PrevisaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Previsão", description = "Clima dos locais do usuário")
@SecurityRequirement(name = "bearerAuth")
public class PrevisaoController {

    private final PrevisaoService previsaoService;

    // Features 15 (Detalhes do local), 16 e 19 (a Home usa as 24 primeiras horas)
    @Operation(summary = "Previsão de um local",
               description = "Clima de agora, próximas 48 horas e 16 dias. "
                           + "Enquanto a previsão do endereço não chega, responde 200 com os dados do local "
                           + "e os campos de clima nulos ou vazios. "
                           + "As listas de alertas e de mudanças ficam vazias até o Epic 5 e a feature 21.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Previsão do local"),
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou vencido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Local não encontrado ou de outro usuário",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/locais/{LocalId}/previsao")
    public ResponseEntity<PrevisaoResponse> buscar(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable Long LocalId) {
        return ResponseEntity.ok(previsaoService.buscarDoLocal(usuarioId(jwt), LocalId));
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}