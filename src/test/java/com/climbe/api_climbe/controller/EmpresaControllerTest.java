package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.EmpresaDetalheDto;
import com.climbe.api_climbe.service.EmpresaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes standalone do {@link EmpresaController} focados em:
 * - POST /api/empresas retorna 201 com payload válido
 * - PUT /api/empresas/{id} retorna 200
 * - Validação Bean Validation (400) quando payload inválido
 * - Propagação de 409 do service (CNPJ duplicado)
 *
 * Não valida {@code @PreAuthorize} (seria necessário {@link org.springframework.test.context.junit.jupiter.SpringExtension}
 * com security). A cobertura de 403 ocorre em nível de filtro em runtime.
 */
@ExtendWith(MockitoExtension.class)
class EmpresaControllerTest {

    @Mock private EmpresaService empresaService;

    @InjectMocks private EmpresaController controller;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void post_payloadValido_retorna201() throws Exception {
        EmpresaDetalheDto detalhe = new EmpresaDetalheDto(
                10, "Razão", "Fantasia", "12345678000190", null, null, null, null, null, null, null,
                "contato@x.com", null, null, null, null, 0L, 0L, 0L);
        when(empresaService.criar(any())).thenReturn(detalhe);

        String body = mapper.writeValueAsString(Map.of(
                "nomeFantasia", "Fantasia",
                "cnpj", "12.345.678/0001-90",
                "email", "contato@x.com"
        ));

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEmpresa").value(10));
    }

    @Test
    void post_payloadInvalido_retorna400() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "nomeFantasia", "",
                "cnpj", "",
                "email", "invalido"
        ));

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_cnpjDuplicado_retorna409() throws Exception {
        when(empresaService.criar(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado"));

        String body = mapper.writeValueAsString(Map.of(
                "nomeFantasia", "Fantasia",
                "cnpj", "12.345.678/0001-90",
                "email", "contato@x.com"
        ));

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void put_payloadValido_retorna200() throws Exception {
        EmpresaDetalheDto detalhe = new EmpresaDetalheDto(
                5, null, "Fantasia Nova", "12345678000190", null, null, null, null, null, null, null,
                "novo@x.com", null, null, null, null, 0L, 0L, 0L);
        when(empresaService.atualizar(eq(5), any())).thenReturn(detalhe);

        String body = mapper.writeValueAsString(Map.of(
                "nomeFantasia", "Fantasia Nova",
                "cnpj", "12.345.678/0001-90",
                "email", "novo@x.com"
        ));

        mockMvc.perform(put("/api/empresas/5").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeFantasia").value("Fantasia Nova"));
    }
}
