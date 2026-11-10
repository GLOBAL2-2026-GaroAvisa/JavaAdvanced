package br.com.GaroAvisa.model;

import br.com.GaroAvisa.model.converter.SimNaoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_GA_LOCAL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_local")
    private Long id;

    @Setter
    @Column(name = "apelido", nullable = false, length = 50)
    private String apelido;

    @Setter
    @Convert(converter = SimNaoConverter.class)
    @Column(name = "local_principal", nullable = false, columnDefinition = "CHAR(1)")
    private Boolean principal = false;

    @Setter
    @Convert(converter = SimNaoConverter.class)
    @Column(name = "receber_alertas", nullable = false, columnDefinition = "CHAR(1)")
    private Boolean receberAlertas = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_id_usuario", nullable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_id_endereco", nullable = false, updatable = false)
    private Endereco endereco;

    public Local(Usuario usuario, Endereco endereco, String apelido) {
        this.usuario = usuario;
        this.endereco = endereco;
        this.apelido = apelido;
    }
}