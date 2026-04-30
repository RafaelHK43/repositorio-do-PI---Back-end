package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import java.util.List;

public record UsuarioRequestDTO(
    String nome,
    String email,
    String senha,
    PerfilUsuario perfil,
    List<Long> cursoIds
) {
}
