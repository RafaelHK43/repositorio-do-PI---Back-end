package br.edu.senac.sistema_ac.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "logs_acao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String acao;

    @Column(nullable = false, length = 120)
    private String usuarioEmail;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    void prePersist() {
        this.dataHora = LocalDateTime.now();
    }
}
