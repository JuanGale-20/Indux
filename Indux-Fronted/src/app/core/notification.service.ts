import { Injectable, signal } from '@angular/core';

export interface Notificacion {
  id: number;
  tipo: 'info' | 'exito' | 'advertencia' | 'error';
  titulo?: string;
  mensaje: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private contador = 0;
  readonly notificaciones = signal<Notificacion[]>([]);

  mostrar(mensaje: string, tipo: 'info' | 'exito' | 'advertencia' | 'error' = 'info', titulo?: string): void {
    const id = ++this.contador;
    const item: Notificacion = { id, tipo, mensaje, titulo };
    this.notificaciones.update((arr) => [...arr, item]);
    setTimeout(() => this.remover(id), 5000);
  }

  exito(mensaje: string, titulo?: string): void {
    this.mostrar(mensaje, 'exito', titulo ?? 'Operación exitosa');
  }

  error(mensaje: string, titulo?: string): void {
    this.mostrar(mensaje, 'error', titulo ?? 'Error');
  }

  advertencia(mensaje: string, titulo?: string): void {
    this.mostrar(mensaje, 'advertencia', titulo ?? 'Atención');
  }

  info(mensaje: string, titulo?: string): void {
    this.mostrar(mensaje, 'info', titulo);
  }

  remover(id: number): void {
    this.notificaciones.update((arr) => arr.filter((n) => n.id !== id));
  }
}
