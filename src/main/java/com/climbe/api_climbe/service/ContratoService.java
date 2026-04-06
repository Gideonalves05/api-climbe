package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.ContratoDto;
import com.climbe.api_climbe.dto.CriarContratoDto;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Proposta;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.model.enums.StatusContrato;
import com.climbe.api_climbe.model.enums.StatusProposta;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.PropostaRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final PropostaRepository propostaRepository;
    private final UsuarioRepository usuarioRepository;

    public ContratoService(
            ContratoRepository contratoRepository,
            PropostaRepository propostaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.contratoRepository = contratoRepository;
        this.propostaRepository = propostaRepository;
        this.usuarioRepository = usuarioRepository;
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
        contrato.setDataInicio(dto.dataInicio());
        contrato.setDataFim(dto.dataFim());
        contrato.setStatus(StatusContrato.VIGENTE);

        Contrato salvo = contratoRepository.save(contrato);
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
