package br.edu.senac.sistema_ac.config;

import br.edu.senac.sistema_ac.domain.entity.AtividadeComplementar;
import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.repository.AtividadeComplementarRepository;
import br.edu.senac.sistema_ac.repository.CursoRepository;
import br.edu.senac.sistema_ac.repository.SubmissaoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final AtividadeComplementarRepository atividadeComplementarRepository;
    private final SubmissaoRepository submissaoRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedInitialData() {
        return args -> {
            if (usuarioRepository.count() == 0) {
                Curso curso = cursoRepository.findByNomeIgnoreCase("Análise e Desenvolvimento de Sistemas (ADS)")
                    .orElseGet(() -> cursoRepository.save(Curso.builder()
                        .nome("Análise e Desenvolvimento de Sistemas (ADS)")
                        .cargaHorariaMinima(120)
                        .build()));

                Usuario coordenador = usuarioRepository.save(Usuario.builder()
                    .nome("Coordenador Geral")
                    .email("coordenador@teste.com")
                    .senha(passwordEncoder.encode("123456"))
                    .perfil(PerfilUsuario.COORDENADOR)
                    .build());

                Usuario aluno = usuarioRepository.save(Usuario.builder()
                    .nome("Gustavo Moura")
                    .email("gustavobmoura3z9@gmail.com")
                    .senha(passwordEncoder.encode("123456"))
                    .perfil(PerfilUsuario.ALUNO)
                    .cursos(List.of(curso))
                    .build());

                AtividadeComplementar atividade = atividadeComplementarRepository.save(AtividadeComplementar.builder()
                    .titulo("Atividade de apresentação do SGAC")
                    .descricao("Seed inicial para testes e demonstração do sistema.")
                    .area(AreaAtividade.ENSINO)
                    .horasDeclaradas(BigDecimal.valueOf(40))
                    .dataAtividade(LocalDate.now())
                    .aluno(aluno)
                    .curso(curso)
                    .build());

                submissaoRepository.save(Submissao.builder()
                    .aluno(aluno)
                    .atividadeComplementar(atividade)
                    .status(StatusSubmissao.PENDENTE)
                    .horasAprovadas(BigDecimal.valueOf(40))
                    .dataSubmissao(LocalDateTime.now())
                    .build());
            }
        };
    }
}
