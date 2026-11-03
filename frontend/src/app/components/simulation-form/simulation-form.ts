import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Component, effect, inject } from '@angular/core';
import { CommonModule, DecimalPipe, NgClass } from '@angular/common';
import { SimulationService } from '../../services/simulation.service';
import { CreateSimulationRequest } from '../../models/simulation.model';

/**
 * Validador cruzado: Garante que o valor da entrada seja menor que o valor do imóvel.
 */
function downPaymentValidator(control: AbstractControl): ValidationErrors | null {
  const propertyValue = control.get('propertyValue')?.value;
  const downPayment = control.get('downPayment')?.value;

  if (propertyValue != null && downPayment != null && downPayment >= propertyValue) {
    return { downPaymentTooHigh: true };
  }
  return null;
}

@Component({
  selector: 'app-simulation-form',
  imports: [CommonModule, ReactiveFormsModule, DecimalPipe, NgClass],
  templateUrl: './simulation-form.html',
  styleUrl: './simulation-form.css'
})
export class SimulationForm {
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly simulationService = inject(SimulationService);

  // Lista dos estados brasileiros para o select
  protected readonly states = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
    'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
    'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
  ];

  // Formulário Reativo Fortemente Tipado
  protected readonly form = this.fb.group(
    {
      propertyValue: [500000, [Validators.required, Validators.min(50000)]],
      downPayment: [100000, [Validators.required, Validators.min(0)]],
      termMonths: [360, [Validators.required, Validators.min(12), Validators.max(420)]],
      annualInterestRate: [9.99, [Validators.required, Validators.min(1.0), Validators.max(30.0)]],
      state: ['SP', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
      city: ['São Paulo'],
      isFirstProperty: [true, [Validators.required]]
    },
    { validators: [downPaymentValidator] }
  );

  constructor() {
    // UX Proativa: ao alterar qualquer valor no formulário, limpa o alerta de erro anterior
    this.form.valueChanges.subscribe(() => {
      if (this.simulationService.errorMessage()) {
        this.simulationService.clearError();
      }
    });

    // UX de Compartilhamento: sincroniza o formulário com a simulação carregada via URL
    effect(() => {
      const sim = this.simulationService.currentSimulation();
      if (sim) {
        this.form.patchValue({
          propertyValue: sim.propertyValue,
          downPayment: sim.downPayment,
          termMonths: sim.termMonths,
          annualInterestRate: sim.annualInterestRate,
          state: sim.state,
          city: sim.city || '',
          isFirstProperty: sim.isFirstProperty
        }, { emitEvent: false });
      }
    });
  }

  /**
   * Converte valor monetário em notação humana concisa (ex: 500000 -> "R$ 500 mil", 1200000 -> "R$ 1,2 milhão").
   */
  formatCompactCurrency(value: number | null | undefined): string {
    if (!value || isNaN(value)) return '';
    if (value >= 1_000_000) {
      const formatted = (value / 1_000_000).toLocaleString('pt-BR', { maximumFractionDigits: 2 });
      return `R$ ${formatted} milh${value >= 2_000_000 ? 'ões' : 'ão'}`;
    }
    if (value >= 1_000) {
      const formatted = (value / 1_000).toLocaleString('pt-BR', { maximumFractionDigits: 1 });
      return `R$ ${formatted} mil`;
    }
    return `R$ ${value.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`;
  }

  /**
   * Percentual de entrada em relação ao valor total do imóvel.
   */
  get downPaymentPercent(): number {
    const prop = this.propertyValueCtrl.value;
    const down = this.downPaymentCtrl.value;
    if (!prop || prop === 0 || !down) return 0;
    return (down / prop) * 100;
  }

  /**
   * Atalho de UX: preenche instantaneamente o valor de 20% exigido pelos bancos no SAC.
   */
  applyStandard20Percent(): void {
    const prop = this.propertyValueCtrl.value;
    if (prop) {
      this.downPaymentCtrl.setValue(Math.round(prop * 0.20));
      this.downPaymentCtrl.markAsDirty();
    }
  }

  /**
   * Envia a requisição de cálculo e salvamento de simulação ao backend.
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: CreateSimulationRequest = this.form.getRawValue();
    this.simulationService.createAndSave(payload).subscribe({
      next: () => {
        // Auto-scroll suave de UX: rola a tela até a área de resultados
        setTimeout(() => {
          const resultElement = document.getElementById('resultado');
          if (resultElement) {
            resultElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }, 100);
      }
    });
  }

  // Atalhos para facilitar a validação no template HTML
  get propertyValueCtrl() {
    return this.form.controls.propertyValue;
  }

  get downPaymentCtrl() {
    return this.form.controls.downPayment;
  }

  get termMonthsCtrl() {
    return this.form.controls.termMonths;
  }

  get annualInterestRateCtrl() {
    return this.form.controls.annualInterestRate;
  }
}
