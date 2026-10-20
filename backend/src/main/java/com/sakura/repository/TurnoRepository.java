package com.sakura.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.sakura.model.Turno;

import jakarta.persistence.LockModeType;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

	List<Turno> findByActivoTrueOrderByHora();

	/** Bloquea el turno hasta el final de la transacción, para que dos reservas no ocupen a la vez el último hueco. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Turno> findByHoraAndActivoTrue(LocalTime hora);
}
