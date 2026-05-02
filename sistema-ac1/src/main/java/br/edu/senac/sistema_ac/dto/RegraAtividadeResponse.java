package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.entity.RegraAtividade;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import java.math.BigDecimal;

public record RegraAtividadeResponse(
    Long id,
    Long cursoId,
    String cursoNome,
    AreaAtividade area,
    BigDecimal limiteHoras
) {
    public static RegraAtividadeResponse fromEntity(RegraAtividade regra) {
        return new RegraAtividadeResponse(
            regra.getId(),
            regra.getCurso() != null ? regra.getCurso().getId() : null,
            regra.getCurso() != null ? regra.getCurso().getNome() : null,
            regra.getArea(),
            regra.getLimiteHoras()
        );
    }
}
