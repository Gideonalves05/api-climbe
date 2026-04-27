package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Notificacao;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    List<Notificacao> findByUsuarioOrderByCriadoEmDesc(Usuario usuario);

    List<Notificacao> findByUsuarioAndLidaFalseOrderByCriadoEmDesc(Usuario usuario);

    List<Notificacao> findByUsuarioAndTipoOrderByCriadoEmDesc(Usuario usuario, TipoNotificacao tipo);

    @Query("SELECT n FROM Notificacao n WHERE n.usuario = :usuario AND n.criadoEm >= :desde ORDER BY n.criadoEm DESC")
    List<Notificacao> findByUsuarioAndCriadoEmAfterOrderByCriadoEmDesc(
        @Param("usuario") Usuario usuario,
        @Param("desde") LocalDateTime desde
    );

    @Query("SELECT n FROM Notificacao n WHERE n.usuario = :usuario AND n.lida = false AND n.tipo = :tipo ORDER BY n.criadoEm DESC")
    List<Notificacao> findByUsuarioAndLidaFalseAndTipoOrderByCriadoEmDesc(
        @Param("usuario") Usuario usuario,
        @Param("tipo") TipoNotificacao tipo
    );

    List<Notificacao> findByUsuarioAndLidaOrderByCriadoEmDesc(Usuario usuario, Boolean lida);

    List<Notificacao> findByUsuarioAndLidaAndTipoOrderByCriadoEmDesc(Usuario usuario, Boolean lida, TipoNotificacao tipo);

    @Query("SELECT n FROM Notificacao n WHERE n.usuario = :usuario AND n.lida = :lida AND n.tipo = :tipo AND n.criadoEm >= :desde ORDER BY n.criadoEm DESC")
    List<Notificacao> findByUsuarioAndLidaAndTipoAndCriadoEmAfterOrderByCriadoEmDesc(
        @Param("usuario") Usuario usuario,
        @Param("lida") Boolean lida,
        @Param("tipo") TipoNotificacao tipo,
        @Param("desde") LocalDateTime desde
    );
}
