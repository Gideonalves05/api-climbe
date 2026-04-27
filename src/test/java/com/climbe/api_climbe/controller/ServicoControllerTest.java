package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.ServicoDto;
import com.climbe.api_climbe.service.ServicoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    @Mock
    private ServicoService servicoService;

    @InjectMocks
    private ServicoController controller;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void get_listar_retorna200() throws Exception {
        when(servicoService.listar()).thenReturn(List.of(
                new ServicoDto(1, "Consultoria"),
                new ServicoDto(2, "Auditoria")
        ));

        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Consultoria"));
    }

    @Test
    void get_buscarPorId_encontrado_retorna200() throws Exception {
        when(servicoService.buscarPorId(1)).thenReturn(new ServicoDto(1, "Consultoria"));

        mockMvc.perform(get("/api/servicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idServico").value(1))
                .andExpect(jsonPath("$.nome").value("Consultoria"));
    }

    @Test
    void get_buscarPorId_naoEncontrado_retorna404() throws Exception {
        when(servicoService.buscarPorId(99))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Servico nao encontrado"));

        mockMvc.perform(get("/api/servicos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_payloadValido_retorna201() throws Exception {
        when(servicoService.criar(any())).thenReturn(new ServicoDto(1, "Novo Servico"));

        String body = mapper.writeValueAsString(Map.of("nome", "Novo Servico"));

        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idServico").value(1))
                .andExpect(jsonPath("$.nome").value("Novo Servico"));
    }

    @Test
    void post_payloadInvalido_nomeVazio_retorna400() throws Exception {
        String body = mapper.writeValueAsString(Map.of("nome", ""));

        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_nomeDuplicado_retorna409() throws Exception {
        when(servicoService.criar(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um servico com este nome"));

        String body = mapper.writeValueAsString(Map.of("nome", "Consultoria"));

        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void put_payloadValido_retorna200() throws Exception {
        when(servicoService.atualizar(eq(1), any())).thenReturn(new ServicoDto(1, "Nome Atualizado"));

        String body = mapper.writeValueAsString(Map.of("nome", "Nome Atualizado"));

        mockMvc.perform(put("/api/servicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));
    }

    @Test
    void put_naoEncontrado_retorna404() throws Exception {
        when(servicoService.atualizar(eq(99), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Servico nao encontrado"));

        String body = mapper.writeValueAsString(Map.of("nome", "Nome"));

        mockMvc.perform(put("/api/servicos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_nomeDuplicado_retorna409() throws Exception {
        when(servicoService.atualizar(eq(1), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um servico com este nome"));

        String body = mapper.writeValueAsString(Map.of("nome", "Outro"));

        mockMvc.perform(put("/api/servicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_excluirComSucesso_retorna204() throws Exception {
        mockMvc.perform(delete("/api/servicos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Servico nao encontrado"))
                .when(servicoService).excluir(99);

        mockMvc.perform(delete("/api/servicos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_vinculadoEmPropostasOuContratos_retorna400() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Nao e possivel excluir o servico pois ele esta vinculado a propostas ou contratos"))
                .when(servicoService).excluir(1);

        mockMvc.perform(delete("/api/servicos/1"))
                .andExpect(status().isBadRequest());
    }
}
