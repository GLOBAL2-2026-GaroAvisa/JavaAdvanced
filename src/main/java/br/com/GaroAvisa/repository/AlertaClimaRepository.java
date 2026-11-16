package br.com.GaroAvisa.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.GaroAvisa.model.AlertaClima;

public interface AlertaClimaRepository extends JpaRepository<AlertaClima, Long> {

    // Todos os alertas do endereco, ativos ou nao, numa consulta so.
    // O motor monta um mapa por (tipo, dia) em memoria e decide dali:
    // o caminho ingenuo faria 16 dias x 6 tipos = 96 consultas por endereco.
    List<AlertaClima> findByEnderecoId(Long enderecoId);

    // Alertas ATIVOS de um ou mais enderecos, de hoje em diante (telas)
    @Query("""
            SELECT a FROM AlertaClima a
            WHERE a.endereco.id IN :enderecoIds
              AND a.dia >= :aPartirDe
              AND a.ativo = true
            ORDER BY a.dia, a.tipo
            """)
    List<AlertaClima> buscarDosEnderecos(@Param("enderecoIds") Collection<Long> enderecoIds,
                                         @Param("aPartirDe") LocalDate aPartirDe);

    // Limpeza dos alertas de dias que ja passaram
    @Modifying
    @Query("DELETE FROM AlertaClima a WHERE a.dia < :aPartirDe")
    int apagarAntesDe(@Param("aPartirDe") LocalDate aPartirDe);
}