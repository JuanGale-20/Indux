import { describe, expect, it } from 'vitest';
import {
  obtenerClasificacionInfo,
  obtenerEstadoReporteInfo,
  obtenerFactorCausaInfo,
  obtenerNivelRecomendacionInfo,
  obtenerTendenciaInfo,
} from './steelforce-constants';

describe('SteelForce Constants & Helpers', () => {
  it('clasifica correctamente SOBRECARGA', () => {
    const info = obtenerClasificacionInfo('SOBRECARGA');
    expect(info.clave).toBe('SOBRECARGA');
    expect(info.rango).toBe('> 100 %');
    expect(info.textColor).toContain('text-rose');
  });

  it('clasifica correctamente UTILIZACION_ADECUADA', () => {
    const info = obtenerClasificacionInfo('UTILIZACION_ADECUADA');
    expect(info.clave).toBe('UTILIZACION_ADECUADA');
    expect(info.rango).toBe('80 % – 90 %');
  });

  it('retorna fallback para clasificaciones desconocidas', () => {
    const info = obtenerClasificacionInfo('DESCONOCIDA' as any);
    expect(info.clave).toBe('BAJA_UTILIZACION');
  });

  it('retorna etiqueta correcta de estado de reporte', () => {
    expect(obtenerEstadoReporteInfo('ANALIZADO').etiqueta).toBe('Analizado');
    expect(obtenerEstadoReporteInfo('BORRADOR').etiqueta).toBe('Borrador');
    expect(obtenerEstadoReporteInfo('CON_ERRORES').etiqueta).toBe('Con Errores');
  });

  it('obtiene nivel de recomendación info', () => {
    expect(obtenerNivelRecomendacionInfo('CRITICA').etiqueta).toBe('Crítica');
    expect(obtenerNivelRecomendacionInfo('ALTA').etiqueta).toBe('Alta');
  });

  it('obtiene tendencia info', () => {
    expect(obtenerTendenciaInfo('CRECIENTE').icono).toBe('↗');
    expect(obtenerTendenciaInfo('DECRECIENTE').icono).toBe('↘');
  });

  it('obtiene factor de causa info', () => {
    expect(obtenerFactorCausaInfo('DEMANDA').etiqueta).toBe('Alta Demanda');
    expect(obtenerFactorCausaInfo('MERMA').etiqueta).toContain('Merma');
  });
});
