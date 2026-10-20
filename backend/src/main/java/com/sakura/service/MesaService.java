package com.sakura.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.dto.MesaRequest;
import com.sakura.dto.MesaResponse;
import com.sakura.exception.ApiException;
import com.sakura.model.EstadoMesa;
import com.sakura.model.EstadoPedido;
import com.sakura.model.Mesa;
import com.sakura.repository.MesaRepository;
import com.sakura.repository.PedidoRepository;

/** CRUD de mesas y su estado (RF-07, HU-08). */
@Service
public class MesaService {

	private static final List<EstadoPedido> CERRADOS = List.of(EstadoPedido.PAGADO, EstadoPedido.CANCELADO);

	private final MesaRepository mesas;
	private final PedidoRepository pedidos;
	private final ReservaService reservas;

	public MesaService(MesaRepository mesas, PedidoRepository pedidos, ReservaService reservas) {
		this.mesas = mesas;
		this.pedidos = pedidos;
		this.reservas = reservas;
	}

	/**
	 * Mesas con lo que les toca ahora: las que se guardan a una reserva salen como reservadas. Con
	 * {@code conDetalle} (mesero y administrador) incluye la hora, las personas y el nombre de la reserva.
	 */
	@Transactional(readOnly = true)
	public List<MesaResponse> listar(boolean conDetalle) {
		var reservadas = reservas.reservadasAhora();
		return mesas.findAllByOrderByNumero().stream()
				.map(m -> MesaResponse.de(m, reservadas.get(m.getId()), conDetalle))
				.toList();
	}

	@Transactional(readOnly = true)
	public MesaResponse obtener(Long id) {
		return MesaResponse.de(buscar(id));
	}

	@Transactional
	public MesaResponse crear(MesaRequest req) {
		if (mesas.existsByNumero(req.numero())) {
			throw new ApiException(HttpStatus.CONFLICT, "Ya existe la mesa " + req.numero());
		}
		return MesaResponse.de(mesas.save(new Mesa(req.numero(), req.capacidad(), vacioANull(req.ubicacion()))));
	}

	@Transactional
	public MesaResponse actualizar(Long id, MesaRequest req) {
		var mesa = buscar(id);
		if (mesas.existsByNumeroAndIdNot(req.numero(), id)) {
			throw new ApiException(HttpStatus.CONFLICT, "Ya existe la mesa " + req.numero());
		}
		mesa.setNumero(req.numero());
		mesa.setCapacidad(req.capacidad());
		mesa.setUbicacion(vacioANull(req.ubicacion()));
		return MesaResponse.de(mesa);
	}

	/** Cambio manual de estado. Una mesa con un pedido abierto no puede marcarse como disponible. */
	@Transactional
	public MesaResponse cambiarEstado(Long id, EstadoMesa estado) {
		var mesa = buscar(id);
		if (estado == EstadoMesa.DISPONIBLE && pedidos.existsByMesaIdAndEstadoNotIn(id, CERRADOS)) {
			throw new ApiException(HttpStatus.CONFLICT, "La mesa tiene un pedido abierto. Cóbralo o cancélalo primero");
		}
		mesa.setEstado(estado);
		return MesaResponse.de(mesa);
	}

	/** Una mesa con pedidos en el historial no se borra, para no perder las ventas; tampoco si tiene reservas por llegar. */
	@Transactional
	public void eliminar(Long id) {
		var mesa = buscar(id);
		if (pedidos.existsByMesaId(id)) {
			throw new ApiException(HttpStatus.CONFLICT, "La mesa tiene pedidos en el historial y no se puede eliminar");
		}
		if (reservas.tieneReservasFuturas(id)) {
			throw new ApiException(HttpStatus.CONFLICT, "La mesa tiene reservas pendientes. Muévelas a otra mesa antes");
		}
		mesas.delete(mesa);
	}

	private Mesa buscar(Long id) {
		return mesas.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe esa mesa"));
	}

	private static String vacioANull(String texto) {
		return texto == null || texto.isBlank() ? null : texto.trim();
	}
}
