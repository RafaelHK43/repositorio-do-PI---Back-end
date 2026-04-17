package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegraAtividadeRequest(
    @NotNull(message = "cursoId e obrigatorio")
    Long cursoId,

    @NotNull(message = "area e obrigatoria")
    AreaAtividade area,

    @NotNull(message = "limiteHoras e obrigatorio")
    @DecimalMin(value = "0.1", message = "limiteHoras deve ser maior que zero")
    BigDecimal limiteHoras
) {
}
