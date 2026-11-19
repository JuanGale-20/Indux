export type Rol = 'ADMIN' | 'LIDER' | 'ANALISTA';

export type EstadoRecurso = 'DISPONIBLE' | 'NO_DISPONIBLE';

export type FrecuenciaReporte = 'DIARIA' | 'SEMANAL' | 'MENSUAL' | 'ANUAL';

export type EstadoReporte = 'BORRADOR' | 'VALIDADO' | 'CON_ERRORES' | 'ANALIZADO';

export type ClasificacionRestriccion =
  | 'BAJA_UTILIZACION'
  | 'UTILIZACION_ADECUADA'
  | 'CAPACIDAD_AJUSTADA'
  | 'SOBRECARGA';

export type NivelRecomendacion =
  | 'CRITICA'
  | 'ALTA'
  | 'MEDIA'
  | 'BAJA'
  | 'INFORMATIVA';

export type FactorCausa =
  | 'DEMANDA'
  | 'TIEMPO_PROCESAMIENTO'
  | 'MERMA'
  | 'RECURSO_COMPARTIDO'
  | 'TIEMPO_NO_DISPONIBLE';

export type DireccionTendencia =
  | 'CRECIENTE'
  | 'DECRECIENTE'
  | 'ESTABLE'
  | 'SIN_DATOS_SUFICIENTES';

export interface Usuario {
  id: number;
  empresaId: number | null;
  nombre: string;
  email: string;
  rol: Rol;
  activo: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEnSegundos: number;
  usuario: Usuario;
}

export interface Empresa {
  id: number;
  nombre: string;
  sector: string | null;
  activa: boolean;
}

export interface CrearEmpresaRequest {
  nombre: string;
  sector?: string | null;
}

export interface ActualizarEmpresaRequest {
  nombre: string;
  sector?: string | null;
}

export interface RegistrarUsuarioRequest {
  empresaId?: number | null;
  nombre: string;
  email: string;
  password: string;
  rol: Rol;
}

export interface ActualizarUsuarioRequest {
  empresaId?: number | null;
  nombre: string;
  email: string;
  password?: string | null;
  rol: Rol;
}

export interface Producto {
  id: number;
  empresaId: number;
  codigo: string;
  nombre: string;
  unidadMedida: string;
}

export interface CrearProductoRequest {
  empresaId: number;
  codigo: string;
  nombre: string;
  unidadMedida: string;
}

export interface ActualizarProductoRequest {
  codigo: string;
  nombre: string;
  unidadMedida: string;
}

export interface Proceso {
  id: number;
  productoId: number;
  liderId: number | null;
  codigo: string;
  nombre: string;
  secuencia: number;
  unidadProcesada: string;
  merma: number | null;
  unidadEntrada: string | null;
}

export interface CrearProcesoRequest {
  productoId: number;
  liderId?: number | null;
  codigo: string;
  nombre: string;
  secuencia: number;
  unidadProcesada: string;
  merma?: number | null;
  unidadEntrada?: string | null;
}

export interface ActualizarProcesoRequest {
  liderId?: number | null;
  codigo: string;
  nombre: string;
  secuencia: number;
  unidadProcesada: string;
  merma?: number | null;
  unidadEntrada?: string | null;
}

export interface Recurso {
  id: number;
  empresaId: number;
  codigo: string;
  nombre: string;
  tipo: string | null;
  cantidadDisponible: number;
  estado: EstadoRecurso;
}

export interface CrearRecursoRequest {
  empresaId: number;
  codigo: string;
  nombre: string;
  tipo?: string | null;
  cantidadDisponible: number;
  estado?: string | null;
}

export interface ActualizarRecursoRequest {
  codigo: string;
  nombre: string;
  tipo?: string | null;
  cantidadDisponible: number;
  estado?: string | null;
}

export interface ProcesoRecurso {
  procesoId: number;
  recursoId: number;
  tiempoReferencia: number | null;
}

export interface VincularProcesoRequest {
  procesoId: number;
  tiempoReferencia?: number | null;
}

export interface LineaReporteRequest {
  recursoId: number;
  tiempoProcesamiento: number;
  unidadReferencia: string;
  tiempoCalendario: number;
  tiempoNoDisponible?: number | null;
  numeroTurnos: number;
  horasPorTurno: number;
  diasOperacion: number;
}

export interface LineaReporte {
  id: number;
  recursoId: number;
  tiempoProcesamiento: number;
  unidadReferencia: string;
  tiempoCalendario: number;
  tiempoNoDisponible: number | null;
  numeroTurnos: number;
  horasPorTurno: number;
  diasOperacion: number;
}

export interface CrearReporteRequest {
  productoId: number;
  plantaArea: string;
  frecuencia: FrecuenciaReporte;
  periodoInicio: string; // ISO date YYYY-MM-DD
  periodoFin: string;    // ISO date YYYY-MM-DD
  demanda: number;
  produccionReal: number;
  lineas?: LineaReporteRequest[];
}

