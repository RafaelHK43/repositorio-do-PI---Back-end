package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record SubmissaoUpdateRequest(
    @NotNull(message = "status e obrigatorio")
    StatusSubmissao status,

    @DecimalMin(value = "0.1", message = "horasAprovadas deve ser maior que zero")
    BigDecimal horasAprovadas,

    @Size(max = 1000, message = "observacaoCoordenacao deve ter no maximo 1000 caracteres")
    String observacaoCoordenacao
) {
}