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

            criarUsuarioSeNaoExistir("Super Admin", "admin@senac.br", PerfilUsuario.SUPER_ADMIN, List.of());
            criarUsuarioSeNaoExistir("Coordenador", "coordenador@senac.br", PerfilUsuario.COORDENADOR, List.of(ads));
            criarUsuarioSeNaoExistir("Aluno", "aluno@senac.br", PerfilUsuario.ALUNO, List.of(ads));

            criarRegraSeNaoExistir(ads, AreaAtividade.ENSINO, 40);
            criarRegraSeNaoExistir(ads, AreaAtividade.PESQUISA, 40);
            criarRegraSeNaoExistir(ads, AreaAtividade.EXTENSAO, 40);
            criarRegraSeNaoExistir(ads, AreaAtividade.CULTURA, 20);
            criarRegraSeNaoExistir(ads, AreaAtividade.EVENTOS, 20);
        };
    }

    private Usuario criarUsuarioSeNaoExistir(String nome, String email, PerfilUsuario perfil, List<Curso> cursos) {
        Usuario usuario = usuarioRepository.findWithCursosByEmail(email)
            .orElseGet(() -> Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode("123456"))
                .perfil(perfil)
                .cursos(new ArrayList<>())
                .build());

        usuario.setNome(nome);
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
