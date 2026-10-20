package com.sakura.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Agrupa los productos del menú: entrantes, nigiri, postres, bebidas… */
@Entity
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 40)
	private String nombre;

	@Column(length = 200)
	private String descripcion;

	@Column(nullable = false)
	private int orden;

	protected Categoria() {
	}

	public Categoria(String nombre, String descripcion, int orden) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.orden = orden;
	}

	public Long getId() { return id; }
	public String getNombre() { return nombre; }
	public String getDescripcion() { return descripcion; }
	public int getOrden() { return orden; }

	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
	public void setOrden(int orden) { this.orden = orden; }
}
