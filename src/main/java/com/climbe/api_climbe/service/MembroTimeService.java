package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.MembroTime;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.PapelTime;
import com.climbe.api_climbe.repository.MembroTimeRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import com.climbe.api_climbe.repository.ContratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MembroTimeService {

    private final MembroTimeRepository membroTimeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContratoRepository contratoRepository;
    private final UsuarioLogadoService usuarioLogadoService;

    public MembroTime adicionarMembro(Integer contratoId, Integer usuarioId, PapelTime papel) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        // Validar que contrato existe
        Contrato contrato = contratoRepository.findById(contratoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Contrato não encontrado"));
        
        // Validar que usuário existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Usuário não encontrado"));
        
        // Validar que usuário não é membro ativo do time
        if (membroTimeRepository.existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(contratoId, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Usuário já é membro ativo do time deste contrato");
        }
        
        MembroTime membro = new MembroTime();
        membro.setContrato(contrato);
        membro.setUsuario(usuario);
        membro.setPapel(papel != null ? papel : PapelTime.MEMBRO);
        membro.setDataEntrada(java.time.LocalDate.now());
        membro.setAtivo(true);
        
        return membroTimeRepository.save(membro);
    }

    public void removerMembro(Integer contratoId, Integer membroId) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        MembroTime membro = membroTimeRepository.findById(membroId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Membro do time não encontrado"));
        
        if (!membro.getContrato().getIdContrato().equals(contratoId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Membro não pertence ao contrato especificado");
        }
        
        // Soft delete
        membro.setAtivo(false);
        membroTimeRepository.save(membro);
    }

    public List<MembroTime> listarMembros(Integer contratoId) {
        return membroTimeRepository.findByContrato_IdContratoAndAtivoTrue(contratoId);
    }

    public MembroTime atualizarPapel(Integer membroId, PapelTime novoPapel) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        MembroTime membro = membroTimeRepository.findById(membroId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Membro do time não encontrado"));
        
        membro.setPapel(novoPapel);
        return membroTimeRepository.save(membro);
    }
}
