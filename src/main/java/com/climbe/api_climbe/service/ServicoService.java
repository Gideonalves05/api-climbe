package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.AtualizarServicoDto;
import com.climbe.api_climbe.dto.CriarServicoDto;
import com.climbe.api_climbe.dto.ServicoDto;
import com.climbe.api_climbe.model.Servico;
import com.climbe.api_climbe.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public List<ServicoDto> listar() {
        return servicoRepository.findAllByOrderByNomeAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicoDto buscarPorId(Integer id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
        return toDto(servico);
    }

    @Transactional
    public ServicoDto criar(CriarServicoDto dto) {
        String nomeNormalizado = normalizarNome(dto.nome());
        validarNomeUnico(nomeNormalizado, null);

        Servico servico = new Servico();
        servico.setNome(nomeNormalizado);

        Servico salvo = servicoRepository.save(servico);
        return toDto(salvo);
    }

    @Transactional
    public ServicoDto atualizar(Integer id, AtualizarServicoDto dto) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

        String nomeNormalizado = normalizarNome(dto.nome());
        validarNomeUnico(nomeNormalizado, id);

        servico.setNome(nomeNormalizado);
        Servico atualizado = servicoRepository.save(servico);
        return toDto(atualizado);
    }

    @Transactional
    public void excluir(Integer id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

        if (servicoRepository.existsVinculoEmPropostasOuContratos(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não é possível excluir o serviço pois ele está vinculado a propostas ou contratos");
        }

        servicoRepository.delete(servico);
    }

    private void validarNomeUnico(String nome, Integer idServicoAtual) {
        servicoRepository.findByNomeIgnoreCase(nome).ifPresent(existente -> {
            if (!Objects.equals(existente.getIdServico(), idServicoAtual)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Já existe um serviço com este nome");
            }
        });
    }

    private String normalizarNome(String nome) {
        if (nome == null) return null;
        // Trim e capitaliza primeira letra de cada palavra
        return nome.trim().replaceAll("\\s+", " ");
    }

    private ServicoDto toDto(Servico servico) {
        return new ServicoDto(servico.getIdServico(), servico.getNome());
    }
}
