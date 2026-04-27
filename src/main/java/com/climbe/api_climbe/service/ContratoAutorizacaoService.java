package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.PermissoesContratoDto;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.MembroTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centraliza a regra de autorização sobre contratos/Kanban.
 *
 * Regra (conforme task 10.5):
 *   Pode interagir no contrato ⇔
 *     usuario ∈ time(contrato) ATIVO
 *     OU cargo do usuário == "CEO"
 *     OU usuário tem permissão CONTRATO_INTERAGIR_QUALQUER (reservado/opcional — não no enum atual).
 */
@Service
@RequiredArgsConstructor
public class ContratoAutorizacaoService {

    private static final String CARGO_CEO = "CEO";

    private final ContratoRepository contratoRepository;
    private final MembroTimeRepository membroTimeRepository;
    private final UsuarioLogadoService usuarioLogadoService;

    /**
     * @return true se o {@code usuario} pode interagir (mover cartão, criar/editar/excluir
     *         tarefa, gerenciar colunas) no contrato informado. Não faz efeito colateral.
     */
    @Transactional(readOnly = true)
    public boolean podeInteragir(Integer contratoId, Usuario usuario) {
        if (contratoId == null || usuario == null) {
            return false;
        }
        if (ehCeo(usuario)) {
            return true;
        }
        if (usuarioLogadoService.temPermissao(com.climbe.api_climbe.model.enums.CodigoPermissao.CONTRATO_INTERAGIR_QUALQUER)) {
            return true;
        }
        return membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(contratoId, usuario.getIdUsuario());
    }

    /**
     * Variante para uso nos controllers: resolve o usuário logado do contexto e lança 403 caso
     * não possa interagir. Usa `UsuarioLogadoService.exigirFuncionarioAtivo()` para garantir
     * autenticação.
     */
    @Transactional(readOnly = true)
    public void exigirInteracao(Integer contratoId) {
        Usuario usuario = usuarioLogadoService.exigirFuncionarioAtivo();
        if (!podeInteragir(contratoId, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não pertence ao time deste contrato nem tem permissão para interagir");
        }
    }

    /**
     * Resolve flags completas para o usuário logado sobre o contrato.
     */
    @Transactional(readOnly = true)
    public PermissoesContratoDto resolverFlagsUsuarioLogado(Integer contratoId) {
        Usuario usuario = usuarioLogadoService.exigirFuncionarioAtivo();
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado"));

        boolean membro = membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(contrato.getIdContrato(), usuario.getIdUsuario());
        boolean ceo = ehCeo(usuario);
        boolean interagirQualquer = usuarioLogadoService
                .temPermissao(com.climbe.api_climbe.model.enums.CodigoPermissao.CONTRATO_INTERAGIR_QUALQUER);
        boolean podeInteragir = membro || ceo || interagirQualquer;
        boolean podeVisualizar = podeInteragir
                || usuarioLogadoService.temPermissao(com.climbe.api_climbe.model.enums.CodigoPermissao.CONTRATO_VER);
        boolean podeGerenciarTime = ceo
                || usuarioLogadoService.temPermissao(com.climbe.api_climbe.model.enums.CodigoPermissao.TIME_CONTRATO_ADICIONAR);

        return new PermissoesContratoDto(
                contrato.getIdContrato(),
                podeVisualizar,
                podeInteragir,
                podeGerenciarTime,
                membro,
                ceo
        );
    }

    private boolean ehCeo(Usuario usuario) {
        return usuario.getCargo() != null
                && CARGO_CEO.equalsIgnoreCase(usuario.getCargo().getNomeCargo());
    }
}
