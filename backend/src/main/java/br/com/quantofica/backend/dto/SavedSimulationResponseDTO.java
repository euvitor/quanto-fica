package br.com.quantofica.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representação completa de uma simulação salva para exibição e compartilhamento.
 */
public record SavedSimulationResponseDTO(
        UUID id,
        BigDecimal propertyValue,
        BigDecimal downPayment,
        BigDecimal financedAmount,
        Integer termMonths,
        BigDecimal annualInterestRate,
        BigDecimal firstInstallmentAmount,
        BigDecimal lastInstallmentAmount,
        BigDecimal totalInterest,
        BigDecimal totalPaid,
        String state,
        String city,
        Boolean isFirstProperty,
        BigDecimal itbiEstimated,
        BigDecimal registryEstimated,
        BigDecimal bankAppraisalFee,
        BigDecimal totalAcquisitionCosts,
        Instant createdAt
) {
}