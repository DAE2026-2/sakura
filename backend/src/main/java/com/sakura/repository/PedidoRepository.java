package com.sakura.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sakura.model.EstadoPedido;
import com.sakura.model.Pedido;

/** Los filtros del historial (fecha, mesa, cliente) se arman con Specifications en PedidoService. */
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

	@Override
	@EntityGraph(attributePaths = { "mesa", "usuario" })
	List<Pedido> findAll(Specification<Pedido> spec, Sort sort);

	@EntityGraph(attributePaths = { "mesa", "usuario" })
	Optional<Pedido> findWithMesaById(Long id);

	boolean existsByMesaIdAndEstadoNotIn(Long mesaId, Collection<EstadoPedido> estados);

	boolean existsByMesaId(Long mesaId);

	boolean existsByUsuarioId(Long usuarioId);
}
