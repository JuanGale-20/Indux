import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EmpresasApi, ProcesosApi, ProductosApi, RecursosApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { Empresa, EstadoRecurso, Proceso, ProcesoRecurso, Producto, Recurso } from '../core/models';
import { NotificationService } from '../core/notification.service';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-recursos',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 app-fade-in">
      <!-- Cabecera -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">Recursos y Máquinas</h1>
          <p class="text-xs text-slate-500 mt-1">
            Gestión de puestos de trabajo, estaciones y su vinculación con etapas productivas (SteelForce).
          </p>
        </div>
        @if (puedeEscribir()) {
          <button
            (click)="abrirModalCrear()"
            [disabled]="!empresaSeleccionadaId()"
            class="inline-flex items-center gap-2 rounded-xl bg-emerald-700 px-4 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-emerald-800 transition disabled:opacity-50 shadow-emerald-700/20"
          >
            <app-icon name="plus" [size]="15" />
            <span>Nuevo Recurso</span>
          </button>
        }
      </div>

      <!-- Filtros: Empresa (ADMIN) y Búsqueda -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 bg-white p-3 rounded-2xl border border-slate-200 shadow-sm">
        @if (esAdmin()) {
          <div>
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
        } @else {
          <div class="flex items-center gap-2 text-xs font-semibold text-emerald-800 bg-emerald-50 px-3 py-2 rounded-xl border border-emerald-200">
            <app-icon name="building-2" [size]="16" class="text-emerald-700" />
            <span>Empresa asignada: #{{ auth.usuario()?.empresaId }}</span>
          </div>
        }

        <div class="relative">
          <input
            type="text"
            [value]="busqueda()"
            (input)="busqueda.set($any($event.target).value)"
            placeholder="Buscar por código, nombre o tipo…"
            class="w-full rounded-xl border border-slate-200 pl-9 pr-4 py-2 text-xs focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
          />
          <app-icon name="search" [size]="15" class="absolute left-3 top-2.5 text-slate-400" />
        </div>

        <div class="flex items-center justify-end text-xs text-slate-500 font-medium">
          Total: {{ recursosFiltrados().length }} recurso(s)
        </div>
      </div>

      <!-- Estados de carga / error / vacío -->
      @if (cargando()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p class="mt-3 text-xs text-slate-500">Cargando inventario de recursos…</p>
        </div>
      } @else if (error()) {
        <div class="p-6 text-center bg-rose-50 rounded-2xl border border-rose-200 text-rose-700 text-xs">
          <p class="font-bold">Error</p>
          <p class="mt-1">{{ error() }}</p>
        </div>
      } @else if (!empresaSeleccionadaId()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <p class="text-xs text-slate-500">Selecciona una empresa para gestionar sus recursos.</p>
        </div>
      } @else if (recursosFiltrados().length === 0) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center mx-auto text-slate-400">
            <app-icon name="factory" [size]="24" />
          </div>
          <h3 class="mt-3 text-sm font-bold text-slate-800">No hay recursos configurados</h3>
          <p class="text-xs text-slate-500 mt-1">Registra máquinas o puestos de trabajo para vincular a los procesos.</p>
          @if (puedeEscribir()) {
            <button
              (click)="abrirModalCrear()"
              class="mt-4 inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-700 text-white text-xs font-semibold hover:bg-emerald-800 transition"
            >
              <app-icon name="plus" [size]="14" />
              <span>Crear primer recurso</span>
            </button>
          }
        </div>
      } @else {
        <!-- Tabla de recursos -->
        <div class="overflow-hidden bg-white rounded-2xl border border-slate-200 shadow-sm">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase tracking-wider text-[10px]">
                <th class="py-3.5 px-4 font-semibold">Código</th>
                <th class="py-3.5 px-4 font-semibold">Nombre del Recurso</th>
                <th class="py-3.5 px-4 font-semibold">Tipo</th>
                <th class="py-3.5 px-4 font-semibold">Disponibles</th>
                <th class="py-3.5 px-4 font-semibold">Estado Operativo</th>
                <th class="py-3.5 px-4 font-semibold">Vínculos a Procesos</th>
                <th class="py-3.5 px-4 font-semibold text-right">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              @for (r of recursosFiltrados(); track r.id) {
                <tr class="hover:bg-slate-50/80 transition">
                  <td class="py-3.5 px-4 font-mono font-bold text-emerald-800 bg-emerald-50/30">
                    {{ r.codigo }}
                  </td>
                  <td class="py-3.5 px-4 font-bold text-slate-900">{{ r.nombre }}</td>
                  <td class="py-3.5 px-4 text-slate-600">{{ r.tipo || 'Sin categoría' }}</td>
                  <td class="py-3.5 px-4 font-semibold text-slate-800">{{ r.cantidadDisponible }} unid.</td>
                  <td class="py-3.5 px-4">
                    <span
                      class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold"
                      [class]="r.estado === 'DISPONIBLE' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-rose-50 text-rose-700 border border-rose-200'"
                    >
                      <span class="w-1.5 h-1.5 rounded-full" [class]="r.estado === 'DISPONIBLE' ? 'bg-emerald-500' : 'bg-rose-500'"></span>
                      {{ r.estado === 'DISPONIBLE' ? 'Disponible' : 'No disponible' }}
                    </span>
                  </td>
                  <td class="py-3.5 px-4">
                    <button
                      (click)="abrirModalVinculos(r)"
                      class="inline-flex items-center gap-1.5 text-xs text-emerald-800 hover:text-emerald-950 font-semibold bg-emerald-50 hover:bg-emerald-100 px-2.5 py-1 rounded-lg transition"
                    >
                      <app-icon name="workflow" [size]="13" />
                      <span>Gestionar vínculos</span>
                    </button>
                  </td>
                  <td class="py-3.5 px-4 text-right whitespace-nowrap">
                    <div class="inline-flex items-center gap-2">
                      <a
                        [routerLink]="['/app/historico']"
                        [queryParams]="{ recursoId: r.id }"
                        class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-emerald-700 bg-emerald-50 hover:bg-emerald-100 transition font-medium"
                        title="Ver histórico de utilización y tendencia"
                      >
                        <app-icon name="chart-no-axes-combined" [size]="13" />
                        <span>Tendencia</span>
                      </a>
                      @if (puedeEscribir()) {
                        <button
                          (click)="abrirModalEditar(r)"
                          class="px-2.5 py-1 rounded-lg text-slate-600 bg-slate-100 hover:bg-slate-200 hover:text-slate-900 transition font-medium"
                        >
                          Editar
                        </button>
                        <button
                          (click)="eliminar(r)"
                          class="px-2.5 py-1 rounded-lg text-rose-600 bg-rose-50 hover:bg-rose-100 transition font-medium"
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

      <!-- Modal Crear / Editar Recurso -->
      @if (modalAbierto()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 app-fade-in">
          <div class="w-full max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden app-slide-up">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <h2 class="text-sm font-bold text-slate-900">
                {{ recursoEnEdicion() ? 'Editar Recurso' : 'Registrar Nuevo Recurso' }}
              </h2>
              <button (click)="cerrarModal()" class="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100 transition">
                <app-icon name="x" [size]="18" />
              </button>
            </div>

            <form [formGroup]="form" (ngSubmit)="guardar()" class="p-6 space-y-4">
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="rec-codigo">
                    Código *
                  </label>
                  <input
                    id="rec-codigo"
                    type="text"
                    formControlName="codigo"
                    placeholder="Ej. CNC-01"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 uppercase font-mono focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  />
                  @if (form.controls.codigo.touched && form.controls.codigo.invalid) {
                    <p class="mt-1 text-xs text-rose-600">Requerido.</p>
                  }
                </div>

                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="rec-tipo">
                    Tipo de Recurso
                  </label>
                  <input
                    id="rec-tipo"
                    type="text"
                    formControlName="tipo"
                    placeholder="Ej. Máquina, Estación…"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  />
                </div>
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="rec-nombre">
                  Nombre del Recurso *
                </label>
                <input
                  id="rec-nombre"
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Torno CNC Haas ST-20"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
                @if (form.controls.nombre.touched && form.controls.nombre.invalid) {
                  <p class="mt-1 text-xs text-rose-600">Requerido.</p>
                }
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="rec-cantidad">
                    Cantidad Disponible *
                  </label>
                  <input
                    id="rec-cantidad"
                    type="number"
                    min="1"
                    formControlName="cantidadDisponible"
                    placeholder="1"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  />
                  @if (form.controls.cantidadDisponible.touched && form.controls.cantidadDisponible.invalid) {
                    <p class="mt-1 text-xs text-rose-600">Mínimo 1.</p>
                  }
                </div>

                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="rec-estado">
                    Estado
                  </label>
                  <select
                    id="rec-estado"
                    formControlName="estado"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  >
                    <option value="DISPONIBLE">DISPONIBLE</option>
                    <option value="NO_DISPONIBLE">NO_DISPONIBLE</option>
                  </select>
                </div>
              </div>

              @if (errorFormulario()) {
                <div class="rounded-xl bg-rose-50 border border-rose-200 p-3 text-xs text-rose-700">
                  {{ errorFormulario() }}
                </div>
              }

              <div class="pt-4 flex items-center justify-end gap-3 border-t border-slate-100">
                <button
                  type="button"
                  (click)="cerrarModal()"
                  class="px-4 py-2 rounded-xl text-xs font-medium text-slate-600 hover:bg-slate-100 transition"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  [disabled]="guardando()"
                  class="px-4 py-2 rounded-xl text-xs font-semibold text-white bg-emerald-700 hover:bg-emerald-800 transition disabled:opacity-60 shadow-sm"
                >
                  {{ guardando() ? 'Guardando…' : 'Guardar Recurso' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }

      <!-- Modal Gestionar Vínculos Recurso <-> Procesos -->
      @if (modalVinculosAbierto()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 app-fade-in">
          <div class="w-full max-w-xl bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden app-slide-up">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <div>
                <h2 class="text-sm font-bold text-slate-900">
                  Vínculos del Recurso: {{ recursoSeleccionado()?.nombre }} ({{ recursoSeleccionado()?.codigo }})
                </h2>
                <p class="text-xs text-slate-500">Asocia este recurso a etapas de procesos con su tiempo estándar de referencia.</p>
              </div>
              <button (click)="cerrarModalVinculos()" class="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100 transition">
                <app-icon name="x" [size]="18" />
              </button>
            </div>

            <div class="p-6 space-y-6">
              <!-- Formulario para agregar un nuevo vínculo (ADMIN) -->
              @if (puedeEscribir()) {
                <div class="bg-slate-50 p-4 rounded-xl border border-slate-200 space-y-3">
                  <h3 class="text-xs font-bold text-slate-800 uppercase tracking-wider">Vincular a un Proceso</h3>
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div>
                      <label class="block text-[10px] font-semibold uppercase text-slate-600 mb-1">Proceso / Etapa *</label>
                      <select
                        [value]="nuevoVinculoProcesoId() ?? ''"
                        (change)="alSeleccionarProcesoVinculo($any($event.target).value)"
                        class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                      >
                        <option value="">Selecciona proceso…</option>
                        @for (p of todosLosProcesos(); track p.id) {
                          <option [value]="p.id">{{ p.codigo }} - {{ p.nombre }}</option>
                        }
                      </select>
                    </div>

                    <div>
                      <label class="block text-[10px] font-semibold uppercase text-slate-600 mb-1">Tiempo Referencia (horas)</label>
                      <input
                        type="number"
                        step="0.0001"
                        min="0"
                        [value]="nuevoVinculoTiempo()"
                        (input)="nuevoVinculoTiempo.set(+$any($event.target).value)"
                        placeholder="0.0000"
                        class="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none"
                      />
                    </div>
                  </div>

                  <div class="flex justify-end">
                    <button
                      (click)="vincularProceso()"
                      [disabled]="!nuevoVinculoProcesoId() || vinculando()"
                      class="px-3 py-1.5 rounded-lg text-xs font-semibold text-white bg-emerald-700 hover:bg-emerald-800 transition disabled:opacity-50"
                    >
                      {{ vinculando() ? 'Vinculando…' : 'Agregar Vínculo' }}
                    </button>
                  </div>
                </div>
              }

              <!-- Lista de vínculos actuales -->
              <div>
                <h3 class="text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">Procesos Vinculados Actualmente</h3>
                @if (cargandoVinculos()) {
                  <p class="text-xs text-slate-500 py-4 text-center">Cargando vínculos…</p>
                } @else if (vinculosActuales().length === 0) {
                  <p class="text-xs text-slate-500 py-4 text-center">Este recurso no está vinculado a ningún proceso todavía.</p>
                } @else {
                  <div class="overflow-hidden rounded-xl border border-slate-200">
                    <table class="w-full text-left text-xs border-collapse">
                      <thead class="bg-slate-50 text-[10px] uppercase text-slate-600 border-b border-slate-200">
                        <tr>
                          <th class="py-2.5 px-3">Proceso</th>
                          <th class="py-2.5 px-3">Tiempo Referencia</th>
                          @if (puedeEscribir()) {
                            <th class="py-2.5 px-3 text-right">Acción</th>
                          }
                        </tr>
                      </thead>
                      <tbody class="divide-y divide-slate-100">
                        @for (v of vinculosActuales(); track v.procesoId) {
                          <tr class="hover:bg-slate-50">
                            <td class="py-2.5 px-3 font-medium text-slate-900">
                              {{ obtenerNombreProceso(v.procesoId) }}
                            </td>
                            <td class="py-2.5 px-3 font-mono text-slate-700">
                              {{ v.tiempoReferencia != null ? v.tiempoReferencia + ' hrs' : 'No definido' }}
                            </td>
                            @if (puedeEscribir()) {
                              <td class="py-2.5 px-3 text-right">
                                <button
                                  (click)="desvincularProceso(v.procesoId)"
                                  class="text-rose-600 hover:text-rose-800 font-bold px-2 py-0.5 rounded"
                                >
                                  Quitar
                                </button>
                              </td>
                            }
                          </tr>
                        }
                      </tbody>
                    </table>
                  </div>
                }
              </div>
            </div>

            <div class="px-6 py-3 bg-slate-50 border-t border-slate-100 flex justify-end">
              <button
                (click)="cerrarModalVinculos()"
                class="px-4 py-2 rounded-xl text-xs font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-100 transition"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
})
export class Recursos implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly recursosApi = inject(RecursosApi);
  private readonly empresasApi = inject(EmpresasApi);
  private readonly productosApi = inject(ProductosApi);
  private readonly procesosApi = inject(ProcesosApi);
  private readonly notify = inject(NotificationService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly empresas = signal<Empresa[]>([]);
  protected readonly empresaSeleccionadaId = signal<number | null>(null);
  protected readonly recursos = signal<Recurso[]>([]);
  protected readonly busqueda = signal('');
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly modalAbierto = signal(false);
  protected readonly recursoEnEdicion = signal<Recurso | null>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  // Vínculos
  protected readonly modalVinculosAbierto = signal(false);
  protected readonly recursoSeleccionado = signal<Recurso | null>(null);
  protected readonly vinculosActuales = signal<ProcesoRecurso[]>([]);
  protected readonly todosLosProcesos = signal<Proceso[]>([]);
  protected readonly cargandoVinculos = signal(false);
  protected readonly vinculando = signal(false);
  protected readonly nuevoVinculoProcesoId = signal<number | null>(null);
  protected readonly nuevoVinculoTiempo = signal<number>(0);

  protected readonly form = this.fb.group({
    codigo: ['', [Validators.required]],
    nombre: ['', [Validators.required]],
    tipo: [''],
    cantidadDisponible: [1, [Validators.required, Validators.min(1)]],
    estado: ['DISPONIBLE' as EstadoRecurso],
  });

  esAdmin(): boolean {
    return this.auth.usuario()?.rol === 'ADMIN';
  }

  puedeEscribir(): boolean {
    return this.esAdmin();
  }

  ngOnInit(): void {
    if (this.esAdmin()) {
      this.empresasApi.listar().subscribe({
        next: (data) => {
          this.empresas.set(data);
          if (data.length > 0) {
            this.empresaSeleccionadaId.set(data[0].id);
            this.cargarRecursos();
            this.cargarProcesosDeEmpresa(data[0].id);
          }
        },
        error: () => {},
      });
    } else {
      const empId = this.auth.usuario()?.empresaId;
      if (empId) {
        this.empresaSeleccionadaId.set(empId);
        this.cargarRecursos();
        this.cargarProcesosDeEmpresa(empId);
      }
    }
  }

  alCambiarEmpresa(val: string): void {
    const id = Number(val);
    if (!isNaN(id)) {
      this.empresaSeleccionadaId.set(id);
      this.cargarRecursos();
      this.cargarProcesosDeEmpresa(id);
    }
  }

  alSeleccionarProcesoVinculo(val: string): void {
    this.nuevoVinculoProcesoId.set(val ? Number(val) : null);
  }

  cargarRecursos(): void {
    const empId = this.empresaSeleccionadaId();
    if (!empId) return;

    this.cargando.set(true);
    this.error.set(null);
    this.recursosApi.listarPorEmpresa(empId).subscribe({
      next: (data) => {
        this.recursos.set(data);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(e.error?.detail || 'No se pudieron cargar los recursos');
      },
    });
  }

  cargarProcesosDeEmpresa(empresaId: number): void {
    this.productosApi.listarPorEmpresa(empresaId).subscribe({
      next: (prods) => {
        const acumulado: Proceso[] = [];
        let pendientes = prods.length;
        if (pendientes === 0) {
          this.todosLosProcesos.set([]);
          return;
        }
        for (const prod of prods) {
          this.procesosApi.listarPorProducto(prod.id).subscribe({
            next: (procs) => {
              acumulado.push(...procs);
              pendientes--;
              if (pendientes === 0) {
                this.todosLosProcesos.set(acumulado);
              }
            },
            error: () => {
              pendientes--;
              if (pendientes === 0) {
                this.todosLosProcesos.set(acumulado);
              }
            },
          });
        }
      },
      error: () => {},
    });
  }

  recursosFiltrados(): Recurso[] {
    const q = this.busqueda().toLowerCase().trim();
    if (!q) return this.recursos();
    return this.recursos().filter(
      (r) =>
        r.codigo.toLowerCase().includes(q) ||
        r.nombre.toLowerCase().includes(q) ||
        (r.tipo && r.tipo.toLowerCase().includes(q)),
    );
  }

  abrirModalCrear(): void {
    this.recursoEnEdicion.set(null);
    this.form.reset({
      codigo: '',
      nombre: '',
      tipo: '',
      cantidadDisponible: 1,
      estado: 'DISPONIBLE',
    });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  abrirModalEditar(r: Recurso): void {
    this.recursoEnEdicion.set(r);
    this.form.patchValue({
      codigo: r.codigo,
      nombre: r.nombre,
      tipo: r.tipo || '',
      cantidadDisponible: r.cantidadDisponible,
      estado: r.estado,
    });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.recursoEnEdicion.set(null);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const empId = this.empresaSeleccionadaId();
    if (!empId) {
      this.errorFormulario.set('Empresa no seleccionada');
      return;
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);
    const v = this.form.getRawValue();
    const edicion = this.recursoEnEdicion();

    if (edicion) {
      this.recursosApi
        .actualizar(edicion.id, {
          codigo: v.codigo,
          nombre: v.nombre,
          tipo: v.tipo || null,
          cantidadDisponible: v.cantidadDisponible,
          estado: v.estado,
        })
        .subscribe({
          next: () => {
            this.guardando.set(false);
            this.cerrarModal();
            this.notify.exito('Recurso actualizado correctamente');
            this.cargarRecursos();
          },
          error: (err) => {
            this.guardando.set(false);
            this.errorFormulario.set(err.error?.detail || 'Error al actualizar el recurso');
          },
        });
    } else {
      this.recursosApi
        .crear({
          empresaId: empId,
          codigo: v.codigo,
          nombre: v.nombre,
          tipo: v.tipo || null,
          cantidadDisponible: v.cantidadDisponible,
          estado: v.estado,
        })
        .subscribe({
          next: () => {
            this.guardando.set(false);
            this.cerrarModal();
            this.notify.exito('Recurso creado correctamente');
            this.cargarRecursos();
          },
          error: (err) => {
            this.guardando.set(false);
            this.errorFormulario.set(err.error?.detail || 'Error al crear el recurso');
          },
        });
    }
  }

  eliminar(r: Recurso): void {
    if (!confirm(`¿Eliminar recurso "${r.nombre}" (${r.codigo})?`)) return;
    this.recursosApi.eliminar(r.id).subscribe({
      next: () => {
        this.notify.info(`Recurso "${r.nombre}" eliminado.`);
        this.cargarRecursos();
      },
      error: (err) => {
        this.notify.error(err.error?.detail || 'No se pudo eliminar el recurso.');
      },
    });
  }

  // Gestión de Vínculos
  abrirModalVinculos(r: Recurso): void {
    this.recursoSeleccionado.set(r);
    this.modalVinculosAbierto.set(true);
    this.nuevoVinculoProcesoId.set(null);
    this.nuevoVinculoTiempo.set(0);
    this.cargarVinculos(r.id);
  }

  cerrarModalVinculos(): void {
    this.modalVinculosAbierto.set(false);
    this.recursoSeleccionado.set(null);
  }

  cargarVinculos(recursoId: number): void {
    this.cargandoVinculos.set(true);
    this.recursosApi.listarVinculos(recursoId).subscribe({
      next: (v) => {
        this.vinculosActuales.set(v);
        this.cargandoVinculos.set(false);
      },
      error: () => {
        this.vinculosActuales.set([]);
        this.cargandoVinculos.set(false);
      },
    });
  }

  obtenerNombreProceso(procesoId: number): string {
    const p = this.todosLosProcesos().find((proc) => proc.id === procesoId);
    return p ? `${p.codigo} - ${p.nombre}` : `Proceso #${procesoId}`;
  }

  vincularProceso(): void {
    const rec = this.recursoSeleccionado();
    const procId = this.nuevoVinculoProcesoId();
    if (!rec || !procId) return;

    this.vinculando.set(true);
    const tiempo = this.nuevoVinculoTiempo() > 0 ? this.nuevoVinculoTiempo() : null;

    this.recursosApi.vincular(rec.id, { procesoId: procId, tiempoReferencia: tiempo }).subscribe({
      next: () => {
        this.vinculando.set(false);
        this.notify.exito('Proceso vinculado correctamente al recurso');
        this.nuevoVinculoProcesoId.set(null);
        this.nuevoVinculoTiempo.set(0);
        this.cargarVinculos(rec.id);
      },
      error: (err) => {
        this.vinculando.set(false);
        this.notify.error(err.error?.detail || 'No se pudo vincular el proceso');
      },
    });
  }

  desvincularProceso(procesoId: number): void {
    const rec = this.recursoSeleccionado();
    if (!rec) return;
    if (!confirm('¿Deseas desvincular este proceso del recurso?')) return;

    this.recursosApi.desvincular(rec.id, procesoId).subscribe({
      next: () => {
        this.notify.info('Proceso desvinculado.');
        this.cargarVinculos(rec.id);
      },
      error: (err) => {
        this.notify.error(err.error?.detail || 'Error al desvincular');
      },
    });
  }
}
