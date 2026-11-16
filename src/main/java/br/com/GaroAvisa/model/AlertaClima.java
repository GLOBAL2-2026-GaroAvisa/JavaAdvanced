package br.com.GaroAvisa.model;

import java.time.Instant;
import java.time.LocalDate;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Um alerta de clima de UM dia, em UM endereco.
// Vale para todos os locais que usam esse endereco.
@Entity
@Table(name = "T_GA_ALERTA_CLIMA")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlertaClima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_endereco", nullable = false, updatable = false)
    private Endereco endereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_alerta", nullable = false, length = 15, updatable = false)
    private TipoAlerta tipo;

    // O dia da previsao a que o alerta se refere
    @Column(name = "dt_dia", nullable = false, updatable = false)
    private LocalDate dia;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravidade", nullable = false, length = 10)
    private GravidadeAlerta gravidade;

    // O valor que disparou: 92 (%), 35.4 (graus), 11.3 (UV), 74.2 (km/h)
    @Column(name = "vl_atingido")
    private Double valor;

    // A primeira hora do dia que atinge o limite (0 a 23). Nula se nenhuma atingir
    @Column(name = "hr_primeira")
    private Integer primeiraHora;

    @Column(name = "st_ativo", nullable = false)
    private boolean ativo;

    @Column(name = "dt_atualizacao", nullable = false)
    private Instant atualizadoEm;

    public AlertaClima(Endereco endereco, TipoAlerta tipo, LocalDate dia,
                       GravidadeAlerta gravidade, Double valor, Integer primeiraHora,
                       Instant atualizadoEm) {
        this.endereco = endereco;
        this.tipo = tipo;
        this.dia = dia;
        this.gravidade = gravidade;
        this.valor = valor;
        this.primeiraHora = primeiraHora;
        this.ativo = true;
        this.atualizadoEm = atualizadoEm;
    }

    // A previsao nova confirma o alerta: o valor, a gravidade e a hora podem ter mudado
    public void confirmar(GravidadeAlerta gravidade, Double valor, Integer primeiraHora,
                          Instant atualizadoEm) {
        this.gravidade = gravidade;
        this.valor = valor;
        this.primeiraHora = primeiraHora;
        this.ativo = true;
        this.atualizadoEm = atualizadoEm;
    }

    // A condicao deixou de ser atendida. A linha NAO e apagada de proposito:
    // e ela que lembra que esse alerta ja existiu, para ele nao parecer novo
    // se a condicao voltar no ciclo seguinte.
    public void desativar(Instant atualizadoEm) {
        this.ativo = false;
        this.atualizadoEm = atualizadoEm;
    }
}