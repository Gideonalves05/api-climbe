package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.CriarPropostaComercialDto;
import com.climbe.api_climbe.dto.DecisaoPropostaDto;
import com.climbe.api_climbe.dto.PropostaDto;
import com.climbe.api_climbe.model.Empresa;
import com.climbe.api_climbe.model.Proposta;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.model.enums.StatusProposta;
import com.climbe.api_climbe.repository.EmpresaRepository;
import com.climbe.api_climbe.repository.PropostaRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PropostaComercialService {

    private final PropostaRepository propostaRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public PropostaComercialService(
            PropostaRepository propostaRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.propostaRepository = propostaRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public PropostaDto criarProposta(CriarPropostaComercialDto dto, Authentication authentication) {
        Usuario criador = obterFuncionarioAutenticado(authentication);

        Empresa empresa = empresaRepository.findById(dto.idEmpresa())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        Proposta proposta = new Proposta();
        proposta.setEmpresa(empresa);
        proposta.setUsuarioResponsavel(criador);
        proposta.setDocumentoProposta(dto.documentoProposta().trim());
        proposta.setStatus(StatusProposta.PENDENTE_APROVACAO);
        proposta.setDataCriacao(LocalDate.now());

        return paraDto(propostaRepository.save(proposta));
    }

    @Transactional
    public PropostaDto decidirProposta(Integer idProposta, DecisaoPropostaDto dto, Authentication authentication) {
        obterFuncionarioAutenticado(authentication);

        Proposta proposta = propostaRepository.findById(idProposta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta não encontrada"));

        if (proposta.getStatus() == StatusProposta.CANCELADA || proposta.getStatus() == StatusProposta.RECUSADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proposta não permite nova decisão");
        }

        if (Boolean.TRUE.equals(dto.aprovada())) {
            if (dto.idFuncionarioResponsavelContrato() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ao aprovar a proposta, informe o funcionário responsável pela criação do contrato"
                );
            }

            Usuario responsavelContrato = usuarioRepository.findById(dto.idFuncionarioResponsavelContrato())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário responsável não encontrado"));

            if (responsavelContrato.getSituacao() != SituacaoUsuario.ATIVO) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Funcionário responsável está inativo");
            }

            proposta.setStatus(StatusProposta.ACEITA);
            proposta.setUsuarioResponsavel(responsavelContrato);
        } else {
            proposta.setStatus(StatusProposta.RECUSADA);
        }

        return paraDto(propostaRepository.save(proposta));
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
}
