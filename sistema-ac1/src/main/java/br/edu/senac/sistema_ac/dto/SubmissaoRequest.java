package br.edu.senac.sistema_ac.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubmissaoRequest(
    Long alunoId,
    Long cursoId,
    String titulo,
    String descricao,
    Object area,
    BigDecimal cargaHoraria,
    LocalDate dataAtividade,

    Long studentId,
    String title,
    Object areaId,
    BigDecimal workload,
    LocalDate activityDate,
    String description
) {
    public Long getAlunoId() {
        return alunoId != null ? alunoId : studentId;
    }

    public String getTitulo() {
        return primeiroTexto(titulo, title);
    }

    public Object getArea() {
        return area != null ? area : areaId;
    }

    public BigDecimal getCargaHoraria() {
        return cargaHoraria != null ? cargaHoraria : workload;
    }

    public LocalDate getDataAtividade() {
        return dataAtividade != null ? dataAtividade : activityDate;
    }

    public String getDescricao() {
        return primeiroTexto(descricao, description);
    }

    private static String primeiroTexto(String novo, String antigo) {
        if (novo != null) {
            return novo;
        }
        return antigo;
    }
}
