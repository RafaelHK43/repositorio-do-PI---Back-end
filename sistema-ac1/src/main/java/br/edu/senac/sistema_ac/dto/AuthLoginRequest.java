package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthLoginRequest(
    @NotBlank(message = "email e obrigatorio")
    @Email(message = "email invalido")
    String email,

    @NotBlank(message = "senha e obrigatoria")
    String senha,

    @NotNull(message = "perfil e obrigatorio")
    PerfilUsuario perfil
) {
}