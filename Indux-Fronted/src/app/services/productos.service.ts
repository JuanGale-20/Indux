import { Injectable, inject } from '@angular/core';
import { ProductosApi } from '../core/api';

@Injectable({ providedIn: 'root' })
export class ProductosService {
  private readonly api = inject(ProductosApi);
  listarPorEmpresa = (empresaId: number) => this.api.listarPorEmpresa(empresaId);
  obtener = (id: number) => this.api.obtener(id);
  crear = (data: Parameters<ProductosApi['crear']>[0]) => this.api.crear(data);
  actualizar = (id: number, data: Parameters<ProductosApi['actualizar']>[1]) => this.api.actualizar(id, data);
  eliminar = (id: number) => this.api.eliminar(id);
}
