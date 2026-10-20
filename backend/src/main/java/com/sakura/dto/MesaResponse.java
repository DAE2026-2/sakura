package com.sakura.dto;

import com.sakura.model.EstadoMesa;
import com.sakura.model.Mesa;

public record MesaResponse(Long id, int numero, int capacidad, String ubicacion, EstadoMesa estado) {

	public static MesaResponse de(Mesa m) {
		return new MesaResponse(m.getId(), m.getNumero(), m.getCapacidad(), m.getUbicacion(), m.getEstado());
	}
}
