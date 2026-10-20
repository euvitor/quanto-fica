package br.com.quantofica.backend.service;

import br.com.quantofica.backend.dto.AcquisitionCostRequestDTO;
import br.com.quantofica.backend.dto.AcquisitionCostResponseDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável pela estimativa de custos acessórios de aquisição imobiliária à vista
 * (ITBI, Cartório de Registro e Tarifa Bancária de Avaliação).
 */
@Service
public class AcquisitionCostService {

    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    // Tarifa média de avaliação física de garantia cobrada pelos bancos (SFH)
    private static final BigDecimal BANK_APPRAISAL_FEE = new BigDecimal("3500.00");

    // Alíquota média base de emolumentos cartorários estaduais (antes de eventuais descontos legais)
    private static final BigDecimal REGISTRY_BASE_RATE = new BigDecimal("0.0168");

    // Fator de desconto legal de 50% para primeiro imóvel (Art. 290 da Lei 6.015/73)
    private static final BigDecimal FIRST_PROPERTY_DISCOUNT_FACTOR = new BigDecimal("0.50");

    // Tabela de referência de alíquotas de ITBI das principais capitais/estados
    private static final Map<String, BigDecimal> ITBI_RATES = Map.of(
            "SP", new BigDecimal("0.03"),    // São Paulo: 3.0%
            "RJ", new BigDecimal("0.03"),    // Rio de Janeiro: 3.0%
            "MG", new BigDecimal("0.03"),    // Belo Horizonte: 3.0%
            "PR", new BigDecimal("0.027"),   // Curitiba: 2.7%
            "DF", new BigDecimal("0.03"),    // Brasília: 3.0%
            "RS", new BigDecimal("0.03")     // Porto Alegre: 3.0%
    );

    private static final BigDecimal DEFAULT_ITBI_RATE = new BigDecimal("0.03"); //Fallback padrão nacional: 3.0%

    public AcquisitionCostResponseDTO calculate(AcquisitionCostRequestDTO request) {
        validateRequest(request);

        BigDecimal propertyValue = request.propertyValue().setScale(CURRENCY_SCALE, ROUNDING_MODE);
        String state = request.state().toUpperCase().trim();
        boolean isFirstProperty = Boolean.TRUE.equals(request.isFirstProperty());

        // 1. Cálculo do ITBI
        BigDecimal itbiRate = ITBI_RATES.getOrDefault(state, DEFAULT_ITBI_RATE);
        BigDecimal itbiEstimated = propertyValue.multiply(itbiRate).setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 2.Cálculo do Cartório de Registro de Imóveis
        BigDecimal baseRegistry = propertyValue.multiply(REGISTRY_BASE_RATE).setScale(CURRENCY_SCALE, ROUNDING_MODE);
        BigDecimal registryEstimated;
        List<String> discountsApplied = new ArrayList<>();

        if (isFirstProperty) {
            // Aplica 50% de desconto garantido por lei federal
            registryEstimated = baseRegistry.multiply(FIRST_PROPERTY_DISCOUNT_FACTOR).setScale(CURRENCY_SCALE, ROUNDING_MODE);
            discountsApplied.add("Desconto legal de 50% nas custas cartorárias do 1º imóvel financiado (Art. 290 da Lei Federal 6.015/73)");
        } else {
            registryEstimated = baseRegistry;
        }

        // 3. Tarifa bancária de engenharia/avaliação
        BigDecimal bankAppraisalFee = BANK_APPRAISAL_FEE;

        // 4. Custo total à vista
        BigDecimal totalAcquisitionCosts = itbiEstimated
                .add(registryEstimated)
                .add(bankAppraisalFee)
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);

        return new AcquisitionCostResponseDTO(
                propertyValue,
                itbiEstimated,
                registryEstimated,
                bankAppraisalFee,
                totalAcquisitionCosts,
                discountsApplied
        );
    }

    private void validateRequest(AcquisitionCostRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("A requisição de custos de aquisição não pode ser nula.");
        }
        if (request.propertyValue() == null || request.propertyValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do imóvel deve ser maior que zero.");
        }
        if (request.state() == null || request.state().trim().length() != 2) {
            throw new IllegalArgumentException("O estado (UF) deve ser informado com exatamente 2 caracteres.");
        }
    }

}
