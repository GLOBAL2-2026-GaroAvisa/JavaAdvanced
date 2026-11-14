package br.com.GaroAvisa.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.GaroAvisa.evento.EnderecoParaPrevisao;
import br.com.GaroAvisa.model.Endereco;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    Optional<Endereco> findByLatitudeAndLongitude(BigDecimal latitude, BigDecimal longitude);
    
 // Endereços com pelo menos um Local (de qualquer usuário), já no formato da mensagem
    @Query("""
            SELECT new br.com.GaroAvisa.evento.EnderecoParaPrevisao(e.id, e.latitude, e.longitude, e.fuso)
            FROM Endereco e
            WHERE EXISTS (SELECT 1 FROM Local l WHERE l.endereco = e)
            ORDER BY e.id
            """)
    List<EnderecoParaPrevisao> listarEmUsoParaPrevisao();
}