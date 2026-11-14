package br.com.GaroAvisa.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// A ultima previsao de UM endereco (uma linha por endereco)
@Entity
@Table(name = "T_GA_PREVISAO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Previsao {

    // A chave e o proprio id do endereco (nao e gerada)
    @Id
    @Column(name = "fk_id_endereco")
    private Long enderecoId;

    // O E2 inteiro, em JSON
    @Lob
    @Column(name = "dados", nullable = false)
    private String dados;

    @Column(name = "dt_atualizacao", nullable = false)
    private Instant atualizadoEm;

    public Previsao(Long enderecoId, String dados, Instant atualizadoEm) {
        this.enderecoId = enderecoId;
        this.dados = dados;
        this.atualizadoEm = atualizadoEm;
    }

    // Substitui a previsao anterior pela nova
    public void atualizar(String dados, Instant atualizadoEm) {
        this.dados = dados;
        this.atualizadoEm = atualizadoEm;
    }
}