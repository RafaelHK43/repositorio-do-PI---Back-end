package br.edu.senac.sistema_ac.config;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import br.edu.senac.sistema_ac.repository.CursoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitialDataConfig {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsuariosPadrao() {
        return args -> {
            Curso ads = cursoRepository.findByNomeIgnoreCase("ADS")
                .orElseGet(() -> cursoRepository.save(Curso.builder().nome("ADS").build()));

            criarUsuarioSeNaoExistir("superadmin@senac.br", "Super Admin", PerfilUsuario.SUPER_ADMIN, ads);
            criarUsuarioSeNaoExistir("coordenador@senac.br", "Coordenador", PerfilUsuario.COORDENADOR, ads);
            criarUsuarioSeNaoExistir("aluno@senac.br", "Aluno", PerfilUsuario.ALUNO, ads);
        };
    }

    private void criarUsuarioSeNaoExistir(String email, String nome, PerfilUsuario perfil, Curso curso) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return;
        }

        Usuario usuario = Usuario.builder()
            .email(email)
            .nome(nome)
            .perfil(perfil)
            .curso(curso)
            .senha(passwordEncoder.encode("123456"))
            .build();

        usuarioRepository.save(usuario);
    }
}
