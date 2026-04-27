package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.NotificacaoOutbox;
import com.climbe.api_climbe.model.enums.CanalNotificacao;
import com.climbe.api_climbe.model.enums.StatusEntregaNotificacao;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacaoOutboxRepository extends JpaRepository<NotificacaoOutbox, Long> {

    List<NotificacaoOutbox> findByStatusAndProximaTentativaLessThanEqualOrderByProximaTentativaAsc(
        StatusEntregaNotificacao status,
        LocalDateTime proximaTentativa
    );

    @Query("SELECT o FROM NotificacaoOutbox o WHERE o.status = :status AND o.proximaTentativa <= :agora ORDER BY o.proximaTentativa ASC")
    List<NotificacaoOutbox> findPendentesParaProcessar(
        @Param("status") StatusEntregaNotificacao status,
        @Param("agora") LocalDateTime agora
    );

    List<NotificacaoOutbox> findByNotificacaoIdNotificacao(Integer notificacaoId);

    @Query("SELECT o FROM NotificacaoOutbox o WHERE o.notificacao.idNotificacao = :notificacaoId AND o.canal = :canal")
    Optional<NotificacaoOutbox> findByNotificacaoIdAndCanal(
        @Param("notificacaoId") Integer notificacaoId,
        @Param("canal") CanalNotificacao canal
    );

    @Query("SELECT COUNT(o) FROM NotificacaoOutbox o WHERE o.status = :status")
    long countByStatus(@Param("status") StatusEntregaNotificacao status);
}
