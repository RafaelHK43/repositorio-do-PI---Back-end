package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    Optional<Curso> findByNomeIgnoreCase(String nome);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId")
    java.util.List<Curso> findByUsuarioId(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);
}
