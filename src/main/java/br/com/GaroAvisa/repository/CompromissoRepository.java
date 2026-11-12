package br.com.GaroAvisa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.GaroAvisa.model.Compromisso;

public interface CompromissoRepository extends JpaRepository<Compromisso, Long> {
}