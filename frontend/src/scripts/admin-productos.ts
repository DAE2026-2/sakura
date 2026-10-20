// CRUD de productos del menú contra /api/admin/productos (RF-01, HU-01).
import { escapar, initSesion, SesionCaducada, type Pedir } from './admin-sesion';
import { initDialogo } from './admin-crud';
import { precio } from '../lib/api';

type Categoria = { id: number; nombre: string };
type Producto = {
  id: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  disponible: boolean;
  destacado: boolean;
  orden: number;
  categoriaId: number;
  categoria: string;
};

export function initProductos() {
  const root = document.querySelector<HTMLElement>('[data-admin]');
  if (!root) return;
  const filas = root.querySelector<HTMLElement>('[data-filas]')!;
  const filtro = root.querySelector<HTMLSelectElement>('[data-filtro]')!;
  const panelError = root.querySelector<HTMLElement>('[data-panel-error]')!;
  const dialogo = initDialogo(root);
  let pedir: Pedir | undefined;
  let productos: Producto[] = [];
  let categorias: Categoria[] = [];

  const fallo = (error: unknown, mensaje: string) => {
    if (error instanceof SesionCaducada) return sesion.cerrarSesion('La sesión ha caducado. Vuelve a entrar.');
    panelError.textContent = error instanceof Error && error.message ? error.message : mensaje;
  };

  const pintar = () => {
    const visibles = filtro.value ? productos.filter((p) => String(p.categoriaId) === filtro.value) : productos;
    filas.innerHTML = visibles.length
      ? visibles
          .map(
            (p) => `
        <tr class="${p.disponible ? '' : 'text-crema/50'}">
          <td class="px-4 py-3 tabular-nums text-crema/60">${p.orden}</td>
          <td class="px-4 py-3">${escapar(p.nombre)}${p.destacado ? ' <span class="text-xs text-sakura">· destacado</span>' : ''}<span class="block text-xs text-crema/50">${escapar(p.descripcion ?? '')}</span></td>
          <td class="px-4 py-3">${escapar(p.categoria)}</td>
          <td class="px-4 py-3 tabular-nums">${precio(p.precio)}</td>
          <td class="px-4 py-3">${p.disponible ? '<span class="text-sakura">Sí</span>' : 'No'}</td>
          <td class="px-4 py-3 text-right whitespace-nowrap">
            <button type="button" data-editar="${p.id}" class="btn btn-ghost btn-sm"><span>Editar</span></button>
            <button type="button" data-eliminar="${p.id}" class="btn btn-ghost btn-sm"><span>Eliminar</span></button>
          </td>
        </tr>`,
          )
          .join('')
      : '<tr><td colspan="6" class="px-4 py-8 text-center text-crema/50">No hay productos.</td></tr>';
  };

  const opciones = (seleccion = '') =>
    categorias.map((c) => `<option value="${c.id}" ${String(c.id) === seleccion ? 'selected' : ''}>${escapar(c.nombre)}</option>`).join('');

  const cargar = async () => {
    if (!pedir) return;
    panelError.textContent = '';
    try {
      [productos, categorias] = await Promise.all([
        pedir<Producto[]>('/admin/productos'),
        pedir<Categoria[]>('/admin/categorias'),
      ]);
      filtro.innerHTML = `<option value="">Todas</option>${opciones(filtro.value)}`;
      pintar();
    } catch (error) {
      fallo(error, 'No se pudieron cargar los productos. ¿Está arrancado el backend?');
    }
  };

  const abrir = (p?: Producto) => {
    dialogo.abrir(p ? 'Editar producto' : 'Nuevo producto');
    (dialogo.campo('categoriaId') as unknown as HTMLSelectElement).innerHTML = opciones(String(p?.categoriaId ?? filtro.value));
    dialogo.campo('id').value = p ? String(p.id) : '';
    dialogo.campo('nombre').value = p?.nombre ?? '';
    dialogo.campo('descripcion').value = p?.descripcion ?? '';
    dialogo.campo('precio').value = p ? String(p.precio) : '';
    dialogo.campo('orden').value = String(p?.orden ?? Math.max(0, ...productos.map((x) => x.orden)) + 1);
    dialogo.campo('disponible').checked = p?.disponible ?? true;
    dialogo.campo('destacado').checked = p?.destacado ?? false;
  };

  dialogo.form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!pedir) return;
    dialogo.limpiarErrores();
    const datos = new FormData(dialogo.form);
    const id = datos.get('id');
    try {
      await pedir(id ? `/admin/productos/${id}` : '/admin/productos', {
        method: id ? 'PUT' : 'POST',
        body: JSON.stringify({
          categoriaId: Number(datos.get('categoriaId')) || null,
          nombre: datos.get('nombre'),
          descripcion: datos.get('descripcion'),
          precio: datos.get('precio') === '' ? null : Number(datos.get('precio')),
          orden: Number(datos.get('orden') || 0),
          disponible: datos.get('disponible') === 'on',
          destacado: datos.get('destacado') === 'on',
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
    const producto = productos.find((p) => String(p.id) === (boton.dataset.editar ?? boton.dataset.eliminar));
    if (!producto) return;
    if (boton.dataset.editar) return abrir(producto);
    if (!confirm(`¿Eliminar "${producto.nombre}" del menú?`)) return;
    try {
      await pedir(`/admin/productos/${producto.id}`, { method: 'DELETE' });
      cargar();
    } catch (error) {
      fallo(error, 'No se pudo eliminar el producto.');
    }
  });

  filtro.addEventListener('change', pintar);
  root.querySelector('[data-nuevo]')?.addEventListener('click', () => abrir());

  const sesion = initSesion(root, (p) => {
    pedir = p;
    cargar();
  });
}
