package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.TarefaChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaChecklistItemRepository extends JpaRepository<TarefaChecklistItem, Integer> {
    
    List<TarefaChecklistItem> findByTarefa_IdTarefaOrderByOrdemAsc(Integer tarefaId);
}
