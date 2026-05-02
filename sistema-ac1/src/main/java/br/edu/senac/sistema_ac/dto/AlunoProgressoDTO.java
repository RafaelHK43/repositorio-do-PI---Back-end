package br.edu.senac.sistema_ac.dto;

import java.math.BigDecimal;
import java.util.List;

public record AlunoProgressoDTO(
    long totalAtividades,
    long pendentes,
    long aprovadas,
    long reprovadas,
    BigDecimal horasAprovadas,
    Integer cargaHorariaMinima,
    Double progressoPercentual,
    BigDecimal totalHorasAprovadas,
    BigDecimal totalHorasPendentes,
    Double percentualConcluido,
    List<HorasAreaDTO> horasPorArea
) {
}
