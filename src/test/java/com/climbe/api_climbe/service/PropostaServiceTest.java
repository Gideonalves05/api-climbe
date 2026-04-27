package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.PropostaDto;
import com.climbe.api_climbe.model.Cargo;
import com.climbe.api_climbe.model.Empresa;
import com.climbe.api_climbe.model.Proposta;
import com.climbe.api_climbe.model.PropostaArquivo;
import com.climbe.api_climbe.model.Servico;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.model.enums.StatusProposta;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.EmpresaRepository;
import com.climbe.api_climbe.repository.PropostaArquivoRepository;
import com.climbe.api_climbe.repository.PropostaRepository;
import com.climbe.api_climbe.repository.ServicoRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PropostaService - Upload de PDF e gestão de propostas")
class PropostaServiceTest {

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private PropostaArquivoRepository propostaArquivoRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PropostaService propostaService;

    private Usuario usuarioAtivo;
    private Empresa empresa;
    private Servico servico;
    private Proposta propostaSalva;

    @BeforeEach
    void setUp() {
        Cargo cargoAnalista = new Cargo();
        cargoAnalista.setIdCargo(1);
        cargoAnalista.setNomeCargo("Analista");

        usuarioAtivo = new Usuario();
        usuarioAtivo.setIdUsuario(1);
        usuarioAtivo.setEmail("teste@climbe.com.br");
        usuarioAtivo.setCargo(cargoAnalista);
        usuarioAtivo.setSituacao(SituacaoUsuario.ATIVO);

        empresa = new Empresa();
        empresa.setIdEmpresa(10);
        empresa.setNomeFantasia("Empresa Teste");

        servico = new Servico();
        servico.setIdServico(5);
        servico.setNome("Assessoria Financeira");

        propostaSalva = new Proposta();
        propostaSalva.setIdProposta(100);
        propostaSalva.setEmpresa(empresa);
        propostaSalva.setUsuarioResponsavel(usuarioAtivo);
        propostaSalva.setServico(servico);
        propostaSalva.setStatus(StatusProposta.RASCUNHO);
        propostaSalva.setDataCriacao(LocalDate.now());
    }

    @Test
    @DisplayName("Deve criar proposta com arquivo PDF válido - happy path")
    void testeCriarPropostaComArquivoPDF() throws IOException {
        // Given
        byte[] pdfBytes = "%PDF-1.4 test content".getBytes();
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "proposta.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfBytes
        );