export interface ActualizarReporteRequest {
  plantaArea: string;
  frecuencia: FrecuenciaReporte;
  periodoInicio: string;
  periodoFin: string;
  demanda: number;
  produccionReal: number;
  lineas?: LineaReporteRequest[];
}

export interface ReporteResumen {
  id: number;
  productoId: number;
  creadoPor: number;
  plantaArea: string;
  frecuencia: FrecuenciaReporte;
  periodoInicio: string;
  periodoFin: string;
  demanda: number;
  produccionReal: number;
  estado: EstadoReporte;
}

export interface Reporte {
  id: number;
  productoId: number;
  creadoPor: number;
  plantaArea: string;
  frecuencia: FrecuenciaReporte;
  periodoInicio: string;
  periodoFin: string;
  demanda: number;
  produccionReal: number;
  estado: EstadoReporte;
  lineas: LineaReporte[];
}

export interface ValidacionResultado {
  grupo: string;
  variable: string;
  severidad: 'ERROR_CRITICO' | 'ADVERTENCIA' | string;
  mensaje: string;
}

export interface ResumenValidacion {
  estado: EstadoReporte;
  errores: number;
  advertencias: number;
  resultados: ValidacionResultado[];
}

export interface AnalisisRecurso {
  prioridad: number | null;
  reporteRecursoId: number;
  recursoId: number;
  codigo: string;
  nombre: string;
  aporteHoras: number;
  horasRequeridas: number;
  horasDisponibles: number;
  capacidad: number;
  utilizacion: number;
  holgura: number;
  deficitHoras: number;
  impacto: number;
  deficitCapacidad: number;
  clasificacion: ClasificacionRestriccion;
  diagnostico: string;
}

export interface AnalisisReporte {
  reporteId: number;
  estado: EstadoReporte;
  validaciones: ValidacionResultado[];
  recursos: AnalisisRecurso[];
}

export interface Recomendacion {
  recursoId: number;
  codigo: string;
  nombre: string;
  prioridad: number | null;
  nivel: NivelRecomendacion;
  resumen: string;
  acciones: string[];
}

export interface DetalleFactor {
  factor: FactorCausa;
  horas: number;
  porcentajeDelDeficit: number;
  descripcion: string;
}

export interface CausaDeficit {
  recursoId: number;
  codigo: string;
  nombre: string;
  prioridad: number | null;
  deficitHoras: number;
  causaPrincipal: FactorCausa;
  factores: DetalleFactor[];
}

export interface PuntoHistorico {
  reporteId: number;
  productoId: number;
  periodoInicio: string;
  periodoFin: string;
  frecuencia: FrecuenciaReporte;
  utilizacion: number;
  horasRequeridas: number;
  horasDisponibles: number;
  deficitHoras: number;
  impacto: number;
  clasificacion: ClasificacionRestriccion;
}

export interface Tendencia {
  direccion: DireccionTendencia;
  variacion: number;
  periodos: number;
  sostenida: boolean;
  mensaje: string;
}

export interface HistoricoRecurso {
  recursoId: number;
  codigo: string;
  nombre: string;
  puntos: PuntoHistorico[];
  tendencia: Tendencia | null;
}

export interface ReporteImportadoResumen {
  productoCodigo: string;
  plantaArea: string;
  frecuencia: FrecuenciaReporte;
  periodoInicio: string;
  periodoFin: string;
  recursos: number;
  reporteId?: number;
}

export interface ErrorImportacion {
  fila: number;
  columna: string;
  mensaje: string;
}

export interface ResultadoImportacion {
  valido: boolean;
  guardado: boolean;
  filasLeidas: number;
  reportes: ReporteImportadoResumen[];
  errores: ErrorImportacion[];
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errores?: Record<string, string>;
}

// Modelos para cálculo y simulación al vuelo (POST /api/analisis/calcular)
export interface RecursoCalculoRequest {
  nombre: string;
  tiempoProcesamiento: number;
  turnos: number;
  horasPorTurno: number;
  diasOperacion: number;
  cantidadRecursos: number;
}

export interface AnalizarRequest {
  demanda: number;
  recursos: RecursoCalculoRequest[];
}

export interface RecursoCalculoResultado {
  prioridad: number;
  nombre: string;
  horasRequeridas: number;
  horasDisponibles: number;
  capacidad: number;
  utilizacion: number;
  holgura: number;
  deficitHoras: number;
  impacto: number;
  deficitCapacidad: number;
  clasificacion: ClasificacionRestriccion;
  alerta: string;
}

export interface AnalisisResponse {
  recursos: RecursoCalculoResultado[];
}

// Modelos para Asistente IA (POST /api/asistente/chat)
export interface MensajeHistorial {
  rol: 'user' | 'assistant' | 'system';
  contenido: string;
}

export interface ConsultaAsistenteRequest {
  mensaje: string;
  reporteId?: number | null;
  historial?: MensajeHistorial[];
}

export interface RespuestaAsistenteResponse {
  respuesta: string;
  modelo: string;
  proveedorConfigurado: boolean;
  fuentesContexto: string[];
  motor?: 'GEMINI' | 'LOCAL';
}
