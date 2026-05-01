package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UsuarioRequestDTO(
    @NotBlank(message = "nome e obrigatorio")
    String nome,

    @NotBlank(message = "email e obrigatorio")
    @Email(message = "email deve ser um endereco valido")
    String email,

    String senha,

    @NotNull(message = "perfil e obrigatorio")
    PerfilUsuario perfil,

    List<Long> cursoIds
) {
}
