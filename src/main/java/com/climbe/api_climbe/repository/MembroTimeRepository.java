package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.MembroTime;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembroTimeRepository extends JpaRepository<MembroTime, Integer> {
    
    List<MembroTime> findByContrato_IdContrato(Integer contratoId);
    
    List<MembroTime> findByContrato_IdContratoAndAtivoTrue(Integer contratoId);
    
    Optional<MembroTime> findByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(Integer contratoId, Integer usuarioId);
    
    boolean existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(Integer contratoId, Integer usuarioId);
    
    List<MembroTime> findByUsuario_IdUsuarioAndAtivoTrue(Integer usuarioId);
}
