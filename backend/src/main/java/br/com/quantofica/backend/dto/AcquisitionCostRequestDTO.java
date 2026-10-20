package br.com.quantofica.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

import java.math.BigDecimal;

/**
 * Dados de entrada para o cálculo de despesas iniciais e cartorárias.
 */
public record AcquisitionCostRequestDTO(
        @NotNull(message = "O valor do imóvel é obrigatório")
        @DecimalMin(value = "50000.00",message = "O valor mínimo do imóvel para estimativa é de R$ 50.000,00")
        BigDecimal propertyValue,

        @NotBlank(message = "O estado (UF) é obrigatório")
        @Size(min = 2, max = 2, message = "O estado deve ter exatamente 2 letras (ex: MS, SP, TO)")
        String state,

        String city,

        @NotNull(message = "Informe se este é o seu primeiro imóvel financiado")
        Boolean isFirstProperty
) {
}
