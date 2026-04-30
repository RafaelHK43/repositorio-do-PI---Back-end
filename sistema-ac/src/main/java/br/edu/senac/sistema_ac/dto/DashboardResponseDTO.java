package br.edu.senac.sistema_ac.dto;

public record DashboardResponseDTO(
    Long totalAlunos,
    Long submissoesPendentes,
    Long horasAprovadas
) {
}
