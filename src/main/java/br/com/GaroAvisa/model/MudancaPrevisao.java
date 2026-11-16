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
// Vale para todos os locais que usam esse endereco.
//
// O conteudo depende do motivo:
//   CEU, TEMPERATURA, CHUVA -> anterior e atualizada preenchidos
//   ALERTA                  -> tipoAlerta e gravidadeAlerta preenchidos
// Os dois conjuntos nunca convivem: as fabricas garantem isso.
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

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_motivo", nullable = false, length = 15)
    private MotivoMudanca motivo;

    // --- Preenchidos quando o motivo e CEU, TEMPERATURA ou CHUVA ---

    @Column(name = "ds_anterior", length = 60)
    private String anterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_anterior", length = 25)
    private CategoriaClima categoriaAnterior;

    @Column(name = "ds_atualizada", length = 60)
    private String atualizada;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_atual", length = 25)
    private CategoriaClima categoriaAtual;

    // --- Preenchidos quando o motivo e ALERTA ---

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_alerta", length = 15)
    private TipoAlerta tipoAlerta;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravidade_alerta", length = 10)
    private GravidadeAlerta gravidadeAlerta;

    @Column(name = "dt_registro", nullable = false)
    private Instant registradoEm;

    private MudancaPrevisao(Endereco endereco, LocalDate dia, MotivoMudanca motivo) {
        this.endereco = endereco;
        this.dia = dia;
        this.motivo = motivo;
    }

    // CEU, TEMPERATURA ou CHUVA: o card mostra o antes e o depois
    public static MudancaPrevisao deTexto(Endereco endereco, LocalDate dia, MotivoMudanca motivo,
                                          String anterior, CategoriaClima categoriaAnterior,
                                          String atualizada, CategoriaClima categoriaAtual,
                                          Instant registradoEm) {
        MudancaPrevisao mudanca = new MudancaPrevisao(endereco, dia, motivo);
        mudanca.substituirPorTexto(motivo, anterior, categoriaAnterior,
                atualizada, categoriaAtual, registradoEm);
        return mudanca;
    }

    // ALERTA: o card tem texto proprio, sem antes e depois
    public static MudancaPrevisao deAlerta(Endereco endereco, LocalDate dia,
                                           TipoAlerta tipoAlerta, GravidadeAlerta gravidadeAlerta,
                                           Instant registradoEm) {
        MudancaPrevisao mudanca = new MudancaPrevisao(endereco, dia, MotivoMudanca.ALERTA);
        mudanca.substituirPorAlerta(tipoAlerta, gravidadeAlerta, registradoEm);
        return mudanca;
    }

    // A previsao daquele dia mudou de novo: o card passa a mostrar a mais recente.
    // Limpa os campos do outro motivo para a linha nunca ficar meio preenchida
    public void substituirPorTexto(MotivoMudanca motivo,
                                   String anterior, CategoriaClima categoriaAnterior,
                                   String atualizada, CategoriaClima categoriaAtual,
                                   Instant registradoEm) {
        this.motivo = motivo;
        this.anterior = anterior;
        this.categoriaAnterior = categoriaAnterior;
        this.atualizada = atualizada;
        this.categoriaAtual = categoriaAtual;
        this.tipoAlerta = null;
        this.gravidadeAlerta = null;
        this.registradoEm = registradoEm;
    }

    public void substituirPorAlerta(TipoAlerta tipoAlerta, GravidadeAlerta gravidadeAlerta,
                                    Instant registradoEm) {
        this.motivo = MotivoMudanca.ALERTA;
        this.tipoAlerta = tipoAlerta;
        this.gravidadeAlerta = gravidadeAlerta;
        this.anterior = null;
        this.categoriaAnterior = null;
        this.atualizada = null;
        this.categoriaAtual = null;
        this.registradoEm = registradoEm;
    }
}