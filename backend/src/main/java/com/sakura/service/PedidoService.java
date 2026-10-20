package com.sakura.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.dto.DetalleRequest;
import com.sakura.dto.HistorialResponse;
import com.sakura.dto.PedidoRequest;
import com.sakura.dto.PedidoResponse;
import com.sakura.exception.ApiException;
import com.sakura.model.DetallePedido;
import com.sakura.model.EstadoMesa;
import com.sakura.model.EstadoPedido;
import com.sakura.model.Pedido;
import com.sakura.model.Rol;
import com.sakura.model.Usuario;
import com.sakura.repository.MesaRepository;
import com.sakura.repository.PedidoRepository;
import com.sakura.repository.ProductoRepository;
import com.sakura.repository.UsuarioRepository;

import jakarta.persistence.criteria.Predicate;

/**
 * Pedidos de las mesas (RF-03 a RF-06, RF-09):
 * - Abrir un pedido ocupa la mesa; pagarlo o cancelarlo la libera.
 * - El detalle solo se modifica mientras el pedido está abierto, y el total se recalcula en cada cambio.
 * - Un CLIENTE solo ve sus propios pedidos y solo puede modificarlos o cancelarlos mientras están pendientes.
 */
@Service
public class PedidoService {

	private static final Set<EstadoPedido> CERRADOS = EnumSet.of(EstadoPedido.PAGADO, EstadoPedido.CANCELADO);

