package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.PermissoesContratoDto;
import com.climbe.api_climbe.model.Cargo;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.CodigoPermissao;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.MembroTimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContratoAutorizacaoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;
    @Mock
    private MembroTimeRepository membroTimeRepository;
    @Mock
    private UsuarioLogadoService usuarioLogadoService;

    @InjectMocks
    private ContratoAutorizacaoService service;

    private Usuario membroDoTime;
    private Usuario outsider;
    private Usuario ceo;
    private Contrato contrato;

    @BeforeEach
    void setUp() {
        Cargo cargoAnalista = new Cargo();
        cargoAnalista.setIdCargo(1);
        cargoAnalista.setNomeCargo("Analista");

        Cargo cargoCeo = new Cargo();
        cargoCeo.setIdCargo(2);
        cargoCeo.setNomeCargo("CEO");

        membroDoTime = new Usuario();
        membroDoTime.setIdUsuario(10);
        membroDoTime.setCargo(cargoAnalista);

        outsider = new Usuario();
        outsider.setIdUsuario(20);
        outsider.setCargo(cargoAnalista);

        ceo = new Usuario();
        ceo.setIdUsuario(30);
        ceo.setCargo(cargoCeo);

        contrato = new Contrato();
        contrato.setIdContrato(100);
    }

    @Test
    void podeInteragir_retornaTrueParaMembroDoTime() {
        when(membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(100, 10))
                .thenReturn(true);

        assertThat(service.podeInteragir(100, membroDoTime)).isTrue();
    }

    @Test
    void podeInteragir_retornaTrueParaCeoMesmoSemEstarNoTime() {
        // Não stubba membroTimeRepository: CEO passa sem consultar o time.
        assertThat(service.podeInteragir(100, ceo)).isTrue();
    }

    @Test
    void podeInteragir_retornaFalseParaOutsiderNaoCeo() {
        when(membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(100, 20))
                .thenReturn(false);

        assertThat(service.podeInteragir(100, outsider)).isFalse();
    }

    @Test
    void podeInteragir_retornaFalseQuandoArgumentosNulos() {
        assertThat(service.podeInteragir(null, membroDoTime)).isFalse();
        assertThat(service.podeInteragir(100, null)).isFalse();
    }

    @Test
    void exigirInteracao_naoLancaQuandoPode() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(membroDoTime);
        when(membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(100, 10))
                .thenReturn(true);

        service.exigirInteracao(100);
    }

    @Test
    void exigirInteracao_lanca403QuandoOutsider() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(outsider);
        when(membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(100, 20))
                .thenReturn(false);

        assertThatThrownBy(() -> service.exigirInteracao(100))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void resolverFlags_membroDoTime_podeVisualizarEInteragir() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(membroDoTime);
        when(contratoRepository.findById(100)).thenReturn(Optional.of(contrato));
        when(membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(100, 10))
                .thenReturn(true);
        lenient().when(usuarioLogadoService.temPermissao(any(CodigoPermissao.class))).thenReturn(false);

        PermissoesContratoDto flags = service.resolverFlagsUsuarioLogado(100);

        assertThat(flags.idContrato()).isEqualTo(100);
        assertThat(flags.podeVisualizar()).isTrue();
        assertThat(flags.podeInteragir()).isTrue();
        assertThat(flags.membroDoTime()).isTrue();
        assertThat(flags.ehCeo()).isFalse();
    }

    @Test
    void resolverFlags_ceo_tudoLiberadoExcetoMembroDoTime() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(ceo);
        when(contratoRepository.findById(100)).thenReturn(Optional.of(contrato));
        when(membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(100, 30))
                .thenReturn(false);
        lenient().when(usuarioLogadoService.temPermissao(any(CodigoPermissao.class))).thenReturn(false);

        PermissoesContratoDto flags = service.resolverFlagsUsuarioLogado(100);

        assertThat(flags.ehCeo()).isTrue();
        assertThat(flags.membroDoTime()).isFalse();
        assertThat(flags.podeInteragir()).isTrue();
        assertThat(flags.podeVisualizar()).isTrue();
        assertThat(flags.podeGerenciarTime()).isTrue();
    }

    @Test
    void resolverFlags_outsiderSemPermissao_nadaLiberado() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(outsider);
        when(contratoRepository.findById(100)).thenReturn(Optional.of(contrato));
        when(membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(100, 20))
                .thenReturn(false);
        when(usuarioLogadoService.temPermissao(any(CodigoPermissao.class))).thenReturn(false);

        PermissoesContratoDto flags = service.resolverFlagsUsuarioLogado(100);

        assertThat(flags.podeInteragir()).isFalse();
        assertThat(flags.podeVisualizar()).isFalse();
        assertThat(flags.podeGerenciarTime()).isFalse();
        assertThat(flags.membroDoTime()).isFalse();
        assertThat(flags.ehCeo()).isFalse();
    }

    @Test
    void resolverFlags_outsiderComContratoVer_podeVisualizar() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(outsider);
        when(contratoRepository.findById(100)).thenReturn(Optional.of(contrato));
        when(membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(100, 20))
                .thenReturn(false);
        when(usuarioLogadoService.temPermissao(CodigoPermissao.CONTRATO_VER)).thenReturn(true);
        when(usuarioLogadoService.temPermissao(CodigoPermissao.TIME_CONTRATO_ADICIONAR)).thenReturn(false);

        PermissoesContratoDto flags = service.resolverFlagsUsuarioLogado(100);

        assertThat(flags.podeVisualizar()).isTrue();
        assertThat(flags.podeInteragir()).isFalse();
        assertThat(flags.podeGerenciarTime()).isFalse();
    }

    @Test
    void resolverFlags_contratoInexistente_404() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(membroDoTime);
        when(contratoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolverFlagsUsuarioLogado(999))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
