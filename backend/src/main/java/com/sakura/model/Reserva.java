package com.sakura.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Reserva {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Código que recibe el cliente para consultar o cancelar la reserva, p. ej. SAK-4F2K. */
	@Column(nullable = false, unique = true, length = 12)
	private String codigo;

	@Column(nullable = false)
	private LocalDate fecha;

	@Column(nullable = false)
	private LocalTime hora;

	@Column(nullable = false)
	private int personas;

	@Column(nullable = false, length = 80)
	private String nombre;

	@Column(nullable = false, length = 120)
	private String email;

	@Column(nullable = false, length = 30)
	private String telefono;

	@Column(length = 500)
	private String notas;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoReserva estado = EstadoReserva.PENDIENTE;

	@Column(name = "creada_en", nullable = false)
	private OffsetDateTime creadaEn;

	/** Mesa que se le guarda al cliente. Se asigna sola al reservar y el administrador puede cambiarla. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mesa_id")
	private Mesa mesa;

	protected Reserva() {
	}

	public Reserva(String codigo, LocalDate fecha, LocalTime hora, int personas, String nombre, String email,
			String telefono, String notas, OffsetDateTime creadaEn) {
		this.codigo = codigo;
		this.fecha = fecha;
		this.hora = hora;
		this.personas = personas;
		this.nombre = nombre;
		this.email = email;
		this.telefono = telefono;
		this.notas = notas;
		this.creadaEn = creadaEn;
	}

	public Long getId() { return id; }
	public String getCodigo() { return codigo; }
	public LocalDate getFecha() { return fecha; }
	public LocalTime getHora() { return hora; }
	public int getPersonas() { return personas; }
	public String getNombre() { return nombre; }
	public String getEmail() { return email; }
	public String getTelefono() { return telefono; }
	public String getNotas() { return notas; }
	public EstadoReserva getEstado() { return estado; }
	public OffsetDateTime getCreadaEn() { return creadaEn; }
	public Mesa getMesa() { return mesa; }

	public void setEstado(EstadoReserva estado) { this.estado = estado; }
	public void setMesa(Mesa mesa) { this.mesa = mesa; }
}
