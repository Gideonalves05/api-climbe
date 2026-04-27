package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Integer> {
    List<Servico> findAllByOrderByNomeAsc();

    Optional<Servico> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    @Query("SELECT COUNT(p) > 0 FROM Proposta p WHERE p.servico.idServico = :idServico")
    boolean existsVinculoEmPropostas(@Param("idServico") Integer idServico);

    @Query("SELECT COUNT(c) > 0 FROM Contrato c WHERE c.servico.idServico = :idServico")
    boolean existsVinculoEmContratos(@Param("idServico") Integer idServico);

    default boolean existsVinculoEmPropostasOuContratos(Integer idServico) {
        return existsVinculoEmPropostas(idServico) || existsVinculoEmContratos(idServico);
    }
}
