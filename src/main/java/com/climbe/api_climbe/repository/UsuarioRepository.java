package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.cargo LEFT JOIN FETCH u.permissoes WHERE LOWER(u.email) = LOWER(:email)")
    Optional<Usuario> findByEmailIgnoreCaseWithCargoAndPermissoes(String email);

    Optional<Usuario> findByTokenAtivacao(String token);

    List<Usuario> findBySituacao(SituacaoUsuario situacao);
}
