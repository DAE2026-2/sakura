package com.sakura.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Horas en las que aún caben `personas` un día concreto. Si el día no se puede reservar, `motivo` dice por qué. */
public record DisponibilidadResponse(LocalDate fecha, int personas, List<Hora> horas, String motivo) {

	public record Hora(@JsonFormat(pattern = "HH:mm") LocalTime hora, int plazasLibres) {
	}
}
