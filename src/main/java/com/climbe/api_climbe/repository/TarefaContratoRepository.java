package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.TarefaContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarefaContratoRepository extends JpaRepository<TarefaContrato, Integer> {
    
    List<TarefaContrato> findByContrato_IdContrato(Integer contratoId);
    
    List<TarefaContrato> findByContrato_IdContratoAndColuna_IdColuna(Integer contratoId, Integer colunaId);
    
    List<TarefaContrato> findByContrato_IdContratoAndResponsavelPrincipal_IdUsuario(Integer contratoId, Integer usuarioId);
    
    List<TarefaContrato> findByContrato_IdContratoAndDataLimiteBefore(Integer contratoId, LocalDateTime dataLimite);
    
    @Query("SELECT t FROM TarefaContrato t WHERE t.contrato.idContrato = :contratoId AND t.coluna.tipo = 'FINAL'")
    List<TarefaContrato> findConcluidasByContrato(@Param("contratoId") Integer contratoId);
    
    @Query("SELECT t FROM TarefaContrato t WHERE t.contrato.idContrato = :contratoId AND t.dataLimite < :agora AND t.coluna.tipo != 'FINAL'")
    List<TarefaContrato> findVencidasByContrato(@Param("contratoId") Integer contratoId, @Param("agora") LocalDateTime agora);
    
    @Query("SELECT COUNT(t) FROM TarefaContrato t WHERE t.coluna.idColuna = :colunaId")
    long countByColunaId(@Param("colunaId") Integer colunaId);
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TarefaContrato t WHERE t.coluna.idColuna = :colunaId")
    boolean existsTarefasByColunaId(@Param("colunaId") Integer colunaId);

    @Query("SELECT COUNT(t) FROM TarefaContrato t WHERE t.coluna.tipo = 'INICIAL'")
    long countByColunaTipoInicial();

    @Query("SELECT COUNT(t) FROM TarefaContrato t WHERE t.coluna.tipo = 'INTERMEDIARIA'")
    long countByColunaTipoIntermediaria();

    @Query("SELECT COUNT(t) FROM TarefaContrato t WHERE t.coluna.tipo = 'FINAL'")
    long countByColunaTipoFinal();

    @Query("SELECT COUNT(t) FROM TarefaContrato t WHERE t.dataLimite < :agora AND t.coluna.tipo != 'FINAL'")
    long countVencidas(@Param("agora") LocalDateTime agora);

    @Query("SELECT COUNT(t) FROM TarefaContrato t WHERE t.dataLimite BETWEEN :inicio AND :fim AND t.coluna.tipo != 'FINAL'")
    long countVencendoNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT t.contrato.idContrato, COUNT(t) FROM TarefaContrato t WHERE t.dataLimite < :agora AND t.coluna.tipo != 'FINAL' GROUP BY t.contrato.idContrato ORDER BY COUNT(t) DESC")
    List<Object[]> findContratosComMaisTarefasVencidas(@Param("agora") LocalDateTime agora);

    @Query("SELECT FUNCTION('DATE', t.dataConclusao) as data, COUNT(t) FROM TarefaContrato t WHERE t.dataConclusao BETWEEN :inicio AND :fim AND t.coluna.tipo = 'FINAL' GROUP BY FUNCTION('DATE', t.dataConclusao) ORDER BY data ASC")
    List<Object[]> findConclusoesPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
