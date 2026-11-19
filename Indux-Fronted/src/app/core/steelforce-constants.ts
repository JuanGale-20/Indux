import {
  ClasificacionRestriccion,
  DireccionTendencia,
  EstadoReporte,
  FactorCausa,
  NivelRecomendacion,
} from './models';

export interface ClasificacionInfo {
  clave: ClasificacionRestriccion;
  etiqueta: string;
  rango: string;
  badgeClass: string;
  barColor: string;
  dotColor: string;
  bgLight: string;
  textColor: string;
  descripcion: string;
}

export const MAPA_CLASIFICACION: Record<ClasificacionRestriccion, ClasificacionInfo> = {
  BAJA_UTILIZACION: {
    clave: 'BAJA_UTILIZACION',
    etiqueta: 'Baja Utilización',
    rango: '< 80 %',
    badgeClass: 'bg-sky-50 text-sky-700 border-sky-200',
    barColor: '#0284c7',
    dotColor: 'bg-sky-500',
    bgLight: 'bg-sky-50',
    textColor: 'text-sky-700',
    descripcion: 'Capacidad ociosa. Margen para absorber más carga.',
  },
  UTILIZACION_ADECUADA: {
    clave: 'UTILIZACION_ADECUADA',
    etiqueta: 'Utilización Adecuada',
    rango: '80 % – 90 %',
    badgeClass: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    barColor: '#059669',
    dotColor: 'bg-emerald-500',
    bgLight: 'bg-emerald-50',
    textColor: 'text-emerald-700',
    descripcion: 'Operación balanceada y en rango óptimo de SteelForce.',
  },
  CAPACIDAD_AJUSTADA: {
    clave: 'CAPACIDAD_AJUSTADA',
    etiqueta: 'Capacidad Ajustada',
    rango: '> 90 % – 100 %',
    badgeClass: 'bg-amber-50 text-amber-700 border-amber-200',
    barColor: '#d97706',
    dotColor: 'bg-amber-500',
    bgLight: 'bg-amber-50',
    textColor: 'text-amber-700',
    descripcion: 'Cerca del límite. Vulnerable a retrasos o picos.',
  },
  SOBRECARGA: {
    clave: 'SOBRECARGA',
    etiqueta: 'Sobrecarga (Cuello de Botella)',
    rango: '> 100 %',
    badgeClass: 'bg-rose-50 text-rose-700 border-rose-200',
    barColor: '#e11d48',
    dotColor: 'bg-rose-500',
    bgLight: 'bg-rose-50',
    textColor: 'text-rose-700',
    descripcion: 'Restricción activa del sistema. Genera déficit y retrasos.',
  },
};

export function obtenerClasificacionInfo(c: ClasificacionRestriccion | string | null | undefined): ClasificacionInfo {
  if (c && c in MAPA_CLASIFICACION) {
    return MAPA_CLASIFICACION[c as ClasificacionRestriccion];
  }
  return {
    clave: 'BAJA_UTILIZACION',
    etiqueta: c ?? 'Desconocida',
    rango: '-',
    badgeClass: 'bg-slate-100 text-slate-700 border-slate-200',
    barColor: '#94a3b8',
    dotColor: 'bg-slate-400',
    bgLight: 'bg-slate-50',
    textColor: 'text-slate-700',
    descripcion: '',
  };
}

export interface EstadoReporteInfo {
  etiqueta: string;
  badgeClass: string;
  dotColor: string;
}

export const MAPA_ESTADO_REPORTE: Record<EstadoReporte, EstadoReporteInfo> = {
  BORRADOR: {
    etiqueta: 'Borrador',
    badgeClass: 'bg-slate-100 text-slate-700 border-slate-200',
    dotColor: 'bg-slate-400',
  },
  VALIDADO: {
    etiqueta: 'Validado',
    badgeClass: 'bg-blue-50 text-blue-700 border-blue-200',
    dotColor: 'bg-blue-500',
  },
  CON_ERRORES: {
    etiqueta: 'Con Errores',
    badgeClass: 'bg-rose-50 text-rose-700 border-rose-200',
    dotColor: 'bg-rose-500',
  },
  ANALIZADO: {
    etiqueta: 'Analizado',
    badgeClass: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    dotColor: 'bg-emerald-500',
  },
};

export function obtenerEstadoReporteInfo(e: EstadoReporte | string | null | undefined): EstadoReporteInfo {
  if (e && e in MAPA_ESTADO_REPORTE) {
    return MAPA_ESTADO_REPORTE[e as EstadoReporte];
  }
  return {
    etiqueta: e ?? 'Desconocido',
    badgeClass: 'bg-slate-100 text-slate-600 border-slate-200',
    dotColor: 'bg-slate-400',
  };
}

