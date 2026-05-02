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
    String alunoEmail,
    Long cursoId,
    String cursoNome,
    AreaAtividade area,
    String titulo,
    String descricao,
    BigDecimal cargaHoraria,
    BigDecimal horasAprovadas,
    StatusSubmissao status,
    String certificadoUrl,
    String nomeArquivoComprovante,
    String resultadoOcr,
    LocalDateTime dataSubmissao,
    LocalDate dataAtividade
) {
    private static final int TAMANHO_MAXIMO_RESULTADO_OCR = 5000;

    public static SubmissaoResponse fromEntity(Submissao submissao) {
        AtividadeComplementar atividade = submissao.getAtividadeComplementar();

        return new SubmissaoResponse(
            submissao.getId(),
            submissao.getAluno() != null ? submissao.getAluno().getId() : null,
            submissao.getAluno() != null ? submissao.getAluno().getNome() : null,
            submissao.getAluno() != null ? submissao.getAluno().getEmail() : null,
            atividade != null && atividade.getCurso() != null ? atividade.getCurso().getId() : null,
            atividade != null && atividade.getCurso() != null ? atividade.getCurso().getNome() : null,
            atividade != null ? atividade.getArea() : null,
            atividade != null ? atividade.getTitulo() : null,
            atividade != null ? atividade.getDescricao() : null,
            atividade != null ? atividade.getHorasDeclaradas() : null,
            submissao.getHorasAprovadas(),
            submissao.getStatus(),
            buildCertificadoUrl(submissao.getCertificadoUrl()),
            submissao.getNomeArquivoComprovante(),
            normalizarResultadoOcr(submissao.getResultadoOcr()),
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

    private static String normalizarResultadoOcr(String resultadoOcr) {
        if (resultadoOcr == null || resultadoOcr.isBlank()) {
            return "OCR não processado";
        }

        String texto = resultadoOcr.trim();
        if ("PDF sem texto extraivel".equalsIgnoreCase(texto) || "PDF sem texto extraível".equalsIgnoreCase(texto)) {
            return "Certificado processado";
        }

        if (texto.length() <= TAMANHO_MAXIMO_RESULTADO_OCR) {
            return texto;
        }

        return texto.substring(0, TAMANHO_MAXIMO_RESULTADO_OCR);
    }
}
