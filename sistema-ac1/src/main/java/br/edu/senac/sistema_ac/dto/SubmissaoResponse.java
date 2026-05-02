package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.entity.AtividadeComplementar;
import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubmissaoResponse(
    Long id,
    Long alunoId,
    String alunoNome,
    Long cursoId,
    String cursoNome,
    AreaAtividade area,
    String titulo,
    String descricao,
    BigDecimal cargaHoraria,
    StatusSubmissao status,
    String certificadoUrl,
    String nomeArquivoComprovante,
    LocalDateTime dataSubmissao,
    LocalDate dataAtividade
) {
    public static SubmissaoResponse fromEntity(Submissao submissao) {
        AtividadeComplementar atividade = submissao.getAtividadeComplementar();

        return new SubmissaoResponse(
            submissao.getId(),
            submissao.getAluno() != null ? submissao.getAluno().getId() : null,
            submissao.getAluno() != null ? submissao.getAluno().getNome() : null,
            atividade != null && atividade.getCurso() != null ? atividade.getCurso().getId() : null,
            atividade != null && atividade.getCurso() != null ? atividade.getCurso().getNome() : null,
            atividade != null ? atividade.getArea() : null,
            atividade != null ? atividade.getTitulo() : null,
            atividade != null ? atividade.getDescricao() : null,
            atividade != null ? atividade.getHorasDeclaradas() : null,
            submissao.getStatus(),
            buildCertificadoUrl(submissao.getCertificadoUrl()),
            submissao.getNomeArquivoComprovante(),
            submissao.getDataSubmissao(),
            atividade != null ? atividade.getDataAtividade() : null
        );
    }

    private static String buildCertificadoUrl(String certificadoUrl) {
        if (certificadoUrl == null || certificadoUrl.isBlank()) {
            return null;
        }
        int lastSep = Math.max(certificadoUrl.lastIndexOf('/'), certificadoUrl.lastIndexOf('\\'));
        String filename = lastSep >= 0 ? certificadoUrl.substring(lastSep + 1) : certificadoUrl;
        return "/api/uploads/" + filename;
    }
}
