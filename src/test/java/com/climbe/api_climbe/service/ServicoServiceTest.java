package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.AtualizarServicoDto;
import com.climbe.api_climbe.dto.CriarServicoDto;
import com.climbe.api_climbe.dto.ServicoDto;
import com.climbe.api_climbe.model.Servico;
import com.climbe.api_climbe.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService service;

    private Servico servico;

    @BeforeEach
    void setUp() {
        servico = new Servico();
        servico.setIdServico(1);
        servico.setNome("Consultoria Financeira");
    }

    @Test
    void listar_retornaTodosOrdenados() {
        Servico s2 = new Servico();
        s2.setIdServico(2);
        s2.setNome("Auditoria");

        when(servicoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(s2, servico));

        List<ServicoDto> resultado = service.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).nome()).isEqualTo("Auditoria");
        assertThat(resultado.get(1).nome()).isEqualTo("Consultoria Financeira");
    }

    @Test
    void buscarPorId_encontraServico() {
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));

        ServicoDto resultado = service.buscarPorId(1);

        assertThat(resultado.idServico()).isEqualTo(1);
        assertThat(resultado.nome()).isEqualTo("Consultoria Financeira");
    }

    @Test
    void buscarPorId_naoEncontrado_404() {
        when(servicoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    void criar_comSucesso() {
        CriarServicoDto dto = new CriarServicoDto("Novo Serviço");

        when(servicoRepository.findByNomeIgnoreCase("Novo Serviço")).thenReturn(Optional.empty());
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> {
            Servico s = inv.getArgument(0);
            s.setIdServico(2);
            return s;
        });

        ServicoDto resultado = service.criar(dto);

        assertThat(resultado.idServico()).isEqualTo(2);
        assertThat(resultado.nome()).isEqualTo("Novo Serviço");
    }

    @Test
    void criar_nomeDuplicado_409() {
        CriarServicoDto dto = new CriarServicoDto("Consultoria Financeira");

        when(servicoRepository.findByNomeIgnoreCase("Consultoria Financeira")).thenReturn(Optional.of(servico));

        assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    void atualizar_comSucesso() {
        AtualizarServicoDto dto = new AtualizarServicoDto("Nome Atualizado");

        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(servicoRepository.findByNomeIgnoreCase("Nome Atualizado")).thenReturn(Optional.empty());
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        ServicoDto resultado = service.atualizar(1, dto);

        assertThat(resultado.nome()).isEqualTo("Nome Atualizado");
    }

    @Test
    void atualizar_naoEncontrado_404() {
        AtualizarServicoDto dto = new AtualizarServicoDto("Nome");

        when(servicoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99, dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    void atualizar_nomeDuplicado_409() {
        AtualizarServicoDto dto = new AtualizarServicoDto("Outro Nome");

        Servico outro = new Servico();
        outro.setIdServico(2);
        outro.setNome("Outro Nome");

        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(servicoRepository.findByNomeIgnoreCase("Outro Nome")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> service.atualizar(1, dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    void excluir_comSucesso() {
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(servicoRepository.existsVinculoEmPropostasOuContratos(1)).thenReturn(false);

        service.excluir(1);

        verify(servicoRepository).delete(servico);
    }

    @Test
    void excluir_naoEncontrado_404() {
        when(servicoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    void excluir_vinculadoEmPropostasOuContratos_400() {
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(servicoRepository.existsVinculoEmPropostasOuContratos(1)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }
}
