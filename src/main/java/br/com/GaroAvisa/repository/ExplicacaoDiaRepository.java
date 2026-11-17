package br.com.GaroAvisa.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.GaroAvisa.model.ExplicacaoDia;

public interface ExplicacaoDiaRepository extends JpaRepository<ExplicacaoDia, Long> {

    Optional<ExplicacaoDia> findByChave(String chave);

    // O usuario mudou o "Sobre mim": as explicacoes dele nao valem mais
    @Modifying
    @Query("DELETE FROM ExplicacaoDia e WHERE e.usuarioId = :usuarioId")
    int apagarDoUsuario(@Param("usuarioId") Long usuarioId);

    // Limpeza dos textos de dias que ja passaram
    @Modifying
    @Query("DELETE FROM ExplicacaoDia e WHERE e.dia < :aPartirDe")
    int apagarAntesDe(@Param("aPartirDe") LocalDate aPartirDe);
}