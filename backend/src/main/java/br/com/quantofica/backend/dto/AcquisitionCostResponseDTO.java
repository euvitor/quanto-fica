package br.com.quantofica.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resumo consolidado dos custos acessórios de aquisição imobiliária (ITBI, Cartório e Banco).
 */
public record AcquisitionCostResponseDTO(
        BigDecimal propertyValue,
        BigDecimal itbiEstimated,
        BigDecimal registryEstimated,
        BigDecimal bankAppraisalFee,
        BigDecimal totalAcquisitionCosts,
        List<String> discountsApplied
) {
}