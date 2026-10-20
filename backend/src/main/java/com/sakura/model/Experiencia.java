package com.sakura.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Menú cerrado. El precio es por el número de `personas` indicado (1 = por persona). */
@Entity
public class Experiencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Column(nullable = false, length = 300)
	private String descripcion;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal precio;

	@Column(nullable = false)
	private int personas = 1;

	@Column(length = 40)
	private String etiqueta;

	@Column(nullable = false)
	private int orden;

	public Long getId() { return id; }
	public String getNombre() { return nombre; }
	public String getDescripcion() { return descripcion; }
	public BigDecimal getPrecio() { return precio; }
	public int getPersonas() { return personas; }
	public String getEtiqueta() { return etiqueta; }
	public int getOrden() { return orden; }
}
