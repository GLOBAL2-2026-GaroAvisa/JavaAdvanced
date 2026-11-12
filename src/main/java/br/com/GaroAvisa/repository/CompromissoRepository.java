package br.com.GaroAvisa.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.GaroAvisa.model.Compromisso;

public interface CompromissoRepository extends JpaRepository<Compromisso, Long> {

    // GET /compromissos: próximos de todos os locais do usuário
    @EntityGraph(attributePaths = "local")
    @Query("""
           SELECT c FROM Compromisso c
           WHERE c.local.usuario.id = :usuarioId
             AND c.data >= :aPartirDe
           ORDER BY c.data ASC, c.id ASC
           """)
    Page<Compromisso> buscarProximosDoUsuario(@Param("usuarioId") Long usuarioId,
                                               @Param("aPartirDe") LocalDate aPartirDe,
                                               Pageable pageable);

    // GET /locais/{localId}/compromissos: próximos de um local
    @EntityGraph(attributePaths = "local")
    @Query("""
           SELECT c FROM Compromisso c
           WHERE c.local.id = :localId
             AND c.data >= :aPartirDe
           ORDER BY c.data ASC, c.id ASC
           """)
    Page<Compromisso> buscarProximosDoLocal(@Param("localId") Long localId,
                                             @Param("aPartirDe") LocalDate aPartirDe,
                                             Pageable pageable);
}