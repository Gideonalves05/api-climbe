package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.TarefaDependencia;
import com.climbe.api_climbe.model.TarefaContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarefaDependenciaRepository extends JpaRepository<TarefaDependencia, Integer> {
    
    List<TarefaDependencia> findByTarefa_IdTarefa(Integer tarefaId);
    
    List<TarefaDependencia> findByDependeDe_IdTarefa(Integer dependeDeId);
    
    Optional<TarefaDependencia> findByTarefa_IdTarefaAndDependeDe_IdTarefa(Integer tarefaId, Integer dependeDeId);
    
    boolean existsByTarefa_IdTarefaAndDependeDe_IdTarefa(Integer tarefaId, Integer dependeDeId);
}
