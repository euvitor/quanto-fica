package br.com.quantofica.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidade JPA representando uma simulação persistida no banco de dados.
 * Unifica os dados do financiamento (SAC) e os custos acessórios de aquisição.
 */
@Entity
@Table(name = "simulations")
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // --- Dados de Entrada do Financiamento ---
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal propertyValue;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal downPayment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal financedAmount;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal annualInterestRate;

    // --- Resultados Consolidados do SAC ---
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal firstInstallmentAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal lastInstallmentAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterest;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPaid;

    // --- Custos Acessórios de Aquisição ---
    @Column(nullable = false, length = 2)
    private String state;

    @Column(length = 100)
    private String city;

    @Column(nullable = false)
    private Boolean isFirstProperty;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal itbiEstimated;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal registryEstimated;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bankAppraisalFee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAcquisitionCosts;

    // --- Auditoria ---
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Construtor padrão obrigatório para o JPA/Hibernate
    protected Simulation() {
    }

    public Simulation(
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
            BigDecimal totalAcquisitionCosts
    ) {
        this.propertyValue = propertyValue;
        this.downPayment = downPayment;
        this.financedAmount = financedAmount;
        this.termMonths = termMonths;
        this.annualInterestRate = annualInterestRate;
        this.firstInstallmentAmount = firstInstallmentAmount;
        this.lastInstallmentAmount = lastInstallmentAmount;
        this.totalInterest = totalInterest;
        this.totalPaid = totalPaid;
        this.state = state;
        this.city = city;
        this.isFirstProperty = isFirstProperty;
        this.itbiEstimated = itbiEstimated;
        this.registryEstimated = registryEstimated;
        this.bankAppraisalFee = bankAppraisalFee;
        this.totalAcquisitionCosts = totalAcquisitionCosts;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    // --- Getters e Setters para Testes ---
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getPropertyValue() {
        return propertyValue;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public BigDecimal getFinancedAmount() {
        return financedAmount;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public BigDecimal getFirstInstallmentAmount() {
        return firstInstallmentAmount;
    }

    public BigDecimal getLastInstallmentAmount() {
        return lastInstallmentAmount;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public Boolean getIsFirstProperty() {
        return isFirstProperty;
    }

    public BigDecimal getItbiEstimated() {
        return itbiEstimated;
    }

    public BigDecimal getRegistryEstimated() {
        return registryEstimated;
    }

    public BigDecimal getBankAppraisalFee() {
        return bankAppraisalFee;
    }

    public BigDecimal getTotalAcquisitionCosts() {
        return totalAcquisitionCosts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}