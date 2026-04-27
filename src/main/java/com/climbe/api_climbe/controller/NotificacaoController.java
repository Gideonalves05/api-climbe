package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.AtualizarPreferenciasNotificacaoDto;
import com.climbe.api_climbe.dto.NotificacaoDto;
import com.climbe.api_climbe.dto.PreferenciaNotificacaoDto;
import com.climbe.api_climbe.model.Notificacao;
import com.climbe.api_climbe.model.PreferenciaNotificacao;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import com.climbe.api_climbe.repository.NotificacaoRepository;
import com.climbe.api_climbe.repository.PreferenciaNotificacaoRepository;
import com.climbe.api_climbe.service.UsuarioLogadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notificações", description = "Endpoints de notificações para usuários")
@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificacaoController {

    private final NotificacaoRepository notificacaoRepository;
    private final PreferenciaNotificacaoRepository preferenciaRepository;
    private final UsuarioLogadoService usuarioLogadoService;
    private final com.climbe.api_climbe.service.SseService sseService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar notificações do usuário logado", description = "Retorna lista de notificações com filtros opcionais")
    public List<NotificacaoDto> listar(
        @RequestParam(required = false) Boolean lida,
        @RequestParam(required = false) TipoNotificacao tipo,
        @RequestParam(required = false) LocalDateTime desde
    ) {
        Usuario usuario = usuarioLogadoService.exigirFuncionarioAtivo();
        List<Notificacao> notificacoes;

        if (lida != null && tipo != null && desde != null) {
            notificacoes = notificacaoRepository.findByUsuarioAndLidaAndTipoAndCriadoEmAfterOrderByCriadoEmDesc(
                usuario, lida, tipo, desde
            );
        } else if (lida != null && tipo != null) {
            notificacoes = notificacaoRepository.findByUsuarioAndLidaAndTipoOrderByCriadoEmDesc(usuario, lida, tipo);
        } else if (lida != null) {
            notificacoes = notificacaoRepository.findByUsuarioAndLidaOrderByCriadoEmDesc(usuario, lida);
        } else if (tipo != null) {
            notificacoes = notificacaoRepository.findByUsuarioAndTipoOrderByCriadoEmDesc(usuario, tipo);
        } else if (desde != null) {
            notificacoes = notificacaoRepository.findByUsuarioAndCriadoEmAfterOrderByCriadoEmDesc(usuario, desde);
        } else {
            notificacoes = notificacaoRepository.findByUsuarioOrderByCriadoEmDesc(usuario);
        }

        return notificacoes.stream()
            .map(this::paraDto)
            .collect(Collectors.toList());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream SSE de notificações em tempo real", description = "Abre conexão SSE para receber notificações push")
    public SseEmitter stream() {
        Usuario usuario = usuarioLogadoService.exigirFuncionarioAtivo();
        return sseService.criarEmitter(usuario.getIdUsuario());
    }

    @PostMapping("/{id}/ler")
    @Operation(summary = "Marcar notificação como lida", description = "Marca uma notificação específica como lida")
    public void marcarComoLida(@PathVariable Integer id) {
        Usuario usuario = usuarioLogadoService.exigirFuncionarioAtivo();
        Notificacao notificacao = notificacaoRepository.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Notificação não encontrada"
            ));

        if (!notificacao.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "Acesso negado"
            );
        }

        notificacao.setLida(true);
        notificacao.setLidaEm(LocalDateTime.now());
        notificacaoRepository.save(notificacao);
    }

    @PostMapping("/ler-todas")
    @Operation(summary = "Marcar todas as notificações como lidas", description = "Marca todas as notificações não lidas do usuário como lidas")
    public void marcarTodasComoLidas() {
        Usuario usuario = usuarioLogadoService.exigirFuncionarioAtivo();
        List<Notificacao> naoLidas = notificacaoRepository.findByUsuarioAndLidaFalseOrderByCriadoEmDesc(usuario);
        
        naoLidas.forEach(n -> {
            n.setLida(true);
            n.setLidaEm(LocalDateTime.now());
        });
        
        notificacaoRepository.saveAll(naoLidas);
    }

    @GetMapping("/preferencias")
    @Operation(summary = "Listar preferências de notificação", description = "Retorna as preferências do usuário logado")
    public List<PreferenciaNotificacaoDto> listarPreferencias() {
        Usuario usuario = usuarioLogadoService.exigirFuncionarioAtivo();
        return preferenciaRepository.findAll().stream()
            .filter(p -> p.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .map(this::paraPreferenciaDto)
            .collect(Collectors.toList());
    }

    @PutMapping("/preferencias")
    @Operation(summary = "Atualizar preferências de notificação", description = "Atualiza ou cria preferências em lote")
    public void atualizarPreferencias(@Valid @RequestBody AtualizarPreferenciasNotificacaoDto dto) {
        Usuario usuario = usuarioLogadoService.exigirFuncionarioAtivo();
        
        for (PreferenciaNotificacaoDto prefDto : dto.preferencias()) {
            PreferenciaNotificacao pref = preferenciaRepository
                .findByUsuarioAndTipoAndCanal(usuario, prefDto.tipo(), prefDto.canal())
                .orElse(new PreferenciaNotificacao());
            
            pref.setUsuario(usuario);
            pref.setTipo(prefDto.tipo());
            pref.setCanal(prefDto.canal());
            pref.setHabilitado(prefDto.habilitado());
            
            preferenciaRepository.save(pref);
        }
    }

    private NotificacaoDto paraDto(Notificacao n) {
        return new NotificacaoDto(
            n.getIdNotificacao(),
            n.getUsuario().getIdUsuario(),
            n.getTipo(),
            n.getTitulo(),
            n.getMensagem(),
            n.getLinkDestino(),
            n.getPayload(),
            n.getLida(),
            n.getLidaEm(),
            n.getCriadoEm()
        );
    }

    private PreferenciaNotificacaoDto paraPreferenciaDto(PreferenciaNotificacao p) {
        return new PreferenciaNotificacaoDto(
            p.getTipo(),
            p.getCanal(),
            p.getHabilitado()
        );
    }
}
