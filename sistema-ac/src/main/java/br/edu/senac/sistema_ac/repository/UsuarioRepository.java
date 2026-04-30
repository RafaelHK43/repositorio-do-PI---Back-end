package br.edu.senac.sistema_ac.repository;

import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByCursosId(Long cursoId);

    long countByPerfil(PerfilUsuario perfil);

    default long countByPerfil(String perfil) {
        if (perfil == null) return 0L;
        return countByPerfil(PerfilUsuario.valueOf(perfil));
    }
}
