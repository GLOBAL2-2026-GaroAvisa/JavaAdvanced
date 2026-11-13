package br.com.GaroAvisa.model;

import br.com.GaroAvisa.model.converter.SimNaoConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_GA_USUARIO")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nm_usuario", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, length = 10)
    private TipoUsuario tipo = TipoUsuario.USUARIO;
    
    @Column(name = "perfil_ia", length = 200)
    private String perfilIa;

    // Configurações de notificação
    @Convert(converter = SimNaoConverter.class)
    @Column(name = "resumo_diario_ativo", nullable = false, columnDefinition = "CHAR(1)")
    private Boolean resumoDiarioAtivo = true;

    @Column(name = "resumo_diario_horario", nullable = false, length = 5)
    private String resumoDiarioHorario = "07:00";

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "alertas_do_dia", nullable = false, columnDefinition = "CHAR(1)")
    private Boolean alertasDoDia = true;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "previsao_mudou", nullable = false, columnDefinition = "CHAR(1)")
    private Boolean previsaoMudou = true;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "compromissos", nullable = false, columnDefinition = "CHAR(1)")
    private Boolean compromissos = true;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "silencio_noturno", nullable = false, columnDefinition = "CHAR(1)")
    private Boolean silencioNoturno = true;
}