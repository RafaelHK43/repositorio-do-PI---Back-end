package br.edu.senac.sistema_ac.config;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.RegraAtividade;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import br.edu.senac.sistema_ac.repository.CursoRepository;
import br.edu.senac.sistema_ac.repository.RegraAtividadeRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final RegraAtividadeRepository regraAtividadeRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedInitialData() {
        return args -> {
            Curso ads = cursoRepository.findByNomeIgnoreCase("ADS")
                .orElseGet(() -> cursoRepository.save(Curso.builder()
                    .nome("ADS")
                    .cargaHorariaMinima(120)
                    .build()));

            if (!Integer.valueOf(120).equals(ads.getCargaHorariaMinima())) {
                ads.setCargaHorariaMinima(120);
                ads = cursoRepository.save(ads);
            }

            criarOuAtualizarUsuarioInicial(
                "Mariana Costa",
                "mariana.costa@gmail.com",
                PerfilUsuario.SUPER_ADMIN,
                List.of("admin@senac.br"),
                List.of()
            );
            criarOuAtualizarUsuarioInicial(
                "Carlos Eduardo Lima",
                "mateuh29@gmail.com",
                PerfilUsuario.COORDENADOR,
                List.of("coordenador@senac.br"),
                List.of(ads)
            );
            criarOuAtualizarUsuarioInicial(
                "Ana Beatriz Santos",
                "ana.beatriz@gmail.com",
                PerfilUsuario.ALUNO,
                List.of("aluno@senac.br"),
                List.of(ads)
            );

            criarRegraSeNaoExistir(ads, AreaAtividade.ENSINO, 40);
            criarRegraSeNaoExistir(ads, AreaAtividade.PESQUISA, 40);
            criarRegraSeNaoExistir(ads, AreaAtividade.EXTENSAO, 40);
            criarRegraSeNaoExistir(ads, AreaAtividade.CULTURA, 20);
            criarRegraSeNaoExistir(ads, AreaAtividade.EVENTOS, 20);
        };
    }

    private Usuario criarOuAtualizarUsuarioInicial(
        String nome,
        String email,
        PerfilUsuario perfil,
        List<String> emailsAntigos,
        List<Curso> cursos
    ) {
        Usuario usuario = buscarUsuarioInicial(email, emailsAntigos, perfil)
            .orElseGet(() -> Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode("123456"))
                .perfil(perfil)
                .cursos(new ArrayList<>())
                .build());

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("123456"));
        usuario.setPerfil(perfil);

        for (Curso curso : cursos) {
            boolean jaVinculado = usuario.getCursos().stream()
                .anyMatch(cursoVinculado -> cursoVinculado.getId().equals(curso.getId()));

            if (!jaVinculado) {
                usuario.getCursos().add(curso);
            }
        }

        return usuarioRepository.save(usuario);
    }

    private Optional<Usuario> buscarUsuarioInicial(String email, List<String> emailsAntigos, PerfilUsuario perfil) {
        Optional<Usuario> usuarioPorEmailNovo = usuarioRepository.findWithCursosByEmail(email);
        if (usuarioPorEmailNovo.isPresent()) {
            return usuarioPorEmailNovo;
        }

        for (String emailAntigo : emailsAntigos) {
            Optional<Usuario> usuarioPorEmailAntigo = usuarioRepository.findWithCursosByEmail(emailAntigo);
            if (usuarioPorEmailAntigo.isPresent()) {
                return usuarioPorEmailAntigo;
            }
        }

        return usuarioRepository.findWithCursosByPerfil(perfil).stream().findFirst();
    }

    private void criarRegraSeNaoExistir(Curso curso, AreaAtividade area, int limiteHoras) {
        BigDecimal limite = BigDecimal.valueOf(limiteHoras);

        regraAtividadeRepository.findByCursoIdAndArea(curso.getId(), area)
            .ifPresentOrElse(
                regra -> {
                    if (regra.getLimiteHoras().compareTo(limite) != 0) {
                        regra.setLimiteHoras(limite);
                        regraAtividadeRepository.save(regra);
                    }
                },
                () -> regraAtividadeRepository.save(RegraAtividade.builder()
                    .curso(curso)
                    .area(area)
                    .limiteHoras(limite)
                    .build())
            );
    }
}
