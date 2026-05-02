package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import br.edu.senac.sistema_ac.dto.CursoResponseDTO;
import br.edu.senac.sistema_ac.dto.UsuarioRequestDTO;
import br.edu.senac.sistema_ac.dto.UsuarioResponseDTO;
import br.edu.senac.sistema_ac.exception.RecursoNaoEncontradoException;
import br.edu.senac.sistema_ac.repository.CursoRepository;
import br.edu.senac.sistema_ac.repository.SubmissaoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final SubmissaoRepository submissaoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos(PerfilUsuario perfil, Long cursoId, Authentication authentication) {
        if (authentication != null) {
            Usuario usuarioLogado = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado nao encontrado"));

            if (usuarioLogado.getPerfil() == PerfilUsuario.COORDENADOR) {
                List<Long> cursoIds = usuarioLogado.getCursos().stream()
                    .map(Curso::getId)
                    .toList();
                return usuarioRepository.findAllByFiltrosECursos(perfil, cursoIds)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            }
        }

        return usuarioRepository.findAllByFiltros(perfil, cursoId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        return toDto(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioLogado(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Usuario autenticado nao encontrado");
        }

        Usuario usuario = usuarioRepository.findWithCursosByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado nao encontrado"));

        return toDto(usuario);
    }

    @Transactional
    public UsuarioResponseDTO salvar(UsuarioRequestDTO request) {
        Usuario usuario = Usuario.builder()
            .nome(request.nome())
            .email(request.email())
            .perfil(request.perfil())
            .senha(passwordEncoder.encode(request.senha()))
            .build();

        usuario.setCursos(fetchCursosByIds(request.cursoIds()));

        Usuario salvo = usuarioRepository.save(usuario);
        return toDto(salvo);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }
        usuario.setPerfil(request.perfil());
        usuario.setCursos(fetchCursosByIds(request.cursoIds()));

        Usuario atualizado = usuarioRepository.save(usuario);
        return toDto(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        usuarioRepository.delete(usuario);
    }

    private List<Curso> fetchCursosByIds(List<Long> cursoIds) {
        if (cursoIds == null || cursoIds.isEmpty()) {
            return List.of();
        }

        List<Curso> cursos = cursoRepository.findAllById(cursoIds);
        if (cursos.size() != cursoIds.size()) {
            throw new RecursoNaoEncontradoException("Um ou mais cursos nao encontrados");
        }
        return cursos;
    }

    private UsuarioResponseDTO toDto(Usuario usuario) {
        List<CursoResponseDTO> cursos = usuario.getCursos()
            .stream()
            .map(c -> new CursoResponseDTO(c.getId(), c.getNome(), c.getCargaHorariaMinima()))
            .collect(Collectors.toList());

        CursoResponseDTO cursoPrincipal = cursos.isEmpty() ? null : cursos.get(0);
        List<br.edu.senac.sistema_ac.domain.entity.Submissao> submissoes =
            usuario.getPerfil() == PerfilUsuario.ALUNO
                ? submissaoRepository.findAllByAlunoIdOrderByDataSubmissaoDesc(usuario.getId())
                : List.of();

        long pendentes = submissoes.stream()
            .filter(s -> s.getStatus() == br.edu.senac.sistema_ac.domain.enums.StatusSubmissao.PENDENTE)
            .count();
        long aprovadas = submissoes.stream()
            .filter(s -> s.getStatus() == br.edu.senac.sistema_ac.domain.enums.StatusSubmissao.APROVADA)
            .count();
        long reprovadas = submissoes.stream()
            .filter(s -> s.getStatus() == br.edu.senac.sistema_ac.domain.enums.StatusSubmissao.REPROVADA)
            .count();
        BigDecimal horasAprovadas = submissoes.stream()
            .filter(s -> s.getStatus() == br.edu.senac.sistema_ac.domain.enums.StatusSubmissao.APROVADA && s.getHorasAprovadas() != null)
            .map(br.edu.senac.sistema_ac.domain.entity.Submissao::getHorasAprovadas)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer cargaHorariaMinima = cursoPrincipal != null ? cursoPrincipal.cargaHorariaMinima() : 0;
        Double progressoPercentual = cargaHorariaMinima != null && cargaHorariaMinima > 0
            ? horasAprovadas.multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(cargaHorariaMinima), 2, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100))
                .doubleValue()
            : 0.0;

        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil(),
            cursos,
            cursoPrincipal != null ? cursoPrincipal.id() : null,
            cursoPrincipal != null ? cursoPrincipal.nome() : null,
            submissoes.size(),
            pendentes,
            aprovadas,
            reprovadas,
            horasAprovadas,
            cargaHorariaMinima,
            progressoPercentual
        );
    }
}