	/** Transiciones permitidas para meseros y administradores. */
	private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES = Map.of(
			EstadoPedido.PENDIENTE, EnumSet.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO),
			EstadoPedido.EN_PREPARACION, EnumSet.of(EstadoPedido.SERVIDO, EstadoPedido.CANCELADO),
			EstadoPedido.SERVIDO, EnumSet.of(EstadoPedido.PAGADO, EstadoPedido.CANCELADO),
			EstadoPedido.PAGADO, EnumSet.noneOf(EstadoPedido.class),
			EstadoPedido.CANCELADO, EnumSet.noneOf(EstadoPedido.class));

	private final PedidoRepository pedidos;
	private final MesaRepository mesas;
	private final ProductoRepository productos;
	private final UsuarioRepository usuarios;
	private final Clock clock;

	public PedidoService(PedidoRepository pedidos, MesaRepository mesas, ProductoRepository productos,
			UsuarioRepository usuarios, Clock clock) {
		this.pedidos = pedidos;
		this.mesas = mesas;
		this.productos = productos;
		this.usuarios = usuarios;
		this.clock = clock;
	}

	/** Pedidos abiertos (todos para meseros y admins; los propios para un cliente). */
	@Transactional(readOnly = true)
	public List<PedidoResponse> abiertos(String email, Long mesaId) {
		var usuario = usuario(email);
		Specification<Pedido> spec = (root, q, cb) -> {
			List<Predicate> ps = new ArrayList<>();
			ps.add(cb.not(root.get("estado").in(CERRADOS)));
			if (mesaId != null) ps.add(cb.equal(root.get("mesa").get("id"), mesaId));
			if (usuario.getRol() == Rol.CLIENTE) ps.add(cb.equal(root.get("usuario").get("id"), usuario.getId()));
			return cb.and(ps.toArray(Predicate[]::new));
		};
		return pedidos.findAll(spec, Sort.by("creadoEn")).stream().map(PedidoResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public PedidoResponse obtener(Long id, String email) {
		return PedidoResponse.de(buscar(id, usuario(email)));
	}

	/** RF-03: el pedido nace PENDIENTE en una mesa disponible, que pasa a OCUPADA. */
	@Transactional
	public PedidoResponse crear(PedidoRequest req, String email) {
		var usuario = usuario(email);
		var mesa = mesas.findWithLockById(req.mesaId())
				.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Esa mesa no existe"));
		if (mesa.getEstado() != EstadoMesa.DISPONIBLE) {
			throw new ApiException(HttpStatus.CONFLICT, "La mesa " + mesa.getNumero() + " está ocupada");
		}
		mesa.setEstado(EstadoMesa.OCUPADA);
		var pedido = pedidos.save(new Pedido(mesa, usuario, vacioANull(req.notas()), ahora()));
		return PedidoResponse.de(pedido);
	}

	/** RF-04: añade un producto disponible; si ya estaba en el pedido, suma la cantidad. */
	@Transactional
	public PedidoResponse agregarDetalle(Long id, DetalleRequest req, String email) {
		var pedido = abierto(id, usuario(email));
		var producto = productos.findById(req.productoId())
				.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Ese producto no existe"));
		if (!producto.isDisponible()) {
			throw new ApiException(HttpStatus.CONFLICT, producto.getNombre() + " no está disponible");
		}
		pedido.getDetalles().stream()
				.filter(d -> d.getProducto().getId().equals(producto.getId()))
				.findFirst()
				.ifPresentOrElse(
						d -> d.setCantidad(Math.min(99, d.getCantidad() + req.cantidad())),
						() -> pedido.getDetalles().add(new DetallePedido(pedido, producto, req.cantidad())));
		pedido.recalcularTotal(ahora());
		pedidos.flush();
		return PedidoResponse.de(pedido);
	}

	@Transactional
	public PedidoResponse cambiarCantidad(Long id, Long detalleId, int cantidad, String email) {
		var pedido = abierto(id, usuario(email));
		detalle(pedido, detalleId).setCantidad(cantidad);
		pedido.recalcularTotal(ahora());
		return PedidoResponse.de(pedido);
	}

	@Transactional
	public PedidoResponse quitarDetalle(Long id, Long detalleId, String email) {
		var pedido = abierto(id, usuario(email));
		pedido.getDetalles().remove(detalle(pedido, detalleId));
		pedido.recalcularTotal(ahora());
		return PedidoResponse.de(pedido);
	}

	/** RF-06 y HU-08: cambia el estado siguiendo el flujo y libera la mesa al cerrar el pedido. */
	@Transactional
	public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevo, String email) {
		var usuario = usuario(email);
		var pedido = buscar(id, usuario);
		var actual = pedido.getEstado();
		if (usuario.getRol() == Rol.CLIENTE) {
			if (!(actual == EstadoPedido.PENDIENTE && nuevo == EstadoPedido.CANCELADO)) {
				throw new ApiException(HttpStatus.FORBIDDEN, "Solo puedes cancelar tu pedido mientras está pendiente");
			}
		} else if (!TRANSICIONES.get(actual).contains(nuevo)) {
			throw new ApiException(HttpStatus.CONFLICT, "No se puede pasar de %s a %s".formatted(actual, nuevo));
		}
		if (nuevo == EstadoPedido.PAGADO && pedido.getDetalles().isEmpty()) {
			throw new ApiException(HttpStatus.CONFLICT, "No se puede cobrar un pedido vacío");
		}
		pedido.setEstado(nuevo);
		pedido.setActualizadoEn(ahora());
		if (CERRADOS.contains(nuevo)) pedido.getMesa().setEstado(EstadoMesa.DISPONIBLE);
		return PedidoResponse.de(pedido);
	}

	/** RF-09: historial por rango de fechas, número de mesa y cliente, con la suma de lo pagado. */
	@Transactional(readOnly = true)
	public HistorialResponse historial(LocalDate desde, LocalDate hasta, Integer mesa, Long usuarioId,
			EstadoPedido estado) {
		var zona = clock.getZone();
		Specification<Pedido> spec = (root, q, cb) -> {
			List<Predicate> ps = new ArrayList<>();
			if (desde != null) ps.add(cb.greaterThanOrEqualTo(root.get("creadoEn"), desde.atStartOfDay(zona).toOffsetDateTime()));
			if (hasta != null) ps.add(cb.lessThan(root.get("creadoEn"), hasta.plusDays(1).atStartOfDay(zona).toOffsetDateTime()));
			if (mesa != null) ps.add(cb.equal(root.get("mesa").get("numero"), mesa));
			if (usuarioId != null) ps.add(cb.equal(root.get("usuario").get("id"), usuarioId));
			if (estado != null) ps.add(cb.equal(root.get("estado"), estado));
			return cb.and(ps.toArray(Predicate[]::new));
		};
		var lista = pedidos.findAll(spec, Sort.by(Sort.Direction.DESC, "creadoEn")).stream()
				.map(PedidoResponse::de).toList();
		var ventas = lista.stream()
				.filter(p -> p.estado() == EstadoPedido.PAGADO)
				.map(PedidoResponse::total)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return new HistorialResponse(lista, lista.size(), ventas);
	}

	private Pedido buscar(Long id, Usuario usuario) {
		var pedido = pedidos.findWithMesaById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe ese pedido"));
		boolean ajeno = usuario.getRol() == Rol.CLIENTE
				&& (pedido.getUsuario() == null || !pedido.getUsuario().getId().equals(usuario.getId()));
		if (ajeno) throw new ApiException(HttpStatus.NOT_FOUND, "No existe ese pedido");
		return pedido;
	}

	private Pedido abierto(Long id, Usuario usuario) {
		var pedido = buscar(id, usuario);
		if (pedido.getEstado().cerrado()) {
			throw new ApiException(HttpStatus.CONFLICT, "El pedido está cerrado y ya no se puede modificar");
		}
		// Cuando cocina ya lo recibió, el cliente no puede cambiarlo: tiene que pedírselo al mesero.
		if (usuario.getRol() == Rol.CLIENTE && pedido.getEstado() != EstadoPedido.PENDIENTE) {
			throw new ApiException(HttpStatus.CONFLICT, "Tu pedido ya está en cocina. Pide los cambios al mesero");
		}
		return pedido;
	}

	private static DetallePedido detalle(Pedido pedido, Long detalleId) {
		return pedido.getDetalles().stream()
				.filter(d -> d.getId().equals(detalleId))
				.findFirst()
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ese producto no está en el pedido"));
	}

	private Usuario usuario(String email) {
		return usuarios.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sesión no válida"));
	}

	private OffsetDateTime ahora() {
		return OffsetDateTime.now(clock);
	}

	private static String vacioANull(String texto) {
		return texto == null || texto.isBlank() ? null : texto.trim();
	}
}
