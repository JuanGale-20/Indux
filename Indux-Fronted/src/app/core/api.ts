import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ActualizarEmpresaRequest,
  ActualizarProcesoRequest,
  ActualizarProductoRequest,
  ActualizarRecursoRequest,
  ActualizarReporteRequest,
  ActualizarUsuarioRequest,
  AnalisisReporte,
  AnalisisResponse,
  AnalizarRequest,
  CausaDeficit,
  ConsultaAsistenteRequest,
  CrearEmpresaRequest,
  CrearProcesoRequest,
  CrearProductoRequest,
  CrearRecursoRequest,
  CrearReporteRequest,
  Empresa,
  HistoricoRecurso,
  Proceso,
  ProcesoRecurso,
  Producto,
  Recomendacion,
  Recurso,
  RegistrarUsuarioRequest,
  Reporte,
  ReporteResumen,
  RespuestaAsistenteResponse,
  ResultadoImportacion,
  ResumenValidacion,
  Usuario,
  ValidacionResultado,
  VincularProcesoRequest,
} from './models';

export const API_URL = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class EmpresasApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_URL}/empresas`;

  listar(): Observable<Empresa[]> {
    return this.http.get<Empresa[]>(this.baseUrl);
  }

  obtener(id: number): Observable<Empresa> {
    return this.http.get<Empresa>(`${this.baseUrl}/${id}`);
  }

  crear(data: CrearEmpresaRequest): Observable<Empresa> {
    return this.http.post<Empresa>(this.baseUrl, data);
  }

  actualizar(id: number, data: ActualizarEmpresaRequest): Observable<Empresa> {
    return this.http.put<Empresa>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class UsuariosApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_URL}/usuarios`;

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.baseUrl);
  }

  registrar(data: RegistrarUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.baseUrl, data);
  }

  actualizar(id: number, data: ActualizarUsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class ProductosApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_URL}/productos`;

  listarPorEmpresa(empresaId: number): Observable<Producto[]> {
    const params = new HttpParams().set('empresaId', empresaId.toString());
    return this.http.get<Producto[]>(this.baseUrl, { params });
  }

  obtener(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.baseUrl}/${id}`);
  }

  crear(data: CrearProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.baseUrl, data);
  }

  actualizar(id: number, data: ActualizarProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class ProcesosApi {
  private readonly http = inject(HttpClient);

  listarPorProducto(productoId: number): Observable<Proceso[]> {
    return this.http.get<Proceso[]>(`${API_URL}/productos/${productoId}/procesos`);
  }

  obtener(id: number): Observable<Proceso> {
    return this.http.get<Proceso>(`${API_URL}/procesos/${id}`);
  }

  crear(data: CrearProcesoRequest): Observable<Proceso> {
    return this.http.post<Proceso>(`${API_URL}/procesos`, data);
  }

  actualizar(id: number, data: ActualizarProcesoRequest): Observable<Proceso> {
    return this.http.put<Proceso>(`${API_URL}/procesos/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/procesos/${id}`);
  }

  listarRecursos(procesoId: number): Observable<Recurso[]> {
    return this.http.get<Recurso[]>(`${API_URL}/procesos/${procesoId}/recursos`);
  }
}

@Injectable({ providedIn: 'root' })
export class RecursosApi {
  private readonly http = inject(HttpClient);

  listarPorEmpresa(empresaId: number): Observable<Recurso[]> {
    const params = new HttpParams().set('empresaId', empresaId.toString());
    return this.http.get<Recurso[]>(`${API_URL}/recursos`, { params });
  }

  obtener(id: number): Observable<Recurso> {
    return this.http.get<Recurso>(`${API_URL}/recursos/${id}`);
  }

  crear(data: CrearRecursoRequest): Observable<Recurso> {
    return this.http.post<Recurso>(`${API_URL}/recursos`, data);
  }

  actualizar(id: number, data: ActualizarRecursoRequest): Observable<Recurso> {
    return this.http.put<Recurso>(`${API_URL}/recursos/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/recursos/${id}`);
  }

  listarVinculos(recursoId: number): Observable<ProcesoRecurso[]> {
    return this.http.get<ProcesoRecurso[]>(`${API_URL}/recursos/${recursoId}/procesos`);
  }

  vincular(recursoId: number, data: VincularProcesoRequest): Observable<ProcesoRecurso> {
    return this.http.post<ProcesoRecurso>(`${API_URL}/recursos/${recursoId}/procesos`, data);
  }

  desvincular(recursoId: number, procesoId: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/recursos/${recursoId}/procesos/${procesoId}`);
  }

  obtenerHistorico(recursoId: number, limite: number = 12): Observable<HistoricoRecurso> {
    const params = new HttpParams().set('limite', limite.toString());
    return this.http.get<HistoricoRecurso>(`${API_URL}/recursos/${recursoId}/historico`, { params });
  }
}

@Injectable({ providedIn: 'root' })
export class ReportesApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_URL}/reportes`;

  listar(params?: { productoId?: number; empresaId?: number }): Observable<ReporteResumen[]> {
    let httpParams = new HttpParams();
    if (params?.productoId != null) {
      httpParams = httpParams.set('productoId', params.productoId.toString());
    } else if (params?.empresaId != null) {
      httpParams = httpParams.set('empresaId', params.empresaId.toString());
    }
    return this.http.get<ReporteResumen[]>(this.baseUrl, { params: httpParams });
  }

  obtener(id: number): Observable<Reporte> {
    return this.http.get<Reporte>(`${this.baseUrl}/${id}`);
  }

  crear(data: CrearReporteRequest): Observable<Reporte> {
    return this.http.post<Reporte>(this.baseUrl, data);
  }

  actualizar(id: number, data: ActualizarReporteRequest): Observable<Reporte> {
    return this.http.put<Reporte>(`${this.baseUrl}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  validar(id: number): Observable<ResumenValidacion> {
    return this.http.post<ResumenValidacion>(`${this.baseUrl}/${id}/validar`, {});
  }

  obtenerValidaciones(id: number): Observable<ValidacionResultado[]> {
    return this.http.get<ValidacionResultado[]>(`${this.baseUrl}/${id}/validaciones`);
  }

  analizar(id: number): Observable<AnalisisReporte> {
    return this.http.post<AnalisisReporte>(`${this.baseUrl}/${id}/analizar`, {});
  }

  obtenerAnalisis(id: number): Observable<AnalisisReporte> {
    return this.http.get<AnalisisReporte>(`${this.baseUrl}/${id}/analisis`);
  }

  obtenerRecomendaciones(id: number): Observable<Recomendacion[]> {
    return this.http.get<Recomendacion[]>(`${this.baseUrl}/${id}/recomendaciones`);
  }

  obtenerCausas(id: number): Observable<CausaDeficit[]> {
    return this.http.get<CausaDeficit[]>(`${this.baseUrl}/${id}/causas`);
  }
}

@Injectable({ providedIn: 'root' })
export class AnalisisApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_URL}/analisis`;

  calcular(data: AnalizarRequest): Observable<AnalisisResponse> {
    return this.http.post<AnalisisResponse>(`${this.baseUrl}/calcular`, data);
  }
}

@Injectable({ providedIn: 'root' })
export class ImportacionesApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_URL}/importaciones`;

  importar(archivo: File, confirmar: boolean, empresaId?: number | null): Observable<ResultadoImportacion> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    let params = new HttpParams().set('confirmar', String(confirmar));
    if (empresaId != null) {
      params = params.set('empresaId', empresaId.toString());
    }
    return this.http.post<ResultadoImportacion>(`${this.baseUrl}/reportes`, formData, { params });
  }

  descargarPlantilla(formato: 'xlsx' | 'csv' = 'xlsx'): Observable<Blob> {
    const params = new HttpParams().set('formato', formato);
    return this.http.get(`${this.baseUrl}/reportes/plantilla`, {
      params,
      responseType: 'blob',
    });
  }
}

@Injectable({ providedIn: 'root' })
export class AsistenteApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_URL}/asistente`;

  consultar(data: ConsultaAsistenteRequest): Observable<RespuestaAsistenteResponse> {
    return this.http.post<RespuestaAsistenteResponse>(`${this.baseUrl}/chat`, data);
  }
}
