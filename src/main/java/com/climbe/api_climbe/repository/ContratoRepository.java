package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

    boolean existsByProposta_IdProposta(Integer idProposta);
}
