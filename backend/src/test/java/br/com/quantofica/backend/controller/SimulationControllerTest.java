package br.com.quantofica.backend.controller;

import br.com.quantofica.backend.dto.*;
import br.com.quantofica.backend.exception.GlobalExceptionHandler;
import br.com.quantofica.backend.exception.ResourceNotFoundException;
import br.com.quantofica.backend.service.AcquisitionCostService;
import br.com.quantofica.backend.service.SacSimulationService;
import br.com.quantofica.backend.service.SimulationPersistenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulationController.class)
@Import(GlobalExceptionHandler.class)
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SacSimulationService sacSimulationService;

    @MockitoBean
    private AcquisitionCostService acquisitionCostService;

    @MockitoBean
    private SimulationPersistenceService simulationPersistenceService;

    @Test
    @DisplayName("POST /api/v1/simulations/sac - Deve retornar 200 OK com simulação calculada")
    void shouldReturn200WhenPayloadIsValid() throws Exception {
        SimulationResponseDTO mockResponse = new SimulationResponseDTO(
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
                List.of(
                        new InstallmentDTO(1, new BigDecimal("4441.11"), new BigDecimal("1111.11"), new BigDecimal("3330.00"), new BigDecimal("398888.89")),
                        new InstallmentDTO(360, new BigDecimal("1120.38"), new BigDecimal("1111.15"), new BigDecimal("9.23"), new BigDecimal("0.00"))
                )
        );

        when(sacSimulationService.simulate(any(SimulationRequestDTO.class))).thenReturn(mockResponse);

        String jsonPayload = """
                {
                    "propertyValue": 500000.00,
                    "downPayment": 100000.00,
                    "termMonths": 360,
                    "annualInterestRate": 9.99
                }
                """;

        mockMvc.perform(post("/api/v1/simulations/sac")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyValue").value(500000.00))
                .andExpect(jsonPath("$.financedAmount").value(400000.00))
                .andExpect(jsonPath("$.termMonths").value(360))
                .andExpect(jsonPath("$.firstInstallmentAmount").value(4441.11))
                .andExpect(jsonPath("$.lastInstallmentAmount").value(1120.38))
                .andExpect(jsonPath("$.installments.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/simulations/sac - Deve retornar 400 Bad Request com RFC 7807 quando dados forem inválidos")
    void shouldReturn400WhenPayloadIsInvalid() throws Exception {
        String invalidPayload = """
                {
                    "propertyValue": 10000.00,
                    "downPayment": -50.00,
                    "termMonths": 6,
                    "annualInterestRate": 0.50
                }
                """;

        mockMvc.perform(post("/api/v1/simulations/sac")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://quantofica.com.br/errors/bad-request"))
                .andExpect(jsonPath("$.title").value("Dados de entrada inválidos"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidParams").isArray())
                .andExpect(jsonPath("$.invalidParams.length()").value(4));
    }

    @Test
    @DisplayName("POST /api/v1/simulations/sac - Deve retornar 400 Bad Request quando regra de negócio for violada")
    void shouldReturn400WhenBusinessRuleViolated() throws Exception {
        when(sacSimulationService.simulate(any(SimulationRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("O valor de entrada deve ser menor que o valor do imóvel."));

        String payload = """
                {
                    "propertyValue": 300000.00,
                    "downPayment": 300000.00,
                    "termMonths": 240,
                    "annualInterestRate": 9.50
                }
                """;

        mockMvc.perform(post("/api/v1/simulations/sac")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://quantofica.com.br/errors/business-rule"))
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"))
                .andExpect(jsonPath("$.detail").value("O valor de entrada deve ser menor que o valor do imóvel."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/simulations/acquisition-costs - Deve retornar 200 OK com custos calculados")
    void shouldReturn200WhenAcquisitionCostPayloadIsValid() throws Exception {
        AcquisitionCostResponseDTO mockCostResponse = new AcquisitionCostResponseDTO(
                new BigDecimal("500000.00"),
                new BigDecimal("15000.00"),
                new BigDecimal("4200.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("22700.00"),
                List.of("Desconto legal de 50% nas custas cartorárias do 1º imóvel financiado (Art. 290 da Lei Federal 6.015/73)")
        );

        when(acquisitionCostService.calculate(any(AcquisitionCostRequestDTO.class))).thenReturn(mockCostResponse);

        String jsonPayload = """
                {
                    "propertyValue": 500000.00,
                    "state": "SP",
                    "city": "São Paulo",
                    "isFirstProperty": true
                }
                """;

        mockMvc.perform(post("/api/v1/simulations/acquisition-costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyValue").value(500000.00))
                .andExpect(jsonPath("$.itbiEstimated").value(15000.00))
                .andExpect(jsonPath("$.registryEstimated").value(4200.00))
                .andExpect(jsonPath("$.bankAppraisalFee").value(3500.00))
                .andExpect(jsonPath("$.totalAcquisitionCosts").value(22700.00))
                .andExpect(jsonPath("$.discountsApplied.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/simulations/acquisition-costs - Deve retornar 400 Bad Request quando UF ou valor forem inválidos")
    void shouldReturn400WhenAcquisitionCostPayloadIsInvalid() throws Exception {
        String invalidPayload = """
                {
                    "propertyValue": 1000.00,
                    "state": "SAO PAULO",
                    "isFirstProperty": true
                }
                """;

        mockMvc.perform(post("/api/v1/simulations/acquisition-costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://quantofica.com.br/errors/bad-request"))
                .andExpect(jsonPath("$.title").value("Dados de entrada inválidos"))
                .andExpect(jsonPath("$.invalidParams").isArray())
                .andExpect(jsonPath("$.invalidParams.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/simulations - Deve retornar 201 Created com header Location e dados salvos")
    void shouldReturn201WhenCreateAndSavePayloadIsValid() throws Exception {
        UUID generatedId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        SavedSimulationResponseDTO savedMock = new SavedSimulationResponseDTO(
                generatedId,
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
                new BigDecimal("22700.00"),
                Instant.now()
        );

        when(simulationPersistenceService.createAndSave(any(CreateSimulationRequestDTO.class))).thenReturn(savedMock);

        String jsonPayload = """
                {
                    "propertyValue": 500000.00,
                    "downPayment": 100000.00,
                    "termMonths": 360,
                    "annualInterestRate": 9.99,
                    "state": "SP",
                    "city": "São Paulo",
                    "isFirstProperty": true
                }
                """;

        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/simulations/" + generatedId)))
                .andExpect(jsonPath("$.id").value(generatedId.toString()))
                .andExpect(jsonPath("$.propertyValue").value(500000.00))
                .andExpect(jsonPath("$.state").value("SP"))
                .andExpect(jsonPath("$.totalAcquisitionCosts").value(22700.00));
    }

    @Test
    @DisplayName("GET /api/v1/simulations/{id} - Deve retornar 200 OK quando simulação existir")
    void shouldReturn200WhenSimulationExists() throws Exception {
        UUID existingId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        SavedSimulationResponseDTO savedMock = new SavedSimulationResponseDTO(
                existingId,
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
                new BigDecimal("22700.00"),
                Instant.now()
        );

        when(simulationPersistenceService.findById(existingId)).thenReturn(savedMock);

        mockMvc.perform(get("/api/v1/simulations/{id}", existingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId.toString()))
                .andExpect(jsonPath("$.propertyValue").value(500000.00))
                .andExpect(jsonPath("$.totalPaid").value(1000399.00));
    }

    @Test
    @DisplayName("GET /api/v1/simulations/{id} - Deve retornar 404 Not Found com RFC 7807 quando ID não existir")
    void shouldReturn404WhenSimulationNotFound() throws Exception {
        UUID nonExistingId = UUID.randomUUID();

        when(simulationPersistenceService.findById(nonExistingId))
                .thenThrow(new ResourceNotFoundException("Simulação não encontrada para o ID: " + nonExistingId));

        mockMvc.perform(get("/api/v1/simulations/{id}", nonExistingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://quantofica.com.br/errors/not-found"))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(containsString(nonExistingId.toString())));
    }
}