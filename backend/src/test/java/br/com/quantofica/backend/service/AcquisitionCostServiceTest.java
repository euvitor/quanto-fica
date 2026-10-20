package br.com.quantofica.backend.service;

import br.com.quantofica.backend.dto.AcquisitionCostRequestDTO;
import br.com.quantofica.backend.dto.AcquisitionCostResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AcquisitionCostServiceTest {

    private AcquisitionCostService service;

    @BeforeEach
    void setUp(){
        service = new AcquisitionCostService();
    }

    @Test
    @DisplayName("Deve calcular custos para 1º imóvel em SP aplicando 50% de desconto no cartório")
    void shouldCalculateCostsForFirstPropertyInSPWithDiscount(){
        AcquisitionCostRequestDTO request = new AcquisitionCostRequestDTO(
                new BigDecimal("500000.00"),
                "SP",
                "São Paulo",
                true
        );

        AcquisitionCostResponseDTO response = service.calculate(request);

        assertThat(response).isNotNull();
        assertThat(response.propertyValue()).isEqualByComparingTo("500000.00");
        assertThat(response.itbiEstimated()).isEqualByComparingTo("15000.00"); // 3% de 500k
        assertThat(response.registryEstimated()).isEqualByComparingTo("4200.00"); // 50% de (1.68% de 500k = 8400)
        assertThat(response.bankAppraisalFee()).isEqualByComparingTo("3500.00");
        assertThat(response.totalAcquisitionCosts()).isEqualByComparingTo("22700.00");
    }

    @Test
    @DisplayName("Deve utilizar taxa padrão de ITBI (3.0%) para estados não mapeados explicitamente")
    void shouldUseDefaultItbiRateForUnmappedStates(){
        AcquisitionCostRequestDTO request = new AcquisitionCostRequestDTO(
                new BigDecimal("300000.00"),
                "AM",
                "Manaus",
                true
        );

        AcquisitionCostResponseDTO response = service.calculate(request);

        assertThat(response.itbiEstimated()).isEqualByComparingTo("9000.00"); // 3.0% de 300k
    }

    @Test
    @DisplayName("Deve lançar exceção quando o valor do imóvel for menor ou igual a zero")
    void shouldThrowExceptionWhenPropertyValueIsZeroOrNegative(){
        AcquisitionCostRequestDTO request = new AcquisitionCostRequestDTO(
                BigDecimal.ZERO,
                "SP",
                "São Paulo",
                true
        );

        assertThatThrownBy(()-> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O valor do imóvel deve ser maior que zero.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando a UF for inválida")
    void shouldTrhowExceptionWhenStateIsInvalid(){
        AcquisitionCostRequestDTO request = new AcquisitionCostRequestDTO(
                new BigDecimal("200000.00"),
                "SAO PAULO", // Mais de 2 caracteres
                "São Paulo",
                true
        );

        assertThatThrownBy(()-> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O estado (UF) deve ser informado com exatamente 2 caracteres.");
    }
}
