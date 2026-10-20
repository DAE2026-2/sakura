package com.sakura.model;

public enum EstadoPedido {
	PENDIENTE, EN_PREPARACION, SERVIDO, PAGADO, CANCELADO;

	/** Un pedido pagado o cancelado está cerrado: libera la mesa y ya no se puede modificar. */
	public boolean cerrado() {
		return this == PAGADO || this == CANCELADO;
	}
}
