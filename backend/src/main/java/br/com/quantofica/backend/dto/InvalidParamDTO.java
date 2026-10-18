package br.com.quantofica.backend.dto;

/**
 * Representa um campo específico que violou uma regra de validação.
 */
public record InvalidParamDTO(
        String field,
        String message
) {
}
