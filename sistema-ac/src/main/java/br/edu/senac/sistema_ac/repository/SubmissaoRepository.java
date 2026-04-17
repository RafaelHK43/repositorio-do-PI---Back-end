package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import java.math.BigDecimal;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissaoRepository extends JpaRepository<Submissao, Long> {

    @Query("""
        SELECT COALESCE(SUM(COALESCE(s.horasAprovadas, s.atividadeComplementar.horasDeclaradas)), 0)
        FROM Submissao s
        WHERE s.aluno.id = :alunoId
          AND s.atividadeComplementar.curso.id = :cursoId
          AND s.atividadeComplementar.area = :area
          AND s.status IN :statusPermitidos
        """)
    BigDecimal somarHorasPorAlunoCursoAreaEStatus(
        @Param("alunoId") Long alunoId,
        @Param("cursoId") Long cursoId,
        @Param("area") AreaAtividade area,
        @Param("statusPermitidos") Collection<StatusSubmissao> statusPermitidos
    );

    @Query("""
        SELECT COALESCE(SUM(COALESCE(s.horasAprovadas, s.atividadeComplementar.horasDeclaradas)), 0)
        FROM Submissao s
        WHERE s.aluno.id = :alunoId
          AND s.atividadeComplementar.curso.id = :cursoId
          AND s.status IN :statusPermitidos
        """)
    BigDecimal somarHorasPorAlunoCursoEStatus(
        @Param("alunoId") Long alunoId,
        @Param("cursoId") Long cursoId,
        @Param("statusPermitidos") Collection<StatusSubmissao> statusPermitidos
    );
}