        when(authentication.getName()).thenReturn("teste@climbe.com.br");
        when(usuarioRepository.findByEmailIgnoreCase("teste@climbe.com.br"))
                .thenReturn(Optional.of(usuarioAtivo));
        when(empresaRepository.findById(10)).thenReturn(Optional.of(empresa));
        when(servicoRepository.findById(5)).thenReturn(Optional.of(servico));
        when(propostaRepository.save(any(Proposta.class))).thenReturn(propostaSalva);
        when(propostaArquivoRepository.save(any(PropostaArquivo.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        PropostaDto resultado = propostaService.criarPropostaComArquivo(
                10, 5, new BigDecimal("5000.00"), "Observações", LocalDate.now().plusDays(30),
                arquivo, authentication
        );

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.idProposta()).isEqualTo(100);
        assertThat(resultado.idEmpresa()).isEqualTo(10);

        // Verifica que a proposta foi salva
        ArgumentCaptor<Proposta> propostaCaptor = ArgumentCaptor.forClass(Proposta.class);
        verify(propostaRepository).save(propostaCaptor.capture());
        Proposta propostaCapturada = propostaCaptor.getValue();
        assertThat(propostaCapturada.getServico()).isEqualTo(servico);
        assertThat(propostaCapturada.getStatus()).isEqualTo(StatusProposta.RASCUNHO);

        // Verifica que o arquivo foi salvo separadamente
        ArgumentCaptor<PropostaArquivo> arquivoCaptor = ArgumentCaptor.forClass(PropostaArquivo.class);
        verify(propostaArquivoRepository).save(arquivoCaptor.capture());
        PropostaArquivo arquivoCapturado = arquivoCaptor.getValue();
        assertThat(arquivoCapturado.getNomeArquivo()).isEqualTo("proposta.pdf");
        assertThat(arquivoCapturado.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(arquivoCapturado.getConteudo()).isEqualTo(pdfBytes);
        assertThat(arquivoCapturado.getTamanhoBytes()).isEqualTo((long) pdfBytes.length);

        // Verifica auditoria
        verify(auditoriaService).registrarEvento(eq(TipoEventoAuditoria.PROPOSTA_CRIADA), any(), anyInt(), any());
    }

    @Test
    @DisplayName("Deve rejeitar content-type que não seja PDF")
    void testeRejeitaContentTypeNaoPDF() {
        // Given
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "proposta.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "content".getBytes()
        );

        // When / Then - validação de arquivo acontece antes de acessar repos
        assertThatThrownBy(() -> propostaService.criarPropostaComArquivo(
                10, 5, null, null, null, arquivo, authentication
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).containsIgnoringCase("pdf");
                });

        verify(propostaRepository, never()).save(any());
        verify(propostaArquivoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar arquivo maior que 10 MB")
    void testeRejeitaArquivoAcimaDe10MB() {
        // Given - criar array de ~11 MB
        byte[] conteudoGrande = new byte[11 * 1024 * 1024 + 1];
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "proposta_grande.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                conteudoGrande
        );

        // When / Then - validação de arquivo acontece antes de acessar repos
        assertThatThrownBy(() -> propostaService.criarPropostaComArquivo(
                10, 5, null, null, null, arquivo, authentication
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
                    assertThat(rse.getReason()).contains("10 MB");
                });

        verify(propostaRepository, never()).save(any());
        verify(propostaArquivoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve exigir servicoId obrigatório")
    void testeExigeServicoId() {
        // Given - arquivo PDF válido mas sem servicoId
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "proposta.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4".getBytes()
        );

        // When / Then - validação de servicoId acontece antes de acessar repos
        assertThatThrownBy(() -> propostaService.criarPropostaComArquivo(
                10, null, null, null, null, arquivo, authentication
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).containsIgnoringCase("serviço");
                });

        verify(propostaRepository, never()).save(any());
        verify(servicoRepository, never()).findById(anyInt());
        verify(empresaRepository, never()).findById(anyInt());
        verify(usuarioRepository, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Deve substituir arquivo existente ao atualizar proposta")
    void testeAtualizaArquivoSubstituindo() throws IOException {
        // Given - proposta existe com arquivo
        PropostaArquivo arquivoAntigo = new PropostaArquivo();
        arquivoAntigo.setId(50);
        arquivoAntigo.setProposta(propostaSalva);
        arquivoAntigo.setNomeArquivo("antigo.pdf");

        when(propostaRepository.findById(100)).thenReturn(Optional.of(propostaSalva));
        when(propostaArquivoRepository.findByPropostaIdProposta(100))
                .thenReturn(Optional.of(arquivoAntigo));

        byte[] novoPdf = "%PDF-1.4 novo".getBytes();
        MockMultipartFile arquivoNovo = new MockMultipartFile(
                "arquivo",
                "novo.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                novoPdf
        );

        when(propostaArquivoRepository.save(any(PropostaArquivo.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        propostaService.substituirArquivo(100, arquivoNovo);

        // Then
        verify(propostaArquivoRepository).delete(arquivoAntigo);

        ArgumentCaptor<PropostaArquivo> arquivoCaptor = ArgumentCaptor.forClass(PropostaArquivo.class);
        verify(propostaArquivoRepository).save(arquivoCaptor.capture());
        PropostaArquivo novoArquivo = arquivoCaptor.getValue();
        assertThat(novoArquivo.getNomeArquivo()).isEqualTo("novo.pdf");
        assertThat(novoArquivo.getConteudo()).isEqualTo(novoPdf);
    }

    @Test
    @DisplayName("Deve retornar arquivo binário PDF no download")
    void testeDownloadBinarioRetornaPDF() {
        // Given
        byte[] pdfBytes = "%PDF-1.4 test".getBytes();
        PropostaArquivo arquivo = new PropostaArquivo();
        arquivo.setId(50);
        arquivo.setProposta(propostaSalva);
        arquivo.setNomeArquivo("proposta.pdf");
        arquivo.setContentType(MediaType.APPLICATION_PDF_VALUE);
        arquivo.setConteudo(pdfBytes);
        arquivo.setTamanhoBytes((long) pdfBytes.length);

        when(propostaRepository.findById(100)).thenReturn(Optional.of(propostaSalva));
        when(propostaArquivoRepository.findByPropostaIdProposta(100)).thenReturn(Optional.of(arquivo));

        // When
        PropostaService.DocumentoResultado resultado = propostaService.obterDocumento(100);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.contentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(resultado.nomeArquivo()).isEqualTo("proposta.pdf");
        assertThat(resultado.conteudo()).isEqualTo(pdfBytes);
        assertThat(resultado.isBinario()).isTrue();
    }

    @Test
    @DisplayName("Deve fazer fallback para texto legado quando não há arquivo binário")
    void testeDownloadFallbackTextoLegado() {
        // Given - proposta sem arquivo binário mas com documentoProposta textual
        propostaSalva.setDocumentoProposta("Conteúdo textual da proposta antiga");

        when(propostaRepository.findById(100)).thenReturn(Optional.of(propostaSalva));
        when(propostaArquivoRepository.findByPropostaIdProposta(100)).thenReturn(Optional.empty());

        // When
        PropostaService.DocumentoResultado resultado = propostaService.obterDocumento(100);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.contentType()).isEqualTo(MediaType.TEXT_PLAIN_VALUE);
        assertThat(resultado.nomeArquivo()).isEqualTo("proposta-100.txt");
        assertThat(new String(resultado.conteudo())).isEqualTo("Conteúdo textual da proposta antiga");
        assertThat(resultado.isBinario()).isFalse();
    }

    @Test
    @DisplayName("Deve lançar 404 quando proposta não existe no download")
    void testeDownloadPropostaNaoEncontrada() {
        when(propostaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propostaService.obterDocumento(999))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Deve lançar 404 quando proposta não tem documento nem texto legado")
    void testeDownloadSemDocumento() {
        // Given - proposta sem arquivo e sem texto
        when(propostaRepository.findById(100)).thenReturn(Optional.of(propostaSalva));
        when(propostaArquivoRepository.findByPropostaIdProposta(100)).thenReturn(Optional.empty());
        // propostaSalva.getDocumentoProposta() retorna null

        assertThatThrownBy(() -> propostaService.obterDocumento(100))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(rse.getReason()).containsIgnoringCase("documento");
                });
    }
}
