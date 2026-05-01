package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.RegraAtividade;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.exception.LimiteHorasExcedidoException;
import br.edu.senac.sistema_ac.repository.RegraAtividadeRepository;
import br.edu.senac.sistema_ac.repository.SubmissaoRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ValidacaoHorasService {

    private static final BigDecimal LIMITE_TOTAL_CURSO = new BigDecimal("120.00");

    private final RegraAtividadeRepository regraAtividadeRepository;
    private final SubmissaoRepository submissaoRepository;

    @Transactional(readOnly = true)
    public void validarLimiteHorasPorArea(Long alunoId, Long cursoId, AreaAtividade area, BigDecimal novasHoras) {
        RegraAtividade regra = regraAtividadeRepository.findByCursoIdAndArea(cursoId, area)
            .orElseThrow(() -> new IllegalArgumentException("Nao existe regra cadastrada para esta area neste curso"));

        BigDecimal horasJaValidadas = submissaoRepository.somarHorasPorAlunoCursoAreaEStatus(
            alunoId,
            cursoId,
            area,
            List.of(StatusSubmissao.APROVADA, StatusSubmissao.PENDENTE)
        );

        BigDecimal totalComNovaSubmissao = horasJaValidadas.add(novasHoras);
        if (totalComNovaSubmissao.compareTo(regra.getLimiteHoras()) > 0) {
            throw new LimiteHorasExcedidoException(
                "Limite de horas excedido para a area " + area +
                    ". Limite: " + regra.getLimiteHoras() +
                    ", ja validado: " + horasJaValidadas +
                    ", tentativa: " + novasHoras
            );
        }

        BigDecimal horasTotaisNoCurso = submissaoRepository.somarHorasPorAlunoCursoEStatus(
            alunoId,
            cursoId,
            List.of(StatusSubmissao.APROVADA, StatusSubmissao.PENDENTE)
        );
        BigDecimal totalGeralComNovaSubmissao = horasTotaisNoCurso.add(novasHoras);

        if (totalGeralComNovaSubmissao.compareTo(LIMITE_TOTAL_CURSO) > 0) {
            throw new LimiteHorasExcedidoException(
                "Limite total de 120 horas excedido para o curso. " +
                    "Ja contabilizado: " + horasTotaisNoCurso +
                    ", tentativa: " + novasHoras
            );
        }
    }
}
