// Formulario de reserva: pide las horas libres al cambiar día o personas, envía la reserva y muestra el código.
// Toda la validación de negocio la hace el backend; aquí solo se pintan sus respuestas.

type Hora = { hora: string; plazasLibres: number };
type Disponibilidad = { horas: Hora[]; motivo?: string };
type Reserva = { codigo: string; fecha: string; hora: string; personas: number; nombre: string; estado: string };
type ApiError = { mensaje: string; campos?: Record<string, string> };

const fechaLarga = new Intl.DateTimeFormat('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });
const formatoFecha = (iso: string) => fechaLarga.format(new Date(`${iso}T12:00:00`));
const iso = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

async function pedir<T>(url: string, init?: RequestInit): Promise<T> {
  let res: Response;
  try {
    res = await fetch(url, init);
  } catch {
    throw { mensaje: 'No hay conexión con el servidor de reservas. Inténtalo de nuevo en un momento.' } satisfies ApiError;
  }
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw { mensaje: data.mensaje ?? 'Algo ha fallado. Inténtalo de nuevo.', campos: data.campos } as ApiError;
  return data as T;
}

export function initReservas() {
  const form = document.querySelector<HTMLFormElement>('[data-reserva]');
  if (!form) return;
  const api = form.dataset.api ?? '';
  const horas = form.querySelector<HTMLElement>('[data-horas]')!;
  const errorGeneral = form.querySelector<HTMLElement>('[data-form-error]')!;
  const confirmacion = document.querySelector<HTMLElement>('[data-confirmacion]')!;
  const submit = form.querySelector<HTMLButtonElement>('button[type="submit"]')!;
  const fecha = form.elements.namedItem('fecha') as HTMLInputElement;
  const personas = form.elements.namedItem('personas') as HTMLSelectElement;

  const hoy = new Date();
  fecha.min = iso(hoy);
  fecha.max = iso(new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() + 60));

  const limpiarErrores = () => {
    form.querySelectorAll<HTMLElement>('[data-error]').forEach((el) => (el.textContent = ''));
    form.querySelectorAll('[aria-invalid]').forEach((el) => el.removeAttribute('aria-invalid'));
    errorGeneral.textContent = '';
  };

  const mostrarError = (error: ApiError) => {
    errorGeneral.textContent = error.mensaje;
    for (const [campo, mensaje] of Object.entries(error.campos ?? {})) {
      const hueco = form.querySelector<HTMLElement>(`[data-error="${campo}"]`);
      if (hueco) hueco.textContent = mensaje;
      form.querySelector(`[name="${campo}"]`)?.setAttribute('aria-invalid', 'true');
    }
  };

  const aviso = (texto: string) => {
    horas.replaceChildren(Object.assign(document.createElement('p'), { className: 'text-sm text-crema/50', textContent: texto }));
  };

  let peticion = 0;
  const cargarHoras = async () => {
    if (!fecha.value) return;
    const id = ++peticion;
    aviso('Buscando horas libres…');
    try {
      const data = await pedir<Disponibilidad>(`${api}/disponibilidad?fecha=${fecha.value}&personas=${personas.value}`);
      if (id !== peticion) return;
      if (!data.horas.length) return aviso(data.motivo ?? 'No quedan horas libres este día.');
      horas.replaceChildren(
        ...data.horas.map(({ hora }) => {
          const label = document.createElement('label');
          label.className = 'slot relative';
          label.innerHTML = `<input type="radio" name="hora" value="${hora}" required /><span>${hora}</span>`;
          return label;
        }),
      );
    } catch (error) {
      if (id === peticion) aviso((error as ApiError).mensaje);
    }
  };

  fecha.addEventListener('change', cargarHoras);
  personas.addEventListener('change', cargarHoras);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    limpiarErrores();
    const datos = Object.fromEntries(new FormData(form)) as Record<string, string>;
    if (!datos.hora) {
      form.querySelector<HTMLElement>('[data-error="hora"]')!.textContent = fecha.value ? 'Elige una hora' : 'Elige primero un día';
    }

    submit.disabled = true;
    try {
      const reserva = await pedir<Reserva>(`${api}/reservas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...datos, personas: Number(datos.personas), hora: datos.hora || null, fecha: datos.fecha || null }),
      });
      confirmacion.querySelector('[data-resumen]')!.textContent =
        `${reserva.nombre}, ${formatoFecha(reserva.fecha)} a las ${reserva.hora}, ${reserva.personas} ${reserva.personas === 1 ? 'persona' : 'personas'}`;
      confirmacion.querySelector('[data-codigo]')!.textContent = reserva.codigo;
      form.hidden = true;
      confirmacion.hidden = false;
      confirmacion.focus();
    } catch (error) {
      mostrarError(error as ApiError);
      // Si la hora se acaba de llenar, se refrescan las disponibles.
      if ((error as ApiError).campos === undefined) cargarHoras();
    } finally {
      submit.disabled = false;
    }
  });

  confirmacion.querySelector('[data-otra]')?.addEventListener('click', () => {
    form.reset();
    aviso('Elige un día para ver las horas libres.');
    confirmacion.hidden = true;
    form.hidden = false;
  });

  initGestion(api);
}

/** Consultar y cancelar una reserva con su código y el email con el que se hizo. */
function initGestion(api: string) {
  const form = document.querySelector<HTMLFormElement>('[data-gestion]');
  if (!form) return;
  const resultado = form.querySelector<HTMLElement>('[data-gestion-resultado]')!;
  const estados: Record<string, string> = { PENDIENTE: 'Pendiente de confirmar', CONFIRMADA: 'Confirmada', CANCELADA: 'Cancelada' };

  const pintar = (r: Reserva, email: string) => {
    resultado.innerHTML = '';
    const p = document.createElement('p');
    p.className = 'mt-2';
    p.textContent = `${r.codigo} · ${formatoFecha(r.fecha)} a las ${r.hora} · ${r.personas} ${r.personas === 1 ? 'persona' : 'personas'} · ${estados[r.estado] ?? r.estado}`;
    resultado.append(p);
    if (r.estado === 'CANCELADA') return;
    const boton = document.createElement('button');
    boton.type = 'button';
    boton.className = 'btn btn-ghost btn-sm mt-4';
    boton.innerHTML = '<span>Cancelar esta reserva</span>';
    boton.addEventListener('click', async () => {
      if (!confirm('¿Seguro que quieres cancelar la reserva?')) return;
      try {
        const cancelada = await pedir<Reserva>(`${api}/reservas/${encodeURIComponent(r.codigo)}?email=${encodeURIComponent(email)}`, { method: 'DELETE' });
        pintar(cancelada, email);
      } catch (error) {
        resultado.append(Object.assign(document.createElement('p'), { className: 'field-error mt-2', textContent: (error as ApiError).mensaje }));
      }
    });
    resultado.append(boton);
  };

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const { codigo, email } = Object.fromEntries(new FormData(form)) as Record<string, string>;
    if (!codigo.trim() || !email.trim()) {
      resultado.innerHTML = '<p class="field-error mt-2">Escribe el código y el email.</p>';
      return;
    }
    try {
      const reserva = await pedir<Reserva>(
        `${api}/reservas/${encodeURIComponent(codigo.trim().toUpperCase())}?email=${encodeURIComponent(email.trim())}`,
      );
      pintar(reserva, email.trim());
    } catch (error) {
      resultado.innerHTML = '';
      resultado.append(Object.assign(document.createElement('p'), { className: 'field-error mt-2', textContent: (error as ApiError).mensaje }));
    }
  });
}
