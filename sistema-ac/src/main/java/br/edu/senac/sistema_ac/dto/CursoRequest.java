package br.edu.senac.sistema_ac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CursoRequest(
    @NotBlank(message = "Nome do curso e obrigatorio")
    @Size(max = 120, message = "Nome do curso deve ter no maximo 120 caracteres")
    String nome
) {
}
