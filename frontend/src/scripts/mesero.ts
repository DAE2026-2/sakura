// Pantalla del mesero (HU-03, HU-04, HU-08): mesas con su estado, abrir pedido, añadir productos con el total
// calculado por la API y avanzar el estado hasta cobrar, momento en que la mesa vuelve a quedar disponible.
import { escapar, initSesion, SesionCaducada, type Pedir } from './admin-sesion';
import { nombreEstado, type EstadoPedido, type Pedido } from './pedidos-comun';
import { precio, type CategoriaMenu } from '../lib/api';

type Mesa = { id: number; numero: number; capacidad: number; ubicacion?: string; estado: 'DISPONIBLE' | 'OCUPADA' };

const siguiente: Partial<Record<EstadoPedido, { estado: EstadoPedido; texto: string }>> = {
  PENDIENTE: { estado: 'EN_PREPARACION', texto: 'Enviar a cocina' },
  EN_PREPARACION: { estado: 'SERVIDO', texto: 'Marcar servido' },
  SERVIDO: { estado: 'PAGADO', texto: 'Cobrar' },
};

// Globito de cada mesa ocupada: en qué paso va su pedido, para saber qué mesa falta por atender.
const aviso: Partial<Record<EstadoPedido, { texto: string; clase: string }>> = {
  PENDIENTE: { texto: 'Pedido', clase: 'bg-sakura text-bg' },
  EN_PREPARACION: { texto: 'Preparación', clase: 'bg-bg text-sakura ring-1 ring-sakura/70' },
  SERVIDO: { texto: 'Cobrar', clase: 'bg-crema text-bg' },
};

// Cada cuánto se consultan los pedidos para ver lo que hacen los clientes sin recargar la página.
const REFRESCO_MS = 10_000;

