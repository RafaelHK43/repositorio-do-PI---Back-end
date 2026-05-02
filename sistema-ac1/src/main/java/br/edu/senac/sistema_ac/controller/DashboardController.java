package br.edu.senac.sistema_ac.controller;

import br.edu.senac.sistema_ac.dto.AlunoProgressoDTO;
import br.edu.senac.sistema_ac.dto.DashboardResponseDTO;
import br.edu.senac.sistema_ac.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponseDTO obterMetrics() {
        return dashboardService.obterMetrics();
    }

    @GetMapping("/aluno/{id}")
    public AlunoProgressoDTO obterProgressoAluno(@PathVariable Long id) {
        return dashboardService.obterProgressoAluno(id);
    }
}
