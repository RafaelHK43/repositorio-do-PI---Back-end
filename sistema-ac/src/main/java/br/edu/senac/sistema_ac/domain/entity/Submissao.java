package br.edu.senac.sistema_ac.domain.entity;

import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.domain.enums.TipoArquivoComprovante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "submissoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;

    @OneToOne(optional = false)
    @JoinColumn(name = "atividade_id", nullable = false, unique = true)
    private AtividadeComplementar atividadeComplementar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSubmissao status;

    @Column(precision = 8, scale = 2)
    private BigDecimal horasAprovadas;

    @Column(length = 255)
    private String certificadoUrl;

    @Column(length = 255)
    private String nomeArquivoComprovante;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoArquivoComprovante tipoArquivoComprovante;

    @Column(length = 1000)
    private String observacaoCoordenacao;

    @Column(nullable = false)
    private LocalDateTime dataSubmissao;

    @PrePersist
    void prePersist() {
        this.dataSubmissao = LocalDateTime.now();
    }
}
