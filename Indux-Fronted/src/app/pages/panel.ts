import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EmpresasApi, ProductosApi, RecursosApi, ReportesApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { AnalisisRecurso, Empresa, Producto, Recurso, ReporteResumen } from '../core/models';
import { AppIconComponent } from '../core/icon.component';
import {
  obtenerClasificacionInfo,
  obtenerEstadoReporteInfo,
} from '../core/steelforce-constants';

@Component({
  selector: 'app-panel',
  imports: [CommonModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 max-w-7xl mx-auto animate-fade-in">
      <!-- Banner Ejecutivo de Bienvenida -->
      <div class="bg-gradient-to-r from-emerald-950 via-emerald-900 to-slate-900 rounded-3xl p-6 sm:p-8 text-white shadow-md relative overflow-hidden border border-emerald-800/40">
        <div class="relative z-10 max-w-2xl">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-wider bg-emerald-800/80 text-emerald-200 border border-emerald-700/60 mb-3 backdrop-blur-xs">
            <app-icon name="gauge" [size]="14" class="text-emerald-300" />
            <span>Plataforma Indux · Metodología SteelForce (ODS 9)</span>
          </div>
          <h1 class="text-2xl sm:text-3xl font-extrabold tracking-tight">
            Hola, {{ auth.usuario()?.nombre }}
          </h1>
          <p class="text-xs sm:text-sm text-emerald-200/90 mt-2 font-light leading-relaxed">
            @switch (auth.usuario()?.rol) {
              @case ('ADMIN') {
                Panel de control global. Supervisa empresas, estructura productiva, usuarios y reportes industriales.
              }
              @case ('ANALISTA') {
                Gestión de capacidad industrial. Registra reportes, valida parámetros y analiza cuellos de botella con SteelForce.
              }
              @case ('LIDER') {
                Panel de supervisión operativa. Visualiza el desempeño y capacidad de los recursos bajo tus procesos asignados.
              }
            }
          </p>
        </div>
      </div>

      <!-- Accesos Rápidos Profesionales según Rol -->
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3.5 sm:gap-4">
        @if (puedeOperar()) {
          <a
            routerLink="/app/reportes/nuevo"
            class="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs card-interactive group"
          >
            <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-700 font-bold flex items-center justify-center text-lg mb-2.5 group-hover:bg-emerald-600 group-hover:text-white transition">
              <app-icon name="plus" [size]="18" />
            </div>
            <h3 class="text-xs font-bold text-slate-900 group-hover:text-emerald-700 transition">Nuevo Reporte</h3>
            <p class="text-[11px] text-slate-500 mt-0.5">Crear evaluación manual</p>
          </a>

          <a
            routerLink="/app/importar"
            class="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs card-interactive group"
          >
            <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-700 font-bold flex items-center justify-center text-lg mb-2.5 group-hover:bg-emerald-600 group-hover:text-white transition">
              <app-icon name="upload" [size]="18" />
            </div>
            <h3 class="text-xs font-bold text-slate-900 group-hover:text-emerald-700 transition">Importar Archivo</h3>
            <p class="text-[11px] text-slate-500 mt-0.5">Subir Excel o CSV</p>
          </a>
        }

        <a
          routerLink="/app/calculadora"
          class="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs card-interactive group"
        >
          <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-700 font-bold flex items-center justify-center text-lg mb-2.5 group-hover:bg-emerald-600 group-hover:text-white transition">
            <app-icon name="calculator" [size]="18" />
          </div>
          <h3 class="text-xs font-bold text-slate-900 group-hover:text-emerald-700 transition">Calculadora al Vuelo</h3>
          <p class="text-[11px] text-slate-500 mt-0.5">Simular escenarios</p>
        </a>

        <a
          routerLink="/app/historico"
          class="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs card-interactive group"
        >
          <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-700 font-bold flex items-center justify-center text-lg mb-2.5 group-hover:bg-emerald-600 group-hover:text-white transition">
            <app-icon name="chart-no-axes-combined" [size]="18" />
          </div>
          <h3 class="text-xs font-bold text-slate-900 group-hover:text-emerald-700 transition">Tendencia SteelForce</h3>
          <p class="text-[11px] text-slate-500 mt-0.5">Histórico y series</p>
        </a>

        <a
          routerLink="/app/chatbot"
          class="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs card-interactive group"
        >
          <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-700 font-bold flex items-center justify-center text-lg mb-2.5 group-hover:bg-emerald-600 group-hover:text-white transition">
            <app-icon name="brain-circuit" [size]="18" />
          </div>
          <h3 class="text-xs font-bold text-slate-900 group-hover:text-emerald-700 transition">Asistente IA</h3>
          <p class="text-[11px] text-slate-500 mt-0.5">Diagnóstico inteligente</p>
        </a>
      </div>

      <!-- Tarjetas KPI Ejecutivas -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-white p-4 sm:p-5 rounded-2xl border border-slate-200 shadow-xs card-interactive flex flex-col justify-between">
          <div class="flex items-center justify-between text-slate-400">
            <span class="text-[10px] uppercase font-bold tracking-wider text-slate-500">Total Reportes</span>
            <app-icon name="file-text" [size]="16" class="text-emerald-600" />
          </div>
          <p class="text-2xl sm:text-3xl font-black text-slate-900 mt-2">{{ reportes().length }}</p>
          <span class="text-[11px] text-slate-500 mt-1">Evaluaciones registradas</span>
        </div>

        <div class="bg-white p-4 sm:p-5 rounded-2xl border border-slate-200 shadow-xs card-interactive flex flex-col justify-between">
          <div class="flex items-center justify-between text-slate-400">
            <span class="text-[10px] uppercase font-bold tracking-wider text-slate-500">Productos Activos</span>
            <app-icon name="package" [size]="16" class="text-emerald-600" />
          </div>
          <p class="text-2xl sm:text-3xl font-black text-slate-900 mt-2">{{ productos().length }}</p>
          <span class="text-[11px] text-slate-500 mt-1">En catálogo industrial</span>
        </div>

        <div class="bg-white p-4 sm:p-5 rounded-2xl border border-slate-200 shadow-xs card-interactive flex flex-col justify-between">
          <div class="flex items-center justify-between text-slate-400">
            <span class="text-[10px] uppercase font-bold tracking-wider text-slate-500">Recursos / Puestos</span>
            <app-icon name="factory" [size]="16" class="text-emerald-600" />
          </div>
          <p class="text-2xl sm:text-3xl font-black text-slate-900 mt-2">{{ recursos().length }}</p>
          <span class="text-[11px] text-slate-500 mt-1">Máquinas y estaciones</span>
        </div>

        <div class="bg-white p-4 sm:p-5 rounded-2xl border border-slate-200 shadow-xs card-interactive flex flex-col justify-between">
          <div class="flex items-center justify-between text-slate-400">
            <span class="text-[10px] uppercase font-bold tracking-wider text-slate-500">Reportes Analizados</span>
            <app-icon name="chart-no-axes-combined" [size]="16" class="text-emerald-700" />
          </div>
          <p class="text-2xl sm:text-3xl font-black text-emerald-800 mt-2">{{ contarAnalizados() }}</p>
          <span class="text-[11px] text-slate-500 mt-1">Con diagnóstico completo</span>
        </div>
      </div>

      <!-- Alerta Destacada: Cuello de Botella Primario (Restricción #1) -->
      @if (cuelloBotellaPrimario(); as cb) {
        <div class="rounded-2xl border border-rose-200 bg-rose-50/60 p-5 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4 animate-slide-up">
          <div class="flex items-start gap-3.5">
            <div class="w-10 h-10 rounded-xl bg-rose-600 text-white flex items-center justify-center shrink-0 shadow-xs">
              <app-icon name="shield-alert" [size]="20" />
            </div>
            <div>
              <div class="flex items-center gap-2">
                <span class="px-2 py-0.5 rounded-full text-[10px] font-extrabold uppercase bg-rose-200 text-rose-800">
                  Prioridad #1 · Restricción Primaria
                </span>
                <span class="text-xs font-mono text-rose-700 font-semibold">Código: {{ cb.codigo }}</span>
              </div>
              <h3 class="text-sm font-bold text-slate-900 mt-1">{{ cb.nombre }}</h3>
              <p class="text-xs text-slate-600 mt-0.5 max-w-2xl leading-relaxed">{{ cb.diagnostico }}</p>
            </div>
          </div>
          <div class="flex items-center gap-4 shrink-0 bg-white px-4 py-2.5 rounded-xl border border-rose-200">
            <div class="text-right">
              <span class="text-[10px] uppercase font-bold text-slate-500">Utilización</span>
              <p class="text-lg font-black text-rose-700 font-mono">{{ cb.utilizacion }} %</p>
            </div>
            <div class="h-8 w-px bg-slate-200"></div>
            <div class="text-right">
              <span class="text-[10px] uppercase font-bold text-slate-500">Déficit</span>
              <p class="text-lg font-black text-rose-700 font-mono">{{ cb.deficitHoras }} h</p>
            </div>
          </div>
        </div>
      }

      <!-- Secciones Principales: Últimos Reportes y Recursos Críticos del Último Análisis -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Columna 1 y 2: Últimos Reportes -->
        <div class="lg:col-span-2 bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden flex flex-col">
          <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <app-icon name="file-text" [size]="16" class="text-emerald-700" />
              <h2 class="text-xs font-bold uppercase tracking-wider text-slate-800">
                Últimos Reportes Evaluados
              </h2>
            </div>
            <a routerLink="/app/reportes" class="inline-flex items-center gap-1 text-xs text-emerald-700 hover:text-emerald-900 font-bold transition">
              <span>Ver todos</span>
              <app-icon name="arrow-right" [size]="13" />
            </a>
          </div>

          @if (cargando()) {
            <div class="p-6 space-y-3">
              <div class="h-10 skeleton w-full"></div>
              <div class="h-10 skeleton w-full"></div>
              <div class="h-10 skeleton w-full"></div>
            </div>
          } @else if (reportes().length === 0) {
            <div class="p-10 text-center text-xs text-slate-500 flex flex-col items-center justify-center">
              <app-icon name="file-text" [size]="32" class="text-slate-300 mb-2" />
              <p class="font-medium text-slate-700">No hay reportes de capacidad registrados</p>
              <p class="text-slate-400 mt-0.5">Crea tu primera evaluación de producción para ver los indicadores.</p>
            </div>
          } @else {
            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs border-collapse">
                <thead>
                  <tr class="bg-slate-50/80 text-[10px] uppercase text-slate-600 border-b border-slate-200">
                    <th class="py-3 px-4 font-semibold">ID</th>
                    <th class="py-3 px-4 font-semibold">Área / Planta</th>
                    <th class="py-3 px-4 font-semibold">Periodo</th>
                    <th class="py-3 px-4 font-semibold">Estado</th>
                    <th class="py-3 px-4 text-right font-semibold">Acción</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  @for (r of reportes().slice(0, 5); track r.id) {
                    @let eInfo = obtenerEstado(r.estado);
                    <tr class="hover:bg-slate-50/60 transition">
                      <td class="py-3 px-4 font-mono text-slate-400">#{{ r.id }}</td>
                      <td class="py-3 px-4 font-bold text-slate-900">{{ r.plantaArea }}</td>
                      <td class="py-3 px-4 font-mono text-slate-600 text-[11px] whitespace-nowrap">
                        {{ r.periodoInicio }} → {{ r.periodoFin }}
                      </td>
                      <td class="py-3 px-4">
                        <span
                          class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold border"
                          [class]="eInfo.badgeClass"
                        >
                          <span class="w-1.5 h-1.5 rounded-full" [class]="eInfo.dotColor"></span>
                          {{ eInfo.etiqueta }}
                        </span>
                      </td>
                      <td class="py-3 px-4 text-right">
                        <a
                          [routerLink]="['/app/reportes', r.id]"
                          class="inline-flex items-center gap-1 font-bold text-emerald-800 hover:text-emerald-950 transition"
                        >
                          <span>Ver</span>
                          <app-icon name="arrow-right" [size]="12" />
                        </a>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>

        <!-- Columna 3: Restricciones Críticas del Último Reporte Analizado -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 space-y-4 flex flex-col justify-between">
          <div>
            <div class="border-b border-slate-100 pb-3 flex items-center justify-between">
              <div>
                <h2 class="text-xs font-bold uppercase tracking-wider text-slate-800 flex items-center gap-2">
                  <app-icon name="alert-triangle" [size]="16" class="text-rose-600" />
                  <span>Restricciones Detectadas</span>
                </h2>
                <p class="text-[11px] text-slate-500 mt-0.5">
                  Recursos en sobrecarga en el último análisis.
                </p>
              </div>
            </div>

            @if (cargandoRecursosCriticos()) {
              <div class="p-6 space-y-2">
                <div class="h-14 skeleton w-full"></div>
                <div class="h-14 skeleton w-full"></div>
              </div>
            } @else if (recursosCriticos().length === 0) {
              <div class="text-center py-8 text-xs text-emerald-800 bg-emerald-50/70 rounded-xl p-4 border border-emerald-200 mt-4">
                <app-icon name="check-circle" [size]="28" class="text-emerald-600 mx-auto" />
                <p class="font-bold mt-2">Sin restricciones activas</p>
                <p class="text-[11px] text-emerald-700 mt-0.5">Todos los recursos operan dentro de su capacidad disponible.</p>
              </div>
            } @else {
              <div class="space-y-3 mt-3">
                @for (rc of recursosCriticos(); track rc.reporteRecursoId) {
                  @let cInfo = obtenerClasificacion(rc.clasificacion);
                  <div class="p-3.5 rounded-xl bg-slate-50 border border-slate-200 text-xs space-y-1.5 transition hover:bg-slate-100/70">
                    <div class="flex items-center justify-between font-bold">
                      <span class="text-slate-900">{{ rc.nombre }}</span>
                      <span [class]="cInfo.textColor" class="font-mono">{{ rc.utilizacion }} %</span>
                    </div>
                    <div class="flex items-center justify-between text-[11px] text-slate-500 font-mono">
                      <span>Código: {{ rc.codigo }}</span>
                      @if (rc.prioridad != null) {
                        <span class="text-rose-700 font-bold">Prioridad #{{ rc.prioridad }}</span>
                      }
                    </div>
                    <p class="text-[11px] text-slate-600 leading-snug pt-1 truncate" [title]="rc.diagnostico">
                      {{ rc.diagnostico }}
                    </p>
                  </div>
                }
              </div>
            }
          </div>

          <div class="pt-3 border-t border-slate-100">
            <a
              routerLink="/app/chatbot"
              class="w-full flex items-center justify-center gap-2 rounded-xl bg-emerald-50 hover:bg-emerald-100/80 border border-emerald-200 px-3 py-2 text-xs font-semibold text-emerald-800 transition"
            >
              <app-icon name="brain-circuit" [size]="15" />
              <span>Consultar diagnóstico con la IA</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class Panel implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly reportesApi = inject(ReportesApi);
  private readonly productosApi = inject(ProductosApi);
  private readonly recursosApi = inject(RecursosApi);

  protected readonly reportes = signal<ReporteResumen[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly recursos = signal<Recurso[]>([]);
  protected readonly recursosCriticos = signal<AnalisisRecurso[]>([]);
  protected readonly cuelloBotellaPrimario = signal<AnalisisRecurso | null>(null);

  protected readonly cargando = signal(true);
  protected readonly cargandoRecursosCriticos = signal(false);

  protected readonly obtenerEstado = obtenerEstadoReporteInfo;
  protected readonly obtenerClasificacion = obtenerClasificacionInfo;

  puedeOperar(): boolean {
    const r = this.auth.usuario()?.rol;
    return r === 'ADMIN' || r === 'ANALISTA';
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    const empId = this.auth.usuario()?.empresaId;

    this.reportesApi.listar(empId ? { empresaId: empId } : undefined).subscribe({
      next: (reps) => {
        this.reportes.set(reps);
        this.cargando.set(false);

        // Buscar el último reporte analizado para extraer recursos críticos
        const ultimoAnalizado = reps.find((r) => r.estado === 'ANALIZADO');
        if (ultimoAnalizado) {
          this.cargarRecursosCriticos(ultimoAnalizado.id);
        }
      },
      error: () => this.cargando.set(false),
    });

    if (empId) {
      this.productosApi.listarPorEmpresa(empId).subscribe({
        next: (prods) => this.productos.set(prods),
      });

      this.recursosApi.listarPorEmpresa(empId).subscribe({
        next: (recs) => this.recursos.set(recs),
      });
    }
  }

  cargarRecursosCriticos(reporteId: number): void {
    this.cargandoRecursosCriticos.set(true);
    this.reportesApi.obtenerAnalisis(reporteId).subscribe({
      next: (an) => {
        const criticos = an.recursos.filter(
          (r) => r.clasificacion === 'SOBRECARGA' || r.clasificacion === 'CAPACIDAD_AJUSTADA',
        );
        this.recursosCriticos.set(criticos);

        // Buscar restricción primaria (prioridad 1 o sobrecarga máxima)
        const primario = an.recursos.find((r) => r.prioridad === 1 && r.clasificacion === 'SOBRECARGA')
          || an.recursos.find((r) => r.clasificacion === 'SOBRECARGA');
        this.cuelloBotellaPrimario.set(primario || null);

        this.cargandoRecursosCriticos.set(false);
      },
      error: () => this.cargandoRecursosCriticos.set(false),
    });
  }

  contarAnalizados(): number {
    return this.reportes().filter((r) => r.estado === 'ANALIZADO').length;
  }
}
