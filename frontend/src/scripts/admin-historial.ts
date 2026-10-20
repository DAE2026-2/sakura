// Historial de pedidos con filtros (RF-09, HU-05) contra /api/admin/pedidos.
import { escapar, initSesion, SesionCaducada, type Pedir } from './admin-sesion';
import { formatoFecha, nombreEstado, type Pedido } from './pedidos-comun';
import { precio } from '../lib/api';

type Historial = { pedidos: Pedido[]; cantidad: number; ventasPagadas: number };
type Usuario = { id: number; nombre: string; rol: string };

export function initHistorial() {
  const root = document.querySelector<HTMLElement>('[data-admin]');
  if (!root) return;
  const filtros = root.querySelector<HTMLFormElement>('[data-filtros]')!;
  const filas = root.querySelector<HTMLElement>('[data-filas]')!;
  const resumen = root.querySelector<HTMLElement>('[data-resumen]')!;
  const panelError = root.querySelector<HTMLElement>('[data-panel-error]')!;
  const selectUsuario = filtros.elements.namedItem('usuario') as HTMLSelectElement;
  let pedir: Pedir | undefined;

  const hoy = new Date();
  const iso = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  (filtros.elements.namedItem('desde') as HTMLInputElement).value = iso(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
  (filtros.elements.namedItem('hasta') as HTMLInputElement).value = iso(hoy);

  const pintar = (h: Historial) => {
    resumen.innerHTML = [
      ['Pedidos', String(h.cantidad)],
      ['Ventas pagadas', precio(h.ventasPagadas)],
    ]
      .map(([k, v]) => `<div class="flex flex-col-reverse gap-1"><dt class="text-xs text-crema/60">${k}</dt><dd class="font-display text-3xl tabular-nums">${v}</dd></div>`)
      .join('');
    filas.innerHTML = h.pedidos.length
      ? h.pedidos
          .map(
            (p) => `
        <tr class="${p.estado === 'CANCELADO' ? 'text-crema/40' : ''}">
          <td class="px-4 py-3 tabular-nums">#${p.id}</td>
          <td class="px-4 py-3 whitespace-nowrap">${formatoFecha(p.creadoEn)}</td>
          <td class="px-4 py-3 font-display text-lg tabular-nums">${p.mesa}</td>
          <td class="px-4 py-3">${escapar(p.usuario ?? '—')}</td>
          <td class="max-w-64 px-4 py-3 text-crema/70">${p.detalles.map((d) => `${d.cantidad}× ${escapar(d.producto)}`).join(', ') || '—'}</td>
          <td class="px-4 py-3">${p.estado === 'PAGADO' ? `<span class="text-sakura">${nombreEstado[p.estado]}</span>` : nombreEstado[p.estado]}</td>
          <td class="px-4 py-3 text-right tabular-nums">${precio(p.total)}</td>
        </tr>`,
          )
          .join('')
      : '<tr><td colspan="7" class="px-4 py-8 text-center text-crema/50">No hay pedidos con esos filtros.</td></tr>';
  };

  const cargar = async () => {
    if (!pedir) return;
    panelError.textContent = '';
    const params = new URLSearchParams();
    for (const [k, v] of new FormData(filtros)) if (v) params.set(k, String(v));
    try {
      pintar(await pedir<Historial>(`/admin/pedidos?${params}`));
    } catch (error) {
      if (error instanceof SesionCaducada) return sesion.cerrarSesion('La sesión ha caducado. Vuelve a entrar.');
      panelError.textContent = error instanceof Error ? error.message : 'No se pudo cargar el historial.';
    }
  };

  filtros.addEventListener('submit', (event) => {
    event.preventDefault();
    cargar();
  });

  const sesion = initSesion(root, async (p) => {
    pedir = p;
    try {
      const usuarios = await p<Usuario[]>('/admin/usuarios');
      selectUsuario.innerHTML =
        '<option value="">Todos</option>' + usuarios.map((u) => `<option value="${u.id}">${escapar(u.nombre)}</option>`).join('');
    } catch {}
    cargar();
  });
}
