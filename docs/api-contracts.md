# 🔌 Contratos da API REST (OpenAPI / RFC 7807)

Este documento especifica os endpoints, contratos de entrada (requests), saída (responses) e o padrão de tratamento de erros da API do **Quanto Fica**.

---

## 1. Padrão Global de Erros: RFC 7807 (`ProblemDetail`)

Em conformidade com as melhores práticas do Spring Boot 3+ e da especificação RFC 7807, todos os erros da API retornam com o cabeçalho `Content-Type: application/problem+json` e o seguinte esquema:

```json
{
  "type": "https://quantofica.com.br/errors/bad-request",
  "title": "Dados de entrada inválidos",
  "status": 400,
  "detail": "Um ou mais campos não passaram nas validações de negócio.",
  "instance": "/api/v1/simulations/sac",
  "timestamp": "2026-10-17T19:00:00Z",
  "invalidParams": [
    {
      "field": "propertyValue",
      "message": "O valor mínimo do imóvel para simulação é de R$ 50.000,00"
    }
  ]
}
```

---

## 2. Endpoints do Sistema de Amortização

### 2.1 Simular Financiamento no Sistema SAC
* **Método:** `POST`
* **Rota:** `/api/v1/simulations/sac`
* **Descrição:** Executa o cálculo determinístico do cronograma de amortização constante mês a mês e retorna o resumo executivo.

#### Requisição (`SimulationRequestDTO`)
```json
{
  "propertyValue": 500000.00,
  "downPayment": 100000.00,
  "termMonths": 360,
  "annualInterestRate": 9.99
}
```

**Regras de Validação:**
- `propertyValue`: Obrigatório, mínimo de R$ 50.000,00.
- `downPayment`: Obrigatório, não negativo, deve ser menor que `propertyValue`.
- `termMonths`: Obrigatório, mínimo de 12 meses, máximo de 420 meses (35 anos).
- `annualInterestRate`: Obrigatório, mínimo de 1.00% ao ano.

#### Resposta de Sucesso (`200 OK`) (`SimulationResponseDTO`)
```json
{
  "propertyValue": 500000.00,
  "downPayment": 100000.00,
  "financedAmount": 400000.00,
  "termMonths": 360,
  "annualInterestRate": 9.99,
  "monthlyInterestRate": 0.8325,
  "firstInstallmentAmount": 4441.11,
  "lastInstallmentAmount": 1120.38,
  "totalInterest": 600399.00,
  "totalPaid": 1000399.00,
  "installments": [
    {
      "installmentNumber": 1,
      "installmentAmount": 4441.11,
      "amortization": 1111.11,
      "interest": 3330.00,
      "outstandingBalance": 398888.89
    },
    {
      "installmentNumber": 2,
      "installmentAmount": 4431.86,
      "amortization": 1111.11,
      "interest": 3320.75,
      "outstandingBalance": 397777.78
    },
    {
      "installmentNumber": 360,
      "installmentAmount": 1120.38,
      "amortization": 1111.15,
      "interest": 9.23,
      "outstandingBalance": 0.00
    }
  ]
}
```

---

## 3. Endpoints de Custos Acessórios de Aquisição (Fase 3)

### 3.1 Calcular Despesas Iniciais e Cartorárias
* **Método:** `POST`
* **Rota:** `/api/v1/simulations/acquisition-costs`
* **Descrição:** Estima o montante à vista necessário para ITBI, registro de imóveis e tarifas bancárias.

#### Requisição
```json
{
  "propertyValue": 500000.00,
  "state": "SP",
  "city": "Sao Paulo",
  "isFirstProperty": true
}
```

#### Resposta de Sucesso (`200 OK`)
```json
{
  "propertyValue": 500000.00,
  "itbiEstimated": 15000.00,
  "registryEstimated": 4200.00,
  "bankAppraisalFee": 3500.00,
  "totalAcquisitionCosts": 22700.00,
  "discountsApplied": [
    "Desconto de 50% nas custas cartorárias do 1º imóvel financiado (Lei 6.015/73)"
  ]
}
```
