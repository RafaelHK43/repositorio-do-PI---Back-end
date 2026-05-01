package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.AtividadeComplementar;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeComplementarRepository extends JpaRepository<AtividadeComplementar, Long> {
	boolean existsByCursoId(Long cursoId);

	List<AtividadeComplementar> findAllByCursoId(Long cursoId);
}
