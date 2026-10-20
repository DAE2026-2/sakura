package com.sakura.model;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Hora en la que se puede reservar. La capacidad es el máximo de personas sentadas en ese turno. */
@Entity
public class Turno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private LocalTime hora;

	@Column(name = "capacidad_max", nullable = false)
	private int capacidadMax;

	@Column(nullable = false)
	private boolean activo = true;

	public Long getId() { return id; }
	public LocalTime getHora() { return hora; }
	public int getCapacidadMax() { return capacidadMax; }
	public boolean isActivo() { return activo; }
}
