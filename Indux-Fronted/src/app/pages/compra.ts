import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PLANES, formatearCOP } from '../core/planes';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-compra',
  imports: [RouterLink, AppIconComponent],
  template: `
    <section class="max-w-lg mx-auto px-4 py-16 app-fade-in">
      @if (plan; as p) {
        <h1 class="text-2xl font-bold text-slate-900 mb-6 text-center">Resumen de tu suscripción</h1>
        <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm mb-6">
          <div class="flex items-center justify-between pb-4 border-b border-slate-100">
            <div>
              <span class="text-xs font-semibold uppercase tracking-wider text-emerald-800 bg-emerald-50 px-2.5 py-1 rounded-full">
                Plan Seleccionado
              </span>
              <h2 class="text-xl font-bold text-slate-800 mt-2">{{ p.nombre }}</h2>
            </div>
            <span class="text-2xl font-extrabold text-slate-900">{{ formatear(p.precio) }}</span>
          </div>
          <p class="text-slate-600 text-sm mt-4">
            Duración del periodo: <strong class="text-slate-800">{{ p.meses }} {{ p.meses === 1 ? 'mes' : 'meses' }}</strong>
          </p>
          <p class="text-slate-500 text-xs mt-1">
            Acceso completo a la plataforma Indux y herramientas de análisis SteelForce.
          </p>
        </div>

        <div class="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-6 text-sm text-amber-800 flex items-start gap-3">
          <app-icon name="alert-triangle" [size]="16" class="text-amber-600 shrink-0 mt-0.5" />
          <p>
            <strong>Membresía simulada:</strong> Plataforma universitaria sin cobros reales. No se requieren tarjetas ni datos bancarios.
          </p>
        </div>

        @if (confirmado()) {
          <div class="rounded-2xl bg-emerald-50 border border-emerald-200 p-6 text-center app-slide-up">
            <div class="w-12 h-12 bg-emerald-100 text-emerald-700 rounded-full flex items-center justify-center mx-auto text-xl font-bold">
              <app-icon name="check-circle" [size]="24" />
            </div>
            <h3 class="mt-3 text-lg font-bold text-emerald-900">¡Suscripción Simulada Activa!</h3>
            <p class="mt-1 text-sm text-emerald-700">Tu plan {{ p.nombre }} ha quedado registrado.</p>
            <div class="mt-6 flex flex-col gap-2">
              <a
                routerLink="/login"
                class="rounded-lg bg-emerald-700 px-4 py-2.5 text-center font-medium text-white hover:bg-emerald-800 transition"
              >
                Iniciar sesión en Indux
              </a>
              <a
                routerLink="/planes"
                class="text-sm text-slate-600 hover:text-slate-900 py-1"
              >
                Volver a los planes
              </a>
            </div>
          </div>
        } @else {
          <button
            (click)="confirmar()"
            [disabled]="confirmando()"
            class="w-full rounded-xl bg-emerald-700 px-4 py-3 font-semibold text-white hover:bg-emerald-800 transition disabled:opacity-60 shadow-sm"
          >
            {{ confirmando() ? 'Activando membresía…' : 'Confirmar activación simulada' }}
          </button>
          <a
            routerLink="/planes"
            class="mt-4 block text-center text-sm text-slate-500 hover:text-slate-800"
          >
            Cancelar y volver
          </a>
        }
      } @else {
        <div class="text-center py-12">
          <p class="text-slate-500 text-sm">Plan no encontrado.</p>
          <a routerLink="/planes" class="mt-4 inline-block text-emerald-700 text-xs font-semibold underline">
            Ver catálogo de planes
          </a>
        </div>
      }
    </section>
  `,
})
export class Compra {
  private readonly ruta = inject(ActivatedRoute);
  protected readonly plan = PLANES.find((p) => p.id === this.ruta.snapshot.paramMap.get('id'));
  protected readonly formatear = formatearCOP;
  protected readonly confirmando = signal(false);
  protected readonly confirmado = signal(false);

  confirmar(): void {
    this.confirmando.set(true);
    setTimeout(() => {
      this.confirmando.set(false);
      this.confirmado.set(true);
    }, 400);
  }
}
