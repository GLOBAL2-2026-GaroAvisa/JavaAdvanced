package br.com.GaroAvisa.repository;

import java.util.List;
import java.util.Optional;

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
    
    
    @Query("""
    		SELECT l FROM Local l
    		WHERE l.usuario.id = :usuarioId
    		ORDER BY l.id ASC
    		""")
    List<Local> listarPorUsuario(Long usuarioId);
    
    @Query("""
    		SELECT l FROM Local l
    		WHERE l.usuario.id = :usuarioId AND l.id = :localId
    		ORDER BY l.principal ASC
    		""")
    Optional<Local> findByIdAndUsuarioId(Long usuarioId, Long localId);
    
    
    // PUT /locais/{id}: apelido único, ignorando o próprio local
    boolean existsByUsuarioIdAndApelidoAndIdNot(Long usuarioId, String apelido, Long id);

    // PUT /locais/{id}: encontra o principal atual para desmarcar
    Optional<Local> findByUsuarioIdAndPrincipal(Long usuarioId, Boolean principal);

}