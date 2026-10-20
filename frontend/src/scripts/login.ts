// Página /login: inicia sesión con HTTP Basic (o crea una cuenta de cliente) y lleva a cada rol a su pantalla.
// La sesión se guarda igual que en el panel, así que las pantallas de /admin y /mesero ya no piden la contraseña.
import { credenciales, guardarClave } from './admin-sesion';

type Usuario = { nombre: string; rol: 'ADMIN' | 'MESERO' | 'CLIENTE' };

const destino: Record<Usuario['rol'], string | null> = { ADMIN: '/admin/mesas', MESERO: '/mesero', CLIENTE: null };

export function initLogin() {
  const root = document.querySelector<HTMLElement>('[data-login-pagina]');
  if (!root) return;
  const api = root.dataset.api ?? '';
  const entrar = root.querySelector<HTMLFormElement>('[data-entrar]')!;
  const registro = root.querySelector<HTMLFormElement>('[data-registro]')!;
  const hola = root.querySelector<HTMLElement>('[data-hola]')!;
  const tabs = [...root.querySelectorAll<HTMLButtonElement>('[data-tab]')];

  const mostrarTab = (tab: string) => {
    tabs.forEach((t) => t.setAttribute('aria-selected', String(t.dataset.tab === tab)));
    entrar.hidden = tab !== 'entrar';
    registro.hidden = tab !== 'registro';
  };
  tabs.forEach((t) => t.addEventListener('click', () => mostrarTab(t.dataset.tab!)));

  const iniciar = async (email: string, password: string, error: HTMLElement) => {
    const auth = credenciales(email, password);
    let res: Response;
    try {
      res = await fetch(`${api}/auth/yo`, { headers: { Authorization: `Basic ${auth}` } });
    } catch {
      error.textContent = 'No hay conexión con el servidor. ¿Está arrancado el backend?';
      return;
    }
    if (res.status === 401) {
      error.textContent = 'Email o contraseña incorrectos.';
      return;
    }
    const usuario = (await res.json()) as Usuario;
    guardarClave(auth);
    const url = destino[usuario.rol];
    if (url) {
      location.href = url;
      return;
    }
    entrar.hidden = registro.hidden = true;
    tabs.forEach((t) => (t.hidden = true));
    hola.hidden = false;
    hola.innerHTML = `Hola, ${usuario.nombre.replace(/[<>&]/g, '')}. Ya puedes <a class="link" href="/#reservar">reservar tu mesa</a> o <a class="link" href="/#carta">ver la carta</a>.`;
  };

  entrar.addEventListener('submit', (event) => {
    event.preventDefault();
    const error = entrar.querySelector<HTMLElement>('[data-error]')!;
    error.textContent = '';
    const { email, password } = Object.fromEntries(new FormData(entrar)) as Record<string, string>;
    if (!email || !password) {
      error.textContent = 'Escribe tu email y tu contraseña.';
      return;
    }
    iniciar(email, password, error);
  });

  registro.addEventListener('submit', async (event) => {
    event.preventDefault();
    const error = registro.querySelector<HTMLElement>('[data-error]')!;
    error.textContent = '';
    registro.querySelectorAll<HTMLElement>('[data-error-campo]').forEach((el) => (el.textContent = ''));
    const datos = Object.fromEntries(new FormData(registro)) as Record<string, string>;
    try {
      const res = await fetch(`${api}/auth/registro`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos),
      });
      const cuerpo = await res.json().catch(() => ({}));
      if (!res.ok) {
        error.textContent = cuerpo.mensaje ?? 'No se pudo crear la cuenta.';
        for (const [campo, mensaje] of Object.entries((cuerpo.campos ?? {}) as Record<string, string>)) {
          const hueco = registro.querySelector<HTMLElement>(`[data-error-campo="${campo}"]`);
          if (hueco) hueco.textContent = mensaje;
        }
        return;
      }
      iniciar(datos.email, datos.password, error);
    } catch {
      error.textContent = 'No hay conexión con el servidor. ¿Está arrancado el backend?';
    }
  });
}
