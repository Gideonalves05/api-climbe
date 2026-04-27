package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Empresa;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    Optional<Empresa> findByEmailIgnoreCase(String email);

    Optional<Empresa> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT e FROM Empresa e WHERE :termo IS NULL OR :termo = '' "
            + "OR LOWER(e.razaoSocial) LIKE LOWER(CONCAT('%', :termo, '%')) "
            + "OR LOWER(e.nomeFantasia) LIKE LOWER(CONCAT('%', :termo, '%')) "
            + "OR e.cnpj LIKE CONCAT('%', :termo, '%')")
    Page<Empresa> buscarPorTermo(@Param("termo") String termo, Pageable pageable);
}
