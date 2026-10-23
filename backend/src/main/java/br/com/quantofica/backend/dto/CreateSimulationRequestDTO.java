package br.com.quantofica.backend.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Payload completo para criar e persistir uma simulação consolidada.
 */
public record CreateSimulationRequestDTO(
        @NotNull(message = "O valor do imóvel é obrigatório")
        @DecimalMin(value = "50000.00", message = "O valor mínimo do imóvel para simulação é de R$ 50.000,00")
        BigDecimal propertyValue,

        @NotNull(message = "O valor de entrada é obrigatório")
        @DecimalMin(value = "0.00", message = "O valor de entrada não pode ser negativo")
        BigDecimal downPayment,

        @NotNull(message = "O prazo em meses é obrigatório")
        @Min(value = 12, message = "O prazo mínimo é de 12 meses")
        @Max(value = 420, message = "O prazo máximo é de 420 meses (35 anos)")
        Integer termMonths,

        @NotNull(message = "A taxa de juros anual é obrigatória")
        @DecimalMin(value = "1.00", message = "A taxa de juros anual deve ser de no mínimo 1.00%")
        BigDecimal annualInterestRate,

        @NotBlank(message = "O estado (UF) é obrigatório")
        @Size(min = 2, max = 2, message = "O estado deve ter exatamente 2 caracteres")
        String state,

        String city,

        @NotNull(message = "Informe se este é o seu primeiro imóvel financiado")
        Boolean isFirstProperty
) {
}
