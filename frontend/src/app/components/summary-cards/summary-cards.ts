import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule, DecimalPipe, NgClass } from '@angular/common';
import { SimulationService } from '../../services/simulation.service';

@Component({
  selector: 'app-summary-cards',
  imports: [CommonModule, DecimalPipe, NgClass],
  templateUrl: './summary-cards.html',
  styleUrl: './summary-cards.css'
})
export class SummaryCards {
  protected readonly simulationService = inject(SimulationService);
  protected readonly copied = signal<boolean>(false);
  protected readonly simulation = this.simulationService.currentSimulation;

  // --- Signals Derivados com computed() ---

  // Percentual pago na entrada (ex: 20%)
  protected readonly downPaymentPercent = computed(() => {
    const sim = this.simulation();
    if (!sim || sim.propertyValue === 0) return 0;
    return (sim.downPayment / sim.propertyValue) * 100;
  });

  // Queda em reais entre a 1ª parcela e a última
  protected readonly installmentDrop = computed(() => {
    const sim = this.simulation();
    if (!sim) return 0;
    return sim.firstInstallmentAmount - sim.lastInstallmentAmount;
  });

  // Percentual de queda da parcela no SAC
  protected readonly installmentDropPercent = computed(() => {
    const sim = this.simulation();
    if (!sim || sim.firstInstallmentAmount === 0) return 0;
    return ((sim.firstInstallmentAmount - sim.lastInstallmentAmount) / sim.firstInstallmentAmount) * 100;
  });

  // Custo Real Total (Imóvel + Juros do Financiamento + ITBI + Cartório)
  protected readonly realTotalCost = computed(() => {
    const sim = this.simulation();
    if (!sim) return 0;
    return sim.totalPaid + sim.downPayment + sim.totalAcquisitionCosts;
  });

  /**
   * Copia o link compartilhável da simulação para a área de transferência.
   */
  copyShareLink(): void {
    const sim = this.simulation();
    if (!sim) return;

    const url = `${window.location.origin}/simulacao/${sim.id}`;
    navigator.clipboard.writeText(url).then(() => {
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 3000);
    });
  }
}
