import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PLANES, ahorro, formatearCOP, precioPorMes } from '../core/planes';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-planes',
  imports: [RouterLink, AppIconComponent],
  template: `
    <section class="max-w-6xl mx-auto px-4 py-16 app-fade-in">
      <div class="text-center max-w-2xl mx-auto">
        <span class="inline-block rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-800 uppercase tracking-wider">
          Membresías Informativas
        </span>
        <h1 class="mt-3 text-3xl md:text-4xl font-extrabold text-slate-900">
          Planes y Acceso a Indux
        </h1>
        <p class="mt-3 text-slate-600">
          Elige el periodo ideal para tu organización. Simulador de suscripción académica para proyectos industriales (ODS 9).
        </p>
      </div>

      <div class="mt-12 grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        @for (plan of planes; track plan.id) {
          <article
            class="relative flex flex-col rounded-2xl border bg-white p-6 shadow-sm transition hover:shadow-md card-interactive"
            [class]="plan.destacado ? 'border-emerald-600 ring-2 ring-emerald-600' : 'border-slate-200'"
          >
            @if (plan.destacado) {
              <span
                class="absolute -top-3 left-1/2 -translate-x-1/2 rounded-full bg-emerald-700 px-3 py-1 text-xs font-bold text-white shadow-sm"
              >
                Más recomendado
              </span>
            }
            <h2 class="text-lg font-bold text-slate-900">{{ plan.nombre }}</h2>
            <div class="mt-4">
              <span class="text-3xl font-extrabold text-slate-900">{{ formatear(plan.precio) }}</span>
            </div>
            <p class="text-xs text-slate-500 mt-1">
              {{ formatear(precioMes(plan)) }} / mes estimado
            </p>
            @if (ahorroDe(plan) > 0) {
              <div class="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded">
                <span>Ahorras {{ ahorroDe(plan) }} %</span>
              </div>
            }

            <ul class="mt-6 flex-1 space-y-2 text-xs text-slate-600 border-t border-slate-100 pt-4">
              <li class="flex items-center gap-2">
                <app-icon name="check-circle" [size]="14" class="text-emerald-600 shrink-0" />
                <span>Metodología SteelForce completa</span>
              </li>
              <li class="flex items-center gap-2">
                <app-icon name="check-circle" [size]="14" class="text-emerald-600 shrink-0" />
                <span>Detección de cuellos de botella</span>
              </li>
              <li class="flex items-center gap-2">
                <app-icon name="check-circle" [size]="14" class="text-emerald-600 shrink-0" />
                <span>Importación masiva Excel / CSV</span>
              </li>
              <li class="flex items-center gap-2">
                <app-icon name="check-circle" [size]="14" class="text-emerald-600 shrink-0" />
                <span>Histórico y tendencia de recursos</span>
              </li>
            </ul>

            <a
              [routerLink]="['/compra', plan.id]"
              class="mt-6 w-full rounded-xl py-2.5 text-center text-sm font-semibold transition"
              [class]="plan.destacado ? 'bg-emerald-700 text-white hover:bg-emerald-800' : 'bg-slate-100 text-slate-800 hover:bg-slate-200'"
            >
              Simular acceso
            </a>
          </article>
        }
      </div>

      <div class="mt-12 text-center text-xs text-slate-500 bg-slate-50 rounded-xl p-4 border border-slate-200">
        Nota: Esta plataforma no efectúa transacciones financieras reales. Las opciones presentadas forman parte del prototipo funcional.
      </div>
    </section>
  `,
})
export class Planes {
  protected readonly planes = PLANES;
  protected readonly formatear = formatearCOP;
  protected readonly precioMes = precioPorMes;
  protected readonly ahorroDe = ahorro;
}
