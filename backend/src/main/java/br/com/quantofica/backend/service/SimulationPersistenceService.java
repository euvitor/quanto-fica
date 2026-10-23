package br.com.quantofica.backend.service;

import br.com.quantofica.backend.dto.AcquisitionCostRequestDTO;
import br.com.quantofica.backend.dto.AcquisitionCostResponseDTO;
import br.com.quantofica.backend.dto.CreateSimulationRequestDTO;
import br.com.quantofica.backend.dto.SavedSimulationResponseDTO;
import br.com.quantofica.backend.dto.SimulationRequestDTO;
import br.com.quantofica.backend.dto.SimulationResponseDTO;
import br.com.quantofica.backend.exception.ResourceNotFoundException;
import br.com.quantofica.backend.model.Simulation;
import br.com.quantofica.backend.repository.SimulationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável por orquestrar o cálculo financeiro, apuração de custos e persistência
 * de simulações com links compartilháveis no banco de dados.
 */
@Service
public class SimulationPersistenceService {

    private final SimulationRepository simulationRepository;
    private final SacSimulationService sacSimulationService;
    private final AcquisitionCostService acquisitionCostService;

    public SimulationPersistenceService(
            SimulationRepository simulationRepository,
            SacSimulationService sacSimulationService,
            AcquisitionCostService acquisitionCostService
    ) {
        this.simulationRepository = simulationRepository;
        this.sacSimulationService = sacSimulationService;
        this.acquisitionCostService = acquisitionCostService;
    }

    /**
     * Calcula o SAC, apura custos de aquisição e persiste a simulação gerando um UUID único.
     */
    @Transactional
    public SavedSimulationResponseDTO createAndSave(CreateSimulationRequestDTO request) {
        // 1. Calcula o motor SAC
        SimulationRequestDTO sacRequest = new SimulationRequestDTO(
                request.propertyValue(),
                request.downPayment(),
                request.termMonths(),
                request.annualInterestRate()
        );
        SimulationResponseDTO sacResponse = sacSimulationService.simulate(sacRequest);

        // 2. Calcula os custos acessórios (ITBI e Cartório)
        AcquisitionCostRequestDTO costRequest = new AcquisitionCostRequestDTO(
                request.propertyValue(),
                request.state(),
                request.city(),
                request.isFirstProperty()
        );
        AcquisitionCostResponseDTO costResponse = acquisitionCostService.calculate(costRequest);

        // 3. Monta a entidade JPA consolidada
        Simulation simulation = new Simulation(
                request.propertyValue(),
                request.downPayment(),
                sacResponse.financedAmount(),
                request.termMonths(),
                request.annualInterestRate(),
                sacResponse.firstInstallmentAmount(),
                sacResponse.lastInstallmentAmount(),
                sacResponse.totalInterest(),
                sacResponse.totalPaid(),
                request.state().toUpperCase().trim(),
                request.city(),
                request.isFirstProperty(),
                costResponse.itbiEstimated(),
                costResponse.registryEstimated(),
                costResponse.bankAppraisalFee(),
                costResponse.totalAcquisitionCosts()
        );

        // 4. Salva no banco de dados (o UUID e o createdAt são gerados aqui)
        Simulation saved = simulationRepository.save(simulation);

        return toResponseDTO(saved);
    }

    /**
     * Consulta uma simulação persistida pelo seu UUID compartilhável.
     */
    @Transactional(readOnly = true)
    public SavedSimulationResponseDTO findById(UUID id) {
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulação não encontrada para o ID informado: " + id));

        return toResponseDTO(simulation);
    }

    private SavedSimulationResponseDTO toResponseDTO(Simulation s) {
        return new SavedSimulationResponseDTO(
                s.getId(),
                s.getPropertyValue(),
                s.getDownPayment(),
                s.getFinancedAmount(),
                s.getTermMonths(),
                s.getAnnualInterestRate(),
                s.getFirstInstallmentAmount(),
                s.getLastInstallmentAmount(),
                s.getTotalInterest(),
                s.getTotalPaid(),
                s.getState(),
                s.getCity(),
                s.getIsFirstProperty(),
                s.getItbiEstimated(),
                s.getRegistryEstimated(),
                s.getBankAppraisalFee(),
                s.getTotalAcquisitionCosts(),
                s.getCreatedAt()
        );
    }
}