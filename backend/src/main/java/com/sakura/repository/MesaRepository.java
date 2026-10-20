package com.sakura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.sakura.model.Mesa;

import jakarta.persistence.LockModeType;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

	List<Mesa> findAllByOrderByNumero();

	/** Bloquea la mesa hasta el final de la transacción: dos meseros no pueden abrir pedido en la misma a la vez. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Mesa> findWithLockById(Long id);

	boolean existsByNumero(int numero);

	boolean existsByNumeroAndIdNot(int numero, Long id);
}
