package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.TarefaAnexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaAnexoRepository extends JpaRepository<TarefaAnexo, Integer> {
    
    List<TarefaAnexo> findByTarefa_IdTarefa(Integer tarefaId);
}
