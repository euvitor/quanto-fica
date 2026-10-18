package br.com.quantofica.backend.service;

import br.com.quantofica.backend.dto.InstallmentDTO;
import br.com.quantofica.backend.dto.SimulationRequestDTO;
import br.com.quantofica.backend.dto.SimulationResponseDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class SacSimulationService {
    private static final int CURRENCY_SCALE = 2;       // Escala para valores em Reais (2 casas)
    private static final int RATE_SCALE = 4;           // Escala para taxa mensal exibida (ex: 0.8325%)
    private static final int CALCULATION_SCALE = 10;   // Precisão para taxa mensal decimal em divisões
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);
    private static final BigDecimal ANNUAL_TO_MONTHLY_DIVISOR = BigDecimal.valueOf(1200); // 12 * 100

    private void validateRequest(SimulationRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("A requisição de simulação não pode ser nula.");
        }
        if (request.propertyValue() == null) {
            throw new IllegalArgumentException("O valor do imóvel é obrigatório.");
        }
        if (request.downPayment() == null) {
            throw new IllegalArgumentException("O valor de entrada é obrigatório.");
        }
        if (request.downPayment().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor de entrada não pode ser negativo.");
        }
        if (request.downPayment().compareTo(request.propertyValue()) >= 0) {
            throw new IllegalArgumentException("O valor de entrada deve ser menor que o valor do imóvel.");
        }
        if (request.termMonths() == null || request.termMonths() <= 0) {
            throw new IllegalArgumentException("O prazo do financiamento deve ser maior que zero.");
        }
        if (request.annualInterestRate() == null || request.annualInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A taxa de juros anual deve ser maior que zero.");
        }
    }

    public SimulationResponseDTO simulate(SimulationRequestDTO request) {
        validateRequest(request);

        BigDecimal propertyValue = request.propertyValue().setScale(CURRENCY_SCALE, ROUNDING_MODE);
        BigDecimal downPayment = request.downPayment().setScale(CURRENCY_SCALE, ROUNDING_MODE);
        BigDecimal financedAmount = propertyValue.subtract(downPayment).setScale(CURRENCY_SCALE, ROUNDING_MODE);
        int termMonths = request.termMonths();
        BigDecimal annualInterestRate = request.annualInterestRate();

        // 1. Taxa nominal mensal de exibição (ex: 9.99 / 12 = 0.8325%)
        BigDecimal monthlyInterestRate = annualInterestRate.divide(MONTHS_IN_YEAR, RATE_SCALE, ROUNDING_MODE);

        // 2. Fator decimal para cálculo de juros (ex: 9.99 / 1200 = 0.0083250000)
        BigDecimal monthlyRateFactor = annualInterestRate.divide(ANNUAL_TO_MONTHLY_DIVISOR, CALCULATION_SCALE, ROUNDING_MODE);

        // 3. Amortização base fixa
        BigDecimal baseAmortization = financedAmount.divide(BigDecimal.valueOf(termMonths), CURRENCY_SCALE, ROUNDING_MODE);

        // 4. Estruturas para acumular o cronograma
        List<InstallmentDTO> installments = new ArrayList<>(termMonths);
        BigDecimal currentBalance = financedAmount;
        BigDecimal totalInterest = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 5. Loop mê a mês do SAC
        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interest = currentBalance.multiply(monthlyRateFactor).setScale(CURRENCY_SCALE, ROUNDING_MODE);
            totalInterest = totalInterest.add(interest);

            BigDecimal amortization;
            BigDecimal newBalance;

            if (month == termMonths) {
                amortization = currentBalance;
                newBalance = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
            } else {
                amortization = baseAmortization;
                newBalance = currentBalance.subtract(amortization).setScale(CURRENCY_SCALE, ROUNDING_MODE);
            }

            BigDecimal installmentAmount = amortization.add(interest).setScale(CURRENCY_SCALE, ROUNDING_MODE);

            installments.add(new InstallmentDTO(
                    month,
                    installmentAmount,
                    amortization,
                    interest,
                    newBalance
            ));

            currentBalance = newBalance;
        }

        // 6. Totalizadores e resposta final
        BigDecimal firstInstallmentAmount = installments.get(0).installmentAmount();
        BigDecimal lastInstallmentAmount = installments.get(installments.size() - 1).installmentAmount();
        BigDecimal totalPaid = financedAmount.add(totalInterest).setScale(CURRENCY_SCALE, ROUNDING_MODE);

        return new SimulationResponseDTO(
                propertyValue,
                downPayment,
                financedAmount,
                termMonths,
                annualInterestRate,
                monthlyInterestRate,
                firstInstallmentAmount,
                lastInstallmentAmount,
                totalInterest,
                totalPaid,
                installments
        );
    }
}
