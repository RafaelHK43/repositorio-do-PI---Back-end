package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.LogAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAcaoRepository extends JpaRepository<LogAcao, Long> {
}
