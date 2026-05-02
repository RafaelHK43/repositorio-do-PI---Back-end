package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.dto.AlunoProgressoDTO;
import br.edu.senac.sistema_ac.dto.DashboardResponseDTO;
import br.edu.senac.sistema_ac.dto.HorasAreaDTO;
import br.edu.senac.sistema_ac.dto.MetricaAreaDTO;
import br.edu.senac.sistema_ac.dto.MetricaCursoDTO;
import br.edu.senac.sistema_ac.exception.RecursoNaoEncontradoException;
import br.edu.senac.sistema_ac.repository.SubmissaoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Transactional(readOnly = true)
    public AlunoProgressoDTO obterProgressoAluno(Long alunoId) {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailLogado)
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado nao encontrado"));

        if (usuarioLogado.getPerfil() == PerfilUsuario.ALUNO && !usuarioLogado.getId().equals(alunoId)) {
            throw new AccessDeniedException("Acesso negado: aluno so pode consultar o proprio progresso");
        }

        Usuario aluno = usuarioRepository.findById(alunoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno nao encontrado"));

        Integer cargaHorariaMinima = aluno.getCursos().stream()
            .findFirst()
            .map(Curso::getCargaHorariaMinima)
            .orElse(0);

        List<Submissao> submissoes = submissaoRepository.findAllByAlunoIdOrderByDataSubmissaoDesc(alunoId);

        BigDecimal totalHorasAprovadas = submissoes.stream()
            .filter(s -> s.getStatus() == StatusSubmissao.APROVADA && s.getHorasAprovadas() != null)
            .map(Submissao::getHorasAprovadas)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHorasPendentes = submissoes.stream()
            .filter(s -> s.getStatus() == StatusSubmissao.PENDENTE && s.getAtividadeComplementar() != null)
            .map(s -> s.getAtividadeComplementar().getHorasDeclaradas())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        double percentualConcluido = cargaHorariaMinima > 0
            ? totalHorasAprovadas.multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(cargaHorariaMinima), 2, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100))
                .doubleValue()
            : 0.0;

        Map<AreaAtividade, List<Submissao>> porArea = submissoes.stream()
            .filter(s -> s.getAtividadeComplementar() != null)
            .collect(Collectors.groupingBy(s -> s.getAtividadeComplementar().getArea()));

        List<HorasAreaDTO> horasPorArea = Arrays.stream(AreaAtividade.values())
            .map(area -> {
                List<Submissao> subs = porArea.getOrDefault(area, List.of());
                BigDecimal aprovadas = subs.stream()
                    .filter(s -> s.getStatus() == StatusSubmissao.APROVADA && s.getHorasAprovadas() != null)
                    .map(Submissao::getHorasAprovadas)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal pendentes = subs.stream()
                    .filter(s -> s.getStatus() == StatusSubmissao.PENDENTE)
                    .map(s -> s.getAtividadeComplementar().getHorasDeclaradas())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new HorasAreaDTO(area, aprovadas, pendentes);
            })
            .collect(Collectors.toList());

        return new AlunoProgressoDTO(
            totalHorasAprovadas,
            totalHorasPendentes,
            cargaHorariaMinima,
            percentualConcluido,
            horasPorArea
        );
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
