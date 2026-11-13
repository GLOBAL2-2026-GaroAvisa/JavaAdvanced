package br.com.GaroAvisa.controller;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.GaroAvisa.dto.AlterarNomeRequest;
import br.com.GaroAvisa.dto.AlterarPerfilIaRequest;
import br.com.GaroAvisa.dto.AlterarSenhaRequest;
import br.com.GaroAvisa.dto.UsuarioResponse;
import br.com.GaroAvisa.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios/me")
@RequiredArgsConstructor
@Tag(name = "Usuário", description = "Perfil e conta do usuário logado (sempre o dono do token)")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Feature 60: visualizar perfil
    @Operation(summary = "Visualizar perfil",
               description = "Devolve nome, e-mail e \"Sobre mim\" do usuário do token. perfilIa vem null quando vazio.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil do usuário"),
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou vencido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não existe mais (conta excluída)",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<UsuarioResponse> buscar(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.buscar(usuarioId(jwt)));
    }

    // Feature 61: lápis do nome
    @Operation(summary = "Alterar nome",
               description = "De 3 a 100 caracteres, sem contar os espaços das pontas. Não exige token novo.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário com o nome atualizado"),
        @ApiResponse(responseCode = "400", description = "Nome ausente ou fora do tamanho",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou vencido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não existe mais",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/nome")
    public ResponseEntity<UsuarioResponse> alterarNome(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                                       @RequestBody @Valid AlterarNomeRequest req) {
        return ResponseEntity.ok(usuarioService.alterarNome(usuarioId(jwt), req));
    }

    // Feature 62: lápis do "Sobre mim"
    @Operation(summary = "Alterar \"Sobre mim\"",
               description = "Texto usado pela IA na Explicação do Dia. Até 200 caracteres. \"\" apaga (fica null).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário com o \"Sobre mim\" atualizado"),
        @ApiResponse(responseCode = "400", description = "Texto com mais de 200 caracteres",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou vencido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não existe mais",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/perfil-ia")
    public ResponseEntity<UsuarioResponse> alterarPerfilIa(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                                           @RequestBody @Valid AlterarPerfilIaRequest req) {
        return ResponseEntity.ok(usuarioService.alterarPerfilIa(usuarioId(jwt), req));
    }

    // Feature 67: trocar senha
    @Operation(summary = "Trocar senha",
               description = "Confere a senha atual. A nova precisa ter de 6 a 72 caracteres e ser diferente da atual. "
                           + "Senha atual errada responde 400, e não 401, para o app não tratar como sessão expirada.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha alterada", content = @Content),
        @ApiResponse(responseCode = "400", description = "Senha atual incorreta, nova igual à atual ou fora do tamanho",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou vencido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não existe mais",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/senha")
    public ResponseEntity<Void> alterarSenha(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                             @RequestBody @Valid AlterarSenhaRequest req) {
        usuarioService.alterarSenha(usuarioId(jwt), req);
        return ResponseEntity.noContent().build();
    }

    // Feature 69: excluir minha conta
    @Operation(summary = "Excluir minha conta",
               description = "Apaga o usuário e, em cascata, os Locais e Compromissos dele. "
                           + "Endereços compartilhados não são apagados. O app deve apagar a sessão após o 204.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Conta excluída", content = @Content),
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou vencido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não existe mais",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> excluir(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        usuarioService.excluir(usuarioId(jwt));
        return ResponseEntity.noContent().build();
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}