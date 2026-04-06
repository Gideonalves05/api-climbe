package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.CadastroEmpresaDto;
import com.climbe.api_climbe.dto.EmpresaDto;
import com.climbe.api_climbe.dto.LoginEmpresaDto;
import com.climbe.api_climbe.dto.LoginFuncionarioDto;
import com.climbe.api_climbe.dto.TokenRespostaDto;
import com.climbe.api_climbe.model.Empresa;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.repository.EmpresaRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenJwtService tokenJwtService;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder,
            TokenJwtService tokenJwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenJwtService = tokenJwtService;
    }

    @Transactional(readOnly = true)
    public TokenRespostaDto loginFuncionario(LoginFuncionarioDto dto) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dto.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
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

        String token = tokenJwtService.gerarToken(usuario.getEmail(), claims, authorities);
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

    @Transactional
    public EmpresaDto cadastrarEmpresa(CadastroEmpresaDto dto) {
        String cnpjNormalizado = normalizarCnpj(dto.cnpj());

        if (empresaRepository.existsByCnpj(cnpjNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado");
        }

        if (empresaRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        Empresa empresa = new Empresa();
        empresa.setRazaoSocial(dto.razaoSocial());
        empresa.setNomeFantasia(dto.nomeFantasia());
        empresa.setCnpj(cnpjNormalizado);
        empresa.setLogradouro(dto.logradouro());
        empresa.setNumero(dto.numero());
        empresa.setBairro(dto.bairro());
        empresa.setCidade(dto.cidade());
        empresa.setUf(dto.uf());
        empresa.setCep(dto.cep());
        empresa.setTelefone(dto.telefone());
        empresa.setEmail(dto.email());
        empresa.setRepresentanteNome(dto.representanteNome());
        empresa.setRepresentanteCpf(dto.representanteCpf());
        empresa.setRepresentanteContato(dto.representanteContato());
        empresa.setSenhaHash(passwordEncoder.encode(dto.senha()));

        Empresa salva = empresaRepository.save(empresa);
        return paraDto(salva);
    }

    @Transactional(readOnly = true)
    public TokenRespostaDto loginEmpresa(LoginEmpresaDto dto) {
        String login = Objects.toString(dto.login(), "").trim();

        Empresa empresa = buscarEmpresaPorLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.senha(), empresa.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        List<String> authorities = List.of("ROLE_EMPRESA");
        Map<String, Object> claims = new HashMap<>();
        claims.put("tipoConta", "EMPRESA");
        claims.put("idEmpresa", empresa.getIdEmpresa());
        claims.put("cnpj", empresa.getCnpj());
        claims.put("email", empresa.getEmail());

        String token = tokenJwtService.gerarToken(empresa.getCnpj(), claims, authorities);
        return new TokenRespostaDto(
                token,
                "Bearer",
                tokenJwtService.segundosExpiracao(),
                "EMPRESA",
                String.valueOf(empresa.getIdEmpresa()),
                empresa.getNomeFantasia(),
                "EMPRESA"
        );
    }

    private List<String> montarAuthoritiesFuncionario(Usuario usuario, String cargo) {
        String roleCargo = "ROLE_" + cargo
                .trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("&", "E");

        Set<String> permissoes = usuario.getPermissoes().stream()
                .map(p -> "PERMISSAO_" + p.getDescricao()
                        .trim()
                        .toUpperCase()
                        .replace(" ", "_"))
                .collect(Collectors.toSet());

        permissoes.add(roleCargo);
        return permissoes.stream().toList();
    }

    private java.util.Optional<Empresa> buscarEmpresaPorLogin(String login) {
        String cnpjNormalizado = normalizarCnpj(login);
        if (cnpjNormalizado.length() == 14) {
            return empresaRepository.findByCnpj(cnpjNormalizado);
        }
        return empresaRepository.findByEmailIgnoreCase(login);
    }

    private String normalizarCnpj(String cnpj) {
        return Objects.toString(cnpj, "").replaceAll("\\D", "");
    }

    private EmpresaDto paraDto(Empresa empresa) {
        return new EmpresaDto(
                empresa.getIdEmpresa(),
                empresa.getRazaoSocial(),
                empresa.getNomeFantasia(),
                empresa.getCnpj(),
                empresa.getLogradouro(),
                empresa.getNumero(),
                empresa.getBairro(),
                empresa.getCidade(),
                empresa.getUf(),
                empresa.getCep(),
                empresa.getTelefone(),
                empresa.getEmail(),
                empresa.getRepresentanteNome(),
                empresa.getRepresentanteCpf(),
                empresa.getRepresentanteContato(),
                Set.of()
        );
    }
}
