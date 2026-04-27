package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.ContratoDto;
import com.climbe.api_climbe.dto.CriarContratoDto;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Empresa;
import com.climbe.api_climbe.model.Proposta;
import com.climbe.api_climbe.model.Servico;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.model.enums.StatusContrato;
import com.climbe.api_climbe.model.enums.StatusProposta;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.EmpresaRepository;
import com.climbe.api_climbe.repository.PropostaRepository;
import com.climbe.api_climbe.repository.ServicoRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final PropostaRepository propostaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final EmpresaRepository empresaRepository;
    private final ServicoRepository servicoRepository;
    private final ColunaKanbanService colunaKanbanService;

    public ContratoService(
            ContratoRepository contratoRepository,
            PropostaRepository propostaRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService,
            EmpresaRepository empresaRepository,
            ServicoRepository servicoRepository,
            ColunaKanbanService colunaKanbanService
    ) {
        this.contratoRepository = contratoRepository;
        this.propostaRepository = propostaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.empresaRepository = empresaRepository;
        this.servicoRepository = servicoRepository;
        this.colunaKanbanService = colunaKanbanService;
    }

    @Transactional
    public ContratoDto criarContratoComUpload(
            Integer idEmpresa,
            Integer idServico,
            LocalDate dataInicio,
            LocalDate dataFim,
            String observacoes,
            MultipartFile arquivo,
            Authentication authentication
    ) {
        obterFuncionarioAutenticado(authentication);
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        Contrato contrato = new Contrato();
        contrato.setEmpresa(empresa);
        contrato.setDataInicio(dataInicio);
        contrato.setDataFim(dataFim);
        contrato.setStatus(StatusContrato.VIGENTE);
        contrato.setObservacoes(observacoes);

        if (idServico != null) {
            Servico servico = servicoRepository.findById(idServico)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
            contrato.setServico(servico);
        }

        if (arquivo != null && !arquivo.isEmpty()) {
            try {
                contrato.setArquivoConteudo(arquivo.getBytes());
                contrato.setArquivoNome(arquivo.getOriginalFilename());
                contrato.setArquivoMime(arquivo.getContentType());
                contrato.setArquivoTamanho(arquivo.getSize());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falha ao ler arquivo", e);
            }
        }

        Contrato salvo = contratoRepository.save(contrato);

        // Seed das colunas padrão do Kanban para o novo contrato
        try {
            colunaKanbanService.seedColunasPadrao(salvo.getIdContrato());
        } catch (Exception ex) {
            // Não falha o cadastro por erro de seed
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("idContrato", salvo.getIdContrato());
        payload.put("idEmpresa", idEmpresa);
        payload.put("idServico", idServico);
        payload.put("status", StatusContrato.VIGENTE.name());
        auditoriaService.registrarEvento(
                TipoEventoAuditoria.CONTRATO_CRIADO,
                "CONTRATO",
                salvo.getIdContrato(),
                payload
        );

        return new ContratoDto(
                salvo.getIdContrato(),
                null,
                salvo.getDataInicio(),
                salvo.getDataFim(),
                salvo.getStatus()
        );
    }

    @Transactional
    public ContratoDto criarContrato(CriarContratoDto dto, Authentication authentication) {
        Usuario funcionario = obterFuncionarioAutenticado(authentication);

        Proposta proposta = propostaRepository.findById(dto.idProposta())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta não encontrada"));

        if (proposta.getStatus() != StatusProposta.ACEITA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Somente proposta aprovada pode gerar contrato");
        }

        if (!Objects.equals(proposta.getUsuarioResponsavel().getIdUsuario(), funcionario.getIdUsuario())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas o funcionário selecionado na aprovação da proposta pode criar o contrato"
            );
        }

        if (contratoRepository.existsByProposta_IdProposta(dto.idProposta())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe contrato para esta proposta");
        }

        Contrato contrato = new Contrato();
        contrato.setProposta(proposta);
        contrato.setEmpresa(proposta.getEmpresa());
        contrato.setServico(proposta.getServico());
        contrato.setDataInicio(dto.dataInicio());
        contrato.setDataFim(dto.dataFim());
        contrato.setStatus(StatusContrato.VIGENTE);

        Contrato salvo = contratoRepository.save(contrato);

        Map<String, Object> payload = new HashMap<>();
        payload.put("idProposta", proposta.getIdProposta());
        payload.put("dataInicio", dto.dataInicio());
        payload.put("dataFim", dto.dataFim());
        payload.put("status", StatusContrato.VIGENTE.name());

        auditoriaService.registrarEvento(
                TipoEventoAuditoria.CONTRATO_CRIADO,
                "CONTRATO",
                salvo.getIdContrato(),
                payload
        );

        return new ContratoDto(
                salvo.getIdContrato(),
                salvo.getProposta().getIdProposta(),
                salvo.getDataInicio(),
                salvo.getDataFim(),
                salvo.getStatus()
        );
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
}
