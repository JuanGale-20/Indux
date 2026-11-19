import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EmpresasApi, RecursosApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { Empresa, HistoricoRecurso, PuntoHistorico, Recurso } from '../core/models';
import { NotificationService } from '../core/notification.service';
import {
  obtenerClasificacionInfo,
  obtenerTendenciaInfo,
} from '../core/steelforce-constants';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-historico',
  imports: [CommonModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 max-w-6xl mx-auto app-fade-in">
      <!-- Cabecera -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">
            Histórico y Tendencia de Recursos
          </h1>
          <p class="text-xs text-slate-500 mt-0.5">
            Evolución temporal de la utilización y detección de patrones de sobrecarga SteelForce.
          </p>
        </div>

        <div class="flex items-center gap-3">
          <label class="text-xs font-semibold text-slate-600">Límite de periodos:</label>
          <select
            [value]="limite()"
            (change)="alCambiarLimite($any($event.target).value)"
            class="rounded-xl border border-slate-300 bg-white px-3 py-1.5 text-xs text-slate-800 font-medium focus:border-emerald-600 focus:outline-none"
          >
            <option value="6">Últimos 6 periodos</option>
            <option value="12">Últimos 12 periodos</option>
            <option value="24">Últimos 24 periodos</option>
          </select>
        </div>
      </div>

      <!-- Selectores: Empresa (ADMIN) y Recurso -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
        @if (esAdmin()) {
          <div>
            <label class="block text-[10px] font-bold uppercase tracking-wider text-slate-500 mb-1">Empresa</label>
            <select
              [value]="empresaSeleccionadaId() ?? ''"
              (change)="alCambiarEmpresa($any($event.target).value)"
              class="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
            >
              <option value="" disabled>Selecciona una empresa…</option>
              @for (emp of empresas(); track emp.id) {
                <option [value]="emp.id">{{ emp.nombre }}</option>
              }
            </select>
          </div>
        }

        <div [class]="esAdmin() ? '' : 'md:col-span-2'">
          <label class="block text-[10px] font-bold uppercase tracking-wider text-slate-500 mb-1">
            Recurso o Máquina a Evaluar
          </label>
          <select
            [value]="recursoSeleccionadoId() ?? ''"
            (change)="alCambiarRecurso($any($event.target).value)"
            class="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
          >
            <option value="" disabled>Selecciona un recurso…</option>
            @for (r of recursos(); track r.id) {
              <option [value]="r.id">{{ r.codigo }} - {{ r.nombre }} ({{ r.tipo || 'Recurso' }})</option>
            }
          </select>
        </div>
      </div>

      <!-- Estados de carga / error / sin recurso -->
      @if (cargando()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p class="mt-3 text-xs text-slate-500">Cargando serie histórica…</p>
        </div>
      } @else if (error()) {
        <div class="p-6 text-center bg-rose-50 rounded-2xl border border-rose-200 text-rose-700 text-xs">
          <p class="font-bold">Error</p>
          <p class="mt-1">{{ error() }}</p>
        </div>
      } @else if (!recursoSeleccionadoId()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <p class="text-xs text-slate-500">Selecciona un recurso para visualizar su tendencia e histórico.</p>
        </div>
      } @else if (historico(); as h) {
        <!-- Tarjeta de Diagnóstico de Tendencia -->
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div>
              <span class="font-mono text-xs font-bold text-emerald-800 bg-emerald-50 px-2 py-0.5 rounded">
                {{ h.codigo }}
              </span>
              <h2 class="text-lg font-bold text-slate-900 mt-1">{{ h.nombre }}</h2>
              <p class="text-xs text-slate-500">
                Puntos evaluados: <strong class="text-slate-800">{{ h.puntos.length }}</strong> registros guardados.
              </p>
            </div>

            <!-- Badge de Tendencia -->
            @if (h.tendencia; as t) {
              @let tInfo = obtenerTendencia(t.direccion);
              <div class="flex flex-col items-end">
                <span
                  class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold border uppercase"
                  [class]="tInfo.badgeClass"
                >
                  <span class="font-bold">{{ tInfo.icono }}</span>
                  <span>{{ tInfo.etiqueta }}</span>
                </span>
                <span class="text-[11px] text-slate-500 mt-1">
                  Variación: <strong class="font-mono text-slate-800">{{ t.variacion > 0 ? '+' : '' }}{{ t.variacion }} %</strong> en {{ t.periodos }} periodos
                </span>
              </div>
            }
          </div>

          <!-- Mensaje explicativo de la tendencia -->
          @if (h.tendencia?.mensaje; as msj) {
            <div class="rounded-xl bg-slate-50 p-4 border border-slate-100 text-xs text-slate-700 flex items-start gap-3">
              <app-icon name="chart-no-axes-combined" [size]="16" class="text-emerald-700 shrink-0 mt-0.5" />
              <p class="leading-relaxed"><strong>Diagnóstico de Tendencia:</strong> {{ msj }}</p>
            </div>
          }

          <!-- Si hay menos de 2 puntos, advertencia amigable -->
          @if (h.puntos.length < 2) {
            <div class="py-8 text-center text-xs text-slate-500 bg-amber-50/50 rounded-xl border border-amber-200 p-4">
              <p class="font-bold text-amber-900">Datos históricos insuficientes para graficar la curva</p>
              <p class="mt-1 text-amber-700">
                Se requieren al menos 2 reportes analizados que incluyan a este recurso para trazar la línea de tendencia.
              </p>
            </div>
          } @else {
            <!-- Gráfica SVG de Utilización en el tiempo -->
            <div class="pt-4 space-y-2">
              <div class="flex items-center justify-between text-xs text-slate-600 mb-2">
                <span class="font-bold uppercase tracking-wider text-[10px]">Curva de Utilización (%)</span>
                <div class="flex items-center gap-3 text-[11px]">
                  <span class="inline-flex items-center gap-1.5 text-slate-400">
                    <span class="w-3 h-0.5 bg-slate-300 inline-block"></span> 80% / 90% Guías
                  </span>
                  <span class="inline-flex items-center gap-1.5 text-rose-500 font-semibold">
                    <span class="w-3 h-0.5 bg-rose-400 inline-block"></span> 100% Cuello
                  </span>
                </div>
              </div>

              <!-- Canvas SVG -->
              <div class="w-full bg-slate-50 rounded-2xl border border-slate-200 p-4 overflow-hidden">
                <svg viewBox="0 0 600 220" class="w-full h-52 overflow-visible">
                  <!-- Líneas guía horizontales (escala 0% a 140%) -->
                  <!-- 100% -> y = 200 - (100/140 * 180) = 200 - 128.57 = 71.4 -->
                  <line x1="40" y1="71" x2="580" y2="71" stroke="#fda4af" stroke-dasharray="4" stroke-width="1.5" />
                  <text x="10" y="75" fill="#f43f5e" font-size="10" font-family="monospace">100%</text>

                  <!-- 90% -> y = 200 - (90/140 * 180) = 200 - 115.7 = 84.3 -->
                  <line x1="40" y1="84" x2="580" y2="84" stroke="#e2e8f0" stroke-dasharray="2" stroke-width="1" />
                  <text x="15" y="88" fill="#94a3b8" font-size="9" font-family="monospace">90%</text>

                  <!-- 80% -> y = 200 - (80/140 * 180) = 200 - 102.8 = 97.2 -->
                  <line x1="40" y1="97" x2="580" y2="97" stroke="#e2e8f0" stroke-dasharray="2" stroke-width="1" />
                  <text x="15" y="101" fill="#94a3b8" font-size="9" font-family="monospace">80%</text>

                  <!-- Eje base 0% -->
                  <line x1="40" y1="200" x2="580" y2="200" stroke="#cbd5e1" stroke-width="1" />
                  <text x="20" y="204" fill="#94a3b8" font-size="9" font-family="monospace">0%</text>

                  <!-- Trazo de la línea de puntos -->
                  <polyline
                    fill="none"
                    stroke="#047857"
                    stroke-width="3"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    [attr.points]="construirPuntosSvg(h.puntos)"
                  />

                  <!-- Puntos circulares -->
                  @for (pt of calcularCoordenadasPuntos(h.puntos); track pt.x) {
                    <circle
                      [attr.cx]="pt.x"
                      [attr.cy]="pt.y"
                      r="4.5"
                      fill="#ffffff"
                      stroke="#047857"
                      stroke-width="2.5"
                    />
                    <text
                      [attr.x]="pt.x"
                      [attr.y]="pt.y - 8"
                      fill="#0f172a"
                      font-size="9"
                      font-family="monospace"
                      font-weight="bold"
                      text-anchor="middle"
                    >
                      {{ pt.valor }}%
                    </text>
                  }
                </svg>
              </div>
            </div>
          }
        </div>

        <!-- Tabla detallada de puntos históricos -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div class="px-5 py-4 border-b border-slate-100">
            <h2 class="text-xs font-bold uppercase tracking-wider text-slate-800">
              Registro Cronológico de Evaluaciones
            </h2>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full text-left text-xs border-collapse">
              <thead>
                <tr class="bg-slate-50 text-[10px] uppercase text-slate-600 border-b border-slate-200">
                  <th class="py-3 px-4 font-semibold">Reporte</th>
                  <th class="py-3 px-4 font-semibold">Periodo Evaluado</th>
                  <th class="py-3 px-4 font-semibold">Frecuencia</th>
                  <th class="py-3 px-4 font-semibold">Utilización</th>
                  <th class="py-3 px-4 font-semibold">Req / Disp</th>
                  <th class="py-3 px-4 font-semibold">Déficit Horas</th>
                  <th class="py-3 px-4 font-semibold">Impacto</th>
                  <th class="py-3 px-4 font-semibold">Clasificación</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                @for (p of h.puntos; track p.reporteId) {
                  @let cInfo = obtenerClasificacion(p.clasificacion);
                  <tr class="hover:bg-slate-50/80">
                    <td class="py-3 px-4">
                      <a
                        [routerLink]="['/app/reportes', p.reporteId]"
                        class="font-mono text-emerald-800 font-bold hover:underline"
                      >
                        #{{ p.reporteId }}
                      </a>
                    </td>
                    <td class="py-3 px-4 font-mono text-slate-600 whitespace-nowrap">
                      {{ p.periodoInicio }} → {{ p.periodoFin }}
                    </td>
                    <td class="py-3 px-4 uppercase text-slate-700">{{ p.frecuencia }}</td>
                    <td class="py-3 px-4 font-mono font-bold" [class]="cInfo.textColor">
                      {{ p.utilizacion }} %
                    </td>
                    <td class="py-3 px-4 font-mono text-slate-700">
                      {{ p.horasRequeridas }} / {{ p.horasDisponibles }} hrs
                    </td>
                    <td class="py-3 px-4 font-mono" [class]="p.deficitHoras > 0 ? 'text-rose-700 font-bold' : 'text-slate-500'">
                      {{ p.deficitHoras > 0 ? '-' + p.deficitHoras + ' hrs' : '0 hrs' }}
                    </td>
                    <td class="py-3 px-4 font-mono text-slate-700">
                      {{ p.impacto }} %
                    </td>
                    <td class="py-3 px-4">
                      <span
                        class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold border"
                        [class]="cInfo.badgeClass"
                      >
                        <span class="w-1.5 h-1.5 rounded-full" [class]="cInfo.dotColor"></span>
                        {{ cInfo.etiqueta }}
                      </span>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      }
    </div>
  `,
})
export class Historico implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly ruta = inject(ActivatedRoute);
  private readonly recursosApi = inject(RecursosApi);
  private readonly empresasApi = inject(EmpresasApi);
  private readonly notify = inject(NotificationService);

  protected readonly empresas = signal<Empresa[]>([]);
  protected readonly empresaSeleccionadaId = signal<number | null>(null);
  protected readonly recursos = signal<Recurso[]>([]);
  protected readonly recursoSeleccionadoId = signal<number | null>(null);
  protected readonly limite = signal<number>(12);

  protected readonly historico = signal<HistoricoRecurso | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly obtenerClasificacion = obtenerClasificacionInfo;
  protected readonly obtenerTendencia = obtenerTendenciaInfo;

  esAdmin(): boolean {
    return this.auth.usuario()?.rol === 'ADMIN';
  }

  ngOnInit(): void {
    const qRecursoId = this.ruta.snapshot.queryParamMap.get('recursoId');

    if (this.esAdmin()) {
      this.empresasApi.listar().subscribe({
        next: (data) => {
          this.empresas.set(data);
          if (data.length > 0) {
            this.empresaSeleccionadaId.set(data[0].id);
            this.cargarRecursos(qRecursoId ? Number(qRecursoId) : null);
          }
        },
      });
    } else {
      const empId = this.auth.usuario()?.empresaId;
      if (empId) {
        this.empresaSeleccionadaId.set(empId);
        this.cargarRecursos(qRecursoId ? Number(qRecursoId) : null);
      }
    }
  }

  alCambiarEmpresa(val: string): void {
    const id = Number(val);
    if (!isNaN(id)) {
      this.empresaSeleccionadaId.set(id);
      this.cargarRecursos(null);
    }
  }

  cargarRecursos(preseleccionId: number | null): void {
    const empId = this.empresaSeleccionadaId();
    if (!empId) return;

    this.recursosApi.listarPorEmpresa(empId).subscribe({
      next: (recs) => {
        this.recursos.set(recs);
        if (preseleccionId && recs.some((r) => r.id === preseleccionId)) {
          this.recursoSeleccionadoId.set(preseleccionId);
          this.cargarHistorico();
        } else if (recs.length > 0) {
          this.recursoSeleccionadoId.set(recs[0].id);
          this.cargarHistorico();
        } else {
          this.recursoSeleccionadoId.set(null);
          this.historico.set(null);
        }
      },
      error: () => {},
    });
  }

  alCambiarRecurso(val: string): void {
    const id = Number(val);
    if (!isNaN(id)) {
      this.recursoSeleccionadoId.set(id);
      this.cargarHistorico();
    }
  }

  alCambiarLimite(val: string): void {
    this.limite.set(Number(val));
    this.cargarHistorico();
  }

  cargarHistorico(): void {
    const recId = this.recursoSeleccionadoId();
    if (!recId) return;

    this.cargando.set(true);
    this.error.set(null);
    this.recursosApi.obtenerHistorico(recId, this.limite()).subscribe({
      next: (data) => {
        this.historico.set(data);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(e.error?.detail || 'No se pudo cargar el histórico');
      },
    });
  }

  // Coordenadas para SVG
  calcularCoordenadasPuntos(puntos: PuntoHistorico[]): Array<{ x: number; y: number; valor: number }> {
    if (!puntos || puntos.length === 0) return [];
    const minX = 60;
    const maxX = 560;
    const paso = puntos.length > 1 ? (maxX - minX) / (puntos.length - 1) : 0;
    const maxEscala = 140; // 140% como escala superior

    return puntos.map((p, i) => {
      const x = puntos.length === 1 ? (minX + maxX) / 2 : minX + i * paso;
      // y = 200 - (utilizacion / maxEscala * 180)
      const u = Math.min(Math.max(p.utilizacion, 0), maxEscala);
      const y = 200 - (u / maxEscala) * 180;
      return { x, y, valor: p.utilizacion };
    });
  }

  construirPuntosSvg(puntos: PuntoHistorico[]): string {
    const coords = this.calcularCoordenadasPuntos(puntos);
    return coords.map((c) => `${c.x},${c.y}`).join(' ');
  }
}
