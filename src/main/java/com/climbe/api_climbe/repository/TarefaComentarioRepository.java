package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.TarefaComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaComentarioRepository extends JpaRepository<TarefaComentario, Integer> {
    
    List<TarefaComentario> findByTarefa_IdTarefaOrderByCriadoEmDesc(Integer tarefaId);
}
