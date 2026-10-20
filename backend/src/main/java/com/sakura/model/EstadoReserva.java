package com.sakura.model;

public enum EstadoReserva {
	PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA;

	/** Reservas que siguen guardando su mesa: aún no han llegado ni se han cancelado. */
	public boolean activa() {
		return this == PENDIENTE || this == CONFIRMADA;
	}
}
