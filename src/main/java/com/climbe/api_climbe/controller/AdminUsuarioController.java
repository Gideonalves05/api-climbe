package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.RejeitarUsuarioDto;
import com.climbe.api_climbe.dto.UsuarioPendenteDto;
import com.climbe.api_climbe.service.SolicitacaoAcessoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Administração de Usuários", description = "Endpoints administrativos para gerenciamento de usuários")
public class AdminUsuarioController {

    private final SolicitacaoAcessoService solicitacaoAcessoService;

    public AdminUsuarioController(SolicitacaoAcessoService solicitacaoAcessoService) {
        this.solicitacaoAcessoService = solicitacaoAcessoService;
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasAuthority('USUARIO_LISTAR_PENDENTES')")
    @Operation(summary = "Listar usuários pendentes", description = "Lista todos os usuários com status PENDENTE_APROVACAO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários pendentes retornada"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para listar usuários pendentes")
    })
    public List<UsuarioPendenteDto> listarPendentes() {
        return solicitacaoAcessoService.listarPendentes();
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAuthority('USUARIO_APROVAR')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Aprovar usuário", description = "Aprova usuário pendente, permitindo que ele ative sua conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário aprovado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para aprovar usuários"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "400", description = "Usuário não está pendente de aprovação")
    })
    public void aprovarUsuario(@PathVariable Integer id) {
        solicitacaoAcessoService.aprovarUsuario(id);
    }

    @PostMapping("/{id}/rejeitar")
    @PreAuthorize("hasAuthority('USUARIO_APROVAR')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Rejeitar usuário", description = "Rejeita solicitação de acesso de usuário pendente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário rejeitado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para rejeitar usuários"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "400", description = "Usuário não está pendente de aprovação")
    })
    public void rejeitarUsuario(@PathVariable Integer id, @Valid @RequestBody RejeitarUsuarioDto dto) {
        solicitacaoAcessoService.rejeitarUsuario(id, dto);
    }
}
