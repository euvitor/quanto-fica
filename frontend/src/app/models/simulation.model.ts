/**
 * Parcela individual do cronograma SAC.
 * Espelha: InstallmentDTO.java
 */
export interface Installment {
  installmentNumber: number;
  totalInstallmentAmount: number;
  amortizationAmount: number;
  interestAmount: number;
  outstandingBalance: number;
}

/**
 * Resposta do motor de cálculo SAC.
 * Espelha: SimulationResponseDTO.java
 */
export interface SimulationResponse {
  propertyValue: number;
  downPayment: number;
  financedAmount: number;
  termMonths: number;
  annualInterestRate: number;
  monthlyInterestRate: number;
  firstInstallmentAmount: number;
  lastInstallmentAmount: number;
  totalInterest: number;
  totalPaid: number;
  installments: Installment[];
}

/**
 * Requisição rápida para apuração do SAC.
 * Espelha: SimulationRequestDTO.java
 */
export interface SimulationRequest {
  propertyValue: number;
  downPayment: number;
  termMonths: number;
  annualInterestRate: number;
}

/**
 * Requisição completa para cálculo e persistência no banco.
 * Espelha: CreateSimulationRequestDTO.java
 */
export interface CreateSimulationRequest {
  propertyValue: number;
  downPayment: number;
  termMonths: number;
  annualInterestRate: number;
  state: string;
  city?: string;
  isFirstProperty: boolean;
}

/**
 * Resposta completa de uma simulação salva com custos ocultos.
 * Espelha: SavedSimulationResponseDTO.java
 */
export interface SavedSimulationResponse {
  id: string; // UUID gerado pelo backend
  propertyValue: number;
  downPayment: number;
  financedAmount: number;
  termMonths: number;
  annualInterestRate: number;
  firstInstallmentAmount: number;
  lastInstallmentAmount: number;
  totalInterest: number;
  totalPaid: number;
  state: string;
  city?: string;
  isFirstProperty: boolean;
  itbiEstimated: number;
  registryEstimated: number;
  bankAppraisalFee: number;
  totalAcquisitionCosts: number;
  createdAt: string; // ISO 8601 Instant vindo do Java
}

/**
 * Estrutura padronizada de erros RFC 7807 (Problem Details).
 * Espelha a saída do GlobalExceptionHandler.java
 */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  invalidParams?: {
    field: string;
    reason: string;
  }[];
}
