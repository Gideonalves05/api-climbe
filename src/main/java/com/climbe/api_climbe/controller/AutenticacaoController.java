package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.AtivarContaDto;
import com.climbe.api_climbe.dto.LoginFuncionarioDto;
import com.climbe.api_climbe.dto.SolicitacaoAcessoDto;
import com.climbe.api_climbe.dto.TokenRespostaDto;
import com.climbe.api_climbe.dto.UsuarioAutenticadoDto;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.service.AutenticacaoService;
import com.climbe.api_climbe.service.SolicitacaoAcessoService;
import com.climbe.api_climbe.service.UsuarioLogadoService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação de funcionários e solicitação de acesso")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final SolicitacaoAcessoService solicitacaoAcessoService;
    private final UsuarioLogadoService usuarioLogadoService;

    public AutenticacaoController(
            AutenticacaoService autenticacaoService,
            SolicitacaoAcessoService solicitacaoAcessoService,
            UsuarioLogadoService usuarioLogadoService
    ) {
        this.autenticacaoService = autenticacaoService;
        this.solicitacaoAcessoService = solicitacaoAcessoService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @PostMapping("/funcionarios/login")
    @Operation(summary = "Login do funcionário", description = "Autentica funcionário da Climbe e retorna token JWT com perfil/permissões.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenRespostaDto.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Credenciais inválidas\"}"))),
            @ApiResponse(responseCode = "403", description = "Usuário inativo",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Usuário não está ativo\"}")))
    })
    public TokenRespostaDto loginFuncionario(@Valid @RequestBody LoginFuncionarioDto dto) {
        return autenticacaoService.loginFuncionario(dto);
    }

    @GetMapping("/me")
    @Operation(summary = "Usuário autenticado", description = "Retorna dados do usuário autenticado via JWT, incluindo cargo e permissões efetivas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário autenticado",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioAutenticadoDto.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Usuário não autenticado\"}")))
    })
    public UsuarioAutenticadoDto obterUsuarioAutenticado() {
        Usuario usuario = usuarioLogadoService.exigirFuncionarioAtivo();
        return autenticacaoService.obterUsuarioAutenticado(usuario);
    }

    @PostMapping("/solicitar-acesso")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicitar acesso ao sistema", description = "Cria solicitação de acesso para novo usuário, ficando pendente de aprovação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\":409,\"error\":\"Conflict\",\"message\":\"E-mail já cadastrado no sistema\"}"))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/json"))
    })
    public void solicitarAcesso(@Valid @RequestBody SolicitacaoAcessoDto dto) {
        solicitacaoAcessoService.solicitarAcesso(dto);
    }

    @PostMapping("/ativar")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Ativar conta", description = "Ativa conta de usuário aprovado usando token recebido por e-mail.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido, expirado ou senhas não conferem",
                content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Token não encontrado",
                content = @Content(mediaType = "application/json"))
    })
    public void ativarConta(@Valid @RequestBody AtivarContaDto dto) {
        solicitacaoAcessoService.ativarConta(dto);
    }
}
