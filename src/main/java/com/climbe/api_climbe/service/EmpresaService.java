package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.AtualizarEmpresaDto;
import com.climbe.api_climbe.dto.ContratoResumoDto;
import com.climbe.api_climbe.dto.CriarEmpresaDto;
import com.climbe.api_climbe.dto.EmpresaDetalheDto;
import com.climbe.api_climbe.dto.EmpresaResumoDto;
import com.climbe.api_climbe.dto.PropostaResumoDto;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Empresa;
import com.climbe.api_climbe.model.Proposta;
import com.climbe.api_climbe.model.enums.StatusContrato;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.EmpresaRepository;
import com.climbe.api_climbe.repository.PropostaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ContratoRepository contratoRepository;
    private final PropostaRepository propostaRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<EmpresaResumoDto> listar(String termo, Pageable pageable) {
        return empresaRepository.buscarPorTermo(termo, pageable)
                .map(this::toResumo);
    }

    @Transactional(readOnly = true)
    public EmpresaDetalheDto detalhar(Integer idEmpresa) {
        Empresa e = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        long vigentes = contratoRepository.countByEmpresaIdAndStatus(idEmpresa, StatusContrato.VIGENTE);
        long encerrados = contratoRepository.countByEmpresaIdAndStatus(idEmpresa, StatusContrato.ENCERRADO);
        long propostas = propostaRepository.countByEmpresa_IdEmpresa(idEmpresa);

        return new EmpresaDetalheDto(
                e.getIdEmpresa(),
                e.getRazaoSocial(),
                e.getNomeFantasia(),
                e.getCnpj(),
                e.getLogradouro(),
                e.getNumero(),
                e.getBairro(),
                e.getCidade(),
                e.getUf(),
                e.getCep(),
                e.getTelefone(),
                e.getEmail(),
                e.getRepresentanteNome(),
                e.getRepresentanteCpf(),
                e.getRepresentanteContato(),
                e.getRepresentanteEmail(),
                vigentes,
                encerrados,
                propostas
        );
    }

    @Transactional(readOnly = true)
    public List<ContratoResumoDto> listarContratos(Integer idEmpresa, StatusContrato status) {
        garantirEmpresaExiste(idEmpresa);
        List<Contrato> contratos = (status != null)
                ? contratoRepository.findByEmpresaIdAndStatus(idEmpresa, status)
                : contratoRepository.findByEmpresaId(idEmpresa);
        return contratos.stream().map(this::toContratoResumo).toList();
    }

    @Transactional
    public EmpresaDetalheDto criar(CriarEmpresaDto dto) {
        String cnpjNormalizado = normalizarCnpj(dto.cnpj());
        validarCnpjUnico(cnpjNormalizado, null);
        validarEmailUnico(dto.email(), null);

        Empresa empresa = new Empresa();
        aplicarCriar(empresa, dto, cnpjNormalizado);
        Empresa salva = empresaRepository.save(empresa);

        Map<String, Object> payload = new HashMap<>();
        payload.put("idEmpresa", salva.getIdEmpresa());
        payload.put("cnpj", salva.getCnpj());
        payload.put("nomeFantasia", salva.getNomeFantasia());
        auditoriaService.registrarEvento(TipoEventoAuditoria.EMPRESA_CRIADA, "EMPRESA", salva.getIdEmpresa(), payload);

        return detalhar(salva.getIdEmpresa());
    }

    @Transactional
    public EmpresaDetalheDto atualizar(Integer idEmpresa, AtualizarEmpresaDto dto) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        String cnpjNormalizado = normalizarCnpj(dto.cnpj());
        validarCnpjUnico(cnpjNormalizado, idEmpresa);
        validarEmailUnico(dto.email(), idEmpresa);

        aplicarAtualizar(empresa, dto, cnpjNormalizado);
        Empresa atualizada = empresaRepository.save(empresa);

        Map<String, Object> payload = new HashMap<>();
        payload.put("idEmpresa", atualizada.getIdEmpresa());
        payload.put("cnpj", atualizada.getCnpj());
        payload.put("nomeFantasia", atualizada.getNomeFantasia());
        auditoriaService.registrarEvento(TipoEventoAuditoria.EMPRESA_ATUALIZADA, "EMPRESA", atualizada.getIdEmpresa(), payload);

        return detalhar(atualizada.getIdEmpresa());
    }

    private void aplicarCriar(Empresa e, CriarEmpresaDto dto, String cnpjNormalizado) {
        e.setNomeFantasia(dto.nomeFantasia());
        e.setRazaoSocial(resolverRazaoSocial(dto.razaoSocial(), dto.nomeFantasia()));
        e.setCnpj(cnpjNormalizado);
        e.setEmail(dto.email());
        e.setTelefone(dto.telefone());
        e.setRepresentanteNome(dto.representanteNome());
        e.setRepresentanteEmail(dto.representanteEmail());
        e.setRepresentanteContato(dto.representanteContato());
        e.setRepresentanteCpf(dto.representanteCpf());
        e.setLogradouro(dto.logradouro());
        e.setNumero(dto.numero());
        e.setBairro(dto.bairro());
        e.setCidade(dto.cidade());
        e.setUf(dto.uf());
        e.setCep(dto.cep());
    }

    private void aplicarAtualizar(Empresa e, AtualizarEmpresaDto dto, String cnpjNormalizado) {
        e.setNomeFantasia(dto.nomeFantasia());
        e.setRazaoSocial(resolverRazaoSocial(dto.razaoSocial(), dto.nomeFantasia()));
        e.setCnpj(cnpjNormalizado);
        e.setEmail(dto.email());
        e.setTelefone(dto.telefone());
        e.setRepresentanteNome(dto.representanteNome());
        e.setRepresentanteEmail(dto.representanteEmail());
        e.setRepresentanteContato(dto.representanteContato());
        e.setRepresentanteCpf(dto.representanteCpf());
        e.setLogradouro(dto.logradouro());
        e.setNumero(dto.numero());
        e.setBairro(dto.bairro());
        e.setCidade(dto.cidade());
        e.setUf(dto.uf());
        e.setCep(dto.cep());
    }

    private void validarCnpjUnico(String cnpjNormalizado, Integer idEmpresaAtual) {
        empresaRepository.findByCnpj(cnpjNormalizado).ifPresent(existente -> {
            if (!Objects.equals(existente.getIdEmpresa(), idEmpresaAtual)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado");
            }
        });
    }

    private void validarEmailUnico(String email, Integer idEmpresaAtual) {
        if (email == null) return;
        empresaRepository.findByEmailIgnoreCase(email).ifPresent(existente -> {
            if (!Objects.equals(existente.getIdEmpresa(), idEmpresaAtual)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            }
        });
    }

    private String normalizarCnpj(String cnpj) {
        return Objects.toString(cnpj, "").replaceAll("\\D", "");
    }

    private String resolverRazaoSocial(String razaoSocial, String nomeFantasia) {
        if (razaoSocial != null && !razaoSocial.isBlank()) {
            return razaoSocial;
        }
        return nomeFantasia;
    }

    @Transactional(readOnly = true)
    public List<PropostaResumoDto> listarPropostas(Integer idEmpresa) {
        garantirEmpresaExiste(idEmpresa);
        return propostaRepository.findByEmpresaId(idEmpresa).stream()
                .map(this::toPropostaResumo)
                .toList();
    }

    private void garantirEmpresaExiste(Integer idEmpresa) {
        if (!empresaRepository.existsById(idEmpresa)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada");
        }
    }

    private EmpresaResumoDto toResumo(Empresa e) {
        long total = contratoRepository.countByEmpresaId(e.getIdEmpresa());
        long vigentes = contratoRepository.countByEmpresaIdAndStatus(e.getIdEmpresa(), StatusContrato.VIGENTE);
        return new EmpresaResumoDto(
                e.getIdEmpresa(),
                e.getRazaoSocial(),
                e.getNomeFantasia(),
                e.getCnpj(),
                e.getCidade(),
                e.getUf(),
                e.getEmail(),
                total,
                vigentes
        );
    }

    private ContratoResumoDto toContratoResumo(Contrato c) {
        Proposta p = c.getProposta();
        Empresa empresa = c.getEmpresa() != null
                ? c.getEmpresa()
                : (p != null ? p.getEmpresa() : null);
        Integer idEmpresaContrato = empresa != null ? empresa.getIdEmpresa() : null;
        String nomeEmpresa = empresa != null ? empresa.getNomeFantasia() : null;
        Integer idResponsavel = (p != null && p.getUsuarioResponsavel() != null)
                ? p.getUsuarioResponsavel().getIdUsuario()
                : null;
        String nomeResponsavel = (p != null && p.getUsuarioResponsavel() != null)
                ? p.getUsuarioResponsavel().getNomeCompleto()
                : null;
        return new ContratoResumoDto(
                c.getIdContrato(),
                p != null ? p.getIdProposta() : null,
                idEmpresaContrato,
                nomeEmpresa,
                c.getDataInicio(),
                c.getDataFim(),
                c.getStatus(),
                idResponsavel,
                nomeResponsavel,
                c.getServico() != null ? c.getServico().getIdServico() : null,
                c.getServico() != null ? c.getServico().getNome() : null,
                c.getArquivoTamanho() != null && c.getArquivoTamanho() > 0,
                c.getArquivoNome()
        );
    }

    private PropostaResumoDto toPropostaResumo(Proposta p) {
        return new PropostaResumoDto(
                p.getIdProposta(),
                p.getEmpresa().getIdEmpresa(),
                p.getEmpresa().getNomeFantasia(),
                p.getUsuarioResponsavel() != null ? p.getUsuarioResponsavel().getIdUsuario() : null,
                p.getUsuarioResponsavel() != null ? p.getUsuarioResponsavel().getNomeCompleto() : null,
                p.getStatus(),
                p.getDataCriacao(),
                p.getDocumentoProposta() != null && !p.getDocumentoProposta().isBlank(),
                p.getServico() != null ? p.getServico().getIdServico() : null,
                p.getServico() != null ? p.getServico().getNome() : null,
                p.getValor(),
                p.getDataValidade(),
                p.getArquivoTamanho() != null && p.getArquivoTamanho() > 0
        );
    }
}
