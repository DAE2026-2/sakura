package com.sakura.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sakura.model.EstadoMesa;
import com.sakura.model.Mesa;
import com.sakura.model.Reserva;

/**
 * Una mesa con su estado. {@code reservada} indica que ahora mismo se le guarda a una reserva; los datos de esa
 * reserva ({@code reserva}) solo se envían al personal: el cliente solo sabe que está reservada.
 */
public record MesaResponse(Long id, int numero, int capacidad, String ubicacion, EstadoMesa estado, boolean reservada,
		ReservaMesa reserva) {

	public record ReservaMesa(String codigo, @JsonFormat(pattern = "HH:mm") LocalTime hora, int personas,
			String nombre) {
	}

	public static MesaResponse de(Mesa m) {
		return de(m, null, false);
	}

	public static MesaResponse de(Mesa m, Reserva r, boolean conDetalle) {
		var reserva = r != null && conDetalle
				? new ReservaMesa(r.getCodigo(), r.getHora(), r.getPersonas(), r.getNombre())
				: null;
		return new MesaResponse(m.getId(), m.getNumero(), m.getCapacidad(), m.getUbicacion(), m.getEstado(), r != null,
				reserva);
	}
}
