package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.AgendarReuniaoContratanteDto;
import com.climbe.api_climbe.dto.ReuniaoDto;
import com.climbe.api_climbe.model.Empresa;
import com.climbe.api_climbe.model.Reuniao;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.StatusReuniao;
import com.climbe.api_climbe.repository.EmpresaRepository;
import com.climbe.api_climbe.repository.ReuniaoRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReuniaoService {

    private final ReuniaoRepository reuniaoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReuniaoService(
            ReuniaoRepository reuniaoRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.reuniaoRepository = reuniaoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ReuniaoDto agendarComContratante(AgendarReuniaoContratanteDto dto, Authentication authentication) {
        String emailFuncionario = authentication != null ? authentication.getName() : null;
        Usuario funcionario = usuarioRepository.findByEmailIgnoreCase(Objects.toString(emailFuncionario, ""))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Somente funcionário da Climbe pode agendar reunião com contratante"
                ));

        Empresa empresa = empresaRepository.findById(dto.idEmpresa())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        Set<Integer> idsSolicitados = dto.idsParticipantes() == null ? Set.of() : dto.idsParticipantes();
        Set<Usuario> participantes = new LinkedHashSet<>(usuarioRepository.findAllById(idsSolicitados));
        if (participantes.size() != idsSolicitados.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais participantes não existem");
        }
        participantes.add(funcionario);

        Reuniao reuniao = new Reuniao();
        reuniao.setTitulo(dto.titulo().trim());
        reuniao.setEmpresa(empresa);
        reuniao.setData(dto.data());
        reuniao.setHora(dto.hora());
        reuniao.setPresencial(dto.presencial());
        reuniao.setLocal(dto.local());
        reuniao.setPauta(dto.pauta());
        reuniao.setStatus(StatusReuniao.AGENDADA);
        reuniao.setParticipantes(participantes);

        Reuniao salva = reuniaoRepository.save(reuniao);
        return paraDto(salva);
    }

    private ReuniaoDto paraDto(Reuniao reuniao) {
        return new ReuniaoDto(
                reuniao.getIdReuniao(),
                reuniao.getTitulo(),
                reuniao.getEmpresa() != null ? reuniao.getEmpresa().getIdEmpresa() : null,
                reuniao.getData(),
                reuniao.getHora(),
                reuniao.getPresencial(),
                reuniao.getLocal(),
                reuniao.getPauta(),
                reuniao.getStatus(),
                reuniao.getParticipantes().stream()
                        .map(Usuario::getIdUsuario)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }
}
