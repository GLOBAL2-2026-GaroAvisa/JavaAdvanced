package br.com.GaroAvisa.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.GaroAvisa.model.MudancaPrevisao;

public interface MudancaPrevisaoRepository extends JpaRepository<MudancaPrevisao, Long> {

    // Ja existe uma mudanca registrada para este endereco neste dia?
    Optional<MudancaPrevisao> findByEnderecoIdAndDia(Long enderecoId, LocalDate dia);

    // Mudancas de um ou mais enderecos, so de hoje em diante.
    // Serve aos Detalhes (um endereco) e a Home (os enderecos de todos os locais).
    // ATENCAO: nao chamar com a colecao vazia, o IN () quebra no Oracle.
    @Query("""
            SELECT m FROM MudancaPrevisao m
            WHERE m.endereco.id IN :enderecoIds AND m.dia >= :aPartirDe
            ORDER BY m.dia
            """)
    List<MudancaPrevisao> buscarDosEnderecos(@Param("enderecoIds") Collection<Long> enderecoIds,
                                             @Param("aPartirDe") LocalDate aPartirDe);

    // Limpeza das mudancas de dias que ja passaram
    @Modifying
    @Query("DELETE FROM MudancaPrevisao m WHERE m.dia < :aPartirDe")
    int apagarAntesDe(@Param("aPartirDe") LocalDate aPartirDe);
}