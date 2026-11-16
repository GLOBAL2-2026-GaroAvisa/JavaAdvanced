package br.com.GaroAvisa.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.GaroAvisa.dto.AlertaResponse;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Dia;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Hora;
import br.com.GaroAvisa.model.AlertaClima;
import br.com.GaroAvisa.model.Endereco;
import br.com.GaroAvisa.model.GravidadeAlerta;
import br.com.GaroAvisa.model.TipoAlerta;
import br.com.GaroAvisa.repository.AlertaClimaRepository;
import br.com.GaroAvisa.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Feature 80: gera e le os alertas dos 16 dias de um endereco.
// A regra e uma so; os caminhos sao dois. O local salvo passa pelo consumidor
// do E2 e grava; o lugar pesquisado calcula na hora e nao grava nada.
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaClimaService {

    private final AlertaClimaRepository alertaRepository;
    private final EnderecoRepository enderecoRepository;

    // O que as regras dizem que deveria existir, antes de olhar o banco
    private record AlertaCalculado(TipoAlerta tipo, LocalDate dia, GravidadeAlerta gravidade,
                                   Double valor, Integer primeiraHora) {
    }

    private record Chave(TipoAlerta tipo, LocalDate dia) {
    }

    // Dia mais proximo primeiro e, dentro do dia, o mais grave no topo
    private static final Comparator<AlertaResponse> ORDEM =
            Comparator.comparing(AlertaResponse::dia)
                    .thenComparing(AlertaResponse::gravidade, Comparator.reverseOrder())
                    .thenComparing(AlertaResponse::tipo);

    // ---------- Leitura, para as telas ----------

    // Detalhes do local (feature 15): hoje e os proximos dias
    public List<AlertaResponse> buscarDoEndereco(Long enderecoId, LocalDate aPartirDe) {
        return alertaRepository.buscarDosEnderecos(List.of(enderecoId), aPartirDe).stream()
                .map(AlertaResponse::de)
                .sorted(ORDEM)
                .toList();
    }

    // Home (feature 18): so os alertas de hoje
    public List<AlertaResponse> buscarDeHoje(Long enderecoId, LocalDate hoje) {
        return alertaRepository.buscarDosEnderecos(List.of(enderecoId), hoje).stream()
                .filter(alerta -> alerta.getDia().equals(hoje))
                .map(AlertaResponse::de)
                .sorted(ORDEM)
                .toList();
    }

    // Pesquisa (feature 74): calcula na hora e NAO grava.
    // Lugar nao salvo nao tem historico, entao nao ha o que comparar nem a quem avisar
    public List<AlertaResponse> avaliarDeHoje(PrevisaoAtualizadaEvento evento) {
        LocalDate hoje = LocalDate.now(ZoneId.of(evento.fuso()));
        return calcular(evento).stream()
                .filter(alerta -> alerta.dia().equals(hoje))
                .map(alerta -> AlertaResponse.montar(null, alerta.tipo(), alerta.gravidade(),
                        alerta.dia(), alerta.valor(), alerta.primeiraHora()))
                .sorted(ORDEM)
                .toList();
    }

    // ---------- Escrita, dentro do consumidor do E2 ----------

    /**
     * Concilia o que as regras calcularam com o que ja esta guardado.
     *
     * Devolve, por dia, o alerta mais grave que NASCEU agora, de amanha em
     * diante: e o quarto gatilho da feature 21. O card precisa do tipo e da
     * gravidade para escrever "Novo alerta: tempestade no sabado", por isso
     * devolve o alerta inteiro e nao so a data.
     */
    public Map<LocalDate, AlertaClima> gerar(PrevisaoAtualizadaEvento evento) {
        LocalDate hoje = LocalDate.now(ZoneId.of(evento.fuso()));
        List<AlertaCalculado> calculados = calcular(evento);
        Map<Chave, AlertaClima> existentes = carregarExistentes(evento.enderecoId());

        Set<Chave> vivos = new HashSet<>();
        Map<LocalDate, AlertaClima> alertasNovos = new HashMap<>();
        Endereco endereco = null;

        for (AlertaCalculado calculado : calculados) {
            Chave chave = new Chave(calculado.tipo(), calculado.dia());
            vivos.add(chave);

            AlertaClima existente = existentes.get(chave);

            // Ja existia: so confirma. Reativar NAO conta como novo
            if (existente != null) {
                existente.confirmar(calculado.gravidade(), calculado.valor(),
                        calculado.primeiraHora(), evento.atualizadoEm());
                continue;
            }

            // Referencia sem SELECT, criada so quando ha algo para gravar
            if (endereco == null) {
                endereco = enderecoRepository.getReferenceById(evento.enderecoId());
            }
            AlertaClima novo = alertaRepository.save(new AlertaClima(endereco,
                    calculado.tipo(), calculado.dia(), calculado.gravidade(),
                    calculado.valor(), calculado.primeiraHora(), evento.atualizadoEm()));

            // Alerta novo de HOJE nao vira mudanca brusca: ele ja gera um aviso
            // de origem DIA, e contar os dois seria o mesmo fato duas vezes.
            // Nascendo mais de um no mesmo dia, o card mostra o mais grave
            if (calculado.dia().isAfter(hoje)) {
                AlertaClima atual = alertasNovos.get(calculado.dia());
                if (atual == null || novo.getGravidade().compareTo(atual.getGravidade()) > 0) {
                    alertasNovos.put(calculado.dia(), novo);
                }
            }

            log.info("Alerta novo no endereco {}: {} {} em {} (valor {}, a partir das {}h)",
                    evento.enderecoId(), calculado.tipo(), calculado.gravidade(),
                    calculado.dia(), calculado.valor(), calculado.primeiraHora());
        }

        // O que existia e nao foi calculado agora perdeu a condicao.
        // Desativa, mas NAO apaga: e a linha que lembra que o alerta ja existiu,
        // para ele nao parecer novo se a condicao voltar no ciclo seguinte
        for (Map.Entry<Chave, AlertaClima> entrada : existentes.entrySet()) {
            AlertaClima alerta = entrada.getValue();
            if (!vivos.contains(entrada.getKey()) && alerta.isAtivo()) {
                alerta.desativar(evento.atualizadoEm());
                log.info("Alerta desativado no endereco {}: {} em {}",
                        evento.enderecoId(), alerta.getTipo(), alerta.getDia());
            }
        }

        return alertasNovos;
    }

    // ---------- Regras ----------

    // O que deveria existir para esta previsao. NAO toca no banco:
    // e por isso que a Pesquisa pode usar a mesma regra sem gravar nada
    private List<AlertaCalculado> calcular(PrevisaoAtualizadaEvento evento) {
        Map<LocalDate, List<Hora>> horasPorDia = agruparHoras(evento);
        List<AlertaCalculado> calculados = new ArrayList<>();

        for (Dia dia : evento.dias()) {
            for (TipoAlerta tipo : TipoAlerta.values()) {
                GravidadeAlerta gravidade = avaliar(tipo, dia);
                if (gravidade == null) {
                    continue;
                }
                calculados.add(new AlertaCalculado(tipo, dia.data(), gravidade,
                        valor(tipo, dia),
                        primeiraHora(tipo, gravidade, horasPorDia.get(dia.data()))));
            }
        }
        return calculados;
    }

    private GravidadeAlerta avaliar(TipoAlerta tipo, Dia dia) {
        return switch (tipo) {
            case CHUVA -> RegrasAlerta.chuva(dia.chanceChuvaMaxima());
            case TEMPESTADE -> RegrasAlerta.tempestade(dia.codigoWmo());
            case CALOR -> RegrasAlerta.calor(dia.maxima());
            case FRIO -> RegrasAlerta.frio(dia.minima());
            case RAJADA -> RegrasAlerta.rajada(dia.rajadaMaxima());
            case UV -> RegrasAlerta.uv(dia.uvMaximo());
        };
    }

    // O valor que o card mostra: 92 (%), 35.4 (C), 74.2 (km/h)
    private Double valor(TipoAlerta tipo, Dia dia) {
        return switch (tipo) {
            case CHUVA -> dia.chanceChuvaMaxima() == null ? null : dia.chanceChuvaMaxima().doubleValue();
            case TEMPESTADE -> null; // tempestade nao tem valor, so acontece
            case CALOR -> dia.maxima();
            case FRIO -> dia.minima();
            case RAJADA -> dia.rajadaMaxima();
            case UV -> dia.uvMaximo();
        };
    }

    // ---------- Horario ----------

    // A primeira hora do dia que cruza o limite da gravidade que disparou.
    // Pode ser nula: a agregacao diaria da Open-Meteo e calculada com resolucao
    // mais fina que a serie de hora em hora, entao as duas podem discordar
    private Integer primeiraHora(TipoAlerta tipo, GravidadeAlerta gravidade, List<Hora> horas) {
        if (horas == null) {
            return null;
        }
        for (Hora hora : horas) {
            if (atinge(tipo, gravidade, hora)) {
                return hora.horario().getHour();
            }
        }
        return null;
    }

    private boolean atinge(TipoAlerta tipo, GravidadeAlerta gravidade, Hora hora) {
        if (tipo == TipoAlerta.TEMPESTADE) {
            return hora.codigoWmo() != null && RegrasAlerta.CODIGOS_TEMPESTADE.contains(hora.codigoWmo());
        }

        Double valor = switch (tipo) {
            case CHUVA -> hora.chanceChuva() == null ? null : hora.chanceChuva().doubleValue();
            case CALOR, FRIO -> hora.temperatura();
            case RAJADA -> hora.rajada();
            case UV -> hora.uv();
            case TEMPESTADE -> null;
        };
        if (valor == null) {
            return false;
        }

        double limiar = RegrasAlerta.limiar(tipo, gravidade);
        // No frio a comparacao inverte: a hora precisa CAIR ate o limite
        return tipo == TipoAlerta.FRIO ? valor <= limiar : valor >= limiar;
    }

    // ---------- Auxiliares ----------

    // UMA consulta por endereco. O caminho ingenuo perguntaria ao banco a cada
    // combinacao: 16 dias x 6 tipos = 96 consultas por endereco, de hora em hora
    private Map<Chave, AlertaClima> carregarExistentes(Long enderecoId) {
        Map<Chave, AlertaClima> mapa = new HashMap<>();
        for (AlertaClima alerta : alertaRepository.findByEnderecoId(enderecoId)) {
            mapa.put(new Chave(alerta.getTipo(), alerta.getDia()), alerta);
        }
        return mapa;
    }

    // As horas ja vem em ordem, entao a primeira da lista de um dia e a mais cedo
    private Map<LocalDate, List<Hora>> agruparHoras(PrevisaoAtualizadaEvento evento) {
        return evento.horas().stream()
                .collect(Collectors.groupingBy(hora -> hora.horario().toLocalDate()));
    }
}