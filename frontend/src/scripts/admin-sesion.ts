// Sesión del panel de administración, compartida por todas sus páginas: login con HTTP Basic contra /api/admin y
// credenciales en sessionStorage, que se olvidan al cerrar la pestaña.

const CLAVE = 'sakura-admin';

const leerClave = () => {
  try {
    return sessionStorage.getItem(CLAVE);
  } catch {
    return null;
  }
};

const guardarClave = (valor: string | null) => {
  try {
    if (valor) sessionStorage.setItem(CLAVE, valor);
    else sessionStorage.removeItem(CLAVE);
  } catch {}
};

export class SesionCaducada extends Error {}

class SinPermiso extends Error {}

/** Error de la API con su mensaje y, si es de validación, el error de cada campo. */
export class ErrorApi extends Error {
  constructor(mensaje: string, readonly campos: Record<string, string> = {}) {
    super(mensaje);
  }
}

export type Pedir = <T = unknown>(path: string, init?: RequestInit) => Promise<T>;

export const escapar = (texto = '') =>
  texto.replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]!);

/**
 * Conecta el formulario `[data-login]`, el botón `[data-salir]` y el contenedor `[data-panel]` de una página del panel.
 * `alEntrar` se llama al iniciar sesión (o al cargar la página si ya había una sesión guardada).
 */
export function initSesion(root: HTMLElement, alEntrar: (pedir: Pedir) => void) {
  const api = root.dataset.api ?? '';
  const login = root.querySelector<HTMLFormElement>('[data-login]')!;
  const loginError = root.querySelector<HTMLElement>('[data-login-error]')!;
  const panel = root.querySelector<HTMLElement>('[data-panel]')!;
  const salir = root.querySelector<HTMLButtonElement>('[data-salir]')!;
  let auth = leerClave();

  const pedir: Pedir = async (path, init = {}) => {
    const res = await fetch(`${api}${path}`, {
      ...init,
      headers: { ...init.headers, Authorization: `Basic ${auth}`, 'Content-Type': 'application/json' },
    });
    if (res.status === 401) throw new SesionCaducada();
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      throw new ErrorApi(data.mensaje ?? 'Error del servidor', data.campos);
    }
    return (res.status === 204 ? undefined : await res.json()) as never;
  };

  const mostrar = (dentro: boolean) => {
    login.hidden = dentro;
    panel.hidden = !dentro;
    salir.hidden = !dentro;
  };

  const cerrarSesion = (mensaje = '') => {
    auth = null;
    guardarClave(null);
    loginError.textContent = mensaje;
    mostrar(false);
  };

  login.addEventListener('submit', async (event) => {
    event.preventDefault();
    const { email, password } = Object.fromEntries(new FormData(login)) as Record<string, string>;
    // btoa solo admite latin1: se codifica a UTF-8 antes para aceptar contraseñas con tildes o ñ.
    auth = btoa(String.fromCharCode(...new TextEncoder().encode(`${email.trim()}:${password}`)));
    try {
      const yo = await pedir<{ rol: string }>('/auth/yo');
      if (yo.rol !== 'ADMIN') throw new SinPermiso();
      guardarClave(auth);
      loginError.textContent = '';
      login.reset();
      mostrar(true);
      alEntrar(pedir);
    } catch (error) {
      auth = null;
      loginError.textContent =
        error instanceof SesionCaducada
          ? 'Email o contraseña incorrectos.'
          : error instanceof SinPermiso
            ? 'Tu usuario no es administrador.'
            : 'No hay conexión con el backend.';
    }
  });

  salir.addEventListener('click', () => cerrarSesion());

  if (auth) {
    mostrar(true);
    alEntrar(pedir);
  }

  return { cerrarSesion };
}
