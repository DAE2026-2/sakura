package com.sakura.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

@Entity
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mesa_id", nullable = false)
	private Mesa mesa;

	/** Quien abrió el pedido: un mesero, o el propio cliente. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoPedido estado = EstadoPedido.PENDIENTE;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total = BigDecimal.ZERO;

	@Column(length = 300)
	private String notas;

	@Column(name = "creado_en", nullable = false)
	private OffsetDateTime creadoEn;

	@Column(name = "actualizado_en", nullable = false)
	private OffsetDateTime actualizadoEn;

	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<DetallePedido> detalles = new ArrayList<>();

	protected Pedido() {
	}

	public Pedido(Mesa mesa, Usuario usuario, String notas, OffsetDateTime ahora) {
		this.mesa = mesa;
		this.usuario = usuario;
		this.notas = notas;
		this.creadoEn = ahora;
		this.actualizadoEn = ahora;
	}

	/** RF-05: el total es siempre la suma de los subtotales del detalle. */
	public void recalcularTotal(OffsetDateTime ahora) {
		this.total = detalles.stream().map(DetallePedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		this.actualizadoEn = ahora;
	}

	public Long getId() { return id; }
	public Mesa getMesa() { return mesa; }
	public Usuario getUsuario() { return usuario; }
	public EstadoPedido getEstado() { return estado; }
	public BigDecimal getTotal() { return total; }
	public String getNotas() { return notas; }
	public OffsetDateTime getCreadoEn() { return creadoEn; }
	public OffsetDateTime getActualizadoEn() { return actualizadoEn; }
	public List<DetallePedido> getDetalles() { return detalles; }

	public void setEstado(EstadoPedido estado) { this.estado = estado; }
	public void setNotas(String notas) { this.notas = notas; }
	public void setActualizadoEn(OffsetDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
