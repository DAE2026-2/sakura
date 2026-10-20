// Panel de reservas: lista del día con resumen y cambio de estado.
import { escapar, initSesion, SesionCaducada, type Pedir } from './admin-sesion';

type Reserva = {
  id: number;
  codigo: string;
  hora: string;
  personas: number;
  nombre: string;
  email: string;
  telefono: string;
  notas?: string;
  estado: 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA';
};

const estados = ['PENDIENTE', 'CONFIRMADA', 'CANCELADA'] as const;
const fechaLarga = new Intl.DateTimeFormat('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });
const iso = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

export function initAdmin() {
  const root = document.querySelector<HTMLElement>('[data-admin]');
  if (!root) return;
  const panelError = root.querySelector<HTMLElement>('[data-panel-error]')!;
  const fecha = root.querySelector<HTMLInputElement>('[data-fecha]')!;
  const filas = root.querySelector<HTMLElement>('[data-filas]')!;
  const resumen = root.querySelector<HTMLElement>('[data-resumen]')!;
  const titulo = root.querySelector<HTMLElement>('[data-titulo]')!;
  let pedir: Pedir | undefined;
  fecha.value = iso(new Date());

  const pintar = (reservas: Reserva[]) => {
    const activas = reservas.filter((r) => r.estado !== 'CANCELADA');
    const datos = [
      ['Reservas', activas.length],
      ['Personas', activas.reduce((n, r) => n + r.personas, 0)],
      ['Pendientes', reservas.filter((r) => r.estado === 'PENDIENTE').length],
    ];
    resumen.innerHTML = datos
      .map(([k, v]) => `<div class="flex flex-col-reverse gap-1"><dt class="text-xs text-crema/60">${k}</dt><dd class="font-display text-3xl tabular-nums">${v}</dd></div>`)
      .join('');

    if (!reservas.length) {
      filas.innerHTML = '<tr><td colspan="6" class="px-4 py-8 text-center text-crema/50">No hay reservas este día.</td></tr>';
      return;
    }
    filas.innerHTML = reservas
      .map(
        (r) => `
        <tr class="${r.estado === 'CANCELADA' ? 'text-crema/40 line-through' : ''}">
          <td class="px-4 py-3 font-display text-lg tabular-nums">${r.hora}</td>
          <td class="px-4 py-3 tabular-nums">${r.personas}</td>
          <td class="px-4 py-3">${escapar(r.nombre)}<span class="block text-xs text-crema/50">${r.codigo}</span></td>
          <td class="px-4 py-3">${escapar(r.telefono)}<span class="block text-xs text-crema/50">${escapar(r.email)}</span></td>
          <td class="max-w-56 px-4 py-3 text-crema/70">${escapar(r.notas ?? '')}</td>
          <td class="px-4 py-3">
            <select data-id="${r.id}" class="input min-h-9 py-1 text-sm no-underline" aria-label="Estado de ${escapar(r.nombre)}">
              ${estados.map((e) => `<option value="${e}" ${e === r.estado ? 'selected' : ''}>${e.charAt(0) + e.slice(1).toLowerCase()}</option>`).join('')}
            </select>
          </td>
        </tr>`,
      )
      .join('');
  };

  const cargar = async () => {
    if (!pedir) return;
    panelError.textContent = '';
    titulo.textContent = fechaLarga.format(new Date(`${fecha.value}T12:00:00`));
    try {
      pintar(await pedir<Reserva[]>(`/admin/reservas?fecha=${fecha.value}`));
    } catch (error) {
      if (error instanceof SesionCaducada) return sesion.cerrarSesion('La sesión ha caducado. Vuelve a entrar.');
      panelError.textContent = 'No se pudieron cargar las reservas. ¿Está arrancado el backend?';
    }
  };

  const sesion = initSesion(root, (p) => {
    pedir = p;
    cargar();
  });

  fecha.addEventListener('change', cargar);
  root.querySelectorAll<HTMLButtonElement>('[data-dia]').forEach((boton) =>
    boton.addEventListener('click', () => {
      const d = new Date(`${fecha.value}T12:00:00`);
      d.setDate(d.getDate() + Number(boton.dataset.dia));
      fecha.value = iso(d);
      cargar();
    }),
  );

  filas.addEventListener('change', async (event) => {
    const select = event.target as HTMLSelectElement;
    if (!select.dataset.id || !pedir) return;
    try {
      await pedir(`/admin/reservas/${select.dataset.id}`, { method: 'PATCH', body: JSON.stringify({ estado: select.value }) });
    } catch {
      panelError.textContent = 'No se pudo cambiar el estado.';
    }
    cargar();
  });
}
