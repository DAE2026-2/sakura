package com.sakura.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.config.SakuraProperties;
import com.sakura.dto.DisponibilidadResponse;
import com.sakura.dto.ReservaRequest;
import com.sakura.exception.ApiException;
import com.sakura.model.EstadoReserva;
import com.sakura.model.Mesa;
import com.sakura.model.Reserva;
import com.sakura.model.Turno;
import com.sakura.repository.CierreRepository;
import com.sakura.repository.MesaRepository;
import com.sakura.repository.ReservaRepository;
import com.sakura.repository.TurnoRepository;

/**
 * Reglas de las reservas: qué días y horas se pueden reservar, cuántas plazas quedan, qué mesa se guarda a cada
 * reserva y el ciclo de vida de cada una.
 */
@Service
public class ReservaService {

	private static final Logger log = LoggerFactory.getLogger(ReservaService.class);
	// Sin 0/O ni 1/I/L para que el código no se confunda al dictarlo por teléfono.
	private static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
	private static final Set<EstadoReserva> ACTIVAS = EnumSet.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
	// Se prueba primero la mesa más pequeña donde quepan, para no gastar las grandes en grupos pequeños.
	private static final Comparator<Mesa> MAS_PEQUENA = Comparator.comparingInt(Mesa::getCapacidad)
			.thenComparingInt(Mesa::getNumero);

	private final ReservaRepository reservas;
	private final TurnoRepository turnos;
	private final CierreRepository cierres;
	private final MesaRepository mesas;
	private final SakuraProperties props;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();

