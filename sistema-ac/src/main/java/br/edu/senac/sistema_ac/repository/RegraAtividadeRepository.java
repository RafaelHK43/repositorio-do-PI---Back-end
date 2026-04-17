package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.RegraAtividade;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegraAtividadeRepository extends JpaRepository<RegraAtividade, Long> {
    Optional<RegraAtividade> findByCursoIdAndArea(Long cursoId, AreaAtividade area);
    List<RegraAtividade> findAllByCursoId(Long cursoId);
}