export interface NivelRecomendacionInfo {
  etiqueta: string;
  badgeClass: string;
  borderClass: string;
}

export const MAPA_NIVEL_RECOMENDACION: Record<NivelRecomendacion, NivelRecomendacionInfo> = {
  CRITICA: {
    etiqueta: 'Crítica',
    badgeClass: 'bg-rose-100 text-rose-800 border-rose-300',
    borderClass: 'border-l-rose-500',
  },
  ALTA: {
    etiqueta: 'Alta',
    badgeClass: 'bg-orange-100 text-orange-800 border-orange-300',
    borderClass: 'border-l-orange-500',
  },
  MEDIA: {
    etiqueta: 'Media',
    badgeClass: 'bg-amber-100 text-amber-800 border-amber-300',
    borderClass: 'border-l-amber-500',
  },
  BAJA: {
    etiqueta: 'Baja',
    badgeClass: 'bg-sky-100 text-sky-800 border-sky-300',
    borderClass: 'border-l-sky-500',
  },
  INFORMATIVA: {
    etiqueta: 'Informativa',
    badgeClass: 'bg-slate-100 text-slate-700 border-slate-300',
    borderClass: 'border-l-slate-400',
  },
};

export function obtenerNivelRecomendacionInfo(n: NivelRecomendacion | string): NivelRecomendacionInfo {
  if (n && n in MAPA_NIVEL_RECOMENDACION) {
    return MAPA_NIVEL_RECOMENDACION[n as NivelRecomendacion];
  }
  return {
    etiqueta: n ?? 'Info',
    badgeClass: 'bg-slate-100 text-slate-700 border-slate-200',
    borderClass: 'border-l-slate-300',
  };
}

export interface TendenciaInfo {
  etiqueta: string;
  badgeClass: string;
  icono: string;
}

export const MAPA_TENDENCIA: Record<DireccionTendencia, TendenciaInfo> = {
  CRECIENTE: {
    etiqueta: 'Creciente (Alerta)',
    badgeClass: 'bg-rose-100 text-rose-800 border-rose-300',
    icono: '↗',
  },
  DECRECIENTE: {
    etiqueta: 'Decreciente (Mejora)',
    badgeClass: 'bg-emerald-100 text-emerald-800 border-emerald-300',
    icono: '↘',
  },
  ESTABLE: {
    etiqueta: 'Estable',
    badgeClass: 'bg-blue-100 text-blue-800 border-blue-300',
    icono: '→',
  },
  SIN_DATOS_SUFICIENTES: {
    etiqueta: 'Sin datos suficientes',
    badgeClass: 'bg-slate-100 text-slate-700 border-slate-300',
    icono: '•',
  },
};

export function obtenerTendenciaInfo(t: DireccionTendencia | string | null | undefined): TendenciaInfo {
  if (t && t in MAPA_TENDENCIA) {
    return MAPA_TENDENCIA[t as DireccionTendencia];
  }
  return {
    etiqueta: t ?? 'Sin datos',
    badgeClass: 'bg-slate-100 text-slate-700 border-slate-300',
    icono: '•',
  };
}

export const MAPA_FACTOR_CAUSA: Record<FactorCausa, { etiqueta: string; descripcion: string }> = {
  DEMANDA: {
    etiqueta: 'Alta Demanda',
    descripcion: 'El volumen solicitado supera la capacidad disponible del recurso.',
  },
  TIEMPO_PROCESAMIENTO: {
    etiqueta: 'Tiempo de Procesamiento',
    descripcion: 'El estándar de procesamiento por unidad es elevado.',
  },
  MERMA: {
    etiqueta: 'Merma en Etapas Posteriores',
    descripcion: 'Pérdidas de material que exigen producir piezas adicionales.',
  },
  RECURSO_COMPARTIDO: {
    etiqueta: 'Recurso Compartido',
    descripcion: 'El recurso atiende múltiples productos de la misma empresa.',
  },
  TIEMPO_NO_DISPONIBLE: {
    etiqueta: 'Tiempo no disponible / Paradas',
    descripcion: 'Mantenimiento, paradas u horas no operativas reducen el tiempo útil.',
  },
};

export function obtenerFactorCausaInfo(f: FactorCausa | string) {
  if (f && f in MAPA_FACTOR_CAUSA) {
    return MAPA_FACTOR_CAUSA[f as FactorCausa];
  }
  return {
    etiqueta: f ?? 'Factor',
    descripcion: '',
  };
}
