package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Empresa;
import com.climbe.api_climbe.model.Proposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropostaRepository extends JpaRepository<Proposta, Integer> {
    Optional<Proposta> findFirstByEmpresaOrderByDataCriacaoDesc(Empresa empresa);

    @Query("SELECT p FROM Proposta p JOIN FETCH p.empresa JOIN FETCH p.usuarioResponsavel WHERE p.empresa.idEmpresa = :idEmpresa ORDER BY p.dataCriacao DESC")
    List<Proposta> findByEmpresaId(@Param("idEmpresa") Integer idEmpresa);

    long countByEmpresa_IdEmpresa(Integer idEmpresa);
}
