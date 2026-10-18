package br.com.quantofica.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record SimulationResponseDTO(
        BigDecimal propertyValue,
        BigDecimal downPayment,
        BigDecimal financedAmount,
        Integer termMonths,
        BigDecimal annualInterestRate,
        BigDecimal monthlyInterestRate,
        BigDecimal firstInstallmentAmount,
        BigDecimal lastInstallmentAmount,
        BigDecimal totalInterest,
        BigDecimal totalPaid,
        List<InstallmentDTO> installments
) {
}
