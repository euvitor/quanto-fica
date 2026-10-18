package br.com.quantofica.backend.service;

import br.com.quantofica.backend.dto.InstallmentDTO;
import br.com.quantofica.backend.dto.SimulationRequestDTO;
import br.com.quantofica.backend.dto.SimulationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class SacSimulationServiceTest {
    private SacSimulationService service;

    @BeforeEach
    void setUp() {
        // Inicializa uma instância limpa do serviço antes de cada teste
        service = new SacSimulationService();
    }

    @Test
    @DisplayName("Deve simular com sucesso um financiamento habitacional de 360 meses padrão Caixa/SFH")
    void shouldSimulateStandard360MonthSuccessfully() {
        // 1. Cenário: Imóvel de 500k, 100k de entrada (400k financiado), a 9.99% a.a. por 360 meses
        SimulationRequestDTO request = new SimulationRequestDTO(
                new BigDecimal("500000.00"),
                new BigDecimal("100000.00"),
                360,
                new BigDecimal("9.99")
        );

        // 2. Ação
        SimulationResponseDTO response = service.simulate(request);

        // 3. Validações Gerais
        assertThat(response).isNotNull();
        assertThat(response.propertyValue()).isEqualByComparingTo("500000.00");
        assertThat(response.downPayment()).isEqualByComparingTo("100000.00");
        assertThat(response.financedAmount()).isEqualByComparingTo("400000.00");
        assertThat(response.termMonths()).isEqualTo(360);
        assertThat(response.monthlyInterestRate()).isEqualByComparingTo("0.8325");

        // 4. Validação da Primeira Parcela:
        // Amortização: 400.000 / 360 = 1.111,11
        // Juros mês 1: 400.000 * (9.99 / 1200) = 3.330,00
        // Parcela 1: 1.111,11 + 3.330,00 = 4.441,11
        InstallmentDTO firstInstallment = response.installments().get(0);
        assertThat(firstInstallment.installmentNumber()).isEqualTo(1);
        assertThat(firstInstallment.amortization()).isEqualByComparingTo("1111.11");
        assertThat(firstInstallment.interest()).isEqualByComparingTo("3330.00");
        assertThat(firstInstallment.installmentAmount()).isEqualByComparingTo("4441.11");
        assertThat(firstInstallment.outstandingBalance()).isEqualByComparingTo("398888.89");


        // 5. Validação da Última Parcela: Saldo devedor deve zerar rigorosamente!
        InstallmentDTO lastInstallment = response.installments().get(359);
        assertThat(lastInstallment.installmentNumber()).isEqualTo(360);
        assertThat(lastInstallment.outstandingBalance()).isEqualByComparingTo("0.00");

        // 6. Regra Fundamental: Soma de todas as amortizações deve ser exatamente igual aos 400.000 financiados
        BigDecimal totalAmortization = response.installments().stream()
                .map(InstallmentDTO::amortization)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalAmortization).isEqualByComparingTo(response.financedAmount());

        // 7. Validação da curva do SAC: As parcelas devem ser decrescentes
        for (int i = 0; i < response.installments().size() - 2; i++) {
            BigDecimal current = response.installments().get(i).installmentAmount();
            BigDecimal next = response.installments().get(i + 1).installmentAmount();
            assertThat(current).isGreaterThan(next);
        }
    }

    @Test
    @DisplayName("Deve ajustar centavos na última parcela evitando dízimas acumuladas (Breakage Adjustment)")
    void shouldAdjustCentavosInLastInstallmentWhenDivisionHasRepeatingDecimals() {
        // 100.000 / 360 = 277.777777...
        SimulationRequestDTO request = new SimulationRequestDTO(
                new BigDecimal("100000.00"),
                new BigDecimal("0.00"),
                360,
                new BigDecimal("10.00")
        );

        SimulationResponseDTO response = service.simulate(request);

        assertThat(response.installments()).hasSize(360);

        // A última parcela deve zerar a dívida
        InstallmentDTO last = response.installments().get(359);
        assertThat(last.outstandingBalance()).isEqualByComparingTo("0.00");
        assertThat(last.amortization()).isEqualByComparingTo("276.98");

        // A soma de todas as amortizações deve bater rigorosamente nos R$ 100.000,00
        BigDecimal sumAmortization = response.installments().stream()
                .map(InstallmentDTO::amortization)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(sumAmortization).isEqualByComparingTo("100000.00");
    }

    @Test
    @DisplayName("Deve simular com sucesso prazo curto de 12 meses e zerar saldo devedor")
    void shouldSimulateShortTermSuccessfully() {
        // Imóvel de 120k, 20k de entrada (100k financiado) em 12 meses a 12% a.a.
        SimulationRequestDTO request = new SimulationRequestDTO(
                new BigDecimal("120000.00"),
                new BigDecimal("20000.00"),
                12,
                new BigDecimal("12.00")
        );

        SimulationResponseDTO response = service.simulate(request);

        assertThat(response.installments()).hasSize(12);
        assertThat(response.financedAmount()).isEqualByComparingTo("100000.00");
        assertThat(response.monthlyInterestRate()).isEqualByComparingTo("1.0000"); // 12% / 12 = 1.0000% a.m.

        InstallmentDTO last = response.installments().get(11);
        assertThat(last.outstandingBalance()).isEqualByComparingTo("0.00");

        BigDecimal totalAmortization = response.installments().stream()
                .map(InstallmentDTO::amortization)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalAmortization).isEqualByComparingTo("100000.00");
    }

    @Test
    @DisplayName("Deve rejeitar entrada maior ou igual ao valor do imóvel")
    void shouldThrowExceptionWhenDownPaymentIsGreaterThanOrEqualToPropertyValue() {
        // Cenário 1: Entrada igual ao valor do imóvel
        SimulationRequestDTO requestEqual = new SimulationRequestDTO(
                new BigDecimal("300000.00"),
                new BigDecimal("300000.00"),
                240,
                new BigDecimal("9.50")
        );

        assertThatThrownBy(() -> service.simulate(requestEqual))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O valor de entrada deve ser menor que o valor do imóvel.");

        // Cenário 2: Entrada maior que o valor do imóvel
        SimulationRequestDTO requestGreater = new SimulationRequestDTO(
                new BigDecimal("300000.00"),
                new BigDecimal("350000.00"),
                240,
                new BigDecimal("9.50")
        );

        assertThatThrownBy(() -> service.simulate(requestGreater))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O valor de entrada deve ser menor que o valor do imóvel.");
    }

    @Test
    @DisplayName("Deve rejeitar valor de entrada negativo")
    void shouldThrowExceptionWhenDownPaymentIsNegative() {
        SimulationRequestDTO request = new SimulationRequestDTO(
                new BigDecimal("300000.00"),
                new BigDecimal("-5000.00"),
                240,
                new BigDecimal("9.50")
        );

        assertThatThrownBy(() -> service.simulate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O valor de entrada não pode ser negativo.");
    }

    @Test
    @DisplayName("Deve rejeitar prazo menor ou igual a zero")
    void shouldThrowExceptionWhenTermMonthsIsInvalid() {
        SimulationRequestDTO request = new SimulationRequestDTO(
                new BigDecimal("300000.00"),
                new BigDecimal("50000.00"),
                0,
                new BigDecimal("9.50")
        );

        assertThatThrownBy(() -> service.simulate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O prazo do financiamento deve ser maior que zero.");
    }

    @Test
    @DisplayName("Deve rejeitar taxa de juros menor ou igual a zero")
    void shouldThrowExceptionWhenInterestRateIsInvalid() {
        SimulationRequestDTO request = new SimulationRequestDTO(
                new BigDecimal("300000.00"),
                new BigDecimal("50000.00"),
                120,
                new BigDecimal("0.00")
        );

        assertThatThrownBy(() -> service.simulate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A taxa de juros anual deve ser maior que zero.");
    }
}
