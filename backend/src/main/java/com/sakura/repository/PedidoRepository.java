package com.sakura.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sakura.model.EstadoPedido;
import com.sakura.model.Pedido;

/** Los filtros del historial (fecha, mesa, cliente) se arman con Specifications en el servicio. */
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

	boolean existsByMesaIdAndEstadoNotIn(Long mesaId, Collection<EstadoPedido> estados);

	boolean existsByMesaId(Long mesaId);
}
