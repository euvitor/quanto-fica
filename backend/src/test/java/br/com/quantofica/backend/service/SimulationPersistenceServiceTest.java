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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationPersistenceServiceTest {

    @Mock
    private SimulationRepository simulationRepository;

    @Mock
    private SacSimulationService sacSimulationService;

    @Mock
    private AcquisitionCostService acquisitionCostService;

    @InjectMocks
    private SimulationPersistenceService persistenceService;

    @Test
    @DisplayName("Deve orquestrar cálculo SAC, apurar custos e persistir com sucesso")
    void shouldOrchestrateAndSaveSimulationSuccessfully() {
        CreateSimulationRequestDTO request = new CreateSimulationRequestDTO(
                new BigDecimal("500000.00"),
                new BigDecimal("100000.00"),
                360,
                new BigDecimal("9.99"),
                "sp",
                "São Paulo",
                true
        );

        SimulationResponseDTO mockSac = new SimulationResponseDTO(
                new BigDecimal("500000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("400000.00"),
                360,
                new BigDecimal("9.99"),
                new BigDecimal("0.8325"),
                new BigDecimal("4441.11"),
                new BigDecimal("1120.38"),
                new BigDecimal("600399.00"),
                new BigDecimal("1000399.00"),
                List.of()
        );

        AcquisitionCostResponseDTO mockCost = new AcquisitionCostResponseDTO(
                new BigDecimal("500000.00"),
                new BigDecimal("15000.00"),
                new BigDecimal("4200.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("22700.00"),
                List.of()
        );

        UUID generatedId = UUID.randomUUID();
        when(sacSimulationService.simulate(any(SimulationRequestDTO.class))).thenReturn(mockSac);
        when(acquisitionCostService.calculate(any(AcquisitionCostRequestDTO.class))).thenReturn(mockCost);
        when(simulationRepository.save(any(Simulation.class))).thenAnswer(invocation -> {
            Simulation sim = invocation.getArgument(0);
            sim.setId(generatedId);
            sim.setCreatedAt(Instant.now());
            return sim;
        });

        SavedSimulationResponseDTO response = persistenceService.createAndSave(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(generatedId);
        assertThat(response.financedAmount()).isEqualByComparingTo("400000.00");
        assertThat(response.state()).isEqualTo("SP"); // Garantir normalização uppercase
        assertThat(response.totalAcquisitionCosts()).isEqualByComparingTo("22700.00");
        assertThat(response.createdAt()).isNotNull();

        verify(simulationRepository).save(any(Simulation.class));
    }

    @Test
    @DisplayName("Deve retornar simulação existente ao buscar por UUID")
    void shouldReturnSimulationWhenFoundById() {
        UUID id = UUID.randomUUID();
        Simulation sim = new Simulation(
                new BigDecimal("500000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("400000.00"),
                360,
                new BigDecimal("9.99"),
                new BigDecimal("4441.11"),
                new BigDecimal("1120.38"),
                new BigDecimal("600399.00"),
                new BigDecimal("1000399.00"),
                "SP",
                "São Paulo",
                true,
                new BigDecimal("15000.00"),
                new BigDecimal("4200.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("22700.00")
        );
        sim.setId(id);
        sim.setCreatedAt(Instant.now());

        when(simulationRepository.findById(id)).thenReturn(Optional.of(sim));

        SavedSimulationResponseDTO response = persistenceService.findById(id);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.propertyValue()).isEqualByComparingTo("500000.00");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando UUID não for encontrado")
    void shouldThrowExceptionWhenSimulationNotFound() {
        UUID id = UUID.randomUUID();
        when(simulationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> persistenceService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}