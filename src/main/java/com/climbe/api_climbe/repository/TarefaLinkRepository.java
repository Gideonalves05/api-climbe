package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.TarefaLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarefaLinkRepository extends JpaRepository<TarefaLink, Integer> {
    List<TarefaLink> findByTarefa_IdTarefa(Integer tarefaId);
}
