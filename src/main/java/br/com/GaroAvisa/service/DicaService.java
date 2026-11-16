package br.com.GaroAvisa.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.GaroAvisa.dto.DicaResponse;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Dia;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Hora;
import br.com.GaroAvisa.model.TipoDica;

// Feature 20: o que levar ou usar hoje, no local principal.
// Calculadas na leitura, nao guardadas: valem so para hoje, aparecem so na
// Home e nao tem estado nenhum - sao funcao pura do clima do dia
@Service
public class DicaService {

    public List<DicaResponse> calcular(PrevisaoAtualizadaEvento previsao, LocalDate dia) {
        Dia hoje = diaDe(previsao, dia);
        if (hoje == null) {
            return List.of();
        }
        List<Hora> horas = horasDe(previsao, dia);
        List<DicaResponse> dicas = new ArrayList<>();

        if (guardaChuva(horas)) {
            dicas.add(DicaResponse.de(TipoDica.GUARDA_CHUVA));
        }
        if (uvAlto(hoje)) {
            dicas.add(DicaResponse.de(TipoDica.PROTETOR_SOLAR));
        }
        if (oculosEscuros(hoje)) {
            dicas.add(DicaResponse.de(TipoDica.OCULOS_ESCUROS));
        }
        if (casaco(hoje)) {
            dicas.add(DicaResponse.de(TipoDica.CASACO));
        }
        if (garrafaAgua(hoje)) {
            dicas.add(DicaResponse.de(TipoDica.GARRAFA_AGUA));
        }
        return dicas;
    }

    // ---------- Regras ----------

    // Basta UMA hora entre 7h e 22h com chance de chuva no limite.
    // De madrugada nao conta: a pessoa nao vai estar na rua
    private boolean guardaChuva(List<Hora> horas) {
        for (Hora hora : horas) {
            int h = hora.horario().getHour();
            boolean dentroDaJanela = h >= RegrasDica.GUARDA_CHUVA_HORA_INICIO
                    && h <= RegrasDica.GUARDA_CHUVA_HORA_FIM;

            if (dentroDaJanela && hora.chanceChuva() != null
                    && hora.chanceChuva() >= RegrasDica.GUARDA_CHUVA_CHANCE) {
                return true;
            }
        }
        return false;
    }

    private boolean uvAlto(Dia dia) {
        return dia.uvMaximo() != null && dia.uvMaximo() >= RegrasDica.UV_MINIMO;
    }

    // Oculos so faz sentido com UV alto E pouca chuva: num dia fechado
    // o sol nao incomoda, por mais alto que o indice fique no pico
    private boolean oculosEscuros(Dia dia) {
        return uvAlto(dia)
                && dia.chanceChuvaMaxima() != null
                && dia.chanceChuvaMaxima() < RegrasDica.POUCA_CHUVA;
    }

    // Minima baixa OU variacao grande: o segundo caso pega o dia que comeca
    // quente e desaba a tarde, em que a pessoa sai sem casaco e se arrepende
    private boolean casaco(Dia dia) {
        if (dia.minima() == null) {
            return false;
        }
        if (dia.minima() <= RegrasDica.CASACO_MINIMA) {
            return true;
        }
        return dia.maxima() != null
                && (dia.maxima() - dia.minima()) >= RegrasDica.CASACO_VARIACAO;
    }

    private boolean garrafaAgua(Dia dia) {
        return dia.maxima() != null && dia.maxima() >= RegrasDica.AGUA_MAXIMA;
    }

    // ---------- Auxiliares ----------

    private Dia diaDe(PrevisaoAtualizadaEvento previsao, LocalDate dia) {
        return previsao.dias().stream()
                .filter(d -> dia.equals(d.data()))
                .findFirst()
                .orElse(null);
    }

    private List<Hora> horasDe(PrevisaoAtualizadaEvento previsao, LocalDate dia) {
        return previsao.horas().stream()
                .filter(hora -> dia.equals(hora.horario().toLocalDate()))
                .toList();
    }
}