package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.RegraAtividade;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.repository.RegraAtividadeRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegraAtividadeService {

    private final RegraAtividadeRepository regraAtividadeRepository;
    private final CursoService cursoService;

    @Transactional
    public RegraAtividade criarOuAtualizar(Long cursoId, AreaAtividade area, BigDecimal limiteHoras) {
        Curso curso = cursoService.buscarPorId(cursoId);

        RegraAtividade regra = regraAtividadeRepository.findByCursoIdAndArea(cursoId, area)
            .orElse(RegraAtividade.builder()
                .curso(curso)
                .area(area)
                .build());

        regra.setLimiteHoras(limiteHoras);
        return regraAtividadeRepository.save(regra);
    }

    @Transactional(readOnly = true)
    public List<RegraAtividade> listarPorCurso(Long cursoId) {
        return regraAtividadeRepository.findAllByCursoId(cursoId);
    }
}
