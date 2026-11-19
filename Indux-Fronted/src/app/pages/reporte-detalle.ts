import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProductosApi, RecursosApi, ReportesApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import {
  AnalisisRecurso,
  AnalisisReporte,
  CausaDeficit,
  Producto,
  Recomendacion,
  Recurso,
  Reporte,
  ValidacionResultado,
} from '../core/models';
import { NotificationService } from '../core/notification.service';
import {
  obtenerClasificacionInfo,
  obtenerEstadoReporteInfo,
  obtenerFactorCausaInfo,
  obtenerNivelRecomendacionInfo,
} from '../core/steelforce-constants';
import { AppIconComponent } from '../core/icon.component';

type Pestana = 'resumen' | 'validaciones' | 'analisis' | 'recomendaciones' | 'causas';

@Component({
  selector: 'app-reporte-detalle',
  imports: [CommonModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 max-w-7xl mx-auto app-fade-in">
      <!-- Migas de pan y Acciones -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div class="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <a routerLink="/app/reportes" class="hover:text-emerald-700">← Reportes</a>
            <span>/</span>
            <span>Detalle #{{ idReporte }}</span>
          </div>
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">
              {{ reporte()?.plantaArea || 'Reporte de Capacidad' }}
            </h1>
            @if (reporte(); as r) {
              @let estadoInfo = obtenerEstado(r.estado);
              <span
                class="inline-flex items-center gap-1.5 px-3 py-0.5 rounded-full text-xs font-semibold border"
                [class]="estadoInfo.badgeClass"
              >
                <span class="w-2 h-2 rounded-full" [class]="estadoInfo.dotColor"></span>
                {{ estadoInfo.etiqueta }}
              </span>
            }
          </div>
          <p class="text-xs text-slate-500 mt-1">
            Producto: <strong class="text-slate-800">{{ producto()?.nombre }} ({{ producto()?.codigo }})</strong>
            · Periodo: <span class="font-mono text-slate-700">{{ reporte()?.periodoInicio }} → {{ reporte()?.periodoFin }}</span>
            · Frecuencia: <span class="font-semibold uppercase text-slate-700">{{ reporte()?.frecuencia }}</span>
          </p>
        </div>

        <!-- Botones de Acción de Flujo (Validar, Analizar) -->
        <div class="flex flex-wrap items-center gap-2">
          @if (puedeOperar()) {
            <button
              (click)="ejecutarValidacion()"
              [disabled]="validando() || analizando()"
              class="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold bg-white border border-slate-300 text-slate-700 hover:bg-slate-50 transition shadow-xs disabled:opacity-50"
            >
              <app-icon name="check-circle" [size]="14" class="text-emerald-700" />
              <span>{{ validando() ? 'Validando…' : 'Validar Coherencia' }}</span>
            </button>

            <button
              (click)="ejecutarAnalisis()"
              [disabled]="validando() || analizando() || tieneErroresCriticos()"
              class="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold text-white bg-emerald-700 hover:bg-emerald-800 transition shadow-sm disabled:opacity-50 disabled:bg-slate-400"
              [title]="tieneErroresCriticos() ? 'No se puede analizar: hay errores críticos en el reporte' : 'Ejecutar análisis SteelForce'"
            >
              <app-icon name="sparkles" [size]="15" />
              <span>{{ analizando() ? 'Calculando…' : 'Analizar Capacidad' }}</span>
            </button>
          } @else {
            <span class="text-xs font-medium text-slate-400 bg-slate-100 px-3 py-1.5 rounded-xl">
              Modo Solo Lectura (LÍDER)
            </span>
          }
        </div>
      </div>

      <!-- Alerta si hay errores críticos que bloquean análisis -->
      @if (tieneErroresCriticos()) {
        <div class="rounded-2xl bg-rose-50 border border-rose-200 p-4 text-xs text-rose-800 flex items-start gap-3">
          <app-icon name="alert-triangle" [size]="18" class="text-rose-600 shrink-0 mt-0.5" />
          <div>
            <p class="font-bold">El reporte contiene errores críticos de coherencia:</p>
            <p class="mt-0.5">
              No es posible ejecutar el análisis SteelForce hasta corregir los datos en el formulario o en la configuración del proceso.
              Revisa la pestaña <button (click)="cambiarPestana('validaciones')" class="underline font-bold">Validaciones</button>.
            </p>
          </div>
        </div>
      }

      <!-- Navegación por pestañas -->
      <div class="border-b border-slate-200">
        <nav class="flex space-x-2 sm:space-x-4">
          <button
            (click)="cambiarPestana('resumen')"
            [class]="pestanaActiva() === 'resumen' ? 'border-emerald-600 text-emerald-800 font-bold' : 'border-transparent text-slate-500 hover:text-slate-700'"
            class="py-3 px-3 border-b-2 text-xs whitespace-nowrap transition inline-flex items-center gap-1.5"
          >
            <app-icon name="file-text" [size]="14" />
            <span>Resumen Operativo</span>
          </button>
          <button
            (click)="cambiarPestana('validaciones')"
            [class]="pestanaActiva() === 'validaciones' ? 'border-emerald-600 text-emerald-800 font-bold' : 'border-transparent text-slate-500 hover:text-slate-700'"
            class="py-3 px-3 border-b-2 text-xs whitespace-nowrap transition flex items-center gap-1.5"
          >
            <app-icon name="check-circle" [size]="14" />
            <span>Validaciones</span>
            @if (validaciones().length > 0) {
              <span class="px-1.5 py-0.2 rounded-full text-[10px]"
                [class]="tieneErroresCriticos() ? 'bg-rose-100 text-rose-800 font-bold' : 'bg-slate-100 text-slate-600'">
                {{ validaciones().length }}
              </span>
            }
          </button>
          <button
            (click)="cambiarPestana('analisis')"
            [class]="pestanaActiva() === 'analisis' ? 'border-emerald-600 text-emerald-800 font-bold' : 'border-transparent text-slate-500 hover:text-slate-700'"
            class="py-3 px-3 border-b-2 text-xs whitespace-nowrap transition flex items-center gap-1.5"
          >
            <app-icon name="chart-no-axes-combined" [size]="14" />
            <span>Dashboard SteelForce</span>
            @if (analisis(); as an) {
              <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
            }
          </button>
          <button
            (click)="cambiarPestana('recomendaciones')"
            [class]="pestanaActiva() === 'recomendaciones' ? 'border-emerald-600 text-emerald-800 font-bold' : 'border-transparent text-slate-500 hover:text-slate-700'"
            class="py-3 px-3 border-b-2 text-xs whitespace-nowrap transition flex items-center gap-1.5"
          >
            <app-icon name="sparkles" [size]="14" />
            <span>Recomendaciones</span>
            @if (recomendaciones().length > 0) {
              <span class="px-1.5 py-0.2 rounded-full text-[10px] bg-slate-100 text-slate-600">
                {{ recomendaciones().length }}
              </span>
            }
          </button>
          <button
            (click)="cambiarPestana('causas')"
            [class]="pestanaActiva() === 'causas' ? 'border-emerald-600 text-emerald-800 font-bold' : 'border-transparent text-slate-500 hover:text-slate-700'"
            class="py-3 px-3 border-b-2 text-xs whitespace-nowrap transition flex items-center gap-1.5"
          >
            <app-icon name="search" [size]="14" />
            <span>Desglose de Causas</span>
            @if (causas().length > 0) {
              <span class="px-1.5 py-0.2 rounded-full text-[10px] bg-rose-100 text-rose-700 font-bold">
                {{ causas().length }}
              </span>
            }
          </button>
        </nav>
      </div>

      <!-- CONTENIDO DE PESTAÑAS -->

      <!-- 1. PESTAÑA RESUMEN -->
      @if (pestanaActiva() === 'resumen') {
        <div class="space-y-6">
          <!-- Tarjetas KPI básicas -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div class="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
              <span class="text-[10px] uppercase font-bold text-slate-400">Demanda Requerida</span>
              <p class="text-2xl font-black text-slate-900 mt-1">{{ reporte()?.demanda }}</p>
              <span class="text-[11px] text-slate-500">{{ producto()?.unidadMedida }}</span>
            </div>
            <div class="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
              <span class="text-[10px] uppercase font-bold text-slate-400">Producción Real</span>
              <p class="text-2xl font-black text-emerald-800 mt-1">{{ reporte()?.produccionReal }}</p>
              <span class="text-[11px] text-slate-500">{{ producto()?.unidadMedida }}</span>
            </div>
            <div class="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
              <span class="text-[10px] uppercase font-bold text-slate-400">Cumplimiento de Meta</span>
              <p class="text-2xl font-black text-slate-800 mt-1">
                {{ calcularCumplimiento() }} %
              </p>
              <span class="text-[11px] text-slate-500">Producción vs Demanda</span>
            </div>
            <div class="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
              <span class="text-[10px] uppercase font-bold text-slate-400">Líneas de Recursos</span>
              <p class="text-2xl font-black text-slate-800 mt-1">{{ reporte()?.lineas?.length || 0 }}</p>
              <span class="text-[11px] text-slate-500">Puestos / Máquinas evaluadas</span>
            </div>
          </div>

          <!-- Tabla de Líneas por Recurso -->
          <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
              <h2 class="text-xs font-bold uppercase tracking-wider text-slate-800">
                Detalle de Parámetros por Recurso
              </h2>
              @if (puedeOperar()) {
                <a
                  [routerLink]="['/app/reportes/editar', idReporte]"
                  class="text-xs text-emerald-700 hover:text-emerald-900 font-semibold"
                >
                  Modificar parámetros →
                </a>
              }
            </div>

            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs border-collapse">
                <thead>
                  <tr class="bg-slate-50 text-[10px] uppercase text-slate-600 border-b border-slate-200">
                    <th class="py-3 px-4 font-semibold">Recurso</th>
                    <th class="py-3 px-4 font-semibold">T. Procesamiento</th>
                    <th class="py-3 px-4 font-semibold">Unidad Ref.</th>
                    <th class="py-3 px-4 font-semibold">T. Calendario</th>
                    <th class="py-3 px-4 font-semibold">T. No Disp.</th>
                    <th class="py-3 px-4 font-semibold">Turnos</th>
                    <th class="py-3 px-4 font-semibold">Horas/Turno</th>
                    <th class="py-3 px-4 font-semibold">Días Op.</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  @for (l of reporte()?.lineas; track l.id) {
                    <tr class="hover:bg-slate-50/80">
                      <td class="py-3 px-4 font-bold text-slate-900">
                        {{ obtenerNombreRecurso(l.recursoId) }}
                      </td>
                      <td class="py-3 px-4 font-mono text-slate-700">{{ l.tiempoProcesamiento }}</td>
                      <td class="py-3 px-4 text-slate-600">{{ l.unidadReferencia }}</td>
                      <td class="py-3 px-4 font-mono text-slate-700">{{ l.tiempoCalendario }} hrs</td>
                      <td class="py-3 px-4 font-mono text-slate-700">{{ l.tiempoNoDisponible ?? 0 }} hrs</td>
                      <td class="py-3 px-4 text-slate-700">{{ l.numeroTurnos }}</td>
                      <td class="py-3 px-4 text-slate-700">{{ l.horasPorTurno }} hrs</td>
                      <td class="py-3 px-4 text-slate-700">{{ l.diasOperacion }} días</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        </div>
      }

      <!-- 2. PESTAÑA VALIDACIONES -->
      @if (pestanaActiva() === 'validaciones') {
        <div class="space-y-6">
          <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between">
            <div>
              <h2 class="text-sm font-bold text-slate-900">Auditoría de Coherencia de Datos</h2>
              <p class="text-xs text-slate-500 mt-0.5">
                Reglas de validación previas según SteelForce: coherencia de turnos, horas calendario, unidades de medida y mermas.
              </p>
            </div>
            @if (puedeOperar()) {
              <button
                (click)="ejecutarValidacion()"
                [disabled]="validando()"
                class="px-3.5 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-xs font-semibold text-slate-700 transition"
              >
                {{ validando() ? 'Revalidando…' : 'Revalidar ahora' }}
              </button>
            }
          </div>

          @if (cargandoValidaciones()) {
            <div class="p-8 text-center bg-white rounded-2xl border border-slate-200 text-xs text-slate-500">
              Consultando resultados de validación…
            </div>
          } @else if (validaciones().length === 0) {
            <div class="p-8 text-center bg-emerald-50 rounded-2xl border border-emerald-200 text-emerald-900">
              <app-icon name="check-circle" [size]="28" class="text-emerald-600 mx-auto" />
              <h3 class="font-bold text-sm mt-2">Sin observaciones críticas ni advertencias</h3>
              <p class="text-xs text-emerald-700 mt-1">
                Todos los datos del reporte superaron las pruebas de coherencia SteelForce.
              </p>
            </div>
          } @else {
            <!-- Errores Críticos -->
            @let criticos = erroresCriticos();
            @if (criticos.length > 0) {
              <div class="space-y-3">
                <div class="flex items-center gap-2">
                  <span class="w-3 h-3 rounded-full bg-rose-600"></span>
                  <h3 class="text-xs font-bold uppercase tracking-wider text-rose-900">
                    Errores Críticos ({{ criticos.length }}) · Bloquean el análisis
                  </h3>
                </div>

                <div class="grid gap-3">
                  @for (v of criticos; track v.mensaje) {
                    <div class="bg-rose-50/70 border border-rose-200 rounded-xl p-4 text-xs">
                      <div class="flex items-center justify-between mb-1">
                        <span class="font-bold text-rose-900 uppercase font-mono tracking-wider text-[10px]">
                          [{{ v.grupo }}] {{ v.variable }}
                        </span>
                        <span class="px-2 py-0.5 rounded bg-rose-200 text-rose-900 font-extrabold text-[10px]">
                          CRÍTICO
                        </span>
                      </div>
                      <p class="text-rose-800">{{ v.mensaje }}</p>
                    </div>
                  }
                </div>
              </div>
            }

            <!-- Advertencias -->
            @let advs = advertencias();
            @if (advs.length > 0) {
              <div class="space-y-3 pt-4">
                <div class="flex items-center gap-2">
                  <span class="w-3 h-3 rounded-full bg-amber-500"></span>
                  <h3 class="text-xs font-bold uppercase tracking-wider text-amber-900">
                    Advertencias ({{ advs.length }}) · Permiten continuar
                  </h3>
                </div>

                <div class="grid gap-3">
                  @for (v of advs; track v.mensaje) {
                    <div class="bg-amber-50/70 border border-amber-200 rounded-xl p-4 text-xs">
                      <div class="flex items-center justify-between mb-1">
                        <span class="font-bold text-amber-900 uppercase font-mono tracking-wider text-[10px]">
                          [{{ v.grupo }}] {{ v.variable }}
                        </span>
                        <span class="px-2 py-0.5 rounded bg-amber-200 text-amber-900 font-extrabold text-[10px]">
                          ADVERTENCIA
                        </span>
                      </div>
                      <p class="text-amber-800">{{ v.mensaje }}</p>
                    </div>
                  }
                </div>
              </div>
            }
          }
        </div>
      }

      <!-- 3. PESTAÑA ANÁLISIS (DASHBOARD STEELFORCE) -->
      @if (pestanaActiva() === 'analisis') {
        <div class="space-y-6">
          @if (cargandoAnalisis()) {
            <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
              <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
              <p class="mt-3 text-xs text-slate-500">Cargando análisis de capacidad…</p>
            </div>
          } @else if (analisisPendiente()) {
            <!-- Estado 409: Reporte no analizado todavía -->
            <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm max-w-lg mx-auto">
              <div class="w-16 h-16 bg-emerald-50 text-emerald-700 rounded-full flex items-center justify-center mx-auto mb-4">
                <app-icon name="sparkles" [size]="28" />
              </div>
              <h3 class="text-base font-bold text-slate-900">Este reporte aún no ha sido analizado</h3>
              <p class="text-xs text-slate-500 mt-2 leading-relaxed">
                Ejecuta el motor de cálculo SteelForce para determinar la utilización de recursos, cuellos de botella y ranking de déficit.
              </p>
              @if (puedeOperar()) {
                <button
                  (click)="ejecutarAnalisis()"
                  [disabled]="analizando() || tieneErroresCriticos()"
                  class="mt-6 px-6 py-2.5 rounded-xl bg-emerald-700 text-white font-bold text-xs hover:bg-emerald-800 transition shadow-sm disabled:opacity-50"
                >
                  {{ analizando() ? 'Calculando…' : 'Analizar Ahora' }}
                </button>
              }
            </div>
          } @else if (analisis(); as an) {
            <!-- KPIs SteelForce -->
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
                <span class="text-[10px] uppercase font-bold text-slate-400">Recursos Analizados</span>
                <p class="text-3xl font-extrabold text-slate-900 mt-1">{{ an.recursos.length }}</p>
                <span class="text-[11px] text-slate-500">Total puestos de trabajo</span>
              </div>

              <div class="bg-rose-50/50 p-5 rounded-2xl border border-rose-200 shadow-sm">
                <span class="text-[10px] uppercase font-bold text-rose-700">En Sobrecarga (> 100%)</span>
                <p class="text-3xl font-extrabold text-rose-700 mt-1">{{ contarSobrecarga(an.recursos) }}</p>
                <span class="text-[11px] text-rose-600 font-medium">Cuellos de botella activos</span>
              </div>

              <div class="bg-amber-50/50 p-5 rounded-2xl border border-amber-200 shadow-sm">
                <span class="text-[10px] uppercase font-bold text-amber-700">Capacidad Ajustada (90–100%)</span>
                <p class="text-3xl font-extrabold text-amber-700 mt-1">{{ contarAjustada(an.recursos) }}</p>
                <span class="text-[11px] text-amber-600 font-medium">En riesgo de saturación</span>
              </div>

              <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
                <span class="text-[10px] uppercase font-bold text-slate-400">Déficit Total de Horas</span>
                <p class="text-3xl font-extrabold text-slate-900 mt-1">{{ calcularDeficitTotal(an.recursos) }}</p>
                <span class="text-[11px] text-slate-500">Horas faltantes requeridas</span>
              </div>
            </div>

            <!-- Gráfico de Barras de Utilización con Líneas 80%, 90%, 100% -->
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
              <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-2 border-b border-slate-100">
                <div>
                  <h2 class="text-sm font-bold text-slate-900">
                    Utilización por Recurso (%) · Metodología SteelForce
                  </h2>
                  <p class="text-xs text-slate-500 mt-0.5">
                    Líneas de referencia críticas en 80 % (óptimo), 90 % (ajustado) y 100 % (cuello de botella).
                  </p>
                </div>

                <!-- Leyenda interactiva -->
                <div class="flex flex-wrap items-center gap-3 text-[11px]">
                  <span class="inline-flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-sky-500"></span> &lt;80% Baja</span>
                  <span class="inline-flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> 80-90% Adecuada</span>
                  <span class="inline-flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-amber-500"></span> 90-100% Ajustada</span>
                  <span class="inline-flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-rose-500"></span> &gt;100% Sobrecarga</span>
                </div>
              </div>

              <!-- Barras de recursos -->
              <div class="space-y-4 pt-2">
                @for (rec of an.recursos; track rec.reporteRecursoId) {
                  @let cInfo = obtenerClasificacion(rec.clasificacion);
                  <div class="space-y-1">
                    <div class="flex items-center justify-between text-xs">
                      <div class="flex items-center gap-2">
                        <span class="font-mono font-bold text-slate-800">{{ rec.codigo }}</span>
                        <span class="font-medium text-slate-600">{{ rec.nombre }}</span>
                        @if (rec.prioridad != null) {
                          <span class="px-1.5 py-0.2 rounded bg-rose-100 text-rose-800 text-[10px] font-extrabold">
                            Prioridad #{{ rec.prioridad }}
                          </span>
                        }
                      </div>
                      <div class="flex items-center gap-2 font-mono">
                        <span class="font-extrabold" [class]="cInfo.textColor">{{ rec.utilizacion }} %</span>
                        <span class="text-[10px] text-slate-400">({{ rec.horasRequeridas }}h req / {{ rec.horasDisponibles }}h disp)</span>
                      </div>
                    </div>

                    <!-- Contenedor con líneas de referencia 80%, 90%, 100% -->
                    <div class="relative w-full h-5 bg-slate-100 rounded-lg overflow-hidden border border-slate-200">
                      <!-- Líneas guía -->
                      <div class="absolute inset-y-0 left-[80%] w-px bg-slate-300 z-10" title="80% Límite Baja Utilización"></div>
                      <div class="absolute inset-y-0 left-[90%] w-px bg-slate-300 z-10" title="90% Límite Adecuada"></div>
                      <div class="absolute inset-y-0 left-[100%] w-0.5 bg-rose-400 z-10" title="100% Límite Capacidad Máxima"></div>

                      <!-- Barra de progreso (normalizada a 120% máx para no desbordar) -->
                      <div
                        class="h-full rounded-l-lg transition-all duration-500"
                        [style.width.%]="calcularAnchoBarra(rec.utilizacion)"
                        [style.backgroundColor]="cInfo.barColor"
                      ></div>
                    </div>
                  </div>
                }
              </div>
            </div>

            <!-- RANKING DE PRIORIDAD (Solo recursos con déficit) -->
            @let restringidos = recursosEnRanking(an.recursos);
            @if (restringidos.length > 0) {
              <div class="bg-white rounded-2xl border border-rose-200 shadow-sm overflow-hidden">
                <div class="px-5 py-4 bg-rose-50/50 border-b border-rose-100 flex items-center justify-between">
                  <div>
                    <h2 class="text-xs font-bold uppercase tracking-wider text-rose-900 flex items-center gap-2">
                      <app-icon name="alert-triangle" [size]="16" class="text-rose-600" />
                      <span>Ranking de Prioridad de Restricciones (SteelForce)</span>
                    </h2>
                    <p class="text-xs text-rose-700 mt-0.5">
                      Recursos que presentan déficit horario, ordenados por impacto crítico en la producción.
                    </p>
                  </div>
                  <span class="px-2.5 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-800">
                    {{ restringidos.length }} recurso(s) crítico(s)
                  </span>
                </div>

                <div class="overflow-x-auto">
                  <table class="w-full text-left text-xs border-collapse">
                    <thead>
                      <tr class="bg-rose-50/30 text-[10px] uppercase text-slate-600 border-b border-rose-100">
                        <th class="py-3 px-4 font-semibold">Prioridad</th>
                        <th class="py-3 px-4 font-semibold">Recurso</th>
                        <th class="py-3 px-4 font-semibold">Impacto (%)</th>
                        <th class="py-3 px-4 font-semibold">Déficit Horas</th>
                        <th class="py-3 px-4 font-semibold">Déficit Capacidad</th>
                        <th class="py-3 px-4 font-semibold">Utilización</th>
                        <th class="py-3 px-4 font-semibold">Holgura</th>
                        <th class="py-3 px-4 font-semibold">Diagnóstico</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-rose-50">
                      @for (r of restringidos; track r.reporteRecursoId) {
                        <tr class="hover:bg-rose-50/20">
                          <td class="py-3 px-4">
                            <span class="inline-flex items-center justify-center w-6 h-6 rounded-full bg-rose-700 text-white font-extrabold text-[11px]">
                              #{{ r.prioridad }}
                            </span>
                          </td>
                          <td class="py-3 px-4">
                            <div class="font-bold text-slate-900">{{ r.nombre }}</div>
                            <span class="font-mono text-[10px] text-slate-500">{{ r.codigo }}</span>
                          </td>
                          <td class="py-3 px-4 font-mono font-bold text-rose-700">
                            {{ r.impacto }} %
                          </td>
                          <td class="py-3 px-4 font-mono font-bold text-rose-700">
                            -{{ r.deficitHoras }} hrs
                          </td>
                          <td class="py-3 px-4 font-mono text-slate-700">
                            {{ r.deficitCapacidad }}
                          </td>
                          <td class="py-3 px-4 font-mono font-bold text-rose-700">
                            {{ r.utilizacion }} %
                          </td>
                          <td class="py-3 px-4 font-mono text-slate-500">
                            {{ r.holgura }} hrs
                          </td>
                          <td class="py-3 px-4 text-slate-700">
                            {{ r.diagnostico }}
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              </div>
            }

            <!-- Tabla General de Todos los Recursos Analizados -->
            <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
              <div class="px-5 py-4 border-b border-slate-100">
                <h2 class="text-xs font-bold uppercase tracking-wider text-slate-800">
                  Resumen de Todos los Recursos Analizados
                </h2>
              </div>
              <div class="overflow-x-auto">
                <table class="w-full text-left text-xs border-collapse">
                  <thead>
                    <tr class="bg-slate-50 text-[10px] uppercase text-slate-600 border-b border-slate-200">
                      <th class="py-3 px-4 font-semibold">Código</th>
                      <th class="py-3 px-4 font-semibold">Nombre</th>
                      <th class="py-3 px-4 font-semibold">Req / Disp</th>
                      <th class="py-3 px-4 font-semibold">Capacidad</th>
                      <th class="py-3 px-4 font-semibold">Utilización</th>
                      <th class="py-3 px-4 font-semibold">Holgura</th>
                      <th class="py-3 px-4 font-semibold">Clasificación</th>
                      <th class="py-3 px-4 font-semibold">Diagnóstico</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    @for (r of an.recursos; track r.reporteRecursoId) {
                      @let cInfo = obtenerClasificacion(r.clasificacion);
                      <tr class="hover:bg-slate-50/80">
                        <td class="py-3 px-4 font-mono font-bold text-slate-800">{{ r.codigo }}</td>
                        <td class="py-3 px-4 font-medium text-slate-900">{{ r.nombre }}</td>
                        <td class="py-3 px-4 font-mono text-slate-700">{{ r.horasRequeridas }} / {{ r.horasDisponibles }} hrs</td>
                        <td class="py-3 px-4 font-mono text-slate-700">{{ r.capacidad }}</td>
                        <td class="py-3 px-4 font-mono font-bold" [class]="cInfo.textColor">{{ r.utilizacion }} %</td>
                        <td class="py-3 px-4 font-mono text-slate-600">{{ r.holgura }} hrs</td>
                        <td class="py-3 px-4">
                          <span
                            class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold border"
                            [class]="cInfo.badgeClass"
                          >
                            <span class="w-1.5 h-1.5 rounded-full" [class]="cInfo.dotColor"></span>
                            {{ cInfo.etiqueta }}
                          </span>
                        </td>
                        <td class="py-3 px-4 text-slate-700 max-w-xs truncate" [title]="r.diagnostico">
                          {{ r.diagnostico }}
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            </div>
          }
        </div>
      }

      <!-- 4. PESTAÑA RECOMENDACIONES -->
      @if (pestanaActiva() === 'recomendaciones') {
        <div class="space-y-6">
          <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
            <h2 class="text-sm font-bold text-slate-900">Recomendaciones Accionables SteelForce</h2>
            <p class="text-xs text-slate-500 mt-0.5">
              Sugerencias y acciones preventivas/correctivas según el nivel de utilización de cada recurso.
            </p>
          </div>

          @if (cargandoRecomendaciones()) {
            <div class="p-8 text-center bg-white rounded-2xl border border-slate-200 text-xs text-slate-500">
              Consultando recomendaciones…
            </div>
          } @else if (recomendaciones().length === 0) {
            <div class="p-8 text-center bg-white rounded-2xl border border-slate-200 text-xs text-slate-500">
              No se han emitido recomendaciones aún para este reporte.
            </div>
          } @else {
            <div class="space-y-4">
              @for (rec of recomendaciones(); track rec.recursoId + rec.resumen) {
                @let nInfo = obtenerNivel(rec.nivel);
                <div class="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm border-l-4" [class]="nInfo.borderClass">
                  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-2">
                    <div class="flex items-center gap-2">
                      <span class="font-mono text-xs font-bold text-slate-800 bg-slate-100 px-2 py-0.5 rounded">
                        {{ rec.codigo }}
                      </span>
                      <h3 class="text-sm font-bold text-slate-900">{{ rec.nombre }}</h3>
                      @if (rec.prioridad != null) {
                        <span class="px-2 py-0.5 rounded-full bg-rose-100 text-rose-800 font-extrabold text-[10px]">
                          Prioridad #{{ rec.prioridad }}
                        </span>
                      }
                    </div>

                    <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold border uppercase" [class]="nInfo.badgeClass">
                      Nivel {{ nInfo.etiqueta }}
                    </span>
                  </div>

                  <p class="text-xs text-slate-700 font-medium mt-1">{{ rec.resumen }}</p>

                  @if (rec.acciones && rec.acciones.length > 0) {
                    <div class="mt-3 bg-slate-50 rounded-xl p-3 border border-slate-100">
                      <span class="text-[10px] font-bold uppercase text-slate-500 tracking-wider">Acciones sugeridas:</span>
                      <ul class="mt-1.5 space-y-1 text-xs text-slate-600 list-disc list-inside">
                        @for (a of rec.acciones; track a) {
                          <li>{{ a }}</li>
                        }
                      </ul>
                    </div>
                  }
                </div>
              }
            </div>
          }
        </div>
      }

      <!-- 5. PESTAÑA CAUSAS -->
      @if (pestanaActiva() === 'causas') {
        <div class="space-y-6">
          <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
            <h2 class="text-sm font-bold text-slate-900">Desglose Causal del Déficit</h2>
            <p class="text-xs text-slate-500 mt-0.5">
              Identificación cuantitativa de los factores que provocaron la sobrecarga en cada recurso restringido.
            </p>
          </div>

          @if (cargandoCausas()) {
            <div class="p-8 text-center bg-white rounded-2xl border border-slate-200 text-xs text-slate-500">
              Analizando causas del déficit…
            </div>
          } @else if (causas().length === 0) {
            <div class="p-8 text-center bg-emerald-50 rounded-2xl border border-emerald-200 text-emerald-900">
              <app-icon name="check-circle" [size]="28" class="text-emerald-600 mx-auto" />
              <h3 class="font-bold text-sm mt-2">Sin déficit de capacidad detectado</h3>
              <p class="text-xs text-emerald-700 mt-1">
                Ningún recurso de este reporte presenta sobrecarga horaria ni cuellos de botella activos.
              </p>
            </div>
          } @else {
            <div class="space-y-6">
              @for (c of causas(); track c.recursoId) {
                @let fCausa = obtenerFactor(c.causaPrincipal);
                <div class="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
                  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
                    <div>
                      <div class="flex items-center gap-2">
                        <span class="font-mono text-xs font-bold text-slate-800 bg-slate-100 px-2 py-0.5 rounded">
                          {{ c.codigo }}
                        </span>
                        <h3 class="text-sm font-bold text-slate-900">{{ c.nombre }}</h3>
                        @if (c.prioridad != null) {
                          <span class="px-2 py-0.5 rounded-full bg-rose-100 text-rose-800 font-extrabold text-[10px]">
                            Prioridad #{{ c.prioridad }}
                          </span>
                        }
                      </div>
                      <p class="text-xs text-rose-700 font-semibold mt-1">
                        Déficit acumulado: -{{ c.deficitHoras }} horas requeridas
                      </p>
                    </div>

                    <div class="bg-amber-50 border border-amber-200 rounded-xl px-3 py-1.5 text-right">
                      <span class="text-[10px] uppercase font-bold text-amber-800">Causa Principal</span>
                      <p class="text-xs font-bold text-amber-900">{{ fCausa.etiqueta }}</p>
                    </div>
                  </div>

                  <!-- Desglose por Factor -->
                  <div>
                    <h4 class="text-[10px] font-bold uppercase tracking-wider text-slate-500 mb-2">
                      Aporte por Factor al Déficit
                    </h4>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                      @for (f of c.factores; track f.factor) {
                        @let factorInfo = obtenerFactor(f.factor);
                        <div class="bg-slate-50 rounded-xl p-3 border border-slate-200 text-xs">
                          <div class="flex items-center justify-between font-bold text-slate-800">
                            <span>{{ factorInfo.etiqueta }}</span>
                            <span class="font-mono text-rose-700">{{ f.porcentajeDelDeficit }} %</span>
                          </div>
                          <p class="text-[11px] text-slate-500 mt-1">{{ f.descripcion || factorInfo.descripcion }}</p>
                          <div class="mt-2 text-[10px] font-mono text-slate-600 flex justify-between">
                            <span>Impacto en horas:</span>
                            <strong>{{ f.horas }} hrs</strong>
                          </div>
                        </div>
                      }
                    </div>
                  </div>
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class ReporteDetalle implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly ruta = inject(ActivatedRoute);
  private readonly reportesApi = inject(ReportesApi);
  private readonly productosApi = inject(ProductosApi);
  private readonly recursosApi = inject(RecursosApi);
  private readonly notify = inject(NotificationService);

  protected idReporte!: number;

  protected readonly reporte = signal<Reporte | null>(null);
  protected readonly producto = signal<Producto | null>(null);
  protected readonly recursos = signal<Recurso[]>([]);

  protected readonly pestanaActiva = signal<Pestana>('resumen');

  // Validaciones
  protected readonly validaciones = signal<ValidacionResultado[]>([]);
  protected readonly cargandoValidaciones = signal(false);
  protected readonly validando = signal(false);

  // Análisis
  protected readonly analisis = signal<AnalisisReporte | null>(null);
  protected readonly cargandoAnalisis = signal(false);
  protected readonly analizando = signal(false);
  protected readonly analisisPendiente = signal(false);

  // Recomendaciones
  protected readonly recomendaciones = signal<Recomendacion[]>([]);
  protected readonly cargandoRecomendaciones = signal(false);

  // Causas
  protected readonly causas = signal<CausaDeficit[]>([]);
  protected readonly cargandoCausas = signal(false);

  // Constants helpers
  protected readonly obtenerEstado = obtenerEstadoReporteInfo;
  protected readonly obtenerClasificacion = obtenerClasificacionInfo;
  protected readonly obtenerNivel = obtenerNivelRecomendacionInfo;
  protected readonly obtenerFactor = obtenerFactorCausaInfo;

  puedeOperar(): boolean {
    const rol = this.auth.usuario()?.rol;
    return rol === 'ADMIN' || rol === 'ANALISTA';
  }

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.idReporte = id;
    this.cargarDetalle();
  }

  cargarDetalle(): void {
    this.reportesApi.obtener(this.idReporte).subscribe({
      next: (rep) => {
        this.reporte.set(rep);
        this.cargarProducto(rep.productoId);
        

        // Si ya está analizado, cargar análisis, recomendaciones y causas
        if (rep.estado === 'ANALIZADO') {
          this.cargarAnalisis();
          this.cargarRecomendaciones();
          this.cargarCausas();
        }
      },
      error: (e) => this.notify.error(e.error?.detail || 'No se pudo cargar el reporte'),
    });
  }

  cargarProducto(productoId: number): void {
    this.productosApi.obtener(productoId).subscribe({
      next: (p) => { this.producto.set(p); this.cargarRecursosDeEmpresa(p.empresaId); },
      error: () => {},
    });
  }

  cargarRecursosDeEmpresa(empresaId: number): void {
    this.recursosApi.listarPorEmpresa(empresaId).subscribe({
      next: (recs) => this.recursos.set(recs),
      error: () => {},
    });
  }

  obtenerNombreRecurso(recursoId: number): string {
    const r = this.recursos().find((rec) => rec.id === recursoId);
    return r ? `${r.codigo} - ${r.nombre}` : `Recurso #${recursoId}`;
  }

  cambiarPestana(p: Pestana): void {
    this.pestanaActiva.set(p);

    if (p === 'validaciones' && this.validaciones().length === 0) {
      this.cargarValidaciones();
    } else if (p === 'analisis' && !this.analisis() && !this.analisisPendiente()) {
      this.cargarAnalisis();
    } else if (p === 'recomendaciones' && this.recomendaciones().length === 0) {
      this.cargarRecomendaciones();
    } else if (p === 'causas' && this.causas().length === 0) {
      this.cargarCausas();
    }
  }

  calcularCumplimiento(): string {
    const r = this.reporte();
    if (!r || !r.demanda || r.demanda === 0) return '0.0';
    return ((r.produccionReal / r.demanda) * 100).toFixed(1);
  }

  calcularAnchoBarra(utilizacion: number): number {
    // Normalizar a máx 120% en escala visual
    const maxEscala = 120;
    const porcentaje = (utilizacion / maxEscala) * 100;
    return Math.min(Math.max(porcentaje, 0), 100);
  }

  recursosEnRanking(recursos: AnalisisRecurso[]): AnalisisRecurso[] {
    return recursos
      .filter((r) => r.prioridad != null && r.prioridad > 0)
      .sort((a, b) => (a.prioridad ?? 999) - (b.prioridad ?? 999));
  }

  contarSobrecarga(recursos: AnalisisRecurso[]): number {
    return recursos.filter((r) => r.clasificacion === 'SOBRECARGA').length;
  }

  contarAjustada(recursos: AnalisisRecurso[]): number {
    return recursos.filter((r) => r.clasificacion === 'CAPACIDAD_AJUSTADA').length;
  }

  calcularDeficitTotal(recursos: AnalisisRecurso[]): string {
    const suma = recursos.reduce((acc, r) => acc + (r.deficitHoras > 0 ? r.deficitHoras : 0), 0);
    return suma.toFixed(2);
  }

  // Validaciones
  cargarValidaciones(): void {
    this.cargandoValidaciones.set(true);
    this.reportesApi.obtenerValidaciones(this.idReporte).subscribe({
      next: (val) => {
        this.validaciones.set(val);
        this.cargandoValidaciones.set(false);
      },
      error: () => this.cargandoValidaciones.set(false),
    });
  }

  ejecutarValidacion(): void {
    this.validando.set(true);
    this.reportesApi.validar(this.idReporte).subscribe({
      next: (val) => {
        this.validaciones.set(val.resultados || []);
        this.validando.set(false);
        this.cambiarPestana('validaciones');
        if (this.tieneErroresCriticos()) {
          this.notify.error('Se detectaron errores críticos en el reporte.');
        } else {
          this.notify.exito('Validación completada sin errores críticos.');
        }
      },
      error: (e) => {
        this.validando.set(false);
        this.notify.error(e.error?.detail || 'Error al validar el reporte');
      },
    });
  }

  erroresCriticos(): ValidacionResultado[] {
    return this.validaciones().filter((v) => v.severidad === 'ERROR_CRITICO');
  }

  advertencias(): ValidacionResultado[] {
    return this.validaciones().filter((v) => v.severidad === 'ADVERTENCIA');
  }

  tieneErroresCriticos(): boolean {
    return this.erroresCriticos().length > 0;
  }

  // Análisis
  cargarAnalisis(): void {
    this.cargandoAnalisis.set(true);
    this.analisisPendiente.set(false);
    this.reportesApi.obtenerAnalisis(this.idReporte).subscribe({
      next: (an) => {
        this.analisis.set(an);
        this.cargandoAnalisis.set(false);
      },
      error: (e) => {
        this.cargandoAnalisis.set(false);
        if (e.status === 409) {
          this.analisisPendiente.set(true);
        } else {
          this.notify.error(e.error?.detail || 'No se pudo obtener el análisis');
        }
      },
    });
  }

  ejecutarAnalisis(): void {
    this.analizando.set(true);
    this.reportesApi.analizar(this.idReporte).subscribe({
      next: (an) => {
        this.analisis.set(an);
        this.analizando.set(false);
        this.analisisPendiente.set(false);
        this.notify.exito('Análisis SteelForce calculado exitosamente');
        this.cambiarPestana('analisis');
        this.cargarDetalle();
        this.cargarRecomendaciones();
        this.cargarCausas();
      },
      error: (e) => {
        this.analizando.set(false);
        this.notify.error(e.error?.detail || 'No se pudo ejecutar el análisis');
      },
    });
  }

  // Recomendaciones y Causas
  cargarRecomendaciones(): void {
    this.cargandoRecomendaciones.set(true);
    this.reportesApi.obtenerRecomendaciones(this.idReporte).subscribe({
      next: (recs) => {
        this.recomendaciones.set(recs);
        this.cargandoRecomendaciones.set(false);
      },
      error: () => this.cargandoRecomendaciones.set(false),
    });
  }

  cargarCausas(): void {
    this.cargandoCausas.set(true);
    this.reportesApi.obtenerCausas(this.idReporte).subscribe({
      next: (cs) => {
        this.causas.set(cs);
        this.cargandoCausas.set(false);
      },
      error: () => this.cargandoCausas.set(false),
    });
  }
}
