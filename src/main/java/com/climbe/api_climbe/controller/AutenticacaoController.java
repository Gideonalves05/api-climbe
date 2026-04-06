package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.CadastroEmpresaDto;
import com.climbe.api_climbe.dto.EmpresaDto;
import com.climbe.api_climbe.dto.LoginEmpresaDto;
import com.climbe.api_climbe.dto.LoginFuncionarioDto;
import com.climbe.api_climbe.dto.TokenRespostaDto;
import com.climbe.api_climbe.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação de funcionários e empresas")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
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

    @PostMapping("/empresas/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastro de empresa", description = "Realiza o cadastro inicial da empresa contratante no fluxo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empresa cadastrada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmpresaDto.class))),
            @ApiResponse(responseCode = "409", description = "CNPJ ou e-mail já cadastrado",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\":409,\"error\":\"Conflict\",\"message\":\"CNPJ já cadastrado\"}"))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/json"))
    })
    public EmpresaDto cadastrarEmpresa(@Valid @RequestBody CadastroEmpresaDto dto) {
        return autenticacaoService.cadastrarEmpresa(dto);
    }

    @PostMapping("/empresas/login")
    @Operation(summary = "Login da empresa", description = "Autentica empresa por CNPJ ou e-mail e retorna token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenRespostaDto.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Credenciais inválidas\"}")))
    })
    public TokenRespostaDto loginEmpresa(@Valid @RequestBody LoginEmpresaDto dto) {
        return autenticacaoService.loginEmpresa(dto);
    }
}
