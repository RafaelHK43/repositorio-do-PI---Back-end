package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.dto.DashboardResponseDTO;
import br.edu.senac.sistema_ac.dto.MetricaAreaDTO;
import br.edu.senac.sistema_ac.dto.MetricaCursoDTO;
import br.edu.senac.sistema_ac.repository.SubmissaoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final SubmissaoRepository submissaoRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO obterMetrics() {
        long totalAlunos = usuarioRepository.countByPerfil(PerfilUsuario.ALUNO);
        long pendentes = submissaoRepository.countByStatus(StatusSubmissao.PENDENTE);
        long submissoesAprovadas = submissaoRepository.countByStatus(StatusSubmissao.APROVADA);
        long submissoesReprovadas = submissaoRepository.countByStatus(StatusSubmissao.REPROVADA);
        BigDecimal horasAprovadas = submissaoRepository.somarHorasPorStatus(StatusSubmissao.APROVADA);
        long totalSubmissoes = pendentes + submissoesAprovadas + submissoesReprovadas;

        List<Submissao> todas = submissaoRepository.findAllByOrderByDataSubmissaoDesc();

        List<MetricaCursoDTO> metricasPorCurso = buildMetricasPorCurso(todas);
        List<MetricaAreaDTO> metricasPorArea = buildMetricasPorArea(todas);

        return new DashboardResponseDTO(
            totalAlunos,
            totalSubmissoes,
            pendentes,
            submissoesAprovadas,
            submissoesReprovadas,
            horasAprovadas,
            metricasPorCurso,
            metricasPorArea
        );
    }

    private List<MetricaCursoDTO> buildMetricasPorCurso(List<Submissao> todas) {
        Map<Long, List<Submissao>> porCurso = todas.stream()
            .filter(s -> s.getAtividadeComplementar() != null
                && s.getAtividadeComplementar().getCurso() != null)
            .collect(Collectors.groupingBy(s -> s.getAtividadeComplementar().getCurso().getId()));

        return porCurso.entrySet().stream()
            .map(entry -> {
                List<Submissao> subs = entry.getValue();
                String cursoNome = subs.get(0).getAtividadeComplementar().getCurso().getNome();
                long pendentes = subs.stream().filter(s -> s.getStatus() == StatusSubmissao.PENDENTE).count();
                long aprovadas = subs.stream().filter(s -> s.getStatus() == StatusSubmissao.APROVADA).count();
                long reprovadas = subs.stream().filter(s -> s.getStatus() == StatusSubmissao.REPROVADA).count();
                BigDecimal horas = subs.stream()
                    .filter(s -> s.getStatus() == StatusSubmissao.APROVADA && s.getHorasAprovadas() != null)
                    .map(Submissao::getHorasAprovadas)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new MetricaCursoDTO(entry.getKey(), cursoNome, subs.size(), pendentes, aprovadas, reprovadas, horas);
            })
            .collect(Collectors.toList());
    }

    private List<MetricaAreaDTO> buildMetricasPorArea(List<Submissao> todas) {
        Map<AreaAtividade, List<Submissao>> porArea = todas.stream()
            .filter(s -> s.getAtividadeComplementar() != null)
            .collect(Collectors.groupingBy(s -> s.getAtividadeComplementar().getArea()));

        return Arrays.stream(AreaAtividade.values())
            .map(area -> {
                List<Submissao> subs = porArea.getOrDefault(area, List.of());
                long aprovadas = subs.stream().filter(s -> s.getStatus() == StatusSubmissao.APROVADA).count();
                BigDecimal horas = subs.stream()
                    .filter(s -> s.getStatus() == StatusSubmissao.APROVADA && s.getHorasAprovadas() != null)
                    .map(Submissao::getHorasAprovadas)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new MetricaAreaDTO(area, subs.size(), aprovadas, horas);
            })
            .collect(Collectors.toList());
    }
}
