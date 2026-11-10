package br.com.GaroAvisa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.GaroAvisa.model.Local;

public interface LocalRepository extends JpaRepository<Local, Long> {

    @EntityGraph(attributePaths = "endereco")
    @Query("""
           SELECT l FROM Local l JOIN l.endereco e
           WHERE l.usuario.id = :usuarioId
             AND (:busca IS NULL
                  OR UPPER(l.apelido) LIKE UPPER(CONCAT('%', :busca, '%'))
                  OR UPPER(e.bairro)  LIKE UPPER(CONCAT('%', :busca, '%')))
           ORDER BY l.principal DESC, l.apelido
           """)
    Page<Local> buscarDoUsuario(@Param("usuarioId") Long usuarioId,
                                @Param("busca") String busca,
                                Pageable pageable);
    
    
 // POST /locais: o primeiro local vira principal
    long countByUsuarioId(Long usuarioId);

    // POST /locais: apelido único por usuário
    boolean existsByUsuarioIdAndApelido(Long usuarioId, String apelido);

}