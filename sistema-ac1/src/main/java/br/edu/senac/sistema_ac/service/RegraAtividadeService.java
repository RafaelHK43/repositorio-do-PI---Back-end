package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.RegraAtividade;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.repository.RegraAtividadeRepository;
import br.edu.senac.sistema_ac.exception.RecursoNaoEncontradoException;
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

    @Transactional(readOnly = true)
    public List<RegraAtividade> listarTodas() {
        return regraAtividadeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RegraAtividade buscarPorId(Long id) {
        return regraAtividadeRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Regra de atividade nao encontrada"));
    }

    @Transactional
    public RegraAtividade atualizar(Long id, Long cursoId, AreaAtividade area, BigDecimal limiteHoras) {
        RegraAtividade regra = buscarPorId(id);
        Curso curso = cursoService.buscarPorId(cursoId);

        regraAtividadeRepository.findByCursoIdAndArea(cursoId, area)
            .ifPresent(regraExistente -> {
                if (!regraExistente.getId().equals(id)) {
                    throw new IllegalArgumentException("Ja existe uma regra cadastrada para este curso e area");
                }
            });

        regra.setCurso(curso);
        regra.setArea(area);
        regra.setLimiteHoras(limiteHoras);
        return regraAtividadeRepository.save(regra);
    }

    @Transactional
    public void excluir(Long id) {
        RegraAtividade regra = buscarPorId(id);
        regraAtividadeRepository.delete(regra);
    }
}
