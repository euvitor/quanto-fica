package br.com.quantofica.backend.controller;

import br.com.quantofica.backend.dto.AcquisitionCostRequestDTO;
import br.com.quantofica.backend.dto.AcquisitionCostResponseDTO;
import br.com.quantofica.backend.dto.SimulationRequestDTO;
import br.com.quantofica.backend.dto.SimulationResponseDTO;
import br.com.quantofica.backend.service.AcquisitionCostService;
import br.com.quantofica.backend.service.SacSimulationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST responsável pelos endpoints de simulação financeira e custos de aquisição.
 */
@RestController
@RequestMapping("/api/v1/simulations")
public class SimulationController {

    private final SacSimulationService sacSimulationService;
    private final AcquisitionCostService acquisitionCostService;

    public SimulationController(
            SacSimulationService sacSimulationService,
            AcquisitionCostService acquisitionCostService
    ) {
        this.sacSimulationService = sacSimulationService;
        this.acquisitionCostService = acquisitionCostService;
    }

    @PostMapping("/sac")
    public ResponseEntity<SimulationResponseDTO> simulateSac(@Valid @RequestBody SimulationRequestDTO request) {
        SimulationResponseDTO response = sacSimulationService.simulate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/acquisition-costs")
    public ResponseEntity<AcquisitionCostResponseDTO> calculateAcquisitionCosts(
            @Valid @RequestBody AcquisitionCostRequestDTO request
    ) {
        AcquisitionCostResponseDTO response = acquisitionCostService.calculate(request);
        return ResponseEntity.ok(response);
    }
}
