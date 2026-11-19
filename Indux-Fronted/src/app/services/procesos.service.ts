import { Injectable, inject } from '@angular/core';
import { ProcesosApi } from '../core/api';

@Injectable({ providedIn: 'root' })
export class ProcesosService {
  private readonly api = inject(ProcesosApi);
  listarPorProducto = (productoId: number) => this.api.listarPorProducto(productoId);
  obtener = (id: number) => this.api.obtener(id);
  crear = (data: Parameters<ProcesosApi['crear']>[0]) => this.api.crear(data);
  actualizar = (id: number, data: Parameters<ProcesosApi['actualizar']>[1]) => this.api.actualizar(id, data);
  eliminar = (id: number) => this.api.eliminar(id);
  listarRecursos = (procesoId: number) => this.api.listarRecursos(procesoId);
}
