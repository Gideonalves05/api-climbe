package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.AuditoriaEvento;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, Integer> {

    Page<AuditoriaEvento> findByTipoEventoIn(List<TipoEventoAuditoria> tipos, Pageable pageable);

    Page<AuditoriaEvento> findByEntidadeAndEntidadeId(String entidade, Integer entidadeId, Pageable pageable);

    Page<AuditoriaEvento> findByAtorUsuarioId(Integer atorUsuarioId, Pageable pageable);

    @Query("SELECT a FROM AuditoriaEvento a WHERE " +
           "(:tipoEvento IS NULL OR a.tipoEvento = :tipoEvento) AND " +
           "(:entidade IS NULL OR a.entidade = :entidade) AND " +
           "(:entidadeId IS NULL OR a.entidadeId = :entidadeId) AND " +
           "(:atorUsuarioId IS NULL OR a.atorUsuarioId = :atorUsuarioId) AND " +
           "(:dataInicio IS NULL OR a.criadoEm >= :dataInicio) AND " +
           "(:dataFim IS NULL OR a.criadoEm <= :dataFim)")
    Page<AuditoriaEvento> buscarComFiltros(
            @Param("tipoEvento") TipoEventoAuditoria tipoEvento,
            @Param("entidade") String entidade,
            @Param("entidadeId") Integer entidadeId,
            @Param("atorUsuarioId") Integer atorUsuarioId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);

    Optional<AuditoriaEvento> findByCorrelationId(String correlationId);
}
