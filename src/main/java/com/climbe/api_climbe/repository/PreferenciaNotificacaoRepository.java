package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.PreferenciaNotificacao;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.CanalNotificacao;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferenciaNotificacaoRepository extends JpaRepository<PreferenciaNotificacao, Integer> {

    Optional<PreferenciaNotificacao> findByUsuarioAndTipoAndCanal(
        Usuario usuario,
        TipoNotificacao tipo,
        CanalNotificacao canal
    );

    boolean existsByUsuarioAndTipoAndCanal(
        Usuario usuario,
        TipoNotificacao tipo,
        CanalNotificacao canal
    );
}
