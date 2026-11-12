package br.com.GaroAvisa.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ProblemDetail emailJaCadastrado(EmailJaCadastradoException ex) {
        return problema(HttpStatus.CONFLICT, "E-mail já cadastrado", ex.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail credenciaisInvalidas(CredenciaisInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos não passaram na validação");
        problema.setProperty("erros", erros);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail corpoInvalido(HttpMessageNotReadableException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido",
                "Não foi possível ler o JSON enviado");
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
    
    @ExceptionHandler(ApelidoJaCadastradoException.class)
    public ProblemDetail apelidoJaCadastrado(ApelidoJaCadastradoException ex) {
        return problema(HttpStatus.CONFLICT, "Apelido já cadastrado", ex.getMessage());
    }

    @ExceptionHandler(FusoInvalidoException.class)
    public ProblemDetail fusoInvalido(FusoInvalidoException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Dados inválidos", ex.getMessage());
    }
    
    @ExceptionHandler(DataNoPassadoException.class)
    public ProblemDetail dataNoPassado(DataNoPassadoException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Dados inválidos", ex.getMessage());
    }
}