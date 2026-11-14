package br.com.GaroAvisa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.GaroAvisa.model.Previsao;

public interface PrevisaoRepository extends JpaRepository<Previsao, Long> {
}