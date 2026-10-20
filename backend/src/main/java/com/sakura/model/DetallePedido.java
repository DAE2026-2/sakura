package com.sakura.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Línea de un pedido. El precio se copia del producto al añadirlo para que el pedido no cambie con la carta. */
@Entity
@Table(name = "detalle_pedido", uniqueConstraints = @UniqueConstraint(columnNames = { "pedido_id", "producto_id" }))
public class DetallePedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pedido_id", nullable = false)
	private Pedido pedido;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "producto_id", nullable = false)
	private Producto producto;

	@Column(nullable = false)
	private int cantidad;

	@Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
	private BigDecimal precioUnitario;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;

	protected DetallePedido() {
	}

	public DetallePedido(Pedido pedido, Producto producto, int cantidad) {
		this.pedido = pedido;
		this.producto = producto;
		this.precioUnitario = producto.getPrecio();
		setCantidad(cantidad);
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
		this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
	}

	public Long getId() { return id; }
	public Pedido getPedido() { return pedido; }
	public Producto getProducto() { return producto; }
	public int getCantidad() { return cantidad; }
	public BigDecimal getPrecioUnitario() { return precioUnitario; }
	public BigDecimal getSubtotal() { return subtotal; }
}
