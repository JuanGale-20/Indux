import { describe, expect, it } from 'vitest';
import { PLANES, ahorro, formatearCOP, precioPorMes } from './planes';

describe('Planes & Pricing Utilities', () => {
  it('contiene 4 planes configurados', () => {
    expect(PLANES.length).toBe(4);
    expect(PLANES.map((p) => p.id)).toEqual(['mensual', 'trimestral', 'semestral', 'anual']);
  });

  it('calcula el precio mensual promedio correctamente', () => {
    const semestral = PLANES.find((p) => p.id === 'semestral')!;
    // 504000 / 6 = 84000
    expect(precioPorMes(semestral)).toBe(84000);

    const anual = PLANES.find((p) => p.id === 'anual')!;
    // 948000 / 12 = 79000
    expect(precioPorMes(anual)).toBe(79000);
  });

  it('calcula el ahorro porcentual respecto al precio mensual base', () => {
    const mensual = PLANES.find((p) => p.id === 'mensual')!;
    expect(ahorro(mensual)).toBe(0);

    const semestral = PLANES.find((p) => p.id === 'semestral')!;
    // (1 - 504000 / (6 * 99000)) * 100 = 15.15% -> 15%
    expect(ahorro(semestral)).toBe(15);

    const anual = PLANES.find((p) => p.id === 'anual')!;
    // (1 - 948000 / (12 * 99000)) * 100 = 20.2% -> 20%
    expect(ahorro(anual)).toBe(20);
  });

  it('formatea montos en pesos colombianos (COP)', () => {
    const formateado = formatearCOP(99000);
    expect(formateado).toContain('99');
    expect(formateado).toContain('$');
  });
});
