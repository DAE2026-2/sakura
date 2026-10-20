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

/** Producto del menú. Solo los `disponible` se muestran a los clientes y se pueden añadir a un pedido. */
@Entity
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "categoria_id", nullable = false)
	private Categoria categoria;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Column(length = 300)
	private String descripcion;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal precio;

	@Column(nullable = false)
	private boolean disponible = true;

	/** Se muestra en la sección destacada de la landing. */
	@Column(nullable = false)
	private boolean destacado;

	@Column(nullable = false)
	private int orden;

	/** Ruta pública de la foto (WebP de 800 px en frontend/public/menu); la variante de 400 px termina en -400.webp. */
	@Column(length = 200)
	private String imagen;

	protected Producto() {
	}

	public Producto(Categoria categoria, String nombre, String descripcion, BigDecimal precio, boolean disponible,
			boolean destacado, int orden) {
		this.categoria = categoria;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
		this.disponible = disponible;
		this.destacado = destacado;
		this.orden = orden;
	}

	public Long getId() { return id; }
	public Categoria getCategoria() { return categoria; }
	public String getNombre() { return nombre; }
	public String getDescripcion() { return descripcion; }
	public BigDecimal getPrecio() { return precio; }
	public boolean isDisponible() { return disponible; }
	public boolean isDestacado() { return destacado; }
	public int getOrden() { return orden; }
	public String getImagen() { return imagen; }

	public void setCategoria(Categoria categoria) { this.categoria = categoria; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
	public void setPrecio(BigDecimal precio) { this.precio = precio; }
	public void setDisponible(boolean disponible) { this.disponible = disponible; }
	public void setDestacado(boolean destacado) { this.destacado = destacado; }
	public void setOrden(int orden) { this.orden = orden; }
	public void setImagen(String imagen) { this.imagen = imagen; }
}
