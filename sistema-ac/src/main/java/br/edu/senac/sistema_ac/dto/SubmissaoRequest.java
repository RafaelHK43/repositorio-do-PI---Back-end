package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SubmissaoRequest(
    @NotNull(message = "alunoId e obrigatorio")
    Long alunoId,

    @NotNull(message = "cursoId e obrigatorio")
    Long cursoId,

    @NotBlank(message = "titulo e obrigatorio")
    String titulo,

    String descricao,

    @NotNull(message = "area e obrigatoria")
    AreaAtividade area,

    @NotNull(message = "horasDeclaradas e obrigatorio")
    @DecimalMin(value = "0.1", message = "horasDeclaradas deve ser maior que zero")
    BigDecimal horasDeclaradas,

    @NotNull(message = "dataAtividade e obrigatoria")
    LocalDate dataAtividade
) {
}
