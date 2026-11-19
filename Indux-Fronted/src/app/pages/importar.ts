import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { EmpresasApi, ImportacionesApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { Empresa, ResultadoImportacion } from '../core/models';
import { NotificationService } from '../core/notification.service';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-importar',
  imports: [CommonModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 max-w-5xl mx-auto app-fade-in">
      <!-- Cabecera -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div class="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <a routerLink="/app/reportes" class="hover:text-emerald-700">← Volver a Reportes</a>
          </div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">
            Importación Masiva de Reportes
          </h1>
          <p class="text-xs text-slate-500 mt-0.5">
            Carga reportes y líneas de recursos desde archivos Excel (.xlsx) o CSV con validación todo-o-nada.
          </p>
        </div>

        <!-- Descarga de Plantillas -->
        <div class="flex items-center gap-2">
          <button
            (click)="descargarPlantilla('xlsx')"
            [disabled]="descargandoPlantilla()"
            class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 transition shadow-xs disabled:opacity-50"
          >
            <app-icon name="upload" [size]="14" />
            <span>Plantilla Excel (.xlsx)</span>
          </button>
          <button
            (click)="descargarPlantilla('csv')"
            [disabled]="descargandoPlantilla()"
            class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 transition shadow-xs disabled:opacity-50"
          >
            <app-icon name="file-text" [size]="14" />
            <span>Plantilla CSV</span>
          </button>
        </div>
      </div>

      <!-- Selector de Empresa para ADMIN -->
      @if (esAdmin()) {
        <div class="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <label class="block text-xs font-bold uppercase tracking-wider text-slate-700">
              Empresa Destino para la Importación *
            </label>
            <p class="text-[11px] text-slate-500">
              Como administrador global, debes asociar los reportes a una empresa registrada.
            </p>
          </div>
          <select
            [value]="empresaSeleccionadaId() ?? ''"
            (change)="alSeleccionarEmpresa($any($event.target).value)"
            class="w-full sm:w-72 rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
          >
            <option value="" disabled>Selecciona una empresa…</option>
            @for (emp of empresas(); track emp.id) {
              <option [value]="emp.id">{{ emp.nombre }}</option>
            }
          </select>
        </div>
      }

      <!-- Zona Drag & Drop de Carga -->
      <div
        (dragover)="onDragOver($event)"
        (dragleave)="onDragLeave($event)"
        (drop)="onDrop($event)"
        class="border-2 border-dashed rounded-3xl p-10 text-center transition bg-white"
        [class]="arrastrando() ? 'border-emerald-500 bg-emerald-50/30' : 'border-slate-300 hover:border-emerald-400'"
      >
        <div class="max-w-md mx-auto space-y-3">
          <div class="w-14 h-14 bg-emerald-50 text-emerald-800 rounded-2xl flex items-center justify-center mx-auto">
            <app-icon name="upload" [size]="28" class="text-emerald-700" />
          </div>
          <div>
            <p class="text-sm font-bold text-slate-900">
              Arrastra tu archivo aquí o haz clic para explorar
            </p>
            <p class="text-xs text-slate-500 mt-1">
              Archivos permitidos: <strong class="text-slate-700">.xlsx</strong> o <strong class="text-slate-700">.csv</strong>. Tamaño máximo: 5 MB.
            </p>
          </div>

          <input
            #inputArchivo
            type="file"
            accept=".xlsx, .csv"
            (change)="alSeleccionarArchivo($event)"
            class="hidden"
          />

          <button
            type="button"
            (click)="inputArchivo.click()"
            class="inline-flex items-center gap-2 px-5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-xs font-semibold text-slate-800 transition"
          >
            Seleccionar archivo local
          </button>

          @if (archivoSeleccionado(); as file) {
            <div class="mt-4 inline-flex items-center gap-3 px-4 py-2 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-900 font-medium">
              <app-icon name="file-text" [size]="14" class="text-emerald-800" />
              <span>{{ file.name }} ({{ formatearTamano(file.size) }})</span>
              <button (click)="limpiarArchivo()" class="text-emerald-700 hover:text-emerald-950 p-0.5 rounded hover:bg-emerald-100">
                <app-icon name="x" [size]="14" />
              </button>
            </div>
          }
        </div>
      </div>

      <!-- Botón de Generar Vista Previa -->
      @if (archivoSeleccionado() && !resultadoVistaPrevia()) {
        <div class="flex justify-end">
          <button
            (click)="procesarVistaPrevia()"
            [disabled]="procesando() || (esAdmin() && !empresaSeleccionadaId())"
            class="px-6 py-2.5 rounded-xl text-xs font-bold text-white bg-emerald-700 hover:bg-emerald-800 transition disabled:opacity-50 shadow-sm"
          >
            {{ procesando() ? 'Validando archivo…' : 'Generar Vista Previa (Sin Guardar)' }}
          </button>
        </div>
      }

      <!-- Spinner Procesando -->
      @if (procesando()) {
        <div class="p-8 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p class="mt-3 text-xs text-slate-500">Analizando registros y validando datos…</p>
        </div>
      }

      <!-- RESULTADO DE VISTA PREVIA O CONFIRMACIÓN -->
      @if (resultadoVistaPrevia(); as res) {
        <div class="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-6">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div>
              <div class="flex items-center gap-2">
                <span
                  class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold uppercase"
                  [class]="res.valido ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'"
                >
                  <app-icon [name]="res.valido ? 'check-circle' : 'alert-triangle'" [size]="13" />
                  <span>{{ res.valido ? 'Archivo Válido' : 'Errores Encontrados' }}</span>
                </span>
                <span class="text-xs text-slate-500">Filas leídas: {{ res.filasLeidas }}</span>
              </div>
              <h2 class="text-base font-bold text-slate-900 mt-2">
                {{ res.guardado ? 'Importación Completada Exitosamente' : 'Resultado de Vista Previa' }}
              </h2>
              <p class="text-xs text-slate-500 mt-0.5">
                {{ res.valido
                  ? 'Todos los registros son coherentes y están listos para ser guardados en bloque (todo-o-nada).'
                  : 'Corrige las filas señaladas en tu archivo antes de continuar. La importación es atómica (no se guarda nada si hay errores).' }}
              </p>
            </div>

            <!-- Botones de Acción -->
            <div class="flex items-center gap-2">
              <button
                (click)="limpiarArchivo()"
                class="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100 transition"
              >
                Limpiar y cargar otro
              </button>
              @if (res.valido && !res.guardado) {
                <button
                  (click)="confirmarImportacion()"
                  [disabled]="confirmando()"
                  class="px-6 py-2 rounded-xl text-xs font-bold text-white bg-emerald-700 hover:bg-emerald-800 transition shadow-sm disabled:opacity-50"
                >
                  {{ confirmando() ? 'Guardando reportes…' : 'Confirmar y Guardar Todo' }}
                </button>
              }
            </div>
          </div>

          <!-- Errores por Fila -->
          @if (res.errores && res.errores.length > 0) {
            <div class="space-y-3">
              <h3 class="text-xs font-bold uppercase tracking-wider text-rose-900">
                Detalle de Inconsistencias ({{ res.errores.length }})
              </h3>
              <div class="max-h-60 overflow-y-auto space-y-2 pr-2">
                @for (err of res.errores; track err.fila + err.columna) {
                  <div class="bg-rose-50 border border-rose-200 rounded-xl p-3 text-xs flex items-start gap-3">
                    <span class="font-mono font-bold text-rose-900 bg-rose-200 px-2 py-0.5 rounded text-[10px] shrink-0">
                      Fila {{ err.fila }}
                    </span>
                    <div class="flex-1">
                      <span class="font-bold text-slate-800">Columna: {{ err.columna }}</span>
                      <p class="text-rose-700 mt-0.5">{{ err.mensaje }}</p>
                    </div>
                  </div>
                }
              </div>
            </div>
          }

          <!-- Reportes Identificados en la Plantilla -->
          @if (res.reportes && res.reportes.length > 0) {
            <div class="space-y-3">
              <h3 class="text-xs font-bold uppercase tracking-wider text-slate-700">
                Reportes Identificados en el Archivo ({{ res.reportes.length }})
              </h3>
              <div class="overflow-x-auto rounded-xl border border-slate-200">
                <table class="w-full text-left text-xs border-collapse">
                  <thead class="bg-slate-50 text-[10px] uppercase text-slate-600 border-b border-slate-200">
                    <tr>
                      <th class="py-2.5 px-3">Producto</th>
                      <th class="py-2.5 px-3">Planta / Área</th>
                      <th class="py-2.5 px-3">Frecuencia</th>
                      <th class="py-2.5 px-3">Periodo</th>
                      <th class="py-2.5 px-3">Líneas de Recursos</th>
                      @if (res.guardado) {
                        <th class="py-2.5 px-3 text-right">Acción</th>
                      }
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    @for (rep of res.reportes; track rep.productoCodigo + rep.plantaArea) {
                      <tr class="hover:bg-slate-50">
                        <td class="py-2.5 px-3 font-mono font-bold text-emerald-800">{{ rep.productoCodigo }}</td>
                        <td class="py-2.5 px-3 font-bold text-slate-900">{{ rep.plantaArea }}</td>
                        <td class="py-2.5 px-3 uppercase">{{ rep.frecuencia }}</td>
                        <td class="py-2.5 px-3 font-mono text-slate-600">{{ rep.periodoInicio }} → {{ rep.periodoFin }}</td>
                        <td class="py-2.5 px-3 font-medium">{{ rep.recursos }} recursos</td>
                        @if (res.guardado && rep.reporteId) {
                          <td class="py-2.5 px-3 text-right">
                            <a
                              [routerLink]="['/app/reportes', rep.reporteId]"
                              class="text-emerald-700 font-bold hover:underline"
                            >
                              Ver Reporte →
                            </a>
                          </td>
                        }
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class Importar implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly importacionesApi = inject(ImportacionesApi);
  private readonly empresasApi = inject(EmpresasApi);
  private readonly notify = inject(NotificationService);

  protected readonly empresas = signal<Empresa[]>([]);
  protected readonly empresaSeleccionadaId = signal<number | null>(null);

  protected readonly archivoSeleccionado = signal<File | null>(null);
  protected readonly arrastrando = signal(false);
  protected readonly descargandoPlantilla = signal(false);
  protected readonly procesando = signal(false);
  protected readonly confirmando = signal(false);
  protected readonly resultadoVistaPrevia = signal<ResultadoImportacion | null>(null);

  esAdmin(): boolean {
    return this.auth.usuario()?.rol === 'ADMIN';
  }

  ngOnInit(): void {
    if (this.esAdmin()) {
      this.empresasApi.listar().subscribe({
        next: (emps) => {
          this.empresas.set(emps);
          if (emps.length > 0) {
            this.empresaSeleccionadaId.set(emps[0].id);
          }
        },
      });
    } else {
      this.empresaSeleccionadaId.set(this.auth.usuario()?.empresaId || null);
    }
  }

  alSeleccionarEmpresa(val: string): void {
    this.empresaSeleccionadaId.set(val ? Number(val) : null);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.arrastrando.set(true);
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.arrastrando.set(false);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.arrastrando.set(false);
    if (event.dataTransfer && event.dataTransfer.files.length > 0) {
      this.validarYFijarArchivo(event.dataTransfer.files[0]);
    }
  }

  alSeleccionarArchivo(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      this.validarYFijarArchivo(target.files[0]);
    }
  }

  validarYFijarArchivo(file: File): void {
    const ext = file.name.split('.').pop()?.toLowerCase();
    if (ext !== 'xlsx' && ext !== 'csv') {
      this.notify.error('Solo se admiten archivos .xlsx o .csv', 'Formato no soportado');
      return;
    }
    const maxBytes = 5 * 1024 * 1024; // 5 MB
    if (file.size > maxBytes) {
      this.notify.error('El archivo excede el tamaño máximo permitido de 5 MB.', 'Archivo demasiado grande');
      return;
    }

    this.archivoSeleccionado.set(file);
    this.resultadoVistaPrevia.set(null);
  }

  limpiarArchivo(): void {
    this.archivoSeleccionado.set(null);
    this.resultadoVistaPrevia.set(null);
  }

  formatearTamano(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  }

  descargarPlantilla(formato: 'xlsx' | 'csv'): void {
    this.descargandoPlantilla.set(true);
    this.importacionesApi.descargarPlantilla(formato).subscribe({
      next: (blob) => {
        this.descargandoPlantilla.set(false);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = formato === 'csv' ? 'plantilla_reportes.csv' : 'plantilla_reportes.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
        this.notify.exito(`Plantilla ${formato.toUpperCase()} descargada.`);
      },
      error: (err) => {
        this.descargandoPlantilla.set(false);
        this.notify.error(err.error?.detail || 'Error al descargar plantilla');
      },
    });
  }

  procesarVistaPrevia(): void {
    const archivo = this.archivoSeleccionado();
    if (!archivo) return;
    const empId = this.empresaSeleccionadaId();

    this.procesando.set(true);
    this.importacionesApi.importar(archivo, false, empId).subscribe({
      next: (res) => {
        this.procesando.set(false);
        this.resultadoVistaPrevia.set(res);
        if (res.valido) {
          this.notify.exito('Vista previa generada sin errores.');
        } else {
          this.notify.advertencia(`El archivo contiene ${res.errores.length} inconsistencia(s).`);
        }
      },
      error: (err) => {
        this.procesando.set(false);
        this.notify.error(err.error?.detail || 'Error al procesar el archivo');
      },
    });
  }

  confirmarImportacion(): void {
    const archivo = this.archivoSeleccionado();
    if (!archivo) return;
    const empId = this.empresaSeleccionadaId();

    this.confirmando.set(true);
    this.importacionesApi.importar(archivo, true, empId).subscribe({
      next: (res) => {
        this.confirmando.set(false);
        this.resultadoVistaPrevia.set(res);
        this.notify.exito('¡Reportes importados y guardados con éxito!');
      },
      error: (err) => {
        this.confirmando.set(false);
        this.notify.error(err.error?.detail || 'No se pudo guardar la importación');
      },
    });
  }
}
