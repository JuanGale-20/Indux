import { Injectable, inject } from '@angular/core';
import { EmpresasApi } from '../core/api';

@Injectable({ providedIn: 'root' })
export class EmpresasService {
  private readonly api = inject(EmpresasApi);
  listar = () => this.api.listar();
  obtener = (id: number) => this.api.obtener(id);
  crear = (data: Parameters<EmpresasApi['crear']>[0]) => this.api.crear(data);
  actualizar = (id: number, data: Parameters<EmpresasApi['actualizar']>[1]) => this.api.actualizar(id, data);
  desactivar = (id: number) => this.api.desactivar(id);
}
