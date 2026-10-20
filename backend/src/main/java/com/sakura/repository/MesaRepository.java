package com.sakura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sakura.model.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

	List<Mesa> findAllByOrderByNumero();

	boolean existsByNumero(int numero);

	boolean existsByNumeroAndIdNot(int numero, Long id);
}
