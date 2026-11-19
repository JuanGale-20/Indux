import { Injectable, inject } from '@angular/core';
import { UsuariosApi } from '../core/api';

@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private readonly api = inject(UsuariosApi);
  listar = () => this.api.listar();
  registrar = (data: Parameters<UsuariosApi['registrar']>[0]) => this.api.registrar(data);
  actualizar = (id: number, data: Parameters<UsuariosApi['actualizar']>[1]) => this.api.actualizar(id, data);
  desactivar = (id: number) => this.api.desactivar(id);
}
