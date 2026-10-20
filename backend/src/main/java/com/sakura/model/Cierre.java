package com.sakura.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Día en que el restaurante no abre. */
@Entity
public class Cierre {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private LocalDate fecha;

	@Column(length = 120)
	private String motivo;

	public Long getId() { return id; }
	public LocalDate getFecha() { return fecha; }
	public String getMotivo() { return motivo; }
}
