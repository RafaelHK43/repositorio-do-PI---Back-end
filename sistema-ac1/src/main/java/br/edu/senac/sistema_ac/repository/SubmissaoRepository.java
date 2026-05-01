package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
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

    List<Submissao> findAllByOrderByDataSubmissaoDesc();

    List<Submissao> findAllByAtividadeComplementarCursoIdInOrderByDataSubmissaoDesc(Collection<Long> cursoIds);

    List<Submissao> findAllByAlunoIdOrderByDataSubmissaoDesc(Long alunoId);

    List<Submissao> findAllByAlunoIdAndStatusOrderByDataSubmissaoDesc(Long alunoId, StatusSubmissao status);

    long countByStatus(StatusSubmissao status);

    default long countByStatus(String status) {
        if (status == null) return 0L;
        return countByStatus(StatusSubmissao.valueOf(status));
    }

    @Query("""
        SELECT COALESCE(SUM(COALESCE(s.horasAprovadas, s.atividadeComplementar.horasDeclaradas)), 0)
        FROM Submissao s
        WHERE s.status = :status
        """)
    BigDecimal somarHorasPorStatus(@Param("status") StatusSubmissao status);
}
