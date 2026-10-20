package com.sakura.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sakura.model.EstadoReserva;
import com.sakura.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

	Optional<Reserva> findByCodigo(String codigo);

	boolean existsByCodigo(String codigo);

	List<Reserva> findByFechaOrderByHoraAscCreadaEnAsc(LocalDate fecha);

	/** Personas ya sentadas en un turno, sin contar las reservas canceladas. */
	@Query("""
			select coalesce(sum(r.personas), 0) from Reserva r
			where r.fecha = :fecha and r.hora = :hora and r.estado <> :cancelada""")
	long personasEnTurno(LocalDate fecha, LocalTime hora, EstadoReserva cancelada);

	/** Personas por turno de un día, sin contar las canceladas. Cada fila es [hora, personas]. */
	@Query("""
			select r.hora, sum(r.personas) from Reserva r
			where r.fecha = :fecha and r.estado <> :cancelada
			group by r.hora""")
	List<Object[]> personasPorTurno(LocalDate fecha, EstadoReserva cancelada);
}
