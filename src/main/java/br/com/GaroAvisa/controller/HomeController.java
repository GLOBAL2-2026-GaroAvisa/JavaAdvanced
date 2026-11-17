package br.com.GaroAvisa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.ExplicacaoResponse;
import br.com.GaroAvisa.dto.HomeResponse;
import br.com.GaroAvisa.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Home", description = "Tela inicial do app")
@SecurityRequirement(name = "bearerAuth")
public class HomeController {

    private final HomeService homeService;

    // Features 16 (clima atual) e 19 (proximas 24 horas) do local principal
    @Operation(summary = "Tela inicial",
               description = "Clima do local principal, próximas 24 horas e os 2 próximos compromissos. "
                           + "Sem local principal, os campos de clima vêm nulos e temLocais diz "
                           + "se o app convida a cadastrar um local ou a escolher o principal. "
                           + "Explicação do dia, alertas, dicas e mudanças ficam vazios até as "
                           + "features 23, 18, 20 e 21.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados da tela inicial"),
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou vencido", content = @Content)
    })
    @GetMapping("/home")
    public ResponseEntity<HomeResponse> buscar(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(homeService.buscar(usuarioId(jwt)));
    }
    
    @Operation(summary = "Explicação do Dia do local principal",
            description = "Gera o texto se ainda não existir, e guarda para o resto do dia. "
                    + "A Home devolve explicacaoDoDia nulo quando o texto ainda não foi gerado; "
                    + "é então que o app chama este endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Texto do dia"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sem local principal", content = @Content)
    })
    @GetMapping("/home/explicacao")
    public ResponseEntity<ExplicacaoResponse> explicacao(@AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(homeService.explicacaoDoPrincipal(usuarioId));
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}