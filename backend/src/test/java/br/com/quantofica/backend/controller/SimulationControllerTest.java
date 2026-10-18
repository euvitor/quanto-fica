package br.com.quantofica.backend.controller;

import br.com.quantofica.backend.dto.InstallmentDTO;
import br.com.quantofica.backend.dto.SimulationRequestDTO;
import br.com.quantofica.backend.dto.SimulationResponseDTO;
import br.com.quantofica.backend.exception.GlobalExceptionHandler;
import br.com.quantofica.backend.service.SacSimulationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulationController.class)
@Import(GlobalExceptionHandler.class) // Garante que nosso tratador de erros participe do teste web
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SacSimulationService sacSimulationService;

    @Test
    @DisplayName("POST /api/v1/simulations/sac - Deve retornar 200 OK com simulação calculada")
    void shouldReturn200WhenPayloadIsValid() throws Exception {
        // 1. Cenário: Preparar a resposta simulada que o Mock vai devolver
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

        // Ensinando o Mock: quando qualquer SimulationRequestDTO for passado para simulate, retorne o mockResponse
        when(sacSimulationService.simulate(any(SimulationRequestDTO.class))).thenReturn(mockResponse);

        // JSON de envio (usando Text Blocks """ do Java 17+)
        String jsonPayload = """
                {
                    "propertyValue": 500000.00,
                    "downPayment": 100000.00,
                    "termMonths": 360,
                    "annualInterestRate": 9.99
                }
                """;

        // 2. Ação e Validação HTTP
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
        // Payload com 4 violações: imóvel < 50k, entrada negativa, prazo < 12 meses, taxa < 1%
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
        // Ensinando o Mock a simular uma falha de regra de negócio
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
}