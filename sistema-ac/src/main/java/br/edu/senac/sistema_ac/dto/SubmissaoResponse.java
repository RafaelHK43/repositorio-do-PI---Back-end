package br.edu.senac.sistema_ac.dto;

import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.domain.enums.TipoArquivoComprovante;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubmissaoResponse(
    Long id,
    Long alunoId,
    String alunoNome,
    Long atividadeId,
    String atividadeTitulo,
    String cursoNome,
    StatusSubmissao status,
    BigDecimal horasAprovadas,
    String certificadoUrl,
    String nomeArquivoComprovante,
    TipoArquivoComprovante tipoArquivoComprovante,
    String observacaoCoordenacao,
    LocalDateTime dataSubmissao
) {
    public static SubmissaoResponse fromEntity(Submissao submissao) {
        return new SubmissaoResponse(
            submissao.getId(),
            submissao.getAluno() != null ? submissao.getAluno().getId() : null,
            submissao.getAluno() != null ? submissao.getAluno().getNome() : null,
            submissao.getAtividadeComplementar() != null ? submissao.getAtividadeComplementar().getId() : null,
            submissao.getAtividadeComplementar() != null ? submissao.getAtividadeComplementar().getTitulo() : null,
            submissao.getAtividadeComplementar() != null && submissao.getAtividadeComplementar().getCurso() != null ? submissao.getAtividadeComplementar().getCurso().getNome() : null,
            submissao.getStatus(),
            submissao.getHorasAprovadas(),
            submissao.getCertificadoUrl(),
            submissao.getNomeArquivoComprovante(),
            submissao.getTipoArquivoComprovante(),
            submissao.getObservacaoCoordenacao(),
            submissao.getDataSubmissao()
        );
    }
}
