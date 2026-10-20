// CRUD de mesas contra /api/admin/mesas (RF-07, HU-08). El estado cambia solo con los pedidos o con el botón.
import { escapar, initSesion, SesionCaducada, type Pedir } from './admin-sesion';
import { initDialogo } from './admin-crud';

type Mesa = { id: number; numero: number; capacidad: number; ubicacion?: string; estado: 'DISPONIBLE' | 'OCUPADA' };

export function initMesas() {
  const root = document.querySelector<HTMLElement>('[data-admin]');
  if (!root) return;
  const filas = root.querySelector<HTMLElement>('[data-filas]')!;
  const panelError = root.querySelector<HTMLElement>('[data-panel-error]')!;
  const dialogo = initDialogo(root);
  let pedir: Pedir | undefined;
  let mesas: Mesa[] = [];

  const fallo = (error: unknown, mensaje: string) => {
    if (error instanceof SesionCaducada) return sesion.cerrarSesion('La sesión ha caducado. Vuelve a entrar.');
    panelError.textContent = error instanceof Error && error.message ? error.message : mensaje;
  };

  const pintar = () => {
    filas.innerHTML = mesas.length
      ? mesas
          .map(
            (m) => `
        <tr>
          <td class="px-4 py-3 font-display text-lg tabular-nums">${m.numero}</td>
          <td class="px-4 py-3 tabular-nums">${m.capacidad} pers.</td>
          <td class="px-4 py-3">${escapar(m.ubicacion ?? '')}</td>
          <td class="px-4 py-3">${m.estado === 'OCUPADA' ? '<span class="text-sakura">Ocupada</span>' : 'Disponible'}</td>
          <td class="px-4 py-3 text-right whitespace-nowrap">
            <button type="button" data-estado="${m.id}" class="btn btn-ghost btn-sm"><span>Marcar ${m.estado === 'OCUPADA' ? 'disponible' : 'ocupada'}</span></button>
            <button type="button" data-editar="${m.id}" class="btn btn-ghost btn-sm"><span>Editar</span></button>
            <button type="button" data-eliminar="${m.id}" class="btn btn-ghost btn-sm"><span>Eliminar</span></button>
          </td>
        </tr>`,
          )
          .join('')
      : '<tr><td colspan="5" class="px-4 py-8 text-center text-crema/50">No hay mesas.</td></tr>';
  };

  const cargar = async () => {
    if (!pedir) return;
    panelError.textContent = '';
    try {
      mesas = await pedir<Mesa[]>('/admin/mesas');
      pintar();
    } catch (error) {
      fallo(error, 'No se pudieron cargar las mesas. ¿Está arrancado el backend?');
    }
  };

  const abrir = (m?: Mesa) => {
    dialogo.abrir(m ? `Editar mesa ${m.numero}` : 'Nueva mesa');
    dialogo.campo('id').value = m ? String(m.id) : '';
    dialogo.campo('numero').value = String(m?.numero ?? Math.max(0, ...mesas.map((x) => x.numero)) + 1);
    dialogo.campo('capacidad').value = String(m?.capacidad ?? 4);
    dialogo.campo('ubicacion').value = m?.ubicacion ?? '';
  };

  dialogo.form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!pedir) return;
    dialogo.limpiarErrores();
    const datos = new FormData(dialogo.form);
    const id = datos.get('id');
    try {
      await pedir(id ? `/admin/mesas/${id}` : '/admin/mesas', {
        method: id ? 'PUT' : 'POST',
        body: JSON.stringify({
          numero: Number(datos.get('numero')) || null,
          capacidad: Number(datos.get('capacidad')) || null,
          ubicacion: datos.get('ubicacion'),
        }),
      });
      dialogo.cerrar();
      cargar();
    } catch (error) {
      if (error instanceof SesionCaducada) {
        dialogo.cerrar();
        return fallo(error, '');
      }
      dialogo.mostrarError(error);
    }
  });

  filas.addEventListener('click', async (event) => {
    const boton = (event.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!boton || !pedir) return;
    const id = boton.dataset.editar ?? boton.dataset.eliminar ?? boton.dataset.estado;
    const mesa = mesas.find((m) => String(m.id) === id);
    if (!mesa) return;
    panelError.textContent = '';
    try {
      if (boton.dataset.editar) return abrir(mesa);
      if (boton.dataset.estado) {
        const estado = mesa.estado === 'OCUPADA' ? 'DISPONIBLE' : 'OCUPADA';
        await pedir(`/admin/mesas/${mesa.id}/estado`, { method: 'PATCH', body: JSON.stringify({ estado }) });
      } else {
        if (!confirm(`¿Eliminar la mesa ${mesa.numero}?`)) return;
        await pedir(`/admin/mesas/${mesa.id}`, { method: 'DELETE' });
      }
      cargar();
    } catch (error) {
      fallo(error, 'No se pudo completar la acción.');
    }
  });

  root.querySelector('[data-nuevo]')?.addEventListener('click', () => abrir());

  const sesion = initSesion(root, (p) => {
    pedir = p;
    cargar();
  });
}
