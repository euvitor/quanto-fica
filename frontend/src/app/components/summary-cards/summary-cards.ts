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
   * Compartilha a simulação via Web Share API nativa (WhatsApp, AirDrop, etc.)
   * ou copia para a área de transferência com fallback automático.
   */
  async shareSimulation(): Promise<void> {
    const sim = this.simulation();
    if (!sim) return;

    const url = `${window.location.origin}/simulacao/${sim.id}`;
    const shareData = {
      title: 'QuantoFica - Simulação de Financiamento SAC',
      url
    };

    // 1. Tenta a Web Share API (sem concatenar textos para evitar corrupção de URL)
    if (typeof navigator !== 'undefined' && navigator.share && navigator.canShare && navigator.canShare(shareData)) {
      try {
        await navigator.share(shareData);
        return;
      } catch (err: any) {
        // Se o usuário cancelou o modal do SO, não faz nada
        if (err?.name === 'AbortError') return;
      }
    }

    // 2. Fallback: copia a URL limpa para a área de transferência
    try {
      await navigator.clipboard.writeText(url);
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 3000);
    } catch {
      // Fallback legado com input temporário
      const input = document.createElement('input');
      input.value = url;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 3000);
    }
  }
}
