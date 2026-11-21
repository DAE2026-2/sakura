// Sesión compartida por todas las pantallas con login: POST /api/auth/login devuelve un token JWT que se guarda en
// sessionStorage (se olvida al cerrar la pestaña) y se envía como `Authorization: Bearer`. La contraseña no se guarda.

const CLAVE = 'sakura-admin';

export const leerClave = () => {
  try {
    return sessionStorage.getItem(CLAVE);
  } catch {
    return null;
  }
};

export const guardarClave = (valor: string | null) => {
  try {
    if (valor) sessionStorage.setItem(CLAVE, valor);
    else sessionStorage.removeItem(CLAVE);
  } catch {}
};

export class SesionCaducada extends Error {}

export type Usuario = { nombre: string; email: string; rol: 'ADMIN' | 'MESERO' | 'CLIENTE' };

/** Pide el token. Lanza SesionCaducada si el email o la contraseña no son correctos. */
export async function iniciarSesion(api: string, email: string, password: string) {
  const res = await fetch(`${api}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: email.trim(), password }),
  });
  if (res.status === 401 || res.status === 400) throw new SesionCaducada();
  if (!res.ok) throw new Error(`${res.status}`);
  return (await res.json()) as { token: string; usuario: Usuario };
}

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
  // Roles que pueden usar esta página (data-roles en AdminShell). Por defecto, solo ADMIN.
  const roles = (root.dataset.roles ?? 'ADMIN').split(',');
  let auth = leerClave();

  const pedir: Pedir = async (path, init = {}) => {
    const res = await fetch(`${api}${path}`, {
      ...init,
      headers: { ...init.headers, Authorization: `Bearer ${auth}`, 'Content-Type': 'application/json' },
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
    try {
      const { token, usuario } = await iniciarSesion(api, email, password);
      if (!roles.includes(usuario.rol)) throw new SinPermiso();
      auth = token;
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
            ? 'Tu usuario no tiene permiso para esta pantalla.'
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
