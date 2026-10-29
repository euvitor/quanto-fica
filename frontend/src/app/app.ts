import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SimulationForm } from './components/simulation-form/simulation-form';
import { SimulationService } from './services/simulation.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, SimulationForm],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly simulationService = inject(SimulationService);
}
