package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN u.cursos c " +
           "WHERE (:perfil IS NULL OR u.perfil = :perfil) " +
           "AND (:cursoId IS NULL OR c.id = :cursoId)")
    List<Usuario> findAllByFiltros(@Param("perfil") PerfilUsuario perfil, @Param("cursoId") Long cursoId);

    boolean existsByCursosId(Long cursoId);

    long countByPerfil(PerfilUsuario perfil);

    default long countByPerfil(String perfil) {
        if (perfil == null) return 0L;
        return countByPerfil(PerfilUsuario.valueOf(perfil));
    }
}
