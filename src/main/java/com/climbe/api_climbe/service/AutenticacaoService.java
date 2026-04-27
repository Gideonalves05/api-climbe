package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.LoginFuncionarioDto;
import com.climbe.api_climbe.dto.LoginResponseDto;
import com.climbe.api_climbe.dto.TokenRespostaDto;
import com.climbe.api_climbe.dto.UsuarioAutenticadoDto;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.UsuarioRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenJwtService tokenJwtService;
    private final AuditoriaService auditoriaService;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            TokenJwtService tokenJwtService,
            AuditoriaService auditoriaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenJwtService = tokenJwtService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public TokenRespostaDto loginFuncionario(LoginFuncionarioDto dto) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dto.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        if (usuario.getSituacao() == SituacaoUsuario.PENDENTE_APROVACAO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sua conta está pendente de aprovação. Aguarde o administrador aprovar sua solicitação.");
        }

        if (usuario.getSituacao() == SituacaoUsuario.INATIVO) {
            String motivo = usuario.getMotivoRejeicao() != null ? " Motivo: " + usuario.getMotivoRejeicao() : "";
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sua conta foi desativada." + motivo);
        }

        if (usuario.getSituacao() != SituacaoUsuario.ATIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não está ativo");
        }

        String cargo = usuario.getCargo() != null ? usuario.getCargo().getNomeCargo() : "SEM_CARGO";
        List<String> authorities = montarAuthoritiesFuncionario(usuario, cargo);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tipoConta", "FUNCIONARIO");
        claims.put("idUsuario", usuario.getIdUsuario());
        claims.put("email", usuario.getEmail());
        claims.put("cargo", cargo);
        claims.put("nome", usuario.getNomeCompleto());

        String token = tokenJwtService.gerarToken(usuario.getEmail(), claims, authorities);

        Map<String, Object> payload = new HashMap<>();
        payload.put("usuarioId", usuario.getIdUsuario());
        payload.put("email", usuario.getEmail());
        payload.put("cargo", cargo);

        auditoriaService.registrarEvento(
                TipoEventoAuditoria.LOGIN_REALIZADO,
                "USUARIO",
                usuario.getIdUsuario(),
                payload
        );

        return new TokenRespostaDto(
                token,
                "Bearer",
                tokenJwtService.segundosExpiracao(),
                "FUNCIONARIO",
                String.valueOf(usuario.getIdUsuario()),
                usuario.getNomeCompleto(),
                cargo
        );
    }

    @Transactional(readOnly = true)
    public UsuarioAutenticadoDto obterUsuarioAutenticado(Usuario usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        String nomeCargo = usuario.getCargo() != null ? usuario.getCargo().getNomeCargo() : "SEM_CARGO";
        Integer idCargo = usuario.getCargo() != null ? usuario.getCargo().getIdCargo() : null;

        List<String> permissoes = montarAuthoritiesFuncionario(usuario, nomeCargo);

        return new UsuarioAutenticadoDto(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getNomeCompleto(),
                new UsuarioAutenticadoDto.CargoResumoDto(idCargo, nomeCargo),
                permissoes
        );
    }

    private List<String> montarAuthoritiesFuncionario(Usuario usuario, String cargo) {
        String roleCargo = "ROLE_" + cargo
                .trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("&", "E");

        Set<String> permissoes = usuario.getPermissoes().stream()
                .map(p -> p.getCodigo())
                .collect(Collectors.toSet());

        permissoes.add(roleCargo);
        return permissoes.stream().toList();
    }

    @Transactional
    public LoginResponseDto loginComGoogle(String email, String nome, String googleSub) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (usuario.getSituacao() == SituacaoUsuario.PENDENTE_APROVACAO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sua conta está pendente de aprovação. Aguarde o administrador aprovar sua solicitação.");
        }

        if (usuario.getSituacao() == SituacaoUsuario.INATIVO) {
            String motivo = usuario.getMotivoRejeicao() != null ? " Motivo: " + usuario.getMotivoRejeicao() : "";
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sua conta foi desativada." + motivo);
        }

        if (usuario.getSituacao() != SituacaoUsuario.ATIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não está ativo");
        }

        String cargo = usuario.getCargo() != null ? usuario.getCargo().getNomeCargo() : "SEM_CARGO";
        List<String> authorities = montarAuthoritiesFuncionario(usuario, cargo);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tipoConta", "FUNCIONARIO");
        claims.put("idUsuario", usuario.getIdUsuario());
        claims.put("email", usuario.getEmail());
        claims.put("cargo", cargo);
        claims.put("googleSub", googleSub);

        String token = tokenJwtService.gerarToken(usuario.getEmail(), claims, authorities);

        Map<String, Object> payload = new HashMap<>();
        payload.put("usuarioId", usuario.getIdUsuario());
        payload.put("email", usuario.getEmail());
        payload.put("cargo", cargo);
        payload.put("googleSub", googleSub);

        auditoriaService.registrarEvento(
                TipoEventoAuditoria.LOGIN_REALIZADO,
                "USUARIO",
                usuario.getIdUsuario(),
                payload
        );

        return new LoginResponseDto(
                token,
                "Bearer",
                tokenJwtService.segundosExpiracao(),
                "FUNCIONARIO",
                String.valueOf(usuario.getIdUsuario()),
                usuario.getNomeCompleto(),
                cargo,
                usuario.getEmail()
        );
    }
}
