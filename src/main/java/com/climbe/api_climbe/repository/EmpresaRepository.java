package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Empresa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    Optional<Empresa> findByEmailIgnoreCase(String email);

    Optional<Empresa> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    boolean existsByEmailIgnoreCase(String email);
}
