package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.UsuarioResumoDto;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Listagem de usuários do sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar usuários ativos",
            description = "Lista os usuários ativos do sistema, opcionalmente filtrados por nome/email.")
    public List<UsuarioResumoDto> listar(@RequestParam(required = false) String termo) {
        String filtro = termo == null ? "" : termo.trim().toLowerCase();
        return usuarioRepository.findBySituacao(SituacaoUsuario.ATIVO).stream()
                .filter(u -> filtro.isEmpty()
                        || (u.getNomeCompleto() != null && u.getNomeCompleto().toLowerCase().contains(filtro))
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(filtro)))
                .sorted(Comparator.comparing(Usuario::getNomeCompleto, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(50)
                .map(u -> new UsuarioResumoDto(
                        u.getIdUsuario(),
                        u.getNomeCompleto(),
                        u.getEmail(),
                        u.getCargo() != null ? u.getCargo().getNomeCargo() : null
                ))
                .toList();
    }
}
