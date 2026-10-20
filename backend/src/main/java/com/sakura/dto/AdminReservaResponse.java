package com.sakura.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sakura.model.EstadoReserva;
import com.sakura.model.Reserva;

/** La reserva completa, para el panel del restaurante. */
public record AdminReservaResponse(
		Long id,
		String codigo,
		LocalDate fecha,
		@JsonFormat(pattern = "HH:mm") LocalTime hora,
		int personas,
		String nombre,
		String email,
		String telefono,
		String notas,
		EstadoReserva estado,
		OffsetDateTime creadaEn,
		Long mesaId,
		Integer mesa) {

	public static AdminReservaResponse de(Reserva r) {
		return new AdminReservaResponse(r.getId(), r.getCodigo(), r.getFecha(), r.getHora(), r.getPersonas(),
				r.getNombre(), r.getEmail(), r.getTelefono(), r.getNotas(), r.getEstado(), r.getCreadaEn(),
				r.getMesa() == null ? null : r.getMesa().getId(), r.getMesa() == null ? null : r.getMesa().getNumero());
	}
}
