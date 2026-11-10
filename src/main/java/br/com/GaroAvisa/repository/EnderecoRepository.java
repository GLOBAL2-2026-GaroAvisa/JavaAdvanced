package br.com.GaroAvisa.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.GaroAvisa.model.Endereco;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    Optional<Endereco> findByLatitudeAndLongitude(BigDecimal latitude, BigDecimal longitude);
}