package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Integer> {

    Optional<Permissao> findByCodigo(String codigo);
}
