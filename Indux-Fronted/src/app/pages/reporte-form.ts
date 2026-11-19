import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  FormArray,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { EmpresasApi, ProductosApi, RecursosApi, ReportesApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { Empresa, FrecuenciaReporte, Producto, Recurso, Reporte } from '../core/models';
import { NotificationService } from '../core/notification.service';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-reporte-form',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 max-w-5xl mx-auto app-fade-in">
      <!-- Cabecera -->
      <div class="flex items-center justify-between pb-4 border-b border-slate-200">
        <div>
          <div class="flex items-center gap-2">
            <a routerLink="/app/reportes" class="text-xs text-slate-500 hover:text-emerald-700">← Volver a Reportes</a>
          </div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight mt-1">
            {{ esEdicion() ? 'Editar Reporte de Producción #' + reporteId() : 'Nuevo Reporte de Producción' }}
          </h1>
          <p class="text-xs text-slate-500 mt-0.5">
            Ingresa las condiciones operativas y las líneas de recursos para el cálculo SteelForce.
          </p>
        </div>
      </div>

      @if (cargandoInicial()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200">
          <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p class="mt-3 text-xs text-slate-500">Cargando datos del reporte…</p>
        </div>
      } @else {
        <form [formGroup]="form" (ngSubmit)="guardar()" class="space-y-6">
          <!-- Tarjeta 1: Parámetros del Reporte -->
          <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
            <h2 class="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-emerald-600"></span>
              Parámetros Generales
            </h2>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              @if (esAdmin() && !esEdicion()) {
                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="r-empresa">
                    Empresa *
                  </label>
                  <select
                    id="r-empresa"
                    [value]="empresaSeleccionadaId() ?? ''"
                    (change)="alCambiarEmpresa($any($event.target).value)"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                  >
                    <option value="" disabled>Selecciona empresa…</option>
                    @for (emp of empresas(); track emp.id) {
                      <option [value]="emp.id">{{ emp.nombre }}</option>
                    }
                  </select>
                </div>
              }

              <div [class]="esAdmin() && !esEdicion() ? '' : 'md:col-span-1'">
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="r-producto">
                  Producto *
                </label>
                <select
                  id="r-producto"
                  formControlName="productoId"
                  [disabled]="esEdicion()"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none disabled:bg-slate-100"
                >
                  <option [ngValue]="null" disabled>Selecciona producto…</option>
                  @for (p of productos(); track p.id) {
                    <option [ngValue]="p.id">{{ p.codigo }} - {{ p.nombre }}</option>
                  }
                </select>
                @if (form.controls.productoId.touched && form.controls.productoId.invalid) {
                  <p class="mt-1 text-xs text-rose-600">El producto es obligatorio.</p>
                }
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="r-planta">
                  Planta / Área *
                </label>
                <input
                  id="r-planta"
                  type="text"
                  formControlName="plantaArea"
                  placeholder="Ej. Nave Principal - Línea A"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                />
                @if (form.controls.plantaArea.touched && form.controls.plantaArea.invalid) {
                  <p class="mt-1 text-xs text-rose-600">Requerido (máx 120 caract.).</p>
                }
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="r-frecuencia">
                  Frecuencia de Evaluación *
                </label>
                <select
                  id="r-frecuencia"
                  formControlName="frecuencia"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                >
                  <option value="DIARIA">DIARIA</option>
                  <option value="SEMANAL">SEMANAL</option>
                  <option value="MENSUAL">MENSUAL</option>
                  <option value="ANUAL">ANUAL</option>
                </select>
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-4 gap-4 pt-2">
              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="r-inicio">
                  Periodo Inicio *
                </label>
                <input
                  id="r-inicio"
                  type="date"
                  formControlName="periodoInicio"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="r-fin">
                  Periodo Fin *
                </label>
                <input
                  id="r-fin"
                  type="date"
                  formControlName="periodoFin"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="r-demanda">
                  Demanda Requerida *
                </label>
                <input
                  id="r-demanda"
                  type="number"
                  step="0.01"
                  min="0"
                  formControlName="demanda"
                  placeholder="0.00"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="r-real">
                  Producción Real Alcanzada *
                </label>
                <input
                  id="r-real"
                  type="number"
                  step="0.01"
                  min="0"
                  formControlName="produccionReal"
                  placeholder="0.00"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                />
              </div>
            </div>
          </div>

          <!-- Tarjeta 2: Líneas por Recurso -->
          <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
            <div class="flex items-center justify-between pb-2 border-b border-slate-100">
              <div>
                <h2 class="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-2">
                  <span class="w-2 h-2 rounded-full bg-emerald-600"></span>
                  Líneas Operativas por Recurso ({{ lineasArray.length }})
                </h2>
                <p class="text-xs text-slate-500 mt-0.5">
                  Parámetros de turnos, horas y tiempos de procesamiento por puesto/máquina.
                </p>
              </div>
              <button
                type="button"
                (click)="agregarLinea()"
                class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-50 text-emerald-800 hover:bg-emerald-100 transition text-xs font-semibold"
              >
                <app-icon name="plus" [size]="13" />
                <span>Agregar Recurso</span>
              </button>
            </div>

            @if (lineasArray.length === 0) {
              <div class="py-8 text-center text-xs text-slate-500 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                <p>No se han agregado líneas de recursos a este reporte.</p>
                <button
                  type="button"
                  (click)="agregarLinea()"
                  class="mt-2 text-emerald-700 font-bold underline"
                >
                  Agregar primer recurso
                </button>
              </div>
            } @else {
              <div class="space-y-4">
                @for (lineaForm of lineasArray.controls; track $index) {
                  <div
                    [formGroup]="$any(lineaForm)"
                    class="p-4 rounded-xl border border-slate-200 bg-slate-50/50 space-y-3 relative"
                  >
                    <div class="flex items-center justify-between">
                      <span class="text-xs font-bold text-slate-700">Línea #{{ $index + 1 }}</span>
                      <button
                        type="button"
                        (click)="quitarLinea($index)"
                        class="inline-flex items-center gap-1 text-xs text-rose-600 hover:text-rose-800 font-semibold"
                      >
                        <app-icon name="x" [size]="13" />
                        <span>Quitar</span>
                      </button>
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3 text-xs">
                      <div class="md:col-span-2">
                        <label class="block font-semibold text-slate-700 mb-1">Recurso *</label>
                        <select
                          formControlName="recursoId"
                          class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                        >
                          <option [ngValue]="null" disabled>Selecciona recurso…</option>
                          @for (r of recursosDisponibles(); track r.id) {
                            <option [ngValue]="r.id">{{ r.codigo }} - {{ r.nombre }} ({{ r.tipo || 'Recurso' }})</option>
                          }
                        </select>
                      </div>

                      <div>
                        <label class="block font-semibold text-slate-700 mb-1">Tiempo Proc. *</label>
                        <input
                          type="number"
                          step="0.0001"
                          min="0.0001"
                          formControlName="tiempoProcesamiento"
                          placeholder="0.0000"
                          class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                        />
                      </div>

                      <div>
                        <label class="block font-semibold text-slate-700 mb-1">Unidad Ref. *</label>
                        <input
                          type="text"
                          formControlName="unidadReferencia"
                          placeholder="Ej. HORAS/PIEZA"
                          class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                        />
                      </div>
                    </div>

                    <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3 text-xs pt-1">
                      <div>
                        <label class="block font-semibold text-slate-700 mb-1">Tiempo Calendario *</label>
                        <input
                          type="number"
                          step="0.01"
                          min="0"
                          formControlName="tiempoCalendario"
                          placeholder="0"
                          class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                        />
                      </div>

                      <div>
                        <label class="block font-semibold text-slate-700 mb-1">No Disponible (Paradas)</label>
                        <input
                          type="number"
                          step="0.01"
                          min="0"
                          formControlName="tiempoNoDisponible"
                          placeholder="0 (opcional)"
                          class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                        />
                      </div>

                      <div>
                        <label class="block font-semibold text-slate-700 mb-1">Núm. Turnos *</label>
                        <input
                          type="number"
                          min="1"
                          formControlName="numeroTurnos"
                          placeholder="1"
                          class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                        />
                      </div>

                      <div>
                        <label class="block font-semibold text-slate-700 mb-1">Horas / Turno *</label>
                        <input
                          type="number"
                          step="0.1"
                          min="0.1"
                          max="24"
                          formControlName="horasPorTurno"
                          placeholder="8"
                          class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                        />
                      </div>

                      <div class="col-span-2 sm:col-span-1">
                        <label class="block font-semibold text-slate-700 mb-1">Días Operación *</label>
                        <input
                          type="number"
                          min="1"
                          formControlName="diasOperacion"
                          placeholder="20"
                          class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                        />
                      </div>
                    </div>
                  </div>
                }
              </div>
            }
          </div>

          @if (errorFormulario()) {
            <div class="rounded-xl bg-rose-50 border border-rose-200 p-4 text-xs text-rose-700">
              <p class="font-bold">No se pudo guardar el reporte:</p>
              <p class="mt-1">{{ errorFormulario() }}</p>
            </div>
          }

          <div class="flex items-center justify-end gap-3 pt-4">
            <a
              routerLink="/app/reportes"
              class="px-5 py-2.5 rounded-xl text-xs font-semibold text-slate-600 bg-white border border-slate-200 hover:bg-slate-50 transition"
            >
              Cancelar
            </a>
            <button
              type="submit"
              [disabled]="guardando()"
              class="px-6 py-2.5 rounded-xl text-xs font-bold text-white bg-emerald-700 hover:bg-emerald-800 transition disabled:opacity-60 shadow-sm"
            >
              {{ guardando() ? 'Guardando Reporte…' : esEdicion() ? 'Actualizar Reporte' : 'Guardar y Continuar' }}
            </button>
          </div>
        </form>
      }
    </div>
  `,
})
export class ReporteForm implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reportesApi = inject(ReportesApi);
  private readonly productosApi = inject(ProductosApi);
  private readonly recursosApi = inject(RecursosApi);
  private readonly empresasApi = inject(EmpresasApi);
  private readonly notify = inject(NotificationService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly esEdicion = signal(false);
  protected readonly reporteId = signal<number | null>(null);
  protected readonly cargandoInicial = signal(true);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  protected readonly empresas = signal<Empresa[]>([]);
  protected readonly empresaSeleccionadaId = signal<number | null>(null);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly recursosDisponibles = signal<Recurso[]>([]);

  protected readonly form = this.fb.group({
    productoId: [null as number | null, [Validators.required]],
    plantaArea: ['', [Validators.required, Validators.maxLength(120)]],
    frecuencia: ['MENSUAL' as FrecuenciaReporte, [Validators.required]],
    periodoInicio: ['', [Validators.required]],
    periodoFin: ['', [Validators.required]],
    demanda: [1000, [Validators.required, Validators.min(0)]],
    produccionReal: [950, [Validators.required, Validators.min(0)]],
    lineas: this.fb.array<FormGroup>([]),
  });

  get lineasArray(): FormArray {
    return this.form.get('lineas') as FormArray;
  }

  esAdmin(): boolean {
    return this.auth.usuario()?.rol === 'ADMIN';
  }

  ngOnInit(): void {
    const idParam = this.ruta.snapshot.paramMap.get('id');
    const hoy = new Date();
    const haceUnMes = new Date(hoy.getFullYear(), hoy.getMonth() - 1, hoy.getDate());
    const isoHoy = hoy.toISOString().substring(0, 10);
    const isoHaceUnMes = haceUnMes.toISOString().substring(0, 10);

    this.form.patchValue({
      periodoInicio: isoHaceUnMes,
      periodoFin: isoHoy,
    });

    if (idParam) {
      this.esEdicion.set(true);
      this.reporteId.set(Number(idParam));
      this.cargarParaEdicion(Number(idParam));
    } else {
      this.cargarParaCreacion();
    }
  }

  private cargarParaCreacion(): void {
    if (this.esAdmin()) {
      this.empresasApi.listar().subscribe({
        next: (emps) => {
          this.empresas.set(emps);
          if (emps.length > 0) {
            this.empresaSeleccionadaId.set(emps[0].id);
            this.cargarCatalogosEmpresa(emps[0].id);
          }
          this.cargandoInicial.set(false);
        },
        error: () => this.cargandoInicial.set(false),
      });
    } else {
      const empId = this.auth.usuario()?.empresaId;
      if (empId) {
        this.empresaSeleccionadaId.set(empId);
        this.cargarCatalogosEmpresa(empId);
      }
      this.cargandoInicial.set(false);
    }
  }

  private cargarParaEdicion(id: number): void {
    this.reportesApi.obtener(id).subscribe({
      next: (rep: Reporte) => {
        this.form.patchValue({
          productoId: rep.productoId,
          plantaArea: rep.plantaArea,
          frecuencia: rep.frecuencia,
          periodoInicio: rep.periodoInicio,
          periodoFin: rep.periodoFin,
          demanda: rep.demanda,
          produccionReal: rep.produccionReal,
        });

        // Cargar productos y recursos
        const empId = this.auth.usuario()?.empresaId;
        if (empId) {
          this.empresaSeleccionadaId.set(empId);
          this.cargarCatalogosEmpresa(empId);
        } else if (this.esAdmin()) {
          // ADMIN: averiguar empresa por producto
          this.productosApi.obtener(rep.productoId).subscribe({
            next: (prod) => {
              this.empresaSeleccionadaId.set(prod.empresaId);
              this.cargarCatalogosEmpresa(prod.empresaId);
            },
          });
        }

        // Cargar líneas
        this.lineasArray.clear();
        if (rep.lineas && rep.lineas.length > 0) {
          for (const l of rep.lineas) {
            this.agregarLineaConDatos(l);
          }
        }
        this.cargandoInicial.set(false);
      },
      error: (err) => {
        this.notify.error(err.error?.detail || 'No se pudo cargar el reporte');
        this.cargandoInicial.set(false);
      },
    });
  }

  alCambiarEmpresa(val: string): void {
    const id = Number(val);
    if (!isNaN(id)) {
      this.empresaSeleccionadaId.set(id);
      this.form.patchValue({ productoId: null });
      this.lineasArray.clear();
      this.cargarCatalogosEmpresa(id);
    }
  }

  private cargarCatalogosEmpresa(empresaId: number): void {
    this.productosApi.listarPorEmpresa(empresaId).subscribe({
      next: (prods) => {
        this.productos.set(prods);
        if (!this.form.controls.productoId.value && prods.length > 0) {
          this.form.patchValue({ productoId: prods[0].id });
        }
      },
    });

    this.recursosApi.listarPorEmpresa(empresaId).subscribe({
      next: (recs) => {
        this.recursosDisponibles.set(recs);
        if (!this.esEdicion() && this.lineasArray.length === 0 && recs.length > 0) {
          // Agregar la primera línea por defecto
          this.agregarLinea();
        }
      },
    });
  }

  agregarLinea(): void {
    const primerRecurso = this.recursosDisponibles().length > 0 ? this.recursosDisponibles()[0].id : null;
    const linea = this.fb.group({
      recursoId: [primerRecurso, [Validators.required]],
      tiempoProcesamiento: [0.05, [Validators.required, Validators.min(0.0001)]],
      unidadReferencia: ['HORAS/PIEZA', [Validators.required]],
      tiempoCalendario: [160, [Validators.required, Validators.min(0)]],
      tiempoNoDisponible: [0, [Validators.min(0)]],
      numeroTurnos: [2, [Validators.required, Validators.min(1)]],
      horasPorTurno: [8, [Validators.required, Validators.min(0.1), Validators.max(24)]],
      diasOperacion: [20, [Validators.required, Validators.min(1)]],
    });
    this.lineasArray.push(linea);
  }

  private agregarLineaConDatos(l: any): void {
    const linea = this.fb.group({
      recursoId: [l.recursoId, [Validators.required]],
      tiempoProcesamiento: [l.tiempoProcesamiento, [Validators.required, Validators.min(0.0001)]],
      unidadReferencia: [l.unidadReferencia, [Validators.required]],
      tiempoCalendario: [l.tiempoCalendario, [Validators.required, Validators.min(0)]],
      tiempoNoDisponible: [l.tiempoNoDisponible ?? 0, [Validators.min(0)]],
      numeroTurnos: [l.numeroTurnos, [Validators.required, Validators.min(1)]],
      horasPorTurno: [l.horasPorTurno, [Validators.required, Validators.min(0.1), Validators.max(24)]],
      diasOperacion: [l.diasOperacion, [Validators.required, Validators.min(1)]],
    });
    this.lineasArray.push(linea);
  }

  quitarLinea(index: number): void {
    this.lineasArray.removeAt(index);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.lineasArray.length === 0) {
      this.errorFormulario.set('Debes agregar al menos una línea de recurso.');
      return;
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);

    const val = this.form.getRawValue();
    const lineasData = this.lineasArray.getRawValue().map((l: any) => ({
      recursoId: l.recursoId,
      tiempoProcesamiento: Number(l.tiempoProcesamiento),
      unidadReferencia: l.unidadReferencia,
      tiempoCalendario: Number(l.tiempoCalendario),
      tiempoNoDisponible: l.tiempoNoDisponible ? Number(l.tiempoNoDisponible) : 0,
      numeroTurnos: Number(l.numeroTurnos),
      horasPorTurno: Number(l.horasPorTurno),
      diasOperacion: Number(l.diasOperacion),
    }));

    if (this.esEdicion()) {
      const repId = this.reporteId()!;
      this.reportesApi
        .actualizar(repId, {
          plantaArea: val.plantaArea,
          frecuencia: val.frecuencia,
          periodoInicio: val.periodoInicio,
          periodoFin: val.periodoFin,
          demanda: Number(val.demanda),
          produccionReal: Number(val.produccionReal),
          lineas: lineasData,
        })
        .subscribe({
          next: (res) => {
            this.guardando.set(false);
            this.notify.exito('Reporte actualizado correctamente');
            this.router.navigate(['/app/reportes', res.id]);
          },
          error: (err) => {
            this.guardando.set(false);
            this.errorFormulario.set(err.error?.detail || 'Error al actualizar el reporte');
          },
        });
    } else {
      this.reportesApi
        .crear({
          productoId: val.productoId!,
          plantaArea: val.plantaArea,
          frecuencia: val.frecuencia,
          periodoInicio: val.periodoInicio,
          periodoFin: val.periodoFin,
          demanda: Number(val.demanda),
          produccionReal: Number(val.produccionReal),
          lineas: lineasData,
        })
        .subscribe({
          next: (res) => {
            this.guardando.set(false);
            this.notify.exito('Reporte creado exitosamente');
            this.router.navigate(['/app/reportes', res.id]);
          },
          error: (err) => {
            this.guardando.set(false);
            this.errorFormulario.set(err.error?.detail || 'Error al crear el reporte');
          },
        });
    }
  }
}
