package br.com.quantofica.backend.repository;

import br.com.quantofica.backend.model.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório Spring Data JPA para operações de persistência e consulta da entidade Sumulation.
 */
@Repository
public interface SimulationRepository  extends JpaRepository<Simulation, UUID> {

}
