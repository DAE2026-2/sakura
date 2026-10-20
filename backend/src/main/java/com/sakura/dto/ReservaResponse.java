package com.sakura.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sakura.model.EstadoReserva;
import com.sakura.model.Reserva;

/** Lo que ve el cliente de su reserva. No incluye email ni teléfono. */
public record ReservaResponse(
		String codigo,
		LocalDate fecha,
		@JsonFormat(pattern = "HH:mm") LocalTime hora,
		int personas,
		String nombre,
		EstadoReserva estado) {

	public static ReservaResponse de(Reserva r) {
		return new ReservaResponse(r.getCodigo(), r.getFecha(), r.getHora(), r.getPersonas(), r.getNombre(),
				r.getEstado());
	}
}