export function initMesero() {
  const root = document.querySelector<HTMLElement>('[data-admin]');
  if (!root) return;
  const listaMesas = root.querySelector<HTMLElement>('[data-mesas]')!;
  const panel = root.querySelector<HTMLElement>('[data-pedido]')!;
  const panelError = root.querySelector<HTMLElement>('[data-panel-error]')!;
  const resumen = root.querySelector<HTMLElement>('[data-resumen]')!;
  let pedir: Pedir | undefined;
  let mesas: Mesa[] = [];
  let menu: CategoriaMenu[] = [];
  let mesaActual: Mesa | undefined;
  let pedido: Pedido | undefined;
  // Pedidos abiertos de todas las mesas y la última versión de cada uno que el mesero ya vio al abrir la mesa.
  let abiertos: Pedido[] = [];
  const vistos = new Map<number, string | undefined>();

  const pedidoDe = (mesa: Mesa) => abiertos.find((p) => p.mesaId === mesa.id);
  const esNovedad = (p: Pedido) => vistos.get(p.id) !== p.actualizadoEn;
  const marcarVisto = (p: Pedido | undefined) => {
    if (p) vistos.set(p.id, p.actualizadoEn);
  };

  const fallo = (error: unknown, mensaje = 'No se pudo completar la acción.') => {
    if (error instanceof SesionCaducada) return sesion.cerrarSesion('La sesión ha caducado. Vuelve a entrar.');
    panelError.textContent = error instanceof Error && error.message ? error.message : mensaje;
  };

  const globito = (p: Pedido | undefined) => {
    const a = p && aviso[p.estado];
    if (!p || !a) return '';
    const nuevo = esNovedad(p);
    return `
          <span class="absolute -top-2 left-1/2 inline-flex -translate-x-1/2 items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-medium whitespace-nowrap shadow-lg ${a.clase}">
            ${nuevo ? '<span class="relative flex size-1.5"><span class="absolute inline-flex size-full rounded-full bg-current opacity-75 motion-safe:animate-ping"></span><span class="relative inline-flex size-1.5 rounded-full bg-current"></span></span>' : ''}
            ${a.texto}
          </span>`;
  };

  const pintarResumen = () => {
    const cuantos = (estado: EstadoPedido) => abiertos.filter((p) => p.estado === estado).length;
    resumen.innerHTML = (['PENDIENTE', 'EN_PREPARACION', 'SERVIDO'] as const)
      .map((e) => `<span class="inline-flex items-center gap-2"><span class="rounded-full px-2 py-0.5 text-[11px] font-medium tabular-nums ${aviso[e]!.clase}">${cuantos(e)}</span>${aviso[e]!.texto}</span>`)
      .join('');
    const nuevos = abiertos.filter(esNovedad).length;
    document.title = `${nuevos ? `(${nuevos}) ` : ''}Pedidos · Sakura`;
  };

  const pintarMesas = () => {
    listaMesas.innerHTML = mesas
      .map((m) => {
        const p = pedidoDe(m);
        const etiqueta = p && aviso[p.estado] ? ` · ${aviso[p.estado]!.texto}${esNovedad(p) ? ' (novedad)' : ''}` : '';
        return `
      <li>
        <button type="button" data-mesa="${m.id}" aria-pressed="${m.id === mesaActual?.id}" aria-label="Mesa ${m.numero}${etiqueta}"
          class="relative flex aspect-square w-full flex-col items-center justify-center gap-1 rounded-2xl ring-1 transition-colors duration-700
          ${m.estado === 'OCUPADA' ? 'bg-sakura/15 ring-sakura/60' : 'ring-crema/15 hover:bg-crema/5'}
          ${m.id === mesaActual?.id ? 'outline-2 outline-offset-2 outline-sakura' : ''}">
          ${globito(p)}
          <span class="font-display text-3xl tabular-nums">${m.numero}</span>
          <span class="text-xs text-crema/60">${m.capacidad} pers. · ${escapar(m.ubicacion ?? '')}</span>
          ${p?.detalles.length ? `<span class="text-[11px] text-crema/50 tabular-nums">${precio(p.total)}</span>` : ''}
        </button>
      </li>`;
      })
      .join('');
    pintarResumen();
  };

  const opcionesMenu = () =>
    menu
      .map((c) => `<optgroup label="${escapar(c.nombre)}">${c.productos.map((p) => `<option value="${p.id}">${escapar(p.nombre)} · ${precio(p.precio)}</option>`).join('')}</optgroup>`)
      .join('');

  const pintarPedido = () => {
    if (!mesaActual) return;
    if (!pedido) {
      panel.innerHTML = `
        <p class="text-sm text-sakura">Mesa ${mesaActual.numero}</p>
        <h2 class="mt-2 text-3xl">Sin pedido abierto</h2>
        <form data-abrir class="mt-6 flex flex-col gap-4">
          <label class="field">Notas (opcional)<input class="input" name="notas" maxlength="300" placeholder="Alergias, celebraciones…" /></label>
          <button type="submit" class="btn btn-primary self-start"><span>Abrir pedido</span></button>
        </form>`;
      return;
    }
    const abierto = pedido.estado !== 'PAGADO' && pedido.estado !== 'CANCELADO';
    const paso = siguiente[pedido.estado];
    panel.innerHTML = `
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p class="text-sm text-sakura">Mesa ${pedido.mesa} · Pedido #${pedido.id}</p>
          <h2 class="mt-2 text-3xl">${nombreEstado[pedido.estado]}</h2>
          ${pedido.notas ? `<p class="mt-1 text-sm text-crema/60">${escapar(pedido.notas)}</p>` : ''}
        </div>
        <p class="text-right"><span class="block text-xs text-crema/60">Total</span><span class="font-display text-4xl tabular-nums" data-total>${precio(pedido.total)}</span></p>
      </div>

      <table class="mt-6 w-full text-left text-sm">
        <thead class="text-xs tracking-widest text-crema/50 uppercase">
          <tr><th class="py-2 font-medium">Producto</th><th class="py-2 font-medium">Cant.</th><th class="py-2 text-right font-medium">Subtotal</th><th class="py-2"><span class="sr-only">Quitar</span></th></tr>
        </thead>
        <tbody class="divide-y divide-crema/10">
          ${
            pedido.detalles.length
              ? pedido.detalles
                  .map(
                    (d) => `
            <tr>
              <td class="py-3">${escapar(d.producto)}<span class="block text-xs text-crema/50">${precio(d.precioUnitario)} c/u</span></td>
              <td class="py-3">
                ${abierto ? `<span class="inline-flex items-center gap-1">
                  <button type="button" data-cantidad="${d.id}" data-delta="-1" class="btn btn-ghost btn-sm size-8 p-0" aria-label="Quitar uno de ${escapar(d.producto)}" ${d.cantidad <= 1 ? 'disabled' : ''}><span>−</span></button>
                  <span class="w-6 text-center tabular-nums">${d.cantidad}</span>
                  <button type="button" data-cantidad="${d.id}" data-delta="1" class="btn btn-ghost btn-sm size-8 p-0" aria-label="Añadir uno de ${escapar(d.producto)}"><span>+</span></button>
                </span>` : `<span class="tabular-nums">${d.cantidad}</span>`}
              </td>
              <td class="py-3 text-right tabular-nums">${precio(d.subtotal)}</td>
              <td class="py-3 text-right">${abierto ? `<button type="button" data-quitar="${d.id}" class="link text-xs">Quitar</button>` : ''}</td>
            </tr>`,
                  )
                  .join('')
              : '<tr><td colspan="4" class="py-6 text-center text-crema/50">Aún no hay productos.</td></tr>'
          }
        </tbody>
      </table>

      ${
        abierto
          ? `<form data-agregar class="mt-6 grid gap-3 sm:grid-cols-[1fr_5rem_auto] sm:items-end">
              <label class="field">Producto<select class="input" name="productoId">${opcionesMenu()}</select></label>
              <label class="field">Cant.<input class="input" type="number" name="cantidad" min="1" max="99" value="1" /></label>
              <button type="submit" class="btn btn-ghost"><span>Añadir</span></button>
            </form>
            <div class="mt-8 flex flex-wrap gap-2">
              ${paso ? `<button type="button" data-estado="${paso.estado}" class="btn btn-primary"><span>${paso.texto}</span></button>` : ''}
              <button type="button" data-estado="CANCELADO" class="btn btn-ghost"><span>Cancelar pedido</span></button>
            </div>`
          : ''
      }`;
  };

  const cargarMesas = async () => {
    if (!pedir) return;
    [mesas, abiertos] = await Promise.all([pedir<Mesa[]>('/mesas'), pedir<Pedido[]>('/pedidos')]);
    mesaActual = mesas.find((m) => m.id === mesaActual?.id);
    pintarMesas();
  };

  // Trae los cambios de los clientes. El pedido abierto en el panel solo se repinta si cambió y el mesero
  // no está escribiendo en el formulario, para no borrarle lo que estaba eligiendo.
  const refrescar = async () => {
    if (!pedir || document.hidden || root.querySelector<HTMLElement>('[data-panel]')?.hidden) return;
    try {
      await cargarMesas();
      const actual = mesaActual && pedidoDe(mesaActual);
      const cambio = actual?.id !== pedido?.id || actual?.actualizadoEn !== pedido?.actualizadoEn;
      if (mesaActual && cambio && !panel.contains(document.activeElement)) {
        pedido = actual;
        marcarVisto(pedido);
        pintarMesas();
        pintarPedido();
      }
    } catch (error) {
      // Sin sesión deja de consultar hasta que vuelva a entrar; otros fallos se reintentan en la siguiente vuelta.
      if (error instanceof SesionCaducada) {
        pedir = undefined;
        fallo(error);
      }
    }
  };

  const seleccionar = async (mesa: Mesa) => {
    if (!pedir) return;
    panelError.textContent = '';
    mesaActual = mesa;
    pintarMesas();
    try {
      [pedido] = await pedir<Pedido[]>(`/pedidos?mesaId=${mesa.id}`);
      marcarVisto(pedido);
      pintarMesas();
      pintarPedido();
    } catch (error) {
      fallo(error, 'No se pudo cargar el pedido.');
    }
  };

  // Ejecuta una acción sobre el pedido y repinta con la respuesta de la API (que trae el total recalculado).
  const accion = async (fn: () => Promise<Pedido | undefined>) => {
    panelError.textContent = '';
    try {
      pedido = await fn();
      if (pedido && (pedido.estado === 'PAGADO' || pedido.estado === 'CANCELADO')) {
        const cerrado = pedido;
        pedido = undefined;
        await cargarMesas();
        pintarPedido();
        panel.insertAdjacentHTML('afterbegin', `<p class="mb-4 rounded-xl px-3 py-2 text-sm ring-1 ring-sakura/60">Pedido #${cerrado.id} ${cerrado.estado === 'PAGADO' ? `cobrado: ${precio(cerrado.total)}` : 'cancelado'}. La mesa ${cerrado.mesa} queda disponible.</p>`);
        return;
      }
      // Lo que cambia el propio mesero no cuenta como novedad.
      marcarVisto(pedido);
      await cargarMesas();
      pintarPedido();
    } catch (error) {
      fallo(error);
    }
  };

  listaMesas.addEventListener('click', (event) => {
    const boton = (event.target as HTMLElement).closest<HTMLButtonElement>('[data-mesa]');
    const mesa = mesas.find((m) => String(m.id) === boton?.dataset.mesa);
    if (mesa) seleccionar(mesa);
  });

  panel.addEventListener('submit', (event) => {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const datos = Object.fromEntries(new FormData(form)) as Record<string, string>;
    if (!pedir || !mesaActual) return;
    const p = pedir;
    if (form.matches('[data-abrir]')) {
      const mesaId = mesaActual.id;
      accion(() => p<Pedido>('/pedidos', { method: 'POST', body: JSON.stringify({ mesaId, notas: datos.notas }) }));
    } else if (form.matches('[data-agregar]') && pedido) {
      const id = pedido.id;
      accion(() =>
        p<Pedido>(`/pedidos/${id}/detalles`, {
          method: 'POST',
          body: JSON.stringify({ productoId: Number(datos.productoId), cantidad: Number(datos.cantidad) || 1 }),
        }),
      );
    }
  });

  panel.addEventListener('click', (event) => {
    const boton = (event.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!boton || !pedir || !pedido) return;
    const p = pedir;
    const id = pedido.id;
    if (boton.dataset.cantidad) {
      const detalle = pedido.detalles.find((d) => String(d.id) === boton.dataset.cantidad);
      if (!detalle) return;
      const cantidad = detalle.cantidad + Number(boton.dataset.delta);
      accion(() => p<Pedido>(`/pedidos/${id}/detalles/${detalle.id}`, { method: 'PUT', body: JSON.stringify({ cantidad }) }));
    } else if (boton.dataset.quitar) {
      accion(() => p<Pedido>(`/pedidos/${id}/detalles/${boton.dataset.quitar}`, { method: 'DELETE' }));
    } else if (boton.dataset.estado) {
      if (boton.dataset.estado === 'CANCELADO' && !confirm('¿Cancelar este pedido?')) return;
      accion(() => p<Pedido>(`/pedidos/${id}/estado`, { method: 'PATCH', body: JSON.stringify({ estado: boton.dataset.estado }) }));
    }
  });

  const sesion = initSesion(root, async (p) => {
    pedir = p;
    try {
      const res = await fetch(`${root.dataset.api}/menu`);
      menu = res.ok ? await res.json() : [];
      await cargarMesas();
      // Al entrar, lo que ya estaba abierto no es novedad: solo avisa de lo que llegue después.
      abiertos.forEach(marcarVisto);
      pintarMesas();
    } catch (error) {
      fallo(error, 'No se pudieron cargar las mesas. ¿Está arrancado el backend?');
    }
  });

  setInterval(refrescar, REFRESCO_MS);
  document.addEventListener('visibilitychange', refrescar);
}
