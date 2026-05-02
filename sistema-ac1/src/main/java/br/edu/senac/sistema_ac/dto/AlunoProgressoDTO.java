package br.edu.senac.sistema_ac.dto;

import java.math.BigDecimal;
import java.util.List;

public record AlunoProgressoDTO(
    BigDecimal totalHorasAprovadas,
    BigDecimal totalHorasPendentes,
    Integer cargaHorariaMinima,
    Double percentualConcluido,
    List<HorasAreaDTO> horasPorArea
) {
}
