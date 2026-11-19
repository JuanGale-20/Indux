import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EmpresasApi, ProductosApi, ReportesApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { Empresa, Producto, ReporteResumen } from '../core/models';
import { NotificationService } from '../core/notification.service';
import { obtenerEstadoReporteInfo } from '../core/steelforce-constants';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-reportes',
  imports: [CommonModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 app-fade-in">
      <!-- Cabecera -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">Reportes de Producción</h1>
          <p class="text-xs text-slate-500 mt-1">
            Histórico y análisis de capacidad SteelForce por periodos de operación.
          </p>
        </div>
        @if (puedeCrear()) {
          <div class="flex items-center gap-2">
            <a
              routerLink="/app/importar"
              class="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs font-semibold text-slate-700 shadow-xs hover:bg-slate-50 transition"
            >
              <app-icon name="upload" [size]="14" />
              <span>Importar Excel/CSV</span>
            </a>
            <a
              routerLink="/app/reportes/nuevo"
              class="inline-flex items-center gap-2 rounded-xl bg-emerald-700 px-4 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-emerald-800 transition shadow-emerald-700/20"
            >
              <app-icon name="plus" [size]="15" />
              <span>Nuevo Reporte</span>
            </a>
          </div>
        }
      </div>

      <!-- Filtros: Empresa (si ADMIN) y Producto -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 bg-white p-3 rounded-2xl border border-slate-200 shadow-sm">
        @if (esAdmin()) {
          <div>
            <label class="block text-[10px] font-bold uppercase tracking-wider text-slate-500 mb-1">Empresa</label>
            <select
              [value]="empresaSeleccionadaId() ?? ''"
              (change)="alCambiarEmpresa($any($event.target).value)"
              class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs text-slate-700 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
            >
              <option value="" disabled>Selecciona una empresa…</option>
              @for (emp of empresas(); track emp.id) {
                <option [value]="emp.id">{{ emp.nombre }}</option>
              }
            </select>
          </div>
        }

        <div [class]="esAdmin() ? '' : 'md:col-span-2'">
          <label class="block text-[10px] font-bold uppercase tracking-wider text-slate-500 mb-1">Filtrar por Producto</label>
          <select
            [value]="productoFiltroId() ?? ''"
            (change)="alCambiarProducto($any($event.target).value)"
            class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs text-slate-700 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
          >
            <option value="">Todos los productos</option>
            @for (p of productos(); track p.id) {
              <option [value]="p.id">{{ p.codigo }} - {{ p.nombre }}</option>
            }
          </select>
        </div>

        <div class="flex items-center justify-end text-xs text-slate-500 font-medium">
          Total: {{ reportes().length }} reporte(s)
        </div>
      </div>

      <!-- Estados de carga / error / vacío -->
      @if (cargando()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p class="mt-3 text-xs text-slate-500">Cargando reportes de producción…</p>
        </div>
      } @else if (error()) {
        <div class="p-6 text-center bg-rose-50 rounded-2xl border border-rose-200 text-rose-700 text-xs">
          <p class="font-bold">Error</p>
          <p class="mt-1">{{ error() }}</p>
        </div>
      } @else if (reportes().length === 0) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center mx-auto text-slate-400">
            <app-icon name="file-text" [size]="24" />
          </div>
          <h3 class="mt-3 text-sm font-bold text-slate-800">No hay reportes registrados</h3>
          <p class="text-xs text-slate-500 mt-1">
            Crea un reporte manualmente o importa datos desde una plantilla Excel / CSV.
          </p>
          @if (puedeCrear()) {
            <div class="mt-4 flex items-center justify-center gap-3">
              <a
                routerLink="/app/reportes/nuevo"
                class="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-700 text-white text-xs font-semibold hover:bg-emerald-800 transition shadow-sm"
              >
                <app-icon name="plus" [size]="14" />
                <span>Crear Nuevo Reporte</span>
              </a>
              <a
                routerLink="/app/importar"
                class="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-100 text-slate-700 text-xs font-semibold hover:bg-slate-200 transition"
              >
                <app-icon name="upload" [size]="14" />
                <span>Importar Archivo</span>
              </a>
            </div>
          }
        </div>
      } @else {
        <!-- Tabla de reportes -->
        <div class="overflow-hidden bg-white rounded-2xl border border-slate-200 shadow-sm">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase tracking-wider text-[10px]">
                <th class="py-3.5 px-4 font-semibold">ID</th>
                <th class="py-3.5 px-4 font-semibold">Planta / Área</th>
                <th class="py-3.5 px-4 font-semibold">Producto</th>
                <th class="py-3.5 px-4 font-semibold">Frecuencia</th>
                <th class="py-3.5 px-4 font-semibold">Periodo Evaluado</th>
                <th class="py-3.5 px-4 font-semibold">Demanda / Real</th>
                <th class="py-3.5 px-4 font-semibold">Estado</th>
                <th class="py-3.5 px-4 font-semibold text-right">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              @for (rep of reportes(); track rep.id) {
                <tr class="hover:bg-slate-50/80 transition">
                  <td class="py-3.5 px-4 font-mono text-slate-400">#{{ rep.id }}</td>
                  <td class="py-3.5 px-4 font-bold text-slate-900">{{ rep.plantaArea }}</td>
                  <td class="py-3.5 px-4 text-slate-700 font-medium">
                    {{ obtenerNombreProducto(rep.productoId) }}
                  </td>
                  <td class="py-3.5 px-4">
                    <span class="inline-block px-2 py-0.5 rounded text-[10px] font-bold uppercase bg-slate-100 text-slate-700">
                      {{ rep.frecuencia }}
                    </span>
                  </td>
                  <td class="py-3.5 px-4 font-mono text-slate-600 whitespace-nowrap">
                    {{ rep.periodoInicio }} → {{ rep.periodoFin }}
                  </td>
                  <td class="py-3.5 px-4 font-mono text-slate-700 whitespace-nowrap">
                    <span>{{ rep.demanda }}</span> / <span class="font-bold text-emerald-800">{{ rep.produccionReal }}</span>
                  </td>
                  <td class="py-3.5 px-4">
                    @let info = obtenerEstado(rep.estado);
                    <span
                      class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold border"
                      [class]="info.badgeClass"
                    >
                      <span class="w-1.5 h-1.5 rounded-full" [class]="info.dotColor"></span>
                      {{ info.etiqueta }}
                    </span>
                  </td>
                  <td class="py-3.5 px-4 text-right whitespace-nowrap">
                    <div class="inline-flex items-center gap-2">
                      <a
                        [routerLink]="['/app/reportes', rep.id]"
                        class="px-2.5 py-1 rounded-lg text-emerald-800 bg-emerald-50 hover:bg-emerald-100 transition font-bold"
                      >
                        Ver Detalle / Análisis
                      </a>
                      @if (puedeModificar()) {
                        <a
                          [routerLink]="['/app/reportes/editar', rep.id]"
                          class="px-2 py-1 rounded-lg text-slate-600 bg-slate-100 hover:bg-slate-200 transition font-medium"
                        >
                          Editar
                        </a>
                        <button
                          (click)="eliminar(rep)"
                          class="px-2 py-1 rounded-lg text-rose-600 bg-rose-50 hover:bg-rose-100 transition font-medium"
                        >
                          Eliminar
                        </button>
                      }
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
})
export class Reportes implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly reportesApi = inject(ReportesApi);
  private readonly productosApi = inject(ProductosApi);
  private readonly empresasApi = inject(EmpresasApi);
  private readonly notify = inject(NotificationService);

  protected readonly empresas = signal<Empresa[]>([]);
  protected readonly empresaSeleccionadaId = signal<number | null>(null);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly productoFiltroId = signal<number | null>(null);
  protected readonly reportes = signal<ReporteResumen[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly obtenerEstado = obtenerEstadoReporteInfo;

  esAdmin(): boolean {
    return this.auth.usuario()?.rol === 'ADMIN';
  }

  puedeCrear(): boolean {
    const rol = this.auth.usuario()?.rol;
    return rol === 'ADMIN' || rol === 'ANALISTA';
  }

  puedeModificar(): boolean {
    return this.puedeCrear();
  }

  ngOnInit(): void {
    if (this.esAdmin()) {
      this.empresasApi.listar().subscribe({
        next: (data) => {
          this.empresas.set(data);
          if (data.length > 0) {
            this.empresaSeleccionadaId.set(data[0].id);
            this.cargarProductosYReportes(data[0].id);
          }
        },
        error: () => {},
      });
    } else {
      const empId = this.auth.usuario()?.empresaId;
      if (empId) {
        this.empresaSeleccionadaId.set(empId);
        this.cargarProductosYReportes(empId);
      }
    }
  }

  alCambiarEmpresa(val: string): void {
    const id = Number(val);
    if (!isNaN(id)) {
      this.empresaSeleccionadaId.set(id);
      this.productoFiltroId.set(null);
      this.cargarProductosYReportes(id);
    }
  }

  alCambiarProducto(val: string): void {
    const id = val ? Number(val) : null;
    this.productoFiltroId.set(id);
    this.cargarReportes();
  }

  cargarProductosYReportes(empresaId: number): void {
    this.productosApi.listarPorEmpresa(empresaId).subscribe({
      next: (prods) => {
        this.productos.set(prods);
        this.cargarReportes();
      },
      error: () => this.cargarReportes(),
    });
  }

  cargarReportes(): void {
    this.cargando.set(true);
    this.error.set(null);

    const prodId = this.productoFiltroId();
    const empId = this.empresaSeleccionadaId();

    const params: { productoId?: number; empresaId?: number } = {};
    if (prodId) {
      params.productoId = prodId;
    } else if (empId) {
      params.empresaId = empId;
    }

    this.reportesApi.listar(params).subscribe({
      next: (data) => {
        this.reportes.set(data);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(e.error?.detail || 'No se pudieron cargar los reportes');
      },
    });
  }

  obtenerNombreProducto(productoId: number): string {
    const p = this.productos().find((x) => x.id === productoId);
    return p ? `${p.codigo} - ${p.nombre}` : `Producto #${productoId}`;
  }

  eliminar(rep: ReporteResumen): void {
    if (!confirm(`¿Estás seguro de eliminar el reporte #${rep.id} (${rep.plantaArea})?`)) {
      return;
    }
    this.reportesApi.eliminar(rep.id).subscribe({
      next: () => {
        this.notify.info(`Reporte #${rep.id} eliminado.`);
        this.cargarReportes();
      },
      error: (err) => {
        this.notify.error(err.error?.detail || 'No se pudo eliminar el reporte.');
      },
    });
  }
}
