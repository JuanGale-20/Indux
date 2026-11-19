import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API_URL } from '../core/api';

@Injectable({ providedIn: 'root' })
export class ValidacionesService {
  private readonly http = inject(HttpClient);

  validarEmpresa = (data: { nombre: string; sector?: string }) =>
    this.http.post<any>(`${API_URL}/validaciones/empresa`, data);

  validarProducto = (data: { codigo: string; nombre: string; unidadMedida: string }) =>
    this.http.post<any>(`${API_URL}/validaciones/producto`, data);

  validarProceso = (data: { codigo: string; nombre: string; secuencia?: number; unidadProcesada: string }) =>
    this.http.post<any>(`${API_URL}/validaciones/proceso`, data);
}
