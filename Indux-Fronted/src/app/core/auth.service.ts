import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { API_URL } from './api';
import { LoginResponse, Rol, Usuario } from './models';

interface Sesion {
  token: string;
  usuario: Usuario;
  expiraEn: number;
}

const CLAVE = 'indux.sesion';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sesion = signal<Sesion | null>(this.leer());

  readonly usuario = computed(() => this.sesion()?.usuario ?? null);
  readonly token = computed(() => this.sesion()?.token ?? null);
  readonly autenticado = computed(() => this.sesion() !== null);

  login(email: string, password: string) {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, { email, password }).pipe(
      tap((r) =>
        this.guardar({
          token: r.token,
          usuario: r.usuario,
          expiraEn: Date.now() + r.expiraEnSegundos * 1000,
        }),
      ),
    );
  }

  logout(): void {
    localStorage.removeItem(CLAVE);
    this.sesion.set(null);
    this.router.navigateByUrl('/login');
  }

  tieneRol(...roles: Rol[]): boolean {
    const usuario = this.usuario();
    return usuario !== null && roles.includes(usuario.rol);
  }

  private guardar(sesion: Sesion): void {
    localStorage.setItem(CLAVE, JSON.stringify(sesion));
    this.sesion.set(sesion);
  }

  private leer(): Sesion | null {
    try {
      const crudo = localStorage.getItem(CLAVE);
      if (!crudo) return null;
      const sesion = JSON.parse(crudo) as Sesion;
      return sesion.expiraEn > Date.now() ? sesion : null;
    } catch {
      return null;
    }
  }
}