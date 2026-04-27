package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.ColunaKanban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColunaKanbanRepository extends JpaRepository<ColunaKanban, Integer> {
    
    List<ColunaKanban> findByContrato_IdContratoOrderByOrdemAsc(Integer contratoId);
    
    Optional<ColunaKanban> findByContrato_IdContratoAndTipoAndOrdem(Integer contratoId, String tipo, Integer ordem);
    
    List<ColunaKanban> findByContrato_IdContratoAndTipoOrderByOrdemAsc(Integer contratoId, String tipo);
    
    boolean existsByContrato_IdContratoAndOrdem(Integer contratoId, Integer ordem);

    long countByContrato_IdContrato(Integer contratoId);
}
