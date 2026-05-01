package br.edu.senac.sistema_ac.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
    @NotBlank(message = "email e obrigatorio")
    @Email(message = "email invalido")
    String email,

    @NotBlank(message = "senha e obrigatoria")
    String senha
) {
}