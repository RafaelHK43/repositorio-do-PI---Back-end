package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.dto.DashboardResponseDTO;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import br.edu.senac.sistema_ac.repository.SubmissaoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final SubmissaoRepository submissaoRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO obterMetrics() {
        long totalAlunos = usuarioRepository.countByPerfil(PerfilUsuario.ALUNO);
        long submissoesPendentes = submissaoRepository.countByStatus(StatusSubmissao.PENDENTE);
        BigDecimal horas = submissaoRepository.somarHorasPorStatus(StatusSubmissao.APROVADA);

        return new DashboardResponseDTO(totalAlunos, submissoesPendentes, horas.longValue());
    }
}
