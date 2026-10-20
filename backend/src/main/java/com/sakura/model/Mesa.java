package com.sakura.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Mesa del restaurante. Pasa a OCUPADA al abrir un pedido y vuelve a DISPONIBLE al cerrarlo. */
@Entity
public class Mesa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private int numero;

	@Column(nullable = false)
	private int capacidad;

	@Column(length = 40)
	private String ubicacion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoMesa estado = EstadoMesa.DISPONIBLE;

	protected Mesa() {
	}

	public Mesa(int numero, int capacidad, String ubicacion) {
		this.numero = numero;
		this.capacidad = capacidad;
		this.ubicacion = ubicacion;
	}

	public Long getId() { return id; }
	public int getNumero() { return numero; }
	public int getCapacidad() { return capacidad; }
	public String getUbicacion() { return ubicacion; }
	public EstadoMesa getEstado() { return estado; }

	public void setNumero(int numero) { this.numero = numero; }
	public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
	public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
	public void setEstado(EstadoMesa estado) { this.estado = estado; }
}
