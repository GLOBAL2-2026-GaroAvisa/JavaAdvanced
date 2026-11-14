package br.com.GaroAvisa.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_GA_ENDERECO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // exigido pelo JPA
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_endereco")
    private Long id;

    @Column(name = "pais", nullable = false, length = 50)
    private String pais;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "cidade", nullable = false, length = 60)
    private String cidade;

    @Column(name = "nome", nullable = false, length = 60)
    private String nome;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "fuso", nullable = false, length = 50)
    private String fuso;

    public Endereco(String pais, String estado, String cidade, String bairro,
                    BigDecimal latitude, BigDecimal longitude, String fuso) {
        this.pais = pais;
        this.estado = estado;
        this.cidade = cidade;
        this.nome = bairro;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fuso = fuso;
    }
}