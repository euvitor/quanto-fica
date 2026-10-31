import {Component, effect, inject, input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SimulationForm} from './components/simulation-form/simulation-form';
import {SummaryCards} from './components/summary-cards/summary-cards';
import {SimulationChart} from './components/simulation-chart/simulation-chart';
import {InstallmentTable} from './components/installment-table/installment-table';
import {SimulationService} from './services/simulation.service';

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
  // Signal Input injetado automaticamente pela URL (/simulacao/:id)
  readonly id = input<string | undefined>(undefined);

  protected readonly simulationService = inject(SimulationService);

  constructor() {
    effect(() => {
      const simulationId = this.id();
      if (simulationId) {
        this.simulationService.loadSimulationById(simulationId);
      }
    });
  }
}
