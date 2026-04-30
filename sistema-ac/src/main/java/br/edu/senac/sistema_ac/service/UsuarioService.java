package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.dto.CursoResponseDTO;
import br.edu.senac.sistema_ac.dto.UsuarioRequestDTO;
import br.edu.senac.sistema_ac.dto.UsuarioResponseDTO;
import br.edu.senac.sistema_ac.exception.RecursoNaoEncontradoException;
import br.edu.senac.sistema_ac.repository.CursoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
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

        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(), cursos);
    }
}
