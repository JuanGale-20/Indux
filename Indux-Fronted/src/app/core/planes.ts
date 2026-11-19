export interface Plan {
  id: string;
  nombre: string;
  meses: number;
  precio: number;
  destacado?: boolean;
}

export const PLANES: Plan[] = [
  { id: 'mensual', nombre: 'Mensual', meses: 1, precio: 99000 },
  { id: 'trimestral', nombre: 'Trimestral', meses: 3, precio: 267000 },
  { id: 'semestral', nombre: 'Semestral', meses: 6, precio: 504000, destacado: true },
  { id: 'anual', nombre: 'Anual', meses: 12, precio: 948000 },
];

const PRECIO_MENSUAL = 99000;

export const precioPorMes = (plan: Plan): number => Math.round(plan.precio / plan.meses);

export const ahorro = (plan: Plan): number =>
  Math.round((1 - plan.precio / (plan.meses * PRECIO_MENSUAL)) * 100);

export const formatearCOP = (valor: number): string =>
  new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(valor);