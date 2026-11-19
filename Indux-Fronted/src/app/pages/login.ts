import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  template: `
    <section class="max-w-md mx-auto px-4 py-16">
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-emerald-800 text-white font-bold text-xl shadow-sm mb-3">
          Ix
        </div>
        <h1 class="text-2xl font-extrabold text-slate-900">Acceder a Indux</h1>
        <p class="text-sm text-slate-500 mt-1">Plataforma de Diagnóstico de Capacidad SteelForce</p>
      </div>

      <form
        [formGroup]="form"
        (ngSubmit)="enviar()"
        class="space-y-4 rounded-2xl border border-slate-200 bg-white p-8 shadow-sm"
      >
        <div>
          <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="email">
            Correo Electrónico
          </label>
          <input
            id="email"
            type="email"
            formControlName="email"
            autocomplete="username"
            placeholder="usuario@ejemplo.com"
            class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
          />
          @if (form.controls.email.touched && form.controls.email.invalid) {
            <p class="mt-1 text-xs text-rose-600">Ingresa un correo electrónico válido.</p>
          }
        </div>

        <div>
          <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="password">
            Contraseña
          </label>
          <input
            id="password"
            type="password"
            formControlName="password"
            autocomplete="current-password"
            placeholder="••••••••"
            class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
          />
          @if (form.controls.password.touched && form.controls.password.invalid) {
            <p class="mt-1 text-xs text-rose-600">La contraseña es obligatoria.</p>
          }
        </div>

        @if (error(); as mensaje) {
          <div class="rounded-xl bg-rose-50 border border-rose-200 p-3.5 text-xs text-rose-700">
            {{ mensaje }}
          </div>
        }

        <button
          type="submit"
          [disabled]="cargando()"
          class="w-full rounded-xl bg-emerald-700 px-4 py-3 font-semibold text-white hover:bg-emerald-800 transition disabled:opacity-60 shadow-sm"
        >
          {{ cargando() ? 'Autenticando…' : 'Iniciar Sesión' }}
        </button>

        <div class="pt-2 text-center">
          <p class="text-xs text-slate-500">
            ¿Credenciales por defecto? Consulta con el administrador del sistema.
          </p>
        </div>
      </form>
    </section>
  `,
})
export class Login {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected readonly error = signal<string | null>(null);
  protected readonly cargando = signal(false);

  protected enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set(null);

    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: () => this.router.navigateByUrl('/app'),
      error: (e: HttpErrorResponse) => {
        this.cargando.set(false);
        if (e.status === 401) {
          this.error.set('Credenciales inválidas. Verifica tu correo y contraseña.');
        } else if (e.status === 0) {
          this.error.set('No se pudo conectar con el servidor (http://localhost:8080). ¿Está encendido el backend?');
        } else if (e.error?.detail) {
          this.error.set(e.error.detail);
        } else {
          this.error.set('Ocurrió un error inesperado al intentar iniciar sesión.');
        }
      },
    });
  }
}
