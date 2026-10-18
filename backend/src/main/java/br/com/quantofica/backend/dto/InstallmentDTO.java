package br.com.quantofica.backend.dto;

import java.math.BigDecimal;

public record InstallmentDTO(
        Integer installmentNumber,
        BigDecimal installmentAmount,
        BigDecimal amortization,
        BigDecimal interest,
        BigDecimal outstandingBalance
) {
}
