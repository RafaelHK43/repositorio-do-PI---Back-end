package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.repository.CursoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

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
            .orElseThrow(() -> new IllegalArgumentException("Curso nao encontrado"));
    }
}
