package com.sakura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.dto.MesaResponse;
import com.sakura.dto.PedidoRequest;
import com.sakura.dto.ReservaRequest;
import com.sakura.exception.ApiException;
import com.sakura.model.EstadoPedido;
import com.sakura.model.EstadoReserva;
import com.sakura.model.Reserva;
import com.sakura.repository.ReservaRepository;
import com.sakura.service.MesaService;
import com.sakura.service.PedidoService;
import com.sakura.service.ReservaService;

/** Reservas con mesa asignada (#37), contra la base en memoria de data-local.sql y con el reloj fijo a las 19:40. */
@SpringBootTest
@Transactional
class MesasReservadasTests {

	static final LocalDate HOY = LocalDate.of(2026, 10, 21);

	@TestConfiguration
	static class RelojFijo {
		@Bean
		@Primary
		Clock relojDePrueba() {
			var zona = ZoneId.of("America/Bogota");
			return Clock.fixed(ZonedDateTime.of(HOY, LocalTime.of(19, 40), zona).toInstant(), zona);
		}
	}

	static final String CLIENTE = "cliente@sakura.example";
	static final String MESERO = "mesero@sakura.example";

	@Autowired ReservaService reservas;
	@Autowired MesaService mesas;
	@Autowired PedidoService pedidos;
	@Autowired ReservaRepository repo;

	int clientes = 0;

	/** Cada llamada es un cliente distinto: solo se admite una reserva activa por email y día. */
	Reserva reservar(String hora, int personas) {
		return reservar(hora, personas, "cliente" + (++clientes) + "@example.com");
	}

	Reserva reservar(String hora, int personas, String email) {
		return reservas.crear(new ReservaRequest(HOY, LocalTime.parse(hora), personas, "Ana", email, "3001234567", null));
	}

	MesaResponse mesa(int numero, boolean conDetalle) {
		return mesa(numero, conDetalle, null);
	}

	MesaResponse mesa(int numero, boolean conDetalle, String emailCliente) {
		return mesas.listar(conDetalle, emailCliente).stream().filter(m -> m.numero() == numero).findFirst().orElseThrow();
	}

	@Test
	void asignaLaMesaMasPequenaLibreYNoLaRepiteEnDosHoras() {
		assertThat(reservar("20:00", 4).getMesa().getNumero()).isEqualTo(3);
		assertThat(reservar("21:30", 4).getMesa().getNumero()).isEqualTo(4);
		assertThat(reservar("22:00", 3).getMesa().getNumero()).isEqualTo(3);
	}

	@Test
	void laMesaSaleReservadaDesdeTreintaMinutosAntesYElClienteNoVeLaHora() {
		reservar("20:00", 4); // 19:40 está dentro del margen: mesa 3 reservada
		reservar("20:30", 4); // empieza a las 20:00: la mesa 4 sigue libre

		var paraCliente = mesa(3, false);
		assertThat(paraCliente.reservada()).isTrue();
		assertThat(paraCliente.reserva()).isNull();

		var paraMesero = mesa(3, true);
		assertThat(paraMesero.reserva().hora()).isEqualTo(LocalTime.of(20, 0));
		assertThat(paraMesero.reserva().personas()).isEqualTo(4);

		assertThat(mesa(4, true).reservada()).isFalse();
	}

	@Test
	void elClienteNoPuedePedirEnUnaMesaReservadaPeroElMeseroSiYLaReservaQuedaAtendida() {
		var reserva = reservar("20:00", 4);
		Long mesaId = reserva.getMesa().getId();

		assertThatThrownBy(() -> pedidos.crear(new PedidoRequest(mesaId, null), "cliente@sakura.example"))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("reservada");

		pedidos.crear(new PedidoRequest(mesaId, null), "mesero@sakura.example");
		assertThat(repo.findById(reserva.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.ATENDIDA);
		assertThat(mesa(3, true).reservada()).isFalse();
	}

	@Test
	void elAdminSoloMueveLaReservaAUnaMesaDondeQuepanYEsteLibre() {
		var primera = reservar("20:00", 4);
		var segunda = reservar("20:30", 4);
		Long mesaDeDos = 1L;

		assertThatThrownBy(() -> reservas.cambiarMesa(segunda.getId(), mesaDeDos)).hasMessageContaining("no caben");
		assertThatThrownBy(() -> reservas.cambiarMesa(segunda.getId(), primera.getMesa().getId()))
				.hasMessageContaining("ya está reservada");
		assertThat(reservas.cambiarMesa(segunda.getId(), 8L).getMesa().getNumero()).isEqualTo(8);
	}

	@Test
	void reactivarUnaReservaCanceladaRespetaElCupoDelTurno() {
		var grande = reservar("20:00", 8); // mesa 10
		reservas.cambiarEstado(grande.getId(), EstadoReserva.CANCELADA);
		reservar("20:00", 6); // el turno de las 20:00 admite 20 personas: 6+4+4+4+2 = 20, lleno
		reservar("20:00", 4);
		reservar("20:00", 4);
		reservar("20:00", 4);
		reservar("20:00", 2);

		assertThatThrownBy(() -> reservas.cambiarEstado(grande.getId(), EstadoReserva.CONFIRMADA))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("plazas");
	}

	@Test
	void unClienteSoloPuedeTenerUnPedidoAbierto() {
		var primero = pedidos.crear(new PedidoRequest(1L, null), CLIENTE);
		assertThatThrownBy(() -> pedidos.crear(new PedidoRequest(2L, null), CLIENTE))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Ya tienes un pedido abierto");

		pedidos.cambiarEstado(primero.id(), EstadoPedido.CANCELADO, CLIENTE);
		assertThat(pedidos.crear(new PedidoRequest(2L, null), CLIENTE).mesa()).isEqualTo(2);

		// El mesero atiende muchas mesas a la vez.
		pedidos.crear(new PedidoRequest(5L, null), MESERO);
		pedidos.crear(new PedidoRequest(6L, null), MESERO);
	}

	@Test
	void elClienteQueReservoPuedeAbrirPedidoEnSuMesaReservada() {
		var reserva = reservar("20:00", 4, "Cliente@Sakura.example"); // mismo email que su cuenta, otra capitalización
		Long mesaId = reserva.getMesa().getId();

		assertThat(mesa(3, false, CLIENTE).reservada()).isFalse(); // para él está libre
		assertThat(mesa(3, false, "otro@example.com").reservada()).isTrue();

		pedidos.crear(new PedidoRequest(mesaId, null), CLIENTE);
		assertThat(repo.findById(reserva.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.ATENDIDA);
	}

	@Test
	void unaReservaParaDentroDePocoNoSeAsignaAUnaMesaOcupadaAhora() {
		pedidos.crear(new PedidoRequest(3L, null), MESERO); // mesa 3 (4 pers.) ocupada a las 19:40

		assertThat(reservar("20:00", 4).getMesa().getNumero()).isEqualTo(4);
		// A las 22:00 ya habrán pasado más de 2 h: la mesa 3 vuelve a contar.
		assertThat(reservar("22:00", 4).getMesa().getNumero()).isEqualTo(3);
	}

	@Test
	void noSeRepiteUnaReservaActivaConElMismoEmailElMismoDia() {
		var primera = reservar("20:00", 2, "ana@example.com");
		assertThatThrownBy(() -> reservar("21:00", 2, "ANA@example.com"))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Ya tienes una reserva");

		reservas.cancelar(primera.getCodigo(), "ana@example.com");
		reservar("21:00", 2, "ana@example.com");
	}
}
