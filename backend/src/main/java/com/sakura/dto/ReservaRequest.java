package com.sakura.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Lo que envía el formulario de reserva. Las reglas de negocio (turnos, cierres, plazas) las aplica el servicio. */
public record ReservaRequest(
		@NotNull(message = "Elige una fecha") LocalDate fecha,
		@NotNull(message = "Elige una hora") LocalTime hora,
		@NotNull(message = "Indica cuántas personas sois")
		@Min(value = 1, message = "Mínimo 1 persona")
		@Max(value = 8, message = "Para más de 8 personas, llámanos") Integer personas,
		@NotBlank(message = "Escribe tu nombre") @Size(max = 80, message = "Máximo 80 caracteres") String nombre,
		@NotBlank(message = "Escribe tu email") @Email(message = "El email no es válido")
		@Size(max = 120, message = "Máximo 120 caracteres") String email,
		@NotBlank(message = "Escribe tu teléfono")
		@Pattern(regexp = "^[+0-9 ()-]{6,30}$", message = "El teléfono no es válido") String telefono,
		@Size(max = 500, message = "Máximo 500 caracteres") String notas) {
}
