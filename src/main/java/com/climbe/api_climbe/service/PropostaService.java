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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PropostaService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String ALLOWED_CONTENT_TYPE = MediaType.APPLICATION_PDF_VALUE;

    private final PropostaRepository propostaRepository;
    private final PropostaArquivoRepository propostaArquivoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final AuditoriaService auditoriaService;

    public PropostaService(
            PropostaRepository propostaRepository,
            PropostaArquivoRepository propostaArquivoRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            ServicoRepository servicoRepository,
            AuditoriaService auditoriaService
    ) {
        this.propostaRepository = propostaRepository;
        this.propostaArquivoRepository = propostaArquivoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public PropostaDto criarPropostaComArquivo(
            Integer idEmpresa,
            Integer idServico,
            BigDecimal valor,
            String observacoes,
            LocalDate dataValidade,
            MultipartFile arquivo,
            Authentication authentication
    ) {
        // Validações iniciais
        if (idServico == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço é obrigatório");
        }

        // Valida arquivo antes de qualquer operação no banco
        if (arquivo != null && !arquivo.isEmpty()) {
            validarArquivo(arquivo);
        }

        Usuario criador = obterFuncionarioAutenticado(authentication);
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        Servico servico = servicoRepository.findById(idServico)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

        // Cria proposta
        Proposta proposta = new Proposta();
        proposta.setEmpresa(empresa);
        proposta.setUsuarioResponsavel(criador);
        proposta.setServico(servico);
        proposta.setStatus(StatusProposta.RASCUNHO);
        proposta.setDataCriacao(LocalDate.now());
        proposta.setValor(valor);
        proposta.setObservacoes(observacoes);
        proposta.setDataValidade(dataValidade);

        Proposta propostaSalva = propostaRepository.save(proposta);

        // Processa arquivo se fornecido (já validado)
        if (arquivo != null && !arquivo.isEmpty()) {
            salvarArquivo(propostaSalva, arquivo);
        }

        // Auditoria
        auditoriaService.registrarEvento(
                TipoEventoAuditoria.PROPOSTA_CRIADA,
                "Proposta",
                propostaSalva.getIdProposta(),
                Map.of(
                        "empresa", empresa.getNomeFantasia(),
                        "servico", servico.getNome(),
                        "responsavel", criador.getEmail()
                )
        );

        return paraDto(propostaSalva);
    }

    @Transactional
    public void substituirArquivo(Integer idProposta, MultipartFile arquivo) {
        Proposta proposta = propostaRepository.findById(idProposta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta não encontrada"));

        validarArquivo(arquivo);

        // Remove arquivo antigo se existir
        propostaArquivoRepository.findByPropostaIdProposta(idProposta)
                .ifPresent(propostaArquivoRepository::delete);

        // Salva novo arquivo
        salvarArquivo(proposta, arquivo);
    }

    @Transactional(readOnly = true)
    public DocumentoResultado obterDocumento(Integer idProposta) {
        Proposta proposta = propostaRepository.findById(idProposta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta não encontrada"));

        // Tenta obter arquivo binário primeiro
        Optional<PropostaArquivo> arquivoOpt = propostaArquivoRepository.findByPropostaIdProposta(idProposta);
        if (arquivoOpt.isPresent()) {
            PropostaArquivo arquivo = arquivoOpt.get();
            return new DocumentoResultado(
                    arquivo.getContentType(),
                    arquivo.getNomeArquivo(),
                    arquivo.getConteudo(),
                    true
            );
        }

        // Fallback para documento textual legado
        String documentoTexto = proposta.getDocumentoProposta();
        if (documentoTexto != null && !documentoTexto.isBlank()) {
            return new DocumentoResultado(
                    MediaType.TEXT_PLAIN_VALUE,
                    "proposta-" + idProposta + ".txt",
                    documentoTexto.getBytes(),
                    false
            );
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta sem documento cadastrado");
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Arquivo excede o limite de 10 MB"
            );
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.equals(ALLOWED_CONTENT_TYPE)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Apenas arquivos PDF são permitidos"
            );
        }
    }

    private void salvarArquivo(Proposta proposta, MultipartFile arquivo) {
        try {
            PropostaArquivo propostaArquivo = new PropostaArquivo();
            propostaArquivo.setProposta(proposta);
            propostaArquivo.setNomeArquivo(arquivo.getOriginalFilename());
            propostaArquivo.setContentType(arquivo.getContentType());
            propostaArquivo.setTamanhoBytes(arquivo.getSize());
            propostaArquivo.setConteudo(arquivo.getBytes());

            propostaArquivoRepository.save(propostaArquivo);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falha ao processar arquivo", e);
        }
    }

    private Usuario obterFuncionarioAutenticado(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "";
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(Objects.toString(email, ""))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Somente funcionário da Climbe pode executar esta ação"
                ));

        if (usuario.getSituacao() != SituacaoUsuario.ATIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Funcionário inativo");
        }
        return usuario;
    }

    private PropostaDto paraDto(Proposta proposta) {
        return new PropostaDto(
                proposta.getIdProposta(),
                proposta.getEmpresa().getIdEmpresa(),
                proposta.getUsuarioResponsavel().getIdUsuario(),
                proposta.getStatus(),
                proposta.getDocumentoProposta(),
                proposta.getDataCriacao()
        );
    }

    public record DocumentoResultado(
            String contentType,
            String nomeArquivo,
            byte[] conteudo,
            boolean isBinario
    ) {}
}
