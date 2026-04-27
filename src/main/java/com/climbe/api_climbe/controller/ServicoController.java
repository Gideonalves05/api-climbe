package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.AtualizarServicoDto;
import com.climbe.api_climbe.dto.CriarServicoDto;
import com.climbe.api_climbe.dto.ServicoDto;
import com.climbe.api_climbe.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Catálogo global de serviços")
@SecurityRequirement(name = "bearerAuth")
public class ServicoController {

    private final ServicoService servicoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar serviços", description = "Lista todos os serviços cadastrados, em ordem alfabética")
    public List<ServicoDto> listar() {
        return servicoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar serviço por ID", description = "Retorna os detalhes de um serviço específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content(examples = @ExampleObject(value = """
                            {"title": "Serviço não encontrado", "status": 404}
                            """)))
    })
    public ServicoDto buscarPorId(@PathVariable Integer id) {
        return servicoService.buscarPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SERVICO_EDITAR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar serviço", description = "Cria um novo serviço no catálogo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar serviços"),
            @ApiResponse(responseCode = "409", description = "Já existe um serviço com este nome",
                    content = @Content(examples = @ExampleObject(value = """
                            {"title": "Já existe um serviço com este nome", "status": 409}
                            """)))
    })
    public ServicoDto criar(@Valid @RequestBody CriarServicoDto dto) {
        return servicoService.criar(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SERVICO_EDITAR')")
    @Operation(summary = "Atualizar serviço", description = "Atualiza o nome de um serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para editar serviços"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
            @ApiResponse(responseCode = "409", description = "Já existe um serviço com este nome")
    })
    public ServicoDto atualizar(@PathVariable Integer id, @Valid @RequestBody AtualizarServicoDto dto) {
        return servicoService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SERVICO_EDITAR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir serviço", description = "Remove um serviço do catálogo (apenas se não estiver em uso)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Serviço está vinculado a propostas ou contratos",
                    content = @Content(examples = @ExampleObject(value = """
                            {"title": "Não é possível excluir o serviço pois ele está vinculado a propostas ou contratos", "status": 400}
                            """))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir serviços"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public void excluir(@PathVariable Integer id) {
        servicoService.excluir(id);
    }
}
