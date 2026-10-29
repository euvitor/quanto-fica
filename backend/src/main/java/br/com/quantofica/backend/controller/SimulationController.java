package br.com.quantofica.backend.controller;

import br.com.quantofica.backend.dto.AcquisitionCostRequestDTO;
import br.com.quantofica.backend.dto.AcquisitionCostResponseDTO;
import br.com.quantofica.backend.dto.CreateSimulationRequestDTO;
import br.com.quantofica.backend.dto.SavedSimulationResponseDTO;
import br.com.quantofica.backend.dto.SimulationRequestDTO;
import br.com.quantofica.backend.dto.SimulationResponseDTO;
import br.com.quantofica.backend.service.AcquisitionCostService;
import br.com.quantofica.backend.service.SacSimulationService;
import br.com.quantofica.backend.service.SimulationPersistenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST responsável pelos endpoints de simulação financeira, custos de aquisição
 * e persistência/compartilhamento de simulações.
 */
@CrossOrigin(origins = "http://localhost:4200", exposedHeaders = "Location")
@RestController
@RequestMapping("/api/v1/simulations")
public class SimulationController {

    private final SacSimulationService sacSimulationService;
    private final AcquisitionCostService acquisitionCostService;
    private final SimulationPersistenceService simulationPersistenceService;

    public SimulationController(
            SacSimulationService sacSimulationService,
            AcquisitionCostService acquisitionCostService,
            SimulationPersistenceService simulationPersistenceService
    ) {
        this.sacSimulationService = sacSimulationService;
        this.acquisitionCostService = acquisitionCostService;
        this.simulationPersistenceService = simulationPersistenceService;
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

    @PostMapping
    public ResponseEntity<SavedSimulationResponseDTO> createAndSave(
            @Valid @RequestBody CreateSimulationRequestDTO request
    ) {
        SavedSimulationResponseDTO saved = simulationPersistenceService.createAndSave(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavedSimulationResponseDTO> findById(@PathVariable UUID id) {
        SavedSimulationResponseDTO response = simulationPersistenceService.findById(id);
        return ResponseEntity.ok(response);
    }
}