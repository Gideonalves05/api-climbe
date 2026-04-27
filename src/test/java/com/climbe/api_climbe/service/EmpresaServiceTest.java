package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.AtualizarEmpresaDto;
import com.climbe.api_climbe.dto.ContratoResumoDto;
import com.climbe.api_climbe.dto.CriarEmpresaDto;
import com.climbe.api_climbe.dto.EmpresaDetalheDto;
import com.climbe.api_climbe.dto.EmpresaResumoDto;
import com.climbe.api_climbe.dto.PropostaResumoDto;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Empresa;
import com.climbe.api_climbe.model.Proposta;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.StatusContrato;
import com.climbe.api_climbe.model.enums.StatusProposta;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.EmpresaRepository;
import com.climbe.api_climbe.repository.PropostaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock private EmpresaRepository empresaRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private PropostaRepository propostaRepository;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks private EmpresaService service;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setIdEmpresa(1);
        empresa.setRazaoSocial("Climbe Parceira LTDA");
        empresa.setNomeFantasia("Climbe Parceira");
        empresa.setCnpj("12345678000190");
        empresa.setCidade("São Paulo");
        empresa.setUf("SP");
        empresa.setEmail("contato@climbe.com");
    }

    @Test
    void listar_retornaPaginaComContagens() {
        Page<Empresa> pagina = new PageImpl<>(List.of(empresa));
        when(empresaRepository.buscarPorTermo(anyString(), any())).thenReturn(pagina);
        when(contratoRepository.countByEmpresaId(1)).thenReturn(5L);
        when(contratoRepository.countByEmpresaIdAndStatus(1, StatusContrato.VIGENTE)).thenReturn(3L);

        Page<EmpresaResumoDto> resultado = service.listar("clim", PageRequest.of(0, 10));

        assertThat(resultado.getContent()).hasSize(1);
        EmpresaResumoDto r = resultado.getContent().get(0);
        assertThat(r.idEmpresa()).isEqualTo(1);
        assertThat(r.totalContratos()).isEqualTo(5);
        assertThat(r.contratosVigentes()).isEqualTo(3);
    }

    @Test
    void detalhar_populaContagens() {
        when(empresaRepository.findById(1)).thenReturn(Optional.of(empresa));
        when(contratoRepository.countByEmpresaIdAndStatus(1, StatusContrato.VIGENTE)).thenReturn(2L);
        when(contratoRepository.countByEmpresaIdAndStatus(1, StatusContrato.ENCERRADO)).thenReturn(7L);
        when(propostaRepository.countByEmpresa_IdEmpresa(1)).thenReturn(10L);

        EmpresaDetalheDto dto = service.detalhar(1);

        assertThat(dto.idEmpresa()).isEqualTo(1);
        assertThat(dto.totalContratosVigentes()).isEqualTo(2);
        assertThat(dto.totalContratosEncerrados()).isEqualTo(7);
        assertThat(dto.totalPropostas()).isEqualTo(10);
    }

    @Test
    void detalhar_empresaInexistente_404() {
        when(empresaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalhar(999))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void listarContratos_filtraPorStatusQuandoInformado() {
        Contrato c = contratoFake();
        when(empresaRepository.existsById(1)).thenReturn(true);
        when(contratoRepository.findByEmpresaIdAndStatus(1, StatusContrato.VIGENTE))
                .thenReturn(List.of(c));

        List<ContratoResumoDto> resultado = service.listarContratos(1, StatusContrato.VIGENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).status()).isEqualTo(StatusContrato.VIGENTE);
        assertThat(resultado.get(0).idEmpresa()).isEqualTo(1);
    }

    @Test
    void listarContratos_semStatus_retornaTodos() {
        Contrato c = contratoFake();
        when(empresaRepository.existsById(1)).thenReturn(true);
        when(contratoRepository.findByEmpresaId(1)).thenReturn(List.of(c));

        List<ContratoResumoDto> resultado = service.listarContratos(1, null);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void listarPropostas_mapeiaComFlagDocumento() {
        Usuario resp = new Usuario();
        resp.setIdUsuario(50);
        resp.setNomeCompleto("Responsável Teste");

        Proposta p = new Proposta();
        p.setIdProposta(77);
        p.setEmpresa(empresa);
        p.setUsuarioResponsavel(resp);
        p.setStatus(StatusProposta.PENDENTE_APROVACAO);
        p.setDataCriacao(LocalDate.of(2026, 1, 10));
        p.setDocumentoProposta("conteudo da proposta");

        when(empresaRepository.existsById(1)).thenReturn(true);
        when(propostaRepository.findByEmpresaId(1)).thenReturn(List.of(p));

        List<PropostaResumoDto> resultado = service.listarPropostas(1);

        assertThat(resultado).hasSize(1);
        PropostaResumoDto dto = resultado.get(0);
        assertThat(dto.idProposta()).isEqualTo(77);
        assertThat(dto.possuiDocumento()).isTrue();
        assertThat(dto.idResponsavel()).isEqualTo(50);
    }

    @Test
    void criar_persisteEmpresaNormalizaCnpjERegistraAuditoria() {
        CriarEmpresaDto dto = new CriarEmpresaDto(
                "Nova Empresa", "Nova Empresa LTDA", "12.345.678/0001-90",
                "nova@empresa.com", "(11) 99999-0000",
                "Maria", "maria@empresa.com", "(11) 98888-0000", "123.456.789-00",
                "Av. X", "100", "Centro", "SP", "SP", "01000-000"
        );
        when(empresaRepository.findByCnpj("12345678000190")).thenReturn(Optional.empty());
        when(empresaRepository.findByEmailIgnoreCase("nova@empresa.com")).thenReturn(Optional.empty());
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> {
            Empresa e = inv.getArgument(0);
            e.setIdEmpresa(42);
            return e;
        });
        when(empresaRepository.findById(42)).thenReturn(Optional.of(empresaFakeComId(42)));

        EmpresaDetalheDto resultado = service.criar(dto);

        assertThat(resultado.idEmpresa()).isEqualTo(42);
        verify(auditoriaService).registrarEvento(eq(TipoEventoAuditoria.EMPRESA_CRIADA), eq("EMPRESA"), eq(42), any());
    }

    @Test
    void criar_cnpjDuplicado_lanca409() {
        CriarEmpresaDto dto = new CriarEmpresaDto(
                "X", null, "12.345.678/0001-90", "x@y.com",
                null, null, null, null, null, null, null, null, null, null, null
        );
        Empresa existente = new Empresa();
        existente.setIdEmpresa(99);
        when(empresaRepository.findByCnpj("12345678000190")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void criar_semRazaoSocial_aplicaNomeFantasiaComoFallback() {
        CriarEmpresaDto dto = new CriarEmpresaDto(
                "Empresa Sem Razão", null, "12.345.678/0001-90",
                "fallback@empresa.com", null,
                null, null, null, null,
                null, null, null, null, null, null
        );

        when(empresaRepository.findByCnpj("12345678000190")).thenReturn(Optional.empty());
        when(empresaRepository.findByEmailIgnoreCase("fallback@empresa.com")).thenReturn(Optional.empty());
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> {
            Empresa e = inv.getArgument(0);
            e.setIdEmpresa(77);
            return e;
        });
        when(empresaRepository.findById(77)).thenReturn(Optional.of(empresaFakeComId(77)));

        service.criar(dto);

        verify(empresaRepository).save(argThat(e -> "Empresa Sem Razão".equals(e.getRazaoSocial())));
    }

    @Test
    void criar_emailDuplicado_lanca409() {
        CriarEmpresaDto dto = new CriarEmpresaDto(
                "X", null, "12.345.678/0001-90", "x@y.com",
                null, null, null, null, null, null, null, null, null, null, null
        );
        when(empresaRepository.findByCnpj("12345678000190")).thenReturn(Optional.empty());
        Empresa existente = new Empresa();
        existente.setIdEmpresa(99);
        when(empresaRepository.findByEmailIgnoreCase("x@y.com")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void atualizar_salvaEGeraAuditoria() {
        AtualizarEmpresaDto dto = new AtualizarEmpresaDto(
                "Atualizada", "Atualizada LTDA", "12345678000190",
                "atualizada@empresa.com", null, null, null, null, null,
                null, null, null, null, null, null
        );
        when(empresaRepository.findById(1)).thenReturn(Optional.of(empresa));
        when(empresaRepository.findByCnpj("12345678000190")).thenReturn(Optional.of(empresa));
        when(empresaRepository.findByEmailIgnoreCase("atualizada@empresa.com")).thenReturn(Optional.empty());
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpresaDetalheDto resultado = service.atualizar(1, dto);

        assertThat(resultado.idEmpresa()).isEqualTo(1);
        assertThat(empresa.getNomeFantasia()).isEqualTo("Atualizada");
        assertThat(empresa.getEmail()).isEqualTo("atualizada@empresa.com");
        verify(auditoriaService).registrarEvento(eq(TipoEventoAuditoria.EMPRESA_ATUALIZADA), eq("EMPRESA"), eq(1), any());
    }

    @Test
    void atualizar_empresaInexistente_404() {
        AtualizarEmpresaDto dto = new AtualizarEmpresaDto(
                "X", null, "12345678000190", "x@y.com",
                null, null, null, null, null, null, null, null, null, null, null
        );
        when(empresaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(999, dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void atualizar_cnpjPertenceAOutraEmpresa_409() {
        AtualizarEmpresaDto dto = new AtualizarEmpresaDto(
                "X", null, "99999999000199", "x@y.com",
                null, null, null, null, null, null, null, null, null, null, null
        );
        when(empresaRepository.findById(1)).thenReturn(Optional.of(empresa));
        Empresa outra = new Empresa();
        outra.setIdEmpresa(2);
        when(empresaRepository.findByCnpj("99999999000199")).thenReturn(Optional.of(outra));

        assertThatThrownBy(() -> service.atualizar(1, dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private Empresa empresaFakeComId(Integer id) {
        Empresa e = new Empresa();
        e.setIdEmpresa(id);
        e.setNomeFantasia("Nova Empresa");
        e.setCnpj("12345678000190");
        e.setEmail("nova@empresa.com");
        return e;
    }

    private Contrato contratoFake() {
        Usuario resp = new Usuario();
        resp.setIdUsuario(50);
        resp.setNomeCompleto("Responsável");

        Proposta p = new Proposta();
        p.setIdProposta(11);
        p.setEmpresa(empresa);
        p.setUsuarioResponsavel(resp);

        Contrato c = new Contrato();
        c.setIdContrato(200);
        c.setProposta(p);
        c.setStatus(StatusContrato.VIGENTE);
        c.setDataInicio(LocalDate.of(2026, 1, 1));
        return c;
    }
}
