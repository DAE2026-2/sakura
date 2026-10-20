// Tipos y formato de pedidos compartidos por la pantalla del mesero y el historial.

export type EstadoPedido = 'PENDIENTE' | 'EN_PREPARACION' | 'SERVIDO' | 'PAGADO' | 'CANCELADO';

export type Detalle = { id: number; productoId: number; producto: string; cantidad: number; precioUnitario: number; subtotal: number };

export type Pedido = {
  id: number;
  mesaId: number;
  mesa: number;
  usuario?: string;
  estado: EstadoPedido;
  total: number;
  notas?: string;
  creadoEn: string;
  actualizadoEn?: string;
  detalles: Detalle[];
};

export const nombreEstado: Record<EstadoPedido, string> = {
  PENDIENTE: 'Pendiente',
  EN_PREPARACION: 'En preparación',
  SERVIDO: 'Servido',
  PAGADO: 'Pagado',
  CANCELADO: 'Cancelado',
};

const fechaHora = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
export const formatoFecha = (iso: string) => fechaHora.format(new Date(iso));
