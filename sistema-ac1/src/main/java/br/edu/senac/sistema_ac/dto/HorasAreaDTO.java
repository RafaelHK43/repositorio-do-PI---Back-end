package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import java.math.BigDecimal;

public record HorasAreaDTO(
    AreaAtividade area,
    BigDecimal horasAprovadas,
    BigDecimal horasPendentes
) {
}
