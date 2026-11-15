package br.com.GaroAvisa.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.GaroAvisa.model.MudancaPrevisao;

public interface MudancaPrevisaoRepository extends JpaRepository<MudancaPrevisao, Long> {

    // Ja existe uma mudanca registrada para este local neste dia?
    Optional<MudancaPrevisao> findByLocalIdAndDia(Long localId, LocalDate dia);

    // Mudancas de UM local (tela Detalhes), so de hoje em diante
    @EntityGraph(attributePaths = "local")
    @Query("""
            SELECT m FROM MudancaPrevisao m
            WHERE m.local.id = :localId AND m.dia >= :aPartirDe
            ORDER BY m.dia
            """)
    List<MudancaPrevisao> buscarDoLocal(@Param("localId") Long localId,
                                        @Param("aPartirDe") LocalDate aPartirDe);

    // Mudancas de TODOS os locais do usuario (Home), so de hoje em diante
    @EntityGraph(attributePaths = "local")
    @Query("""
            SELECT m FROM MudancaPrevisao m
            WHERE m.local.usuario.id = :usuarioId AND m.dia >= :aPartirDe
            ORDER BY m.dia
            """)
    List<MudancaPrevisao> buscarDoUsuario(@Param("usuarioId") Long usuarioId,
                                          @Param("aPartirDe") LocalDate aPartirDe);

    // Limpeza das mudancas de dias que ja passaram
    @Modifying
    @Query("DELETE FROM MudancaPrevisao m WHERE m.dia < :aPartirDe")
    int apagarAntesDe(@Param("aPartirDe") LocalDate aPartirDe);
}