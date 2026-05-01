package br.edu.senac.sistema_ac.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponseDTO(
    long totalAlunos,
    long totalSubmissoes,
    long submissoesPendentes,
    long submissoesAprovadas,
    long submissoesReprovadas,
    BigDecimal horasAprovadas,
    List<MetricaCursoDTO> metricasPorCurso,
    List<MetricaAreaDTO> metricasPorArea
) {
}
