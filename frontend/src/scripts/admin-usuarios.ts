// CRUD de usuarios y roles contra /api/admin/usuarios (RF-08, HU-07).
import { escapar, initSesion, SesionCaducada, type Pedir } from './admin-sesion';
import { initDialogo } from './admin-crud';

type Usuario = { id: number; nombre: string; email: string; rol: 'ADMIN' | 'MESERO' | 'CLIENTE'; activo: boolean };
const roles = { ADMIN: 'Administrador', MESERO: 'Mesero', CLIENTE: 'Cliente' };

export function initUsuarios() {
  const root = document.querySelector<HTMLElement>('[data-admin]');
  if (!root) return;
  const filas = root.querySelector<HTMLElement>('[data-filas]')!;
  const panelError = root.querySelector<HTMLElement>('[data-panel-error]')!;
  const ayuda = root.querySelector<HTMLElement>('[data-ayuda-password]')!;
  const dialogo = initDialogo(root);
  let pedir: Pedir | undefined;
  let usuarios: Usuario[] = [];

  const fallo = (error: unknown, mensaje: string) => {
    if (error instanceof SesionCaducada) return sesion.cerrarSesion('La sesión ha caducado. Vuelve a entrar.');
    panelError.textContent = error instanceof Error && error.message ? error.message : mensaje;
  };

  const pintar = () => {
    filas.innerHTML = usuarios
      .map(
        (u) => `
        <tr class="${u.activo ? '' : 'text-crema/50'}">
          <td class="px-4 py-3">${escapar(u.nombre)}</td>
          <td class="px-4 py-3">${escapar(u.email)}</td>
          <td class="px-4 py-3">${u.rol === 'ADMIN' ? '<span class="text-sakura">Administrador</span>' : roles[u.rol]}</td>
          <td class="px-4 py-3">${u.activo ? 'Sí' : 'No'}</td>
          <td class="px-4 py-3 text-right whitespace-nowrap">
            <button type="button" data-editar="${u.id}" class="btn btn-ghost btn-sm"><span>Editar</span></button>
            <button type="button" data-eliminar="${u.id}" class="btn btn-ghost btn-sm"><span>Eliminar</span></button>
          </td>
        </tr>`,
      )
      .join('');
  };

  const cargar = async () => {
    if (!pedir) return;
    panelError.textContent = '';
    try {
      usuarios = await pedir<Usuario[]>('/admin/usuarios');
      pintar();
    } catch (error) {
      fallo(error, 'No se pudieron cargar los usuarios. ¿Está arrancado el backend?');
    }
  };

  const abrir = (u?: Usuario) => {
    dialogo.abrir(u ? 'Editar usuario' : 'Nuevo usuario');
    ayuda.textContent = u ? 'Nueva contraseña (déjala vacía para no cambiarla)' : 'Contraseña (mínimo 8 caracteres)';
    dialogo.campo('id').value = u ? String(u.id) : '';
    dialogo.campo('nombre').value = u?.nombre ?? '';
    dialogo.campo('email').value = u?.email ?? '';
    (dialogo.campo('rol') as unknown as HTMLSelectElement).value = u?.rol ?? 'MESERO';
    dialogo.campo('activo').checked = u?.activo ?? true;
  };

  dialogo.form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!pedir) return;
    dialogo.limpiarErrores();
    const datos = new FormData(dialogo.form);
    const id = datos.get('id');
    try {
      await pedir(id ? `/admin/usuarios/${id}` : '/admin/usuarios', {
        method: id ? 'PUT' : 'POST',
        body: JSON.stringify({
          nombre: datos.get('nombre'),
          email: datos.get('email'),
          password: datos.get('password') || null,
          rol: datos.get('rol'),
          activo: datos.get('activo') === 'on',
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
    const usuario = usuarios.find((u) => String(u.id) === (boton.dataset.editar ?? boton.dataset.eliminar));
    if (!usuario) return;
    if (boton.dataset.editar) return abrir(usuario);
    if (!confirm(`¿Eliminar a ${usuario.nombre}?`)) return;
    try {
      await pedir(`/admin/usuarios/${usuario.id}`, { method: 'DELETE' });
      cargar();
    } catch (error) {
      fallo(error, 'No se pudo eliminar el usuario.');
    }
  });

  root.querySelector('[data-nuevo]')?.addEventListener('click', () => abrir());

  const sesion = initSesion(root, (p) => {
    pedir = p;
    cargar();
  });
}
