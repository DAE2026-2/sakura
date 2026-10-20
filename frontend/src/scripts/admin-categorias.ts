// CRUD de categorías del menú contra /api/admin/categorias (RF-02, HU-02).
import { escapar, initSesion, SesionCaducada, type Pedir } from './admin-sesion';
import { initDialogo } from './admin-crud';

type Categoria = { id: number; nombre: string; descripcion?: string; orden: number };

export function initCategorias() {
  const root = document.querySelector<HTMLElement>('[data-admin]');
  if (!root) return;
  const filas = root.querySelector<HTMLElement>('[data-filas]')!;
  const panelError = root.querySelector<HTMLElement>('[data-panel-error]')!;
  const dialogo = initDialogo(root);
  let pedir: Pedir | undefined;
  let categorias: Categoria[] = [];

  const fallo = (error: unknown, mensaje: string) => {
    if (error instanceof SesionCaducada) return sesion.cerrarSesion('La sesión ha caducado. Vuelve a entrar.');
    panelError.textContent = error instanceof Error && error.message ? error.message : mensaje;
  };

  const pintar = () => {
    filas.innerHTML = categorias.length
      ? categorias
          .map(
            (c) => `
        <tr>
          <td class="px-4 py-3 tabular-nums text-crema/60">${c.orden}</td>
          <td class="px-4 py-3">${escapar(c.nombre)}</td>
          <td class="px-4 py-3 text-crema/70">${escapar(c.descripcion ?? '')}</td>
          <td class="px-4 py-3 text-right whitespace-nowrap">
            <button type="button" data-editar="${c.id}" class="btn btn-ghost btn-sm"><span>Editar</span></button>
            <button type="button" data-eliminar="${c.id}" class="btn btn-ghost btn-sm"><span>Eliminar</span></button>
          </td>
        </tr>`,
          )
          .join('')
      : '<tr><td colspan="4" class="px-4 py-8 text-center text-crema/50">No hay categorías.</td></tr>';
  };

  const cargar = async () => {
    if (!pedir) return;
    panelError.textContent = '';
    try {
      categorias = await pedir<Categoria[]>('/admin/categorias');
      pintar();
    } catch (error) {
      fallo(error, 'No se pudieron cargar las categorías. ¿Está arrancado el backend?');
    }
  };

  const abrir = (c?: Categoria) => {
    dialogo.abrir(c ? 'Editar categoría' : 'Nueva categoría');
    dialogo.campo('id').value = c ? String(c.id) : '';
    dialogo.campo('nombre').value = c?.nombre ?? '';
    dialogo.campo('descripcion').value = c?.descripcion ?? '';
    dialogo.campo('orden').value = String(c?.orden ?? Math.max(0, ...categorias.map((x) => x.orden)) + 1);
  };

  dialogo.form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!pedir) return;
    dialogo.limpiarErrores();
    const datos = new FormData(dialogo.form);
    const id = datos.get('id');
    try {
      await pedir(id ? `/admin/categorias/${id}` : '/admin/categorias', {
        method: id ? 'PUT' : 'POST',
        body: JSON.stringify({
          nombre: datos.get('nombre'),
          descripcion: datos.get('descripcion'),
          orden: Number(datos.get('orden') || 0),
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
    const categoria = categorias.find((c) => String(c.id) === (boton.dataset.editar ?? boton.dataset.eliminar));
    if (!categoria) return;
    if (boton.dataset.editar) return abrir(categoria);
    if (!confirm(`¿Eliminar la categoría "${categoria.nombre}"?`)) return;
    try {
      await pedir(`/admin/categorias/${categoria.id}`, { method: 'DELETE' });
      cargar();
    } catch (error) {
      fallo(error, 'No se pudo eliminar la categoría.');
    }
  });

  root.querySelector('[data-nuevo]')?.addEventListener('click', () => abrir());

  const sesion = initSesion(root, (p) => {
    pedir = p;
    cargar();
  });
}
