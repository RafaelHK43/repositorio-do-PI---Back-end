package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    Optional<Curso> findByNomeIgnoreCase(String nome);
}
