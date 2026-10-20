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

	public MesaService(MesaRepository mesas, PedidoRepository pedidos) {
		this.mesas = mesas;
		this.pedidos = pedidos;
	}

	@Transactional(readOnly = true)
	public List<MesaResponse> listar() {
		return mesas.findAllByOrderByNumero().stream().map(MesaResponse::de).toList();
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

	/** Una mesa con pedidos en el historial no se borra, para no perder las ventas. */
	@Transactional
	public void eliminar(Long id) {
		var mesa = buscar(id);
		if (pedidos.existsByMesaId(id)) {
			throw new ApiException(HttpStatus.CONFLICT, "La mesa tiene pedidos en el historial y no se puede eliminar");
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
