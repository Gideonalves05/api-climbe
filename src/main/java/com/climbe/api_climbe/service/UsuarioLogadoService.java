package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.CodigoPermissao;
import com.climbe.api_climbe.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioLogadoService {

    private final UsuarioRepository usuarioRepository;

    public Usuario exigirFuncionarioAtivo() {
        Usuario usuario = getUsuarioLogado();
        
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        
        if (usuario.getSituacao() != com.climbe.api_climbe.model.enums.SituacaoUsuario.ATIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não está ativo");
        }
        
        return usuario;
    }

    public void exigirEmpresa() {
        Usuario usuario = getUsuarioLogado();
        
        if (usuario != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a empresas");
        }
    }

    public boolean temPermissao(CodigoPermissao codigo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(codigo.name()));
    }

    public void exigirPermissao(CodigoPermissao codigo) {
        if (!temPermissao(codigo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Permissão necessária: " + codigo.getDescricaoPadrao());
        }
    }

    public boolean ehLiderDeTime(Integer contratoId) {
        // TODO: Implementar quando MembroTime for criado
        return false;
    }

    public boolean ehResponsavelDaTarefa(Integer tarefaId) {
        // TODO: Implementar quando TarefaContrato for criado
        return false;
    }

    public Usuario obterUsuarioLogadoOrNull() {
        return getUsuarioLogado();
    }

    private Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        return usuarioRepository.findByEmailIgnoreCaseWithCargoAndPermissoes(email).orElse(null);
    }
}
