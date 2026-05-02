package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import java.math.BigDecimal;
import java.util.List;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    PerfilUsuario perfil,
    List<CursoResponseDTO> cursos,
    Long cursoId,
    String cursoNome,
    long totalAtividades,
    long pendentes,
    long aprovadas,
    long reprovadas,
    BigDecimal horasAprovadas,
    Integer cargaHorariaMinima,
    Double progressoPercentual
) {
}
