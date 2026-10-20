// Cliente de la API de Spring Boot (backend/). Las páginas la consultan al renderizarse: en `astro dev`, en cada
// petición; en `astro build`, una vez al compilar.

export const API_URL = import.meta.env.PUBLIC_API_URL ?? 'http://localhost:8080/api';

export type Producto = {
  id: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  disponible: boolean;
  destacado: boolean;
  categoriaId: number;
  categoria: string;
};

export type CategoriaMenu = { id: number; nombre: string; descripcion?: string; productos: Producto[] };

export type Experiencia = {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  personas: number;
  etiqueta?: string;
};

export type Faq = { id: number; pregunta: string; respuesta: string };

/** Si el backend no responde, la sección se pinta vacía y se avisa en la consola en vez de romper la página. */
async function get<T>(path: string): Promise<T[]> {
  try {
    const res = await fetch(`${API_URL}${path}`);
    if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
    return (await res.json()) as T[];
  } catch (error) {
    console.warn(`[sakura] No se pudo leer ${API_URL}${path}. ¿Está arrancado el backend?`, error);
    return [];
  }
}

/** Menú público: categorías con sus productos disponibles. */
export const getMenu = () => get<CategoriaMenu>('/menu');
export const getExperiencias = () => get<Experiencia>('/experiencias');
export const getFaqs = () => get<Faq>('/faqs');

const cop = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
export const precio = (valor: number) => cop.format(valor);
