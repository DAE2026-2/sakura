// Utilidades compartidas por las pantallas CRUD del panel: diálogo con formulario y errores por campo.
import { ErrorApi } from './admin-sesion';

export function initDialogo(root: HTMLElement) {
  const dialogo = root.querySelector<HTMLDialogElement>('[data-dialogo]')!;
  const form = dialogo.querySelector<HTMLFormElement>('[data-form]')!;
  const titulo = dialogo.querySelector<HTMLElement>('[data-dialogo-titulo]')!;
  const formError = form.querySelector<HTMLElement>('[data-form-error]')!;
  dialogo.querySelector('[data-cancelar]')?.addEventListener('click', () => dialogo.close());

  const limpiarErrores = () => {
    form.querySelectorAll<HTMLElement>('[data-error]').forEach((el) => (el.textContent = ''));
    form.querySelectorAll('[aria-invalid]').forEach((el) => el.removeAttribute('aria-invalid'));
    formError.textContent = '';
  };

  return {
    form,
    campo: (nombre: string) => form.elements.namedItem(nombre) as HTMLInputElement,
    abrir(texto: string) {
      form.reset();
      limpiarErrores();
      titulo.textContent = texto;
      dialogo.showModal();
    },
    cerrar: () => dialogo.close(),
    limpiarErrores,
    mostrarError(error: unknown) {
      formError.textContent = error instanceof Error ? error.message : 'No se pudo guardar.';
      if (!(error instanceof ErrorApi)) return;
      for (const [campo, mensaje] of Object.entries(error.campos)) {
        const hueco = form.querySelector<HTMLElement>(`[data-error="${campo}"]`);
        if (hueco) hueco.textContent = mensaje;
        form.querySelector(`[name="${campo}"]`)?.setAttribute('aria-invalid', 'true');
      }
    },
  };
}
