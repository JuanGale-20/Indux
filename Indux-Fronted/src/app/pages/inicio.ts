import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  template: `
    <!-- Hero principal -->
    <section class="bg-gradient-to-b from-emerald-950 via-slate-900 to-slate-950 text-white py-24 px-4">
      <div class="max-w-4xl mx-auto text-center">
        <span class="inline-flex items-center gap-2 rounded-full bg-emerald-900/60 border border-emerald-500/30 px-3.5 py-1 text-xs font-semibold text-emerald-300 mb-6">
          <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
          ODS 9 · Industria, Innovación e Infraestructura
        </span>
        <h1 class="text-4xl md:text-6xl font-extrabold tracking-tight text-white leading-tight">
          Detecta y resuelve los <span class="text-emerald-400">cuellos de botella</span> industriales
        </h1>
        <p class="mt-6 text-lg md:text-xl text-slate-300 max-w-2xl mx-auto font-light">
          <strong>Indux</strong> aplica la metodología <strong>SteelForce</strong>, co-diseñada con ingenieras industriales, para analizar capacidad, priorizar recursos críticos y recomendar acciones de optimización.
        </p>
        <div class="mt-10 flex flex-wrap justify-center gap-4">
          <a
            routerLink="/login"
            class="rounded-xl bg-emerald-600 px-6 py-3 font-semibold text-white shadow-lg shadow-emerald-900/30 hover:bg-emerald-500 transition"
          >
            Ingresar a la plataforma
          </a>
          <a
            routerLink="/planes"
            class="rounded-xl border border-slate-700 bg-slate-800/80 px-6 py-3 font-semibold text-slate-200 hover:bg-slate-800 hover:text-white transition"
          >
            Ver membresías informativas
          </a>
        </div>
      </div>
    </section>

    <!-- Metodología SteelForce 4 Niveles -->
    <section class="max-w-6xl mx-auto px-4 py-20">
      <div class="text-center max-w-2xl mx-auto mb-14">
        <h2 class="text-2xl md:text-3xl font-bold text-slate-900">
          Metodología SteelForce: Clasificación por Utilización
        </h2>
        <p class="mt-3 text-slate-600 text-sm">
          Monitorea y categoriza automáticamente la capacidad de cada máquina, estación o línea de producción:
        </p>
      </div>

      <div class="grid gap-6 md:grid-cols-4">
        <div class="rounded-2xl border border-sky-200 bg-sky-50/50 p-6">
          <div class="w-10 h-10 rounded-xl bg-sky-100 text-sky-700 flex items-center justify-center font-bold text-sm mb-4">
            &lt; 80%
          </div>
          <h3 class="font-bold text-sky-900">Baja Utilización</h3>
          <p class="mt-2 text-xs text-sky-800 leading-relaxed">
            Capacidad ociosa disponible. Puede absorber mayor demanda o redistribuir actividades.
          </p>
        </div>

        <div class="rounded-2xl border border-emerald-200 bg-emerald-50/50 p-6">
          <div class="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold text-sm mb-4">
            80-90%
          </div>
          <h3 class="font-bold text-emerald-900">Utilización Adecuada</h3>
          <p class="mt-2 text-xs text-emerald-800 leading-relaxed">
            Rango óptimo SteelForce. Operación eficiente con holgura para imprevistos.
          </p>
        </div>

        <div class="rounded-2xl border border-amber-200 bg-amber-50/50 p-6">
          <div class="w-10 h-10 rounded-xl bg-amber-100 text-amber-700 flex items-center justify-center font-bold text-sm mb-4">
            90-100%
          </div>
          <h3 class="font-bold text-amber-900">Capacidad Ajustada</h3>
          <p class="mt-2 text-xs text-amber-800 leading-relaxed">
            Cerca del límite. Requiere monitoreo preventivo de paradas y tiempos de ciclo.
          </p>
        </div>

        <div class="rounded-2xl border border-rose-200 bg-rose-50/50 p-6">
          <div class="w-10 h-10 rounded-xl bg-rose-100 text-rose-700 flex items-center justify-center font-bold text-sm mb-4">
            &gt; 100%
          </div>
          <h3 class="font-bold text-rose-900">Sobrecarga (Cuello)</h3>
          <p class="mt-2 text-xs text-rose-800 leading-relaxed">
            Restricción activa del sistema productivo. Genera déficit horario y pérdida de ritmo.
          </p>
        </div>
      </div>
    </section>

    <!-- Beneficios clave -->
    <section class="bg-slate-50 border-t border-slate-200 py-16 px-4">
      <div class="max-w-6xl mx-auto">
        <div class="grid gap-8 md:grid-cols-3">
          <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <div class="w-10 h-10 rounded-lg bg-emerald-100 text-emerald-800 flex items-center justify-center font-bold mb-3">
              01
            </div>
            <h3 class="font-bold text-slate-900">Validación y Auditoría</h3>
            <p class="mt-2 text-sm text-slate-600">
              Validación de reglas críticas (coherencia de turnos, tiempos calendario, mermas y unidades) antes de procesar el análisis.
            </p>
          </div>

          <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <div class="w-10 h-10 rounded-lg bg-emerald-100 text-emerald-800 flex items-center justify-center font-bold mb-3">
              02
            </div>
            <h3 class="font-bold text-slate-900">Análisis Causal y Ranking</h3>
            <p class="mt-2 text-sm text-slate-600">
              Desglose de causas (demanda, merma acumulada, recurso compartido, paradas) y ranking de prioridad por déficit e impacto.
            </p>
          </div>

          <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <div class="w-10 h-10 rounded-lg bg-emerald-100 text-emerald-800 flex items-center justify-center font-bold mb-3">
              03
            </div>
            <h3 class="font-bold text-slate-900">Importación Masiva</h3>
            <p class="mt-2 text-sm text-slate-600">
              Carga tus reportes mediante plantillas Excel (.xlsx) o CSV con vista previa paso a paso y validación estricta todo-o-nada.
            </p>
          </div>
        </div>
      </div>
    </section>
  `,
})
export class Inicio {}
