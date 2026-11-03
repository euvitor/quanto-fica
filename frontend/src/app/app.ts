import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SimulationForm } from './components/simulation-form/simulation-form';
import { SummaryCards } from './components/summary-cards/summary-cards';
import { SimulationChart } from './components/simulation-chart/simulation-chart';
import { InstallmentTable } from './components/installment-table/installment-table';
import { SimulationService } from './services/simulation.service';

@Component({
  selector: 'app-root',
  imports: [
    CommonModule,
    SimulationForm,
    SummaryCards,
    SimulationChart,
    InstallmentTable
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  // Signal que armazena o UUID caso a página seja acessada via rota compartilhada (/simulacao/:id)
  readonly id = signal<string | null>(null);

  protected readonly simulationService = inject(SimulationService);

  constructor() {
    this.detectAndLoadSharedSimulation();
  }

  /**
   * Detecta o UUID da simulação diretamente na rota e dispara a busca no backend.
   * Utiliza Regex para blindar contra textos concatenados acidentalmente no link.
   */
  private detectAndLoadSharedSimulation(): void {
    if (typeof window === 'undefined') return;

    const path = window.location.pathname;
    const uuidRegex = /[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/;
    const match = path.match(uuidRegex);

    if (match) {
      const simulationId = match[0];
      this.id.set(simulationId);
      this.simulationService.loadSimulationById(simulationId);
    }
  }

  /**
   * Reseta a simulação ativa e limpa a rota para permitir um novo cálculo limpo.
   */
  onNewSimulation(): void {
    this.id.set(null);
    this.simulationService.reset();
    if (typeof window !== 'undefined' && window.history) {
      window.history.pushState({}, '', '/');
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
