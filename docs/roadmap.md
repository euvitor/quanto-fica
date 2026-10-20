# 🗺️ Roadmap de Execução e Checklist de Progresso

Este documento é o plano diretor do projeto **Quanto Fica**. Ele deve ser consultado e atualizado a cada avanço para manter o alinhamento de escopo.

---

## 📌 Status Geral do Projeto

- **Fase Atual:** Fase 4 — Persistência e Compartilhamento (PostgreSQL)
- **Progresso da Fase 1:** 100% concluído ✅
- **Progresso da Fase 2:** 100% concluído ✅
- **Progresso da Fase 3:** 100% concluído ✅
- **Progresso da Fase 4:** 0% concluído

---

## 📋 Fases e Tarefas Detalhadas

### 🔹 Fase 1: Motor de Cálculo SAC (Backend Core) ✅
> **Objetivo:** Ter a lógica financeira do SAC 100% testada e blindada matematicamente antes de mexer com web ou banco de dados.

- [x] **1.1** Criar DTO de entrada: `SimulationRequestDTO` (com validações de formato e limites).
- [x] **1.2** Criar DTO de linha da tabela: `InstallmentDTO`.
- [x] **1.3** Criar DTO consolidado: `SimulationResponseDTO`.
- [x] **1.4** Implementar `SacSimulationService`:
  - Cálculo de amortização fixa: $A = \text{Financiado} / n$.
  - Conversão nominal anual para mensal: $i = i_{anual} / 12$.
  - Loop de cálculo mês a mês com juros decrescentes.
  - Regra de liquidação exata na última parcela (evitar sobras ou faltas de centavos).
- [x] **1.5** Escrever testes unitários em `SacSimulationServiceTest` (JUnit 5 + AssertJ):
  - Cenário de 360 meses (30 anos) com taxa CEF típica.
  - Cenário de prazo curto (12 meses).
  - Validação de tolerância zero de saldo residual no final do contrato.
  - Validação de ajuste de quebra/centavos (breakage).
  - Validações de limites e valores inválidos (`assertThatThrownBy`).

---

### 🔹 Fase 2: Exposição REST e Validações de Entrada ✅
> **Objetivo:** Expor a simulação via HTTP com tratamento de erros robusto.

- [x] **2.1** Criar `SimulationController` com endpoint `POST /api/v1/simulations/sac`.
- [x] **2.2** Configurar anotação `@Valid` e validar regra de negócio cruzada (ex: `downPayment < propertyValue`).
- [x] **2.3** Criar `GlobalExceptionHandler` estendendo padrão RFC 7807 (`ProblemDetail`).
- [x] **2.4** Escrever testes de controller com `@WebMvcTest` (testando retornos 200 e 400).

---

### 🔹 Fase 3: Módulo de Custos de Aquisição (ITBI e Cartório) ✅
> **Objetivo:** Adicionar o cálculo dos custos ocultos de compra de imóvel à vista.

- [x] **3.1** Criar DTOs para requisição e resposta de custos de aquisição (`AcquisitionCostRequestDTO` e `AcquisitionCostResponseDTO`).
- [x] **3.2** Implementar serviço de estimativa de ITBI (alíquotas municipais com fallback).
- [x] **3.3** Implementar serviço de cálculo de emolumentos cartorários e desconto de 50% para 1º imóvel (Art. 290 da Lei 6.015/73).
- [x] **3.4** Expor endpoint `POST /api/v1/simulations/acquisition-costs` no `SimulationController`.
- [x] **3.5** Testes unitários do módulo de custos em `AcquisitionCostServiceTest` e testes web em `SimulationControllerTest`.

---

### 🔹 Fase 4: Persistência e Compartilhamento (PostgreSQL)
> **Objetivo:** Permitir salvar simulações e gerar links compartilháveis para amigos e corretores.

- [ ] **4.1** Modelar entidades JPA (`Simulation`, `SimulationSummary`).
- [ ] **4.2** Configurar migrations com Flyway (versionamento do schema do banco).
- [ ] **4.3** Criar `SimulationRepository` com Spring Data JPA.
- [ ] **4.4** Criar endpoint `GET /api/v1/simulations/{uuid}` para consulta de simulações salvas.

---

### 🔹 Fase 5: Frontend Interativo (Next.js)
> **Objetivo:** Criar a interface visual para o usuário final.

- [ ] **5.1** Inicializar projeto Next.js (App Router, Tailwind CSS, TypeScript).
- [ ] **5.2** Componente de formulário com máscaras monetárias (R$).
- [ ] **5.3** Cards de resumo de impacto financeiro (Primeira Parcela, Última Parcela, Custo Total).
- [ ] **5.4** Gráfico interativo (Recharts) mostrando a curva de amortização versus juros.
- [ ] **5.5** Tabela expansível com o cronograma completo de parcelas.

---

### 🔹 Fase 6: DevOps, Containerização e CI/CD
> **Objetivo:** Preparar o projeto para deploy contínuo em produção.

- [ ] **6.1** Criar `Dockerfile` multi-stage otimizado para a aplicação Spring Boot.
- [ ] **6.2** Criar `docker-compose.yml` unindo Backend, Frontend e PostgreSQL.
- [ ] **6.3** Configurar workflow do GitHub Actions para rodar testes e build a cada commit.
