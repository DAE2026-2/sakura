// Contenido fijo de la landing. La carta, las experiencias y las preguntas frecuentes vienen de la API (src/lib/api.ts).
// Sakura es un restaurante ficticio: lo marcado como `pending` está por definir y solo se ve en desarrollo.

import type { ImageMetadata } from 'astro';
// Primer fotograma de cada clip del paseo (vídeos y pósters de midudev/mcp-higgsfield-landing).
import poster1 from '../assets/paseo/paseo-1.webp';
import poster2 from '../assets/paseo/paseo-2.webp';
import poster3 from '../assets/paseo/paseo-3.webp';
import poster4 from '../assets/paseo/paseo-4.webp';
import poster5 from '../assets/paseo/paseo-5.webp';
import poster6 from '../assets/paseo/paseo-6.webp';
import salon from '../assets/sakura/02_salon_principal.webp';
import comedorPrivado from '../assets/sakura/03_comedor_privado.webp';
import barraTeppanyaki from '../assets/sakura/04_barra_teppanyaki.webp';
import balcon from '../assets/sakura/05_mesa_para_dos_balcon.webp';
import jardin from '../assets/sakura/07_salon_privado_jardin.webp';
import terraza from '../assets/sakura/08_terraza_cerezos.webp';

export const seo = {
  title: 'Sakura · Cocina japonesa y barra teppanyaki',
  description:
    'Restaurante japonés con omakase, barra teppanyaki y terraza bajo los cerezos. Reserva tu mesa online en segundos.',
};

// Paseo (scroll world): un clip 16:9 sin audio por tramo, que avanza o retrocede con el scroll.
// Mientras el clip de un tramo no ha cargado se ve su primer fotograma (`poster`).
export type Parada = {
  id: string;
  lugar: string;
  /** Clip que va de esta parada a la siguiente. El de la última sigue hasta el final de la sección. */
  clip: string;
  /** Punto horizontal del vídeo (0 a 100) que queda a la vista en móvil, donde el 16:9 se recorta. */
  focus: number;
  poster: ImageMetadata;
};

export const paseo: { paradas: Parada[] } = {
  paradas: [
    { id: 'inicio', lugar: 'La entrada', clip: '/video/paseo-1.mp4', focus: 50, poster: poster1 },
    { id: 'sakura', lugar: 'Los espejos', clip: '/video/paseo-2.mp4', focus: 50, poster: poster2 },
    { id: 'menus', lugar: 'La mesa redonda', clip: '/video/paseo-3.mp4', focus: 50, poster: poster3 },
    { id: 'sala', lugar: 'La sala', clip: '/video/paseo-4.mp4', focus: 50, poster: poster4 },
    { id: 'barra', lugar: 'La barra de sushi', clip: '/video/paseo-5.mp4', focus: 50, poster: poster5 },
    { id: 'como-reservar', lugar: 'La bodega', clip: '/video/paseo-6.mp4', focus: 50, poster: poster6 },
  ],
};

export const ambientes = [
  { nombre: 'Salón principal', detalle: 'Mesas amplias bajo la luz cálida de las lámparas de papel.', foto: salon },
  { nombre: 'Barra teppanyaki', detalle: 'Cena frente a la plancha, con el cocinero a un metro.', foto: barraTeppanyaki },
  { nombre: 'Terraza de cerezos', detalle: 'Al aire libre, entre ramas en flor.', foto: terraza },
  { nombre: 'Comedor privado', detalle: 'Para celebraciones y comidas de empresa.', foto: comedorPrivado },
  { nombre: 'Mesa para dos', detalle: 'En el balcón, para las noches especiales.', foto: balcon },
  { nombre: 'Salón del jardín', detalle: 'Un salón privado abierto al jardín japonés.', foto: jardin },
];

export const contacto = {
  direccion: 'Dirección por definir',
  telefono: '+57 300 000 0000',
  telefonoHref: 'tel:+573000000000',
  email: 'reservas@sakura.example',
  instagram: '@sakura',
  horario: [
    { dias: 'Comidas', horas: '13:00 a 16:00' },
    { dias: 'Cenas', horas: '20:00 a 23:30' },
  ],
  pending: 'Dirección, teléfono, email e Instagram son de ejemplo.',
};

export const nav = [
  { label: 'Recorrido', href: '#sakura' },
  { label: 'Carta', href: '#carta' },
  { label: 'Experiencias', href: '#experiencias' },
  { label: 'Ambientes', href: '#ambientes' },
  { label: 'Preguntas', href: '#preguntas' },
];
