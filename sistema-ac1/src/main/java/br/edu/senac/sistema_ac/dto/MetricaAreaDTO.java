package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import java.math.BigDecimal;

public record MetricaAreaDTO(
    AreaAtividade area,
    long totalSubmissoes,
    long submissoesAprovadas,
    BigDecimal horasAprovadas
) {
}
