import { Component, computed, effect, inject, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { SimulationService } from '../../services/simulation.service';
import { Installment } from '../../models/simulation.model';

@Component({
  selector: 'app-installment-table',
  imports: [CommonModule, DecimalPipe],
  templateUrl: './installment-table.html',
  styleUrl: './installment-table.css'
})
export class InstallmentTable {
  protected readonly simulationService = inject(SimulationService);
  protected readonly simulation = this.simulationService.currentSimulation;

  // Signal com o ano atualmente selecionado pelo usuário (ex: Ano 1, Ano 2...)
  protected readonly selectedYear = signal<number>(1);

  // Quantidade total de anos do financiamento (ex: 360 meses = 30 anos)
  protected readonly totalYears = computed(() => {
    const sim = this.simulation();
    if (!sim) return 0;
    return Math.ceil(sim.termMonths / 12);
  });

  // Lista de anos para renderizar os botões/chips de navegação [1, 2, 3, ..., 30]
  protected readonly yearsList = computed(() => {
    return Array.from({ length: this.totalYears() }, (_, i) => i + 1);
  });

  constructor() {
    // Blindagem de UX: sempre que uma nova simulação for calculada,
    // se o ano selecionado for maior que o novo prazo total, reseta para o Ano 1.
    effect(() => {
      const maxYears = this.totalYears();
      if (this.selectedYear() > maxYears && maxYears > 0) {
        this.selectedYear.set(1);
      }
    });
  }

  // Calcula com alta performance as 12 parcelas correspondentes ao ano selecionado
  protected readonly yearInstallments = computed<Installment[]>(() => {
    const sim = this.simulation();
    if (!sim) return [];

    const currentYear = this.selectedYear();
    const termMonths = sim.termMonths;
    const financed = sim.financedAmount;
    const monthlyRate = sim.annualInterestRate / 100 / 12;
    const amortization = financed / termMonths;

    const startMonth = (currentYear - 1) * 12 + 1;
    const endMonth = Math.min(currentYear * 12, termMonths);

    const items: Installment[] = [];

    for (let m = startMonth; m <= endMonth; m++) {
      // Saldo devedor antes de abater a amortização do mês m
      const balanceBefore = financed - (m - 1) * amortization;
      const interest = balanceBefore * monthlyRate;
      const total = amortization + interest;
      const balanceAfter = Math.max(0, financed - m * amortization);

      items.push({
        installmentNumber: m,
        amortizationAmount: Number(amortization.toFixed(2)),
        interestAmount: Number(interest.toFixed(2)),
        totalInstallmentAmount: Number(total.toFixed(2)),
        outstandingBalance: Number(balanceAfter.toFixed(2))
      });
    }

    return items;
  });

  // Total pago em parcelas durante o ano selecionado
  protected readonly yearTotalPaid = computed(() => {
    return this.yearInstallments().reduce((sum, item) => sum + item.totalInstallmentAmount, 0);
  });

  // Total pago em juros durante o ano selecionado
  protected readonly yearTotalInterest = computed(() => {
    return this.yearInstallments().reduce((sum, item) => sum + item.interestAmount, 0);
  });

  // Troca o ano selecionado
  selectYear(year: number): void {
    this.selectedYear.set(year);
  }
}
