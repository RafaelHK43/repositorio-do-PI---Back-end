package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;

public record AuthResponse(
    Long id,
    String nome,
    String email,
    PerfilUsuario perfil
) {
}
