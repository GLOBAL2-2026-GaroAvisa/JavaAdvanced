package br.com.GaroAvisa.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.Setter;

@Entity
@Table(name = "T_GA_COMPROMISSO")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Compromisso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compromisso")
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "nm_compromisso", nullable = false, length = 80)
    private String nome;

    @Column(name = "dt_compromisso", nullable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_id_local", nullable = false)
    private Local local;

    public Compromisso(Local local, String nome, LocalDate data) {
        this.local = local;
        this.nome = nome;
        this.data = data;
    }
}