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

// Uma mudanca brusca na previsao de UM dia, em UM endereco.
// Vale para todos os locais que usam esse endereco: a comparacao e feita
// uma vez so, e quem amarra a mudanca ao Local e quem monta a tela.
@Entity
@Table(name = "T_GA_MUDANCA_PREVISAO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MudancaPrevisao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mudanca")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_endereco", nullable = false, updatable = false)
    private Endereco endereco;

    // O dia da previsao que mudou
    @Column(name = "dt_dia", nullable = false, updatable = false)
    private LocalDate dia;

    @Column(name = "ds_anterior", nullable = false, length = 60)
    private String anterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_anterior", nullable = false, length = 25)
    private CategoriaClima categoriaAnterior;

    @Column(name = "ds_atualizada", nullable = false, length = 60)
    private String atualizada;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_atual", nullable = false, length = 25)
    private CategoriaClima categoriaAtual;

    @Column(name = "dt_registro", nullable = false)
    private Instant registradoEm;

    public MudancaPrevisao(Endereco endereco, LocalDate dia,
                           String anterior, CategoriaClima categoriaAnterior,
                           String atualizada, CategoriaClima categoriaAtual,
                           Instant registradoEm) {
        this.endereco = endereco;
        this.dia = dia;
        this.anterior = anterior;
        this.categoriaAnterior = categoriaAnterior;
        this.atualizada = atualizada;
        this.categoriaAtual = categoriaAtual;
        this.registradoEm = registradoEm;
    }

    // A previsao daquele dia mudou de novo: o card passa a mostrar a mudanca mais recente
    public void substituir(String anterior, CategoriaClima categoriaAnterior,
                           String atualizada, CategoriaClima categoriaAtual,
                           Instant registradoEm) {
        this.anterior = anterior;
        this.categoriaAnterior = categoriaAnterior;
        this.atualizada = atualizada;
        this.categoriaAtual = categoriaAtual;
        this.registradoEm = registradoEm;
    }
}