import {AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SimulationService} from '../../services/simulation.service';
import {CreateSimulationRequest} from '../../models/simulation.model';

/**
 * Validador cruzado: Garante que o valor da entrada seja menor que o valor do imóvel.
 */
function downPaymentValidator(control: AbstractControl): ValidationErrors | null {
  const propertyValue = control.get('propertyValue')?.value
  const downPayment = control.get('downPayment')?.value

  if (propertyValue != null && downPayment != null && downPayment >= propertyValue) {
    return {downPaymentTooHigh: true}
  }
  return null
}

@Component({
  selector: 'app-simulation-form',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './simulation-form.html',
  styleUrl: './simulation-form.css'
})
export class SimulationForm {
  private readonly fb = inject(FormBuilder).nonNullable
  protected readonly simulationService = inject(SimulationService)

  // Lista dos estados brasileiros para o select
  protected readonly states = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
    'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
    'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
  ]

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
    {validators: [downPaymentValidator]}
  )

  /**
   * Envia a requisição de cálculo e salvamento de simulação ao backend.
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched()
      return
    }

    const payload: CreateSimulationRequest = this.form.getRawValue()
    this.simulationService.createAndSave(payload).subscribe({
      next: (res) => {
        console.log('Simulação calculada e persistida com sucesso:', res)
      }
    })
  }

  // Atalhos para facilitar a validação no template HTML
  get propertyValueCtrl() {
    return this.form.controls.propertyValue
  }

  get downPaymentCtrl() {
    return this.form.controls.downPayment
  }

  get termMonthsCtrl() {
    return this.form.controls.termMonths
  }

  get annualInterestRateCtrl() {
    return this.form.controls.annualInterestRate
  }

}