	public ReservaService(ReservaRepository reservas, TurnoRepository turnos, CierreRepository cierres,
			MesaRepository mesas, SakuraProperties props, Clock clock) {
		this.reservas = reservas;
		this.turnos = turnos;
		this.cierres = cierres;
		this.mesas = mesas;
		this.props = props;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public DisponibilidadResponse disponibilidad(LocalDate fecha, int personas) {
		if (personas < 1 || personas > props.reservas().maxPersonas()) {
			return new DisponibilidadResponse(fecha, personas, List.of(),
					"Online se reserva para 1 a %d personas".formatted(props.reservas().maxPersonas()));
		}
		String motivo = motivoFechaNoValida(fecha);
		if (motivo != null) return new DisponibilidadResponse(fecha, personas, List.of(), motivo);

		Map<LocalTime, Long> ocupadas = new HashMap<>();
		for (Object[] fila : reservas.personasPorTurno(fecha, EstadoReserva.CANCELADA)) {
			ocupadas.put((LocalTime) fila[0], ((Number) fila[1]).longValue());
		}

		var delDia = reservas.findByFechaAndEstadoIn(fecha, ACTIVAS);
		var todas = mesas.findAll();
		var horas = turnos.findByActivoTrueOrderByHora().stream()
				.filter(t -> !yaPasada(fecha, t.getHora()))
				.map(t -> new DisponibilidadResponse.Hora(t.getHora(),
						(int) (t.getCapacidadMax() - ocupadas.getOrDefault(t.getHora(), 0L))))
				.filter(h -> h.plazasLibres() >= personas)
				.filter(h -> mesaLibre(todas, delDia, h.hora(), personas, null).isPresent())
				.toList();
		return new DisponibilidadResponse(fecha, personas, horas,
				horas.isEmpty() ? "No quedan horas libres para %d personas este día".formatted(personas) : null);
	}

	@Transactional
	public Reserva crear(ReservaRequest req) {
		String motivo = motivoFechaNoValida(req.fecha());
		if (motivo != null) throw new ApiException(HttpStatus.BAD_REQUEST, motivo);

		// El bloqueo del turno hace que la comprobación de plazas y el guardado sean atómicos.
		Turno turno = turnos.findByHoraAndActivoTrue(req.hora())
				.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "No hay servicio a esa hora"));
		if (yaPasada(req.fecha(), req.hora())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Esa hora ya ha pasado");
		}
		long ocupadas = reservas.personasEnTurno(req.fecha(), req.hora(), EstadoReserva.CANCELADA);
		if (ocupadas + req.personas() > turno.getCapacidadMax()) {
			throw new ApiException(HttpStatus.CONFLICT,
					"Ya no quedan plazas para %d personas a las %s. Prueba otra hora".formatted(req.personas(), req.hora()));
		}
		var mesa = mesaLibre(mesas.findAll(), reservas.findByFechaAndEstadoIn(req.fecha(), ACTIVAS), req.hora(),
				req.personas(), null)
				.orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
						"Ya no quedan mesas para %d personas a las %s. Prueba otra hora".formatted(req.personas(), req.hora())));

		var reserva = new Reserva(nuevoCodigo(), req.fecha(), req.hora(), req.personas(),
				req.nombre().trim(), req.email().trim().toLowerCase(), req.telefono().trim(),
				vacioANull(req.notas()), OffsetDateTime.now(clock));
		reserva.setMesa(mesa);
		reservas.save(reserva);
		// En local no se envían correos: aquí iría el email de confirmación.
		log.info("Reserva {} creada: {} {} · {} personas · mesa {} · {}", reserva.getCodigo(), reserva.getFecha(),
				reserva.getHora(), reserva.getPersonas(), mesa.getNumero(), reserva.getEmail());
		return reserva;
	}

	/**
	 * El cliente consulta su reserva con el código y el email con el que la hizo. Si el email no coincide, responde
	 * igual que si no existiera, para no revelar reservas ajenas adivinando códigos.
	 */
	@Transactional(readOnly = true)
	public Reserva consultar(String codigo, String email) {
		return reservas.findByCodigo(codigo.trim().toUpperCase())
				.filter(r -> email != null && r.getEmail().equalsIgnoreCase(email.trim()))
				.orElseThrow(ApiException::noEncontrada);
	}

	@Transactional
	public Reserva cancelar(String codigo, String email) {
		var reserva = consultar(codigo, email);
		if (reserva.getEstado() == EstadoReserva.CANCELADA) {
			throw new ApiException(HttpStatus.CONFLICT, "Esta reserva ya estaba cancelada");
		}
		if (yaPasada(reserva.getFecha(), reserva.getHora())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "No se puede cancelar una reserva que ya ha pasado");
		}
		reserva.setEstado(EstadoReserva.CANCELADA);
		log.info("Reserva {} cancelada por el cliente", reserva.getCodigo());
		return reserva;
	}

	@Transactional(readOnly = true)
	public List<Reserva> delDia(LocalDate fecha) {
		return reservas.findByFechaOrderByHoraAscCreadaEnAsc(fecha);
	}

	/** Si una reserva cancelada o atendida vuelve a estar activa, necesita otra vez una mesa libre a su hora. */
	@Transactional
	public Reserva cambiarEstado(Long id, EstadoReserva estado) {
		var reserva = buscar(id);
		if (estado.activa() && !reserva.getEstado().activa()) {
			var otras = reservas.findByFechaAndEstadoIn(reserva.getFecha(), ACTIVAS);
			boolean sigueLibre = reserva.getMesa() != null
					&& mesaLibre(List.of(reserva.getMesa()), otras, reserva.getHora(), reserva.getPersonas(), id).isPresent();
			if (!sigueLibre) {
				reserva.setMesa(mesaLibre(mesas.findAll(), otras, reserva.getHora(), reserva.getPersonas(), id)
						.orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
								"No queda ninguna mesa libre para esta reserva a las " + reserva.getHora())));
			}
		}
		reserva.setEstado(estado);
		return reserva;
	}

	/** El administrador mueve la reserva a otra mesa, siempre que quepan y esté libre a esa hora. */
	@Transactional
	public Reserva cambiarMesa(Long id, Long mesaId) {
		var reserva = buscar(id);
		if (!reserva.getEstado().activa()) {
			throw new ApiException(HttpStatus.CONFLICT, "Solo se cambia la mesa de reservas pendientes o confirmadas");
		}
		var mesa = mesas.findById(mesaId)
				.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Esa mesa no existe"));
		if (mesa.getCapacidad() < reserva.getPersonas()) {
			throw new ApiException(HttpStatus.CONFLICT, "En la mesa %d no caben %d personas"
					.formatted(mesa.getNumero(), reserva.getPersonas()));
		}
		var otras = reservas.findByFechaAndEstadoIn(reserva.getFecha(), ACTIVAS);
		if (mesaLibre(List.of(mesa), otras, reserva.getHora(), reserva.getPersonas(), id).isEmpty()) {
			throw new ApiException(HttpStatus.CONFLICT, "La mesa %d ya está reservada cerca de esa hora"
					.formatted(mesa.getNumero()));
		}
		reserva.setMesa(mesa);
		return reserva;
	}

	/**
	 * Reservas que ahora mismo guardan su mesa, por id de mesa: desde {@code minutosAntes} antes de la hora hasta
	 * {@code minutosDespues} después. Pasado ese margen, si el cliente no llegó, la mesa vuelve a quedar libre.
	 */
	@Transactional(readOnly = true)
	public Map<Long, Reserva> reservadasAhora() {
		var ahora = LocalTime.now(clock);
		return reservas.findByFechaAndEstadoIn(LocalDate.now(clock), ACTIVAS).stream()
				.filter(r -> r.getMesa() != null)
				.filter(r -> !ahora.isBefore(r.getHora().minusMinutes(props.reservas().minutosAntes()))
						&& !ahora.isAfter(r.getHora().plusMinutes(props.reservas().minutosDespues())))
				.collect(Collectors.toMap(r -> r.getMesa().getId(), Function.identity(),
						(a, b) -> a.getHora().isBefore(b.getHora()) ? a : b));
	}

	/** La reserva que guarda ahora mismo esa mesa, si la hay. */
	@Transactional(readOnly = true)
	public Optional<Reserva> reservaEnCurso(Long mesaId) {
		return Optional.ofNullable(reservadasAhora().get(mesaId));
	}

	/** ¿La mesa tiene reservas pendientes de hoy en adelante? */
	@Transactional(readOnly = true)
	public boolean tieneReservasFuturas(Long mesaId) {
		return reservas.existsByMesaIdAndFechaGreaterThanEqualAndEstadoIn(mesaId, LocalDate.now(clock), ACTIVAS);
	}

	/**
	 * La mesa más pequeña de {@code candidatas} donde caben las personas y que ninguna otra reserva del día ocupa a
	 * menos de {@code duracionMinutos} de esa hora. {@code ignorar} es la propia reserva cuando se está editando.
	 */
	private Optional<Mesa> mesaLibre(List<Mesa> candidatas, List<Reserva> delDia, LocalTime hora, int personas,
			Long ignorar) {
		int duracion = props.reservas().duracionMinutos();
		return candidatas.stream()
				.filter(m -> m.getCapacidad() >= personas)
				.filter(m -> delDia.stream()
						.filter(r -> !r.getId().equals(ignorar) && r.getMesa() != null)
						.noneMatch(r -> r.getMesa().getId().equals(m.getId())
								&& Math.abs(ChronoUnit.MINUTES.between(r.getHora(), hora)) < duracion))
				.min(MAS_PEQUENA);
	}

	private Reserva buscar(Long id) {
		return reservas.findWithMesaById(id).orElseThrow(ApiException::noEncontrada);
	}

	/** Devuelve por qué no se puede reservar ese día, o null si se puede. */
	private String motivoFechaNoValida(LocalDate fecha) {
		LocalDate hoy = LocalDate.now(clock);
		if (fecha.isBefore(hoy)) return "Esa fecha ya ha pasado";
		if (fecha.isAfter(hoy.plusDays(props.reservas().diasAntelacion()))) {
			return "Se puede reservar con un máximo de %d días de antelación".formatted(props.reservas().diasAntelacion());
		}
		if (cierres.existsByFecha(fecha)) return "Ese día el restaurante está cerrado";
		return null;
	}

	private boolean yaPasada(LocalDate fecha, LocalTime hora) {
		LocalDate hoy = LocalDate.now(clock);
		return fecha.isBefore(hoy) || (fecha.equals(hoy) && !hora.isAfter(LocalTime.now(clock)));
	}

	private String nuevoCodigo() {
		String codigo;
		do {
			var sb = new StringBuilder("SAK-");
			for (int i = 0; i < 4; i++) sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
			codigo = sb.toString();
		} while (reservas.existsByCodigo(codigo));
		return codigo;
	}

	private static String vacioANull(String texto) {
		return texto == null || texto.isBlank() ? null : texto.trim();
	}
}
