package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Integer> {

    Optional<Cargo> findByNomeCargo(String nomeCargo);
}
