package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Proposta;
import com.climbe.api_climbe.model.enums.StatusContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

    boolean existsByProposta_IdProposta(Integer idProposta);
    Optional<Contrato> findByProposta(Proposta proposta);

    @Query("SELECT COUNT(c) FROM Contrato c WHERE c.status = :status")
    long countByStatus(@Param("status") StatusContrato status);

    @Query("SELECT c.idContrato, COALESCE(c.empresa.nomeFantasia, p.empresa.nomeFantasia) FROM Contrato c LEFT JOIN c.proposta p WHERE c.idContrato IN :contratoIds")
    List<Object[]> findNomesEmpresasPorIds(@Param("contratoIds") java.util.List<Integer> contratoIds);

    @Query("SELECT c FROM Contrato c LEFT JOIN FETCH c.proposta p LEFT JOIN FETCH p.empresa LEFT JOIN FETCH p.usuarioResponsavel LEFT JOIN FETCH c.empresa LEFT JOIN FETCH c.servico WHERE (c.empresa.idEmpresa = :idEmpresa OR p.empresa.idEmpresa = :idEmpresa) ORDER BY c.dataInicio DESC")
    List<Contrato> findByEmpresaId(@Param("idEmpresa") Integer idEmpresa);

    @Query("SELECT c FROM Contrato c LEFT JOIN FETCH c.proposta p LEFT JOIN FETCH p.empresa LEFT JOIN FETCH p.usuarioResponsavel LEFT JOIN FETCH c.empresa LEFT JOIN FETCH c.servico WHERE (c.empresa.idEmpresa = :idEmpresa OR p.empresa.idEmpresa = :idEmpresa) AND c.status = :status ORDER BY c.dataInicio DESC")
    List<Contrato> findByEmpresaIdAndStatus(@Param("idEmpresa") Integer idEmpresa, @Param("status") StatusContrato status);

    @Query("SELECT COUNT(c) FROM Contrato c WHERE (c.empresa.idEmpresa = :idEmpresa OR c.proposta.empresa.idEmpresa = :idEmpresa) AND c.status = :status")
    long countByEmpresaIdAndStatus(@Param("idEmpresa") Integer idEmpresa, @Param("status") StatusContrato status);

    @Query("SELECT COUNT(c) FROM Contrato c WHERE (c.empresa.idEmpresa = :idEmpresa OR c.proposta.empresa.idEmpresa = :idEmpresa)")
    long countByEmpresaId(@Param("idEmpresa") Integer idEmpresa);
}
