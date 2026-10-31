import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  effect,
  inject
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  Chart,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import {SimulationService} from '../../services/simulation.service';

Chart.register(
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Tooltip,
  Legend,
  Filler
);

@Component({
  selector: 'app-simulation-chart',
  imports: [CommonModule],
  templateUrl: './simulation-chart.html',
  styleUrl: './simulation-chart.css'
})
export class SimulationChart implements AfterViewInit, OnDestroy {
  @ViewChild('chartCanvas') private chartCanvas!: ElementRef<HTMLCanvasElement>;

  protected readonly simulationService = inject(SimulationService);
  protected readonly simulation = this.simulationService.currentSimulation;

  private chart: Chart | null = null;

  constructor() {
    effect(() => {
      const sim = this.simulation();
      if (sim && this.chartCanvas) {
        this.renderOrUpdateChart();
      }
    });
  }

  ngAfterViewInit(): void {
    if (this.simulation()) {
      this.renderOrUpdateChart();
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private renderOrUpdateChart(): void {
    const sim = this.simulation();
    if (!sim || !this.chartCanvas?.nativeElement) return;

    if (this.chart) {
      this.chart.destroy();
    }

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    // Simulação do cronograma SAC para visualização gráfica:
    // P = A + J
    // A (amortização constante) = financiado / prazo
    // A cada mês, Saldo cai por A.
    const termMonths = sim.termMonths;
    const financed = sim.financedAmount;
    const monthlyRate = sim.annualInterestRate / 100 / 12;
    const amortization = financed / termMonths;

    // Amostragem inteligente: se o prazo for longo (ex: 360 meses),
    // pegamos pontos anuais + primeiro e último mês para não sobrecarregar o canvas
    const sampleMonths: number[] = [];
    for (let m = 1; m <= termMonths; m++) {
      if (m === 1 || m === termMonths || m % 12 === 0) {
        sampleMonths.push(m);
      }
    }

    const labels: string[] = [];
    const totalInstallmentData: number[] = [];
    const amortizationData: number[] = [];
    const interestData: number[] = [];

    sampleMonths.forEach((m) => {
      labels.push(`Mês ${m}`);
      // Saldo no início do mês m
      const outstanding = financed - (m - 1) * amortization;
      const interest = outstanding * monthlyRate;
      const total = amortization + interest;

      amortizationData.push(Number(amortization.toFixed(2)));
      interestData.push(Number(interest.toFixed(2)));
      totalInstallmentData.push(Number(total.toFixed(2)));
    });

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Parcela Total (SAC)',
            data: totalInstallmentData,
            borderColor: '#059669', // emerald-600
            backgroundColor: 'rgba(5, 150, 105, 0.08)',
            fill: true,
            tension: 0.25,
            borderWidth: 2.5,
            pointRadius: 3,
            pointHoverRadius: 6
          },
          {
            label: 'Juros (Decrescente)',
            data: interestData,
            borderColor: '#f59e0b', // amber-500
            backgroundColor: 'rgba(245, 158, 11, 0.05)',
            borderDash: [5, 5],
            fill: false,
            tension: 0.25,
            borderWidth: 2,
            pointRadius: 2
          },
          {
            label: 'Amortização (Fixa)',
            data: amortizationData,
            borderColor: '#3b82f6', // blue-500
            borderDash: [3, 3],
            fill: false,
            tension: 0,
            borderWidth: 1.5,
            pointRadius: 0
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false
        },
        plugins: {
          legend: {
            position: 'top',
            labels: {
              boxWidth: 14,
              font: {size: 12, weight: 600}
            }
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const value = context.parsed.y ?? 0;
                return ` ${context.dataset.label}: R$ ${value.toLocaleString('pt-BR', {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2
                })}`;
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value) =>
                `R$ ${Number(value).toLocaleString('pt-BR', {maximumFractionDigits: 0})}`
            },
            grid: {
              color: 'rgba(226, 232, 240, 0.8)'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  }
}
