// Pantalla del cliente (HU-04): elige una mesa libre, añade platos del menú y sigue el estado de su pedido.
// La API solo le deja ver y cambiar sus propios pedidos, y modificarlos o cancelarlos mientras están pendientes.
import { escapar, initSesion, SesionCaducada, type Pedir } from './admin-sesion';
import { nombreEstado, type EstadoPedido, type Pedido } from './pedidos-comun';
import { precio, srcsetProducto, type CategoriaMenu } from '../lib/api';

type Mesa = { id: number; numero: number; capacidad: number; ubicacion?: string; estado: 'DISPONIBLE' | 'OCUPADA' };

const pasos: EstadoPedido[] = ['PENDIENTE', 'EN_PREPARACION', 'SERVIDO', 'PAGADO'];

export function initCliente() {
  const root = document.querySelector<HTMLElement>('[data-admin]');
  if (!root) return;
  const listaMenu = root.querySelector<HTMLElement>('[data-menu]')!;
  const panel = root.querySelector<HTMLElement>('[data-pedido]')!;
  const panelError = root.querySelector<HTMLElement>('[data-panel-error]')!;
  let pedir: Pedir | undefined;
  let menu: CategoriaMenu[] = [];
  let pedido: Pedido | undefined;

  const fallo = (error: unknown, mensaje = 'No se pudo completar la acción.') => {
    if (error instanceof SesionCaducada) return sesion.cerrarSesion('La sesión ha caducado. Vuelve a entrar.');
    panelError.textContent = error instanceof Error && error.message ? error.message : mensaje;
  };

  const editable = () => pedido?.estado === 'PENDIENTE';

  const pintarMenu = () => {
    listaMenu.innerHTML = menu
      .map(
        (c) => `
      <div>
        <h2 class="font-sans text-xs tracking-widest text-crema/50 uppercase">${escapar(c.nombre)}</h2>
        <ul class="mt-4 grid gap-4 sm:grid-cols-2">
          ${c.productos
            .map(
              (p) => `
            <li class="flex gap-4 rounded-2xl p-3 ring-1 ring-crema/10">
              ${
                p.imagen
                  ? `<img src="${p.imagen.replace(/\.webp$/, '-400.webp')}" srcset="${srcsetProducto(p.imagen)}" sizes="112px" width="400" height="300" alt="" loading="lazy" decoding="async" class="aspect-[4/3] w-28 shrink-0 rounded-xl object-cover" />`
                  : '<span class="grid aspect-[4/3] w-28 shrink-0 place-items-center rounded-xl bg-crema/5 text-xs text-crema/40">Sakura</span>'
              }
              <div class="flex min-w-0 flex-1 flex-col">
                <h3 class="font-sans text-base">${escapar(p.nombre)}</h3>
                ${p.descripcion ? `<p class="mt-1 text-xs text-crema/60">${escapar(p.descripcion)}</p>` : ''}
                <div class="mt-auto flex items-center justify-between gap-2 pt-2">
                  <span class="text-sm tabular-nums">${precio(p.precio)}</span>
                  <button type="button" data-agregar="${p.id}" class="btn btn-ghost btn-sm" ${editable() ? '' : 'disabled'}
                    aria-label="Añadir ${escapar(p.nombre)}"><span>Añadir</span></button>
                </div>
              </div>
            </li>`,
            )
            .join('')}
        </ul>
      </div>`,
      )
      .join('');
  };

  const pintarSinPedido = async () => {
    if (!pedir) return;
    const mesas = (await pedir<Mesa[]>('/mesas')).filter((m) => m.estado === 'DISPONIBLE');
    panel.innerHTML = `
      <p class="text-sm text-sakura">Tu pedido</p>
      <h2 id="pedido-title" class="mt-2 text-2xl">¿En qué mesa estás?</h2>
      ${
        mesas.length
          ? `<form data-abrir class="mt-6 flex flex-col gap-4">
              <label class="field">Mesa
                <select class="input" name="mesaId">${mesas.map((m) => `<option value="${m.id}">Mesa ${m.numero} · ${m.capacidad} pers. · ${escapar(m.ubicacion ?? '')}</option>`).join('')}</select>
              </label>
              <label class="field">Notas para cocina (opcional)<input class="input" name="notas" maxlength="300" placeholder="Alergias, sin picante…" /></label>
              <button type="submit" class="btn btn-primary self-start"><span>Empezar pedido</span></button>
            </form>`
          : '<p class="mt-4 text-crema/70">Ahora mismo no hay mesas libres. Pregunta al mesero.</p>'
      }`;
  };

  const pintarPedido = () => {
    if (!pedido) return;
    const actual = pasos.indexOf(pedido.estado);
    panel.innerHTML = `
      <div class="flex items-start justify-between gap-4">
        <div>
          <p class="text-sm text-sakura">Mesa ${pedido.mesa} · Pedido #${pedido.id}</p>
          <h2 id="pedido-title" class="mt-2 text-2xl">${nombreEstado[pedido.estado]}</h2>
        </div>
        <p class="text-right"><span class="block text-xs text-crema/60">Total</span><span class="font-display text-3xl tabular-nums">${precio(pedido.total)}</span></p>
      </div>

      <ol class="mt-6 grid grid-cols-4 gap-1 text-[11px] text-crema/50" aria-label="Estado del pedido">
        ${pasos
          .map((p, i) => `<li class="flex flex-col gap-2"><span class="h-1 rounded-full ${i <= actual ? 'bg-sakura' : 'bg-crema/15'}"></span><span ${i === actual ? 'class="text-crema" aria-current="step"' : ''}>${nombreEstado[p]}</span></li>`)
          .join('')}
      </ol>

      <ul class="mt-6 divide-y divide-crema/10 text-sm">
        ${
          pedido.detalles.length
            ? pedido.detalles
                .map(
                  (d) => `
          <li class="flex items-center justify-between gap-3 py-3">
            <span class="min-w-0">${escapar(d.producto)}<span class="block text-xs text-crema/50">${precio(d.precioUnitario)} c/u</span></span>
            <span class="flex items-center gap-1">
              ${
                editable()
                  ? `<button type="button" data-cantidad="${d.id}" data-delta="-1" class="btn btn-ghost btn-sm size-8 p-0" aria-label="Quitar uno de ${escapar(d.producto)}"><span>−</span></button>
                     <span class="w-6 text-center tabular-nums">${d.cantidad}</span>
                     <button type="button" data-cantidad="${d.id}" data-delta="1" class="btn btn-ghost btn-sm size-8 p-0" aria-label="Añadir uno de ${escapar(d.producto)}"><span>+</span></button>`
                  : `<span class="tabular-nums">${d.cantidad} ×</span>`
              }
            </span>
            <span class="w-24 text-right tabular-nums">${precio(d.subtotal)}</span>
          </li>`,
                )
                .join('')
            : '<li class="py-6 text-center text-crema/50">Añade platos desde la carta.</li>'
        }
      </ul>

      ${
        editable()
          ? `<p class="mt-6 text-xs text-crema/50">El mesero enviará tu pedido a cocina. Hasta entonces puedes cambiarlo.</p>
             <button type="button" data-cancelar class="btn btn-ghost btn-sm mt-4"><span>Cancelar pedido</span></button>`
          : `<p class="mt-6 text-xs text-crema/50">Tu pedido ya está en marcha. Para cambiarlo, pídeselo al mesero.</p>
             <button type="button" data-refrescar class="btn btn-ghost btn-sm mt-4"><span>Actualizar estado</span></button>`
      }`;
  };

  const cargar = async () => {
    if (!pedir) return;
    panelError.textContent = '';
    try {
      [pedido] = await pedir<Pedido[]>('/pedidos');
      pintarMenu();
      if (pedido) pintarPedido();
      else await pintarSinPedido();
    } catch (error) {
      fallo(error, 'No se pudo cargar tu pedido. ¿Está arrancado el backend?');
    }
  };

  const accion = async (fn: () => Promise<Pedido>) => {
    panelError.textContent = '';
    try {
      pedido = await fn();
      if (pedido.estado === 'CANCELADO') {
        pedido = undefined;
        await cargar();
        panel.insertAdjacentHTML('afterbegin', '<p class="mb-4 rounded-xl px-3 py-2 text-sm ring-1 ring-sakura/60">Pedido cancelado.</p>');
        return;
      }
      pintarMenu();
      pintarPedido();
    } catch (error) {
      fallo(error);
      cargar();
    }
  };

  listaMenu.addEventListener('click', (event) => {
    const boton = (event.target as HTMLElement).closest<HTMLButtonElement>('[data-agregar]');
    if (!boton || !pedir || !pedido) return;
    const p = pedir;
    const id = pedido.id;
    accion(() => p<Pedido>(`/pedidos/${id}/detalles`, { method: 'POST', body: JSON.stringify({ productoId: Number(boton.dataset.agregar), cantidad: 1 }) }));
  });

  panel.addEventListener('submit', (event) => {
    event.preventDefault();
    if (!pedir) return;
    const p = pedir;
    const datos = Object.fromEntries(new FormData(event.target as HTMLFormElement)) as Record<string, string>;
    accion(() => p<Pedido>('/pedidos', { method: 'POST', body: JSON.stringify({ mesaId: Number(datos.mesaId), notas: datos.notas }) }));
  });

  panel.addEventListener('click', (event) => {
    const boton = (event.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!boton || !pedir || !pedido) return;
    const p = pedir;
    const id = pedido.id;
    if (boton.dataset.refrescar !== undefined) return void cargar();
    if (boton.dataset.cancelar !== undefined) {
      if (!confirm('¿Cancelar tu pedido?')) return;
      return void accion(() => p<Pedido>(`/pedidos/${id}/estado`, { method: 'PATCH', body: JSON.stringify({ estado: 'CANCELADO' }) }));
    }
    if (boton.dataset.cantidad) {
      const detalle = pedido.detalles.find((d) => String(d.id) === boton.dataset.cantidad);
      if (!detalle) return;
      const cantidad = detalle.cantidad + Number(boton.dataset.delta);
      if (cantidad < 1) return void accion(() => p<Pedido>(`/pedidos/${id}/detalles/${detalle.id}`, { method: 'DELETE' }));
      accion(() => p<Pedido>(`/pedidos/${id}/detalles/${detalle.id}`, { method: 'PUT', body: JSON.stringify({ cantidad }) }));
    }
  });

  const sesion = initSesion(root, async (p) => {
    pedir = p;
    try {
      const res = await fetch(`${root.dataset.api}/menu`);
      menu = res.ok ? await res.json() : [];
    } catch {}
    cargar();
  });
}
