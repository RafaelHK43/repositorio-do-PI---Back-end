package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import java.util.List;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    PerfilUsuario perfil,
    List<CursoResponseDTO> cursos
) {
}
