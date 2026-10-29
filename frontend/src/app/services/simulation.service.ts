import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import {
  CreateSimulationRequest,
  SavedSimulationResponse,
  SimulationRequest,
  SimulationResponse
} from '../models/simulation.model';

@Injectable({
  providedIn: 'root' // Garante que o serviço seja um Singleton disponível em toda a aplicação
})
export class SimulationService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/v1/simulations';

  // --- Signals de Estado da Aplicação ---
  // Armazena a simulação ativa exibida na tela
  readonly currentSimulation = signal<SavedSimulationResponse | null>(null);
  // Controla o estado de carregamento/spinner na tela
  readonly isLoading = signal<boolean>(false);
  // Armazena mensagens de erro para exibição ao usuário
  readonly errorMessage = signal<string | null>(null);

  /**
   * Executa a simulação matemática do SAC (cálculo rápido sem persistência).
   */
  simulateSac(request: SimulationRequest): Observable<SimulationResponse> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    return this.http.post<SimulationResponse>(`${this.apiUrl}/sac`, request).pipe(
      tap({
        next: () => this.isLoading.set(false),
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(this.extractErrorMessage(err));
        }
      })
    );
  }

  /**
   * Calcula o SAC, apura custos de cartório/ITBI e salva no PostgreSQL gerando um UUID.
   */
  createAndSave(request: CreateSimulationRequest): Observable<SavedSimulationResponse> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    return this.http.post<SavedSimulationResponse>(this.apiUrl, request).pipe(
      tap({
        next: (saved) => {
          this.currentSimulation.set(saved);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(this.extractErrorMessage(err));
        }
      })
    );
  }

  /**
   * Consulta uma simulação persistida no banco pelo seu UUID compartilhável.
   */
  getById(id: string): Observable<SavedSimulationResponse> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    return this.http.get<SavedSimulationResponse>(`${this.apiUrl}/${id}`).pipe(
      tap({
        next: (simulation) => {
          this.currentSimulation.set(simulation);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(this.extractErrorMessage(err));
        }
      })
    );
  }

  /**
   * Limpa o estado atual da simulação (para reiniciar o formulário).
   */
  reset(): void {
    this.currentSimulation.set(null);
    this.errorMessage.set(null);
    this.isLoading.set(false);
  }

  /**
   * Extrai a mensagem amigável de erro a partir do padrão RFC 7807 devolvido pelo Spring.
   */
  private extractErrorMessage(error: any): string {
    if (error?.error?.detail) {
      return error.error.detail;
    }
    if (error?.error?.title) {
      return error.error.title;
    }
    return 'Não foi possível conectar ao servidor. Verifique se o backend está em execução.';
  }
}
