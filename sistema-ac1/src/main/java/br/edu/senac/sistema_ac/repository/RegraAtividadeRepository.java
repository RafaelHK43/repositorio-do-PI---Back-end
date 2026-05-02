package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.RegraAtividade;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegraAtividadeRepository extends JpaRepository<RegraAtividade, Long> {
    @EntityGraph(attributePaths = "curso")
    Optional<RegraAtividade> findByCursoIdAndArea(Long cursoId, AreaAtividade area);

    @EntityGraph(attributePaths = "curso")
    List<RegraAtividade> findAllByCursoId(Long cursoId);

    @Override
    @EntityGraph(attributePaths = "curso")
    List<RegraAtividade> findAll();

    @Override
    @EntityGraph(attributePaths = "curso")
    Optional<RegraAtividade> findById(Long id);
}
