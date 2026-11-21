package com.sakura.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum EstadoPedido {
	PENDIENTE, EN_PREPARACION, SERVIDO, PAGADO, CANCELADO;

	/** Un pedido pagado o cancelado está cerrado: libera la mesa y ya no se puede modificar. */
	public static final Set<EstadoPedido> CERRADOS = Collections.unmodifiableSet(EnumSet.of(PAGADO, CANCELADO));

	public boolean cerrado() {
		return CERRADOS.contains(this);
	}
}
