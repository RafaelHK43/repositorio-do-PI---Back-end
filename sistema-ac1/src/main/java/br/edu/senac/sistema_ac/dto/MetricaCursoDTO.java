package br.edu.senac.sistema_ac.dto;

import java.math.BigDecimal;

public record MetricaCursoDTO(
    Long cursoId,
    String cursoNome,
    long totalSubmissoes,
    long pendentes,
    long submissoesAprovadas,
    long submissoesReprovadas,
    BigDecimal horasAprovadas
) {
}
