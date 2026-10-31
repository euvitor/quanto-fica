import {Component, computed, inject, signal} from '@angular/core';
import {SimulationService} from '../../services/simulation.service';
import {Installment} from '../../models/simulation.model';
import {CommonModule, DecimalPipe} from '@angular/common';

@Component({
  imports: [CommonModule, DecimalPipe],
  selector: 'app-installment-table',
  styleUrl: './installment-table.css',
  templateUrl: './installment-table.html',
})
export class InstallmentTable {
  protected readonly simulationService = inject(SimulationService)
  protected readonly simulation = this.simulationService.currentSimulation
  protected readonly selectedYear = signal<number>(1)
  protected readonly totalYears = computed(() => {
    const sim = this.simulation()
    if (!sim) return 0
    return Math.ceil(sim.termMonths / 12)
  })
  protected readonly yearsList = computed(() => {
    return Array.from({length: this.totalYears()}, (_, i) => i + 1)
  })
  protected readonly yearInstallments = computed<Installment[]>(() => {
    const sim = this.simulation()
    if (!sim) return []

    const currentYear = this.selectedYear()
    const termMonths = sim.termMonths
    const financed = sim.financedAmount
    const monthlyRate = sim.annualInterestRate / 100 / 12
    const amortization = financed / termMonths

    const startMonth = (currentYear - 1) * 12 + 1
    const endMonth = Math.min(currentYear * 12, termMonths)

    const items: Installment[] = []

    for (let m = startMonth; m <= endMonth; m++) {
      // Saldo devedor antes de abater a amortização do mês m
      const balanceBefore = financed - (m - 1) * amortization
      const interest = balanceBefore * monthlyRate
      const total = amortization + interest
      const balanceAfter = Math.max(0, financed - m * amortization)

      items.push({
        installmentNumber: m,
        amortizationAmount: Number(amortization.toFixed(2)),
        interestAmount: Number(interest.toFixed(2)),
        totalInstallmentAmount: Number(total.toFixed(2)),
        outstandingBalance: Number(balanceAfter.toFixed(2))
      })
    }

    return items
  })

  protected readonly yearTotalPaid = computed(() => {
    return this.yearInstallments().reduce((sum, item) => sum + item.totalInstallmentAmount, 0)
  })

  protected readonly yearTotalInterest = computed(() => {
    return this.yearInstallments().reduce((sum, item) => sum + item.interestAmount, 0)
  })

  selectYear(year: number): void {
    this.selectedYear.set(year)
  }
}
