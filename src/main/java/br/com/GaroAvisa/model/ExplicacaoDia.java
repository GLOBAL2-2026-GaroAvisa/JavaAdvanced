package br.com.GaroAvisa.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Feature 23: o texto da Explicacao do Dia guardado para reaproveitar.
// Na Home a chave e por usuario + local + dia; na Pesquisa, por coordenada
// arredondada + dia.
//
// So entram aqui textos vindos da IA. O texto padrao, montado pelas regras,
// nunca e guardado: se fosse, uma falha de rede as 7h congelaria o texto
// ruim ate a meia-noite.
@Entity
@Table(name = "T_GA_EXPLICACAO_DIA")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExplicacaoDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_explicacao")
    private Long id;

    @Column(name = "ds_chave", nullable = false, length = 120, updatable = false)
    private String chave;

    // Id cru, nao relacionamento: a entidade nunca navega ate o Usuario,
    // so precisa ser apagada por ele. Nulo na Pesquisa
    @Column(name = "fk_id_usuario", updatable = false)
    private Long usuarioId;

    @Column(name = "dt_dia", nullable = false, updatable = false)
    private LocalDate dia;

    @Column(name = "ds_texto", nullable = false, length = 1000)
    private String texto;

    @Column(name = "dt_criacao", nullable = false)
    private Instant criadoEm;

    public ExplicacaoDia(String chave, Long usuarioId, LocalDate dia,
                         String texto, Instant criadoEm) {
        this.chave = chave;
        this.usuarioId = usuarioId;
        this.dia = dia;
        this.texto = texto;
        this.criadoEm = criadoEm;
    }
}