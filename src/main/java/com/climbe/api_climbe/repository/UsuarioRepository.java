package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);
}
