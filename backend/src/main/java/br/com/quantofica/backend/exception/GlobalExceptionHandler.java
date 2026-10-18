package br.com.quantofica.backend.exception;

import br.com.quantofica.backend.dto.InvalidParamDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Interceptador global centralizado de erros, aderente à especificação RFC 7807.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI BAD_REQUEST_TYPE = URI.create("https://quantofica.com.br/errors/bad-request");
    private static final URI BUSINESS_RULE_TYPE = URI.create("https://quantofica.com.br/errors/business-rule");

    /**
     * Captura de erros de validação disparados pelo @Valid do Bean Validation (ex: campos nulos, valores mínimos).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos não passaram nas validações de negócio."
        );
        problemDetail.setType(BAD_REQUEST_TYPE);
        problemDetail.setTitle("Dados de entrada inválidos");
        problemDetail.setInstance((URI.create(request.getRequestURI())));
        problemDetail.setProperty("timestamp", Instant.now());

        // Extrai todos os campos que falharam na validação com as suas respectivas mensagens
        List<InvalidParamDTO> invalidParams = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new InvalidParamDTO(error.getField(), error.getDefaultMessage()))
                .toList();

        problemDetail.setProperty("invalidParams", invalidParams);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Captura regras de negócio violadas (ex: valor de entrada ≥ valor do imóvel).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setType(BUSINESS_RULE_TYPE);
        problemDetail.setTitle("Regra de negócio violada");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);

    }
}
