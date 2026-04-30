package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.exception.RecursoNaoEncontradoException;
import br.edu.senac.sistema_ac.repository.AtividadeComplementarRepository;
import br.edu.senac.sistema_ac.repository.CursoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AtividadeComplementarRepository atividadeComplementarRepository;

    @Transactional
    public Curso criar(String nome) {
        cursoRepository.findByNomeIgnoreCase(nome).ifPresent(c -> {
            throw new IllegalArgumentException("Ja existe um curso com este nome");
        });

        Curso curso = Curso.builder()
            .nome(nome)
            .build();

        return cursoRepository.save(curso);
    }

    @Transactional(readOnly = true)
    public List<Curso> listar() {
        return cursoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Curso buscarPorId(Long id) {
        return cursoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Curso nao encontrado"));
    }

    @Transactional
    public Curso atualizar(Long id, String nome) {
        Curso curso = buscarPorId(id);

        cursoRepository.findByNomeIgnoreCase(nome).ifPresent(cursoExistente -> {
            if (!cursoExistente.getId().equals(id)) {
                throw new IllegalArgumentException("Ja existe um curso com este nome");
            }
        });

        curso.setNome(nome);
        return cursoRepository.save(curso);
    }

    @Transactional
    public void excluir(Long id) {
        Curso curso = buscarPorId(id);

        if (usuarioRepository.existsByCursosId(id)) {
            throw new IllegalArgumentException("Nao e possivel excluir o curso porque existem usuarios vinculados");
        }

        if (atividadeComplementarRepository.existsByCursoId(id)) {
            throw new IllegalArgumentException("Nao e possivel excluir o curso porque existem atividades complementares vinculadas");
        }

        cursoRepository.delete(curso);
    }
}
