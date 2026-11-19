import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EmpresasApi, ProcesosApi, ProductosApi, UsuariosApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { Empresa, Proceso, Producto, Recurso, Usuario } from '../core/models';
import { NotificationService } from '../core/notification.service';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-procesos',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 app-fade-in">
      <!-- Cabecera -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">Etapas del Proceso Productivo</h1>
          <p class="text-xs text-slate-500 mt-1">
            Secuencia operacional, factores de merma y unidades de entrada/salida según la metodología SteelForce.
          </p>
        </div>
        @if (puedeEscribir()) {
          <button
            (click)="abrirModalCrear()"
            [disabled]="!productoSeleccionadoId()"
            class="inline-flex items-center gap-2 rounded-xl bg-emerald-700 px-4 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-emerald-800 transition disabled:opacity-50 shadow-emerald-700/20"
          >
            <app-icon name="plus" [size]="15" />
            <span>Nueva Etapa / Proceso</span>
          </button>
        }
      </div>

      <!-- Selectores de contexto: Empresa y Producto -->
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
          <label class="block text-[10px] font-bold uppercase tracking-wider text-slate-500 mb-1">Producto</label>
          <select
            [value]="productoSeleccionadoId() ?? ''"
            (change)="alCambiarProducto($any($event.target).value)"
            class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs text-slate-700 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
          >
            <option value="" disabled>Selecciona un producto…</option>
            @for (prod of productos(); track prod.id) {
              <option [value]="prod.id">{{ prod.codigo }} - {{ prod.nombre }}</option>
            }
          </select>
        </div>

        <div class="flex items-center justify-end text-xs text-slate-500 font-medium">
          Total etapas: {{ procesos().length }}
        </div>
      </div>

      <!-- Estados de carga / error / vacío -->
      @if (cargando()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p class="mt-3 text-xs text-slate-500">Cargando etapas del proceso…</p>
        </div>
      } @else if (error()) {
        <div class="p-6 text-center bg-rose-50 rounded-2xl border border-rose-200 text-rose-700 text-xs">
          <p class="font-bold">Error</p>
          <p class="mt-1">{{ error() }}</p>
        </div>
      } @else if (!productoSeleccionadoId()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <p class="text-xs text-slate-500">Selecciona un producto para visualizar sus procesos.</p>
        </div>
      } @else if (procesos().length === 0) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center mx-auto text-slate-400">
            <app-icon name="workflow" [size]="24" />
          </div>
          <h3 class="mt-3 text-sm font-bold text-slate-800">No hay procesos definidos</h3>
          <p class="text-xs text-slate-500 mt-1">Este producto aún no tiene etapas de fabricación configuradas.</p>
          @if (puedeEscribir()) {
            <button
              (click)="abrirModalCrear()"
              class="mt-4 inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-700 text-white text-xs font-semibold hover:bg-emerald-800 transition"
            >
              <app-icon name="plus" [size]="14" />
              <span>Crear primera etapa</span>
            </button>
          }
        </div>
      } @else {
        <!-- Lista ordenada de procesos -->
        <div class="space-y-4">
          @for (p of procesos(); track p.id) {
            <div
              class="bg-white rounded-2xl border p-5 shadow-sm transition hover:shadow-md"
              [class]="esMiProcesoLider(p) ? 'border-emerald-500 ring-1 ring-emerald-500/30 bg-emerald-50/10' : 'border-slate-200'"
            >
              <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div class="flex items-start gap-4">
                  <!-- Secuencia Badge -->
                  <div class="w-10 h-10 rounded-xl bg-slate-100 border border-slate-200 text-slate-700 font-extrabold flex items-center justify-center text-sm shrink-0">
                    #{{ p.secuencia }}
                  </div>
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="font-mono text-xs font-bold text-emerald-800 bg-emerald-50 px-2 py-0.5 rounded">
                        {{ p.codigo }}
                      </span>
                      <h3 class="text-sm font-bold text-slate-900">{{ p.nombre }}</h3>
                      @if (esMiProcesoLider(p)) {
                        <span class="text-[10px] bg-emerald-100 text-emerald-800 font-bold px-2 py-0.5 rounded-full">
                          Liderado por ti
                        </span>
                      }
                    </div>

                    <div class="mt-2 flex flex-wrap items-center gap-4 text-xs text-slate-500">
                      <span>
                        Unidad procesada: <strong class="text-slate-800">{{ p.unidadProcesada }}</strong>
                      </span>
                      <span>
                        Unidad entrada: <strong class="text-slate-800">{{ p.unidadEntrada || 'Misma' }}</strong>
                      </span>
                      <span>
                        Merma: <strong class="text-slate-800">{{ p.merma ?? 0 }} %</strong>
                      </span>
                      <span>
                        Líder: <strong class="text-slate-800">{{ obtenerNombreLider(p.liderId) }}</strong>
                      </span>
                    </div>
                  </div>
                </div>

                <div class="flex items-center gap-2 shrink-0 self-end md:self-center">
                  <button
                    (click)="verRecursos(p)"
                    class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold text-emerald-800 bg-emerald-50 hover:bg-emerald-100 transition"
                  >
                    <app-icon name="factory" [size]="14" />
                    <span>Recursos vinculados</span>
                  </button>
                  @if (puedeEscribir()) {
                    <button
                      (click)="abrirModalEditar(p)"
                      class="px-2.5 py-1.5 rounded-xl text-xs text-slate-600 bg-slate-100 hover:bg-slate-200 hover:text-slate-900 transition font-medium"
                    >
                      Editar
                    </button>
                    <button
                      (click)="eliminar(p)"
                      class="px-2.5 py-1.5 rounded-xl text-xs text-rose-600 bg-rose-50 hover:bg-rose-100 transition font-medium"
                    >
                      Eliminar
                    </button>
                  }
                </div>
              </div>
            </div>
          }
        </div>
      }

      <!-- Modal Ver Recursos Vinculados al Proceso -->
      @if (modalRecursosAbierto()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 app-fade-in">
          <div class="w-full max-w-lg bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden app-slide-up">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <div>
                <h2 class="text-sm font-bold text-slate-900">
                  Recursos en: {{ procesoSeleccionadoRecursos()?.nombre }}
                </h2>
                <p class="text-xs text-slate-500">Máquinas, estaciones o líneas que aportan capacidad a este proceso.</p>
              </div>
              <button (click)="cerrarModalRecursos()" class="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100 transition">
                <app-icon name="x" [size]="18" />
              </button>
            </div>

            <div class="p-6">
              @if (cargandoRecursos()) {
                <div class="py-8 text-center text-xs text-slate-500">Cargando recursos vinculados…</div>
              } @else if (recursosVinculados().length === 0) {
                <div class="py-8 text-center text-xs text-slate-500">
                  <p>No hay recursos vinculados a este proceso todavía.</p>
                  @if (puedeEscribir()) {
                    <p class="mt-2 text-emerald-700">
                      Puedes vincular recursos desde la sección
                      <a routerLink="/app/recursos" class="font-bold underline">Recursos y Vínculos</a>.
                    </p>
                  }
                </div>
              } @else {
                <ul class="divide-y divide-slate-100 text-xs">
                  @for (r of recursosVinculados(); track r.id) {
                    <li class="py-3 flex items-center justify-between">
                      <div>
                        <span class="font-mono font-bold text-emerald-800 bg-emerald-50 px-1.5 py-0.5 rounded text-[11px] mr-2">
                          {{ r.codigo }}
                        </span>
                        <span class="font-bold text-slate-900">{{ r.nombre }}</span>
                        <span class="text-slate-500 ml-2">({{ r.tipo || 'Recurso' }})</span>
                      </div>
                      <span class="text-slate-600 font-medium">Cant: {{ r.cantidadDisponible }}</span>
                    </li>
                  }
                </ul>
              }
            </div>

            <div class="px-6 py-3 bg-slate-50 border-t border-slate-100 flex justify-end">
              <button
                (click)="cerrarModalRecursos()"
                class="px-4 py-2 rounded-xl text-xs font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-100 transition"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      }

      <!-- Modal Crear / Editar Proceso -->
      @if (modalAbierto()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 app-fade-in">
          <div class="w-full max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden app-slide-up">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <h2 class="text-sm font-bold text-slate-900">
                {{ procesoEnEdicion() ? 'Editar Etapa de Proceso' : 'Crear Nueva Etapa de Proceso' }}
              </h2>
              <button (click)="cerrarModal()" class="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100 transition">
                <app-icon name="x" [size]="18" />
              </button>
            </div>

            <form [formGroup]="form" (ngSubmit)="guardar()" class="p-6 space-y-4">
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="proc-secuencia">
                    Secuencia *
                  </label>
                  <input
                    id="proc-secuencia"
                    type="number"
                    min="1"
                    formControlName="secuencia"
                    placeholder="1, 2, 3…"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  />
                  @if (form.controls.secuencia.touched && form.controls.secuencia.invalid) {
                    <p class="mt-1 text-xs text-rose-600">Secuencia positiva obligatoria.</p>
                  }
                </div>

                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="proc-codigo">
                    Código *
                  </label>
                  <input
                    id="proc-codigo"
                    type="text"
                    formControlName="codigo"
                    placeholder="Ej. ET-CORTE"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 uppercase font-mono focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  />
                  @if (form.controls.codigo.touched && form.controls.codigo.invalid) {
                    <p class="mt-1 text-xs text-rose-600">Código obligatorio.</p>
                  }
                </div>
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="proc-nombre">
                  Nombre del Proceso *
                </label>
                <input
                  id="proc-nombre"
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Corte y Troquelado"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
                @if (form.controls.nombre.touched && form.controls.nombre.invalid) {
                  <p class="mt-1 text-xs text-rose-600">El nombre es obligatorio.</p>
                }
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="proc-unidad-proc">
                    Unidad Procesada *
                  </label>
                  <input
                    id="proc-unidad-proc"
                    type="text"
                    formControlName="unidadProcesada"
                    placeholder="Ej. PIEZA"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  />
                  @if (form.controls.unidadProcesada.touched && form.controls.unidadProcesada.invalid) {
                    <p class="mt-1 text-xs text-rose-600">Requerida.</p>
                  }
                </div>

                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="proc-unidad-ent">
                    Unidad Entrada
                  </label>
                  <input
                    id="proc-unidad-ent"
                    type="text"
                    formControlName="unidadEntrada"
                    placeholder="Ej. LAMINA (opcional)"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  />
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="proc-merma">
                    Merma (%) (0 - 99.99)
                  </label>
                  <input
                    id="proc-merma"
                    type="number"
                    step="0.01"
                    min="0"
                    max="99.99"
                    formControlName="merma"
                    placeholder="0.00"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  />
                </div>

                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="proc-lider">
                    Líder Asignado
                  </label>
                  <select
                    id="proc-lider"
                    formControlName="liderId"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  >
                    <option [ngValue]="null">Sin líder asignado</option>
                    @for (l of lideres(); track l.id) {
                      <option [ngValue]="l.id">{{ l.nombre }}</option>
                    }
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
                  {{ guardando() ? 'Guardando…' : 'Guardar Proceso' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
})
export class Procesos implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly ruta = inject(ActivatedRoute);
  private readonly procesosApi = inject(ProcesosApi);
  private readonly productosApi = inject(ProductosApi);
  private readonly empresasApi = inject(EmpresasApi);
  private readonly usuariosApi = inject(UsuariosApi);
  private readonly notify = inject(NotificationService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly empresas = signal<Empresa[]>([]);
  protected readonly empresaSeleccionadaId = signal<number | null>(null);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly productoSeleccionadoId = signal<number | null>(null);
  protected readonly procesos = signal<Proceso[]>([]);
  protected readonly lideres = signal<Usuario[]>([]);

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly modalAbierto = signal(false);
  protected readonly procesoEnEdicion = signal<Proceso | null>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  protected readonly modalRecursosAbierto = signal(false);
  protected readonly procesoSeleccionadoRecursos = signal<Proceso | null>(null);
  protected readonly recursosVinculados = signal<Recurso[]>([]);
  protected readonly cargandoRecursos = signal(false);

  protected readonly form = this.fb.group({
    codigo: ['', [Validators.required]],
    nombre: ['', [Validators.required]],
    secuencia: [1, [Validators.required, Validators.min(1)]],
    unidadProcesada: ['', [Validators.required]],
    merma: [0],
    unidadEntrada: [''],
    liderId: [null as number | null],
  });

  esAdmin(): boolean {
    return this.auth.usuario()?.rol === 'ADMIN';
  }

  puedeEscribir(): boolean {
    return this.esAdmin();
  }

  esMiProcesoLider(p: Proceso): boolean {
    return this.auth.usuario()?.rol === 'LIDER' && p.liderId === this.auth.usuario()?.id;
  }

  ngOnInit(): void {
    const qProductoId = this.ruta.snapshot.queryParamMap.get('productoId');

    if (this.esAdmin()) {
      this.empresasApi.listar().subscribe({
        next: (data) => {
          this.empresas.set(data);
          if (data.length > 0) {
            this.empresaSeleccionadaId.set(data[0].id);
            this.cargarProductosYProcesos(qProductoId ? Number(qProductoId) : null);
          }
        },
        error: () => {},
      });
      this.cargarLideres();
    } else {
      const empId = this.auth.usuario()?.empresaId;
      if (empId) {
        this.empresaSeleccionadaId.set(empId);
        this.cargarProductosYProcesos(qProductoId ? Number(qProductoId) : null);
      }
      this.cargarLideres();
    }
  }

  cargarLideres(): void {
    if (this.esAdmin()) {
      this.usuariosApi.listar().subscribe({
        next: (users) => this.lideres.set(users.filter((u) => u.rol === 'LIDER')),
        error: () => {},
      });
    }
  }

  alCambiarEmpresa(val: string): void {
    const id = Number(val);
    if (!isNaN(id)) {
      this.empresaSeleccionadaId.set(id);
      this.cargarProductosYProcesos(null);
    }
  }

  cargarProductosYProcesos(preseleccionProductoId: number | null): void {
    const empId = this.empresaSeleccionadaId();
    if (!empId) return;

    this.productosApi.listarPorEmpresa(empId).subscribe({
      next: (prods) => {
        this.productos.set(prods);
        if (preseleccionProductoId && prods.some((p) => p.id === preseleccionProductoId)) {
          this.productoSeleccionadoId.set(preseleccionProductoId);
        } else if (prods.length > 0) {
          this.productoSeleccionadoId.set(prods[0].id);
        } else {
          this.productoSeleccionadoId.set(null);
          this.procesos.set([]);
          return;
        }
        this.cargarProcesos();
      },
      error: () => {},
    });
  }

  alCambiarProducto(val: string): void {
    const id = Number(val);
    if (!isNaN(id)) {
      this.productoSeleccionadoId.set(id);
      this.cargarProcesos();
    }
  }

  cargarProcesos(): void {
    const prodId = this.productoSeleccionadoId();
    if (!prodId) return;

    this.cargando.set(true);
    this.error.set(null);
    this.procesosApi.listarPorProducto(prodId).subscribe({
      next: (data) => {
        // Ordenar por secuencia
        this.procesos.set([...data].sort((a, b) => a.secuencia - b.secuencia));
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(e.error?.detail || 'No se pudieron cargar los procesos');
      },
    });
  }

  obtenerNombreLider(liderId: number | null): string {
    if (!liderId) return 'Sin asignar';
    const l = this.lideres().find((u) => u.id === liderId);
    return l ? l.nombre : `Líder #${liderId}`;
  }

  verRecursos(p: Proceso): void {
    this.procesoSeleccionadoRecursos.set(p);
    this.modalRecursosAbierto.set(true);
    this.cargandoRecursos.set(true);
    this.procesosApi.listarRecursos(p.id).subscribe({
      next: (recs) => {
        this.recursosVinculados.set(recs);
        this.cargandoRecursos.set(false);
      },
      error: () => {
        this.recursosVinculados.set([]);
        this.cargandoRecursos.set(false);
      },
    });
  }

  cerrarModalRecursos(): void {
    this.modalRecursosAbierto.set(false);
    this.procesoSeleccionadoRecursos.set(null);
  }

  abrirModalCrear(): void {
    this.procesoEnEdicion.set(null);
    const siguienteSecuencia = this.procesos().length + 1;
    this.form.reset({
      codigo: '',
      nombre: '',
      secuencia: siguienteSecuencia,
      unidadProcesada: '',
      merma: 0,
      unidadEntrada: '',
      liderId: null,
    });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  abrirModalEditar(p: Proceso): void {
    this.procesoEnEdicion.set(p);
    this.form.patchValue({
      codigo: p.codigo,
      nombre: p.nombre,
      secuencia: p.secuencia,
      unidadProcesada: p.unidadProcesada,
      merma: p.merma ?? 0,
      unidadEntrada: p.unidadEntrada || '',
      liderId: p.liderId,
    });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.procesoEnEdicion.set(null);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const prodId = this.productoSeleccionadoId();
    if (!prodId) {
      this.errorFormulario.set('Producto no seleccionado');
      return;
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);
    const v = this.form.getRawValue();
    const edicion = this.procesoEnEdicion();

    if (edicion) {
      this.procesosApi
        .actualizar(edicion.id, {
          codigo: v.codigo,
          nombre: v.nombre,
          secuencia: v.secuencia,
          unidadProcesada: v.unidadProcesada,
          merma: v.merma != null ? Number(v.merma) : null,
          unidadEntrada: v.unidadEntrada || null,
          liderId: v.liderId,
        })
        .subscribe({
          next: () => {
            this.guardando.set(false);
            this.cerrarModal();
            this.notify.exito('Proceso actualizado correctamente');
            this.cargarProcesos();
          },
          error: (err) => {
            this.guardando.set(false);
            this.errorFormulario.set(err.error?.detail || 'Error al actualizar el proceso');
          },
        });
    } else {
      this.procesosApi
        .crear({
          productoId: prodId,
          codigo: v.codigo,
          nombre: v.nombre,
          secuencia: v.secuencia,
          unidadProcesada: v.unidadProcesada,
          merma: v.merma != null ? Number(v.merma) : null,
          unidadEntrada: v.unidadEntrada || null,
          liderId: v.liderId,
        })
        .subscribe({
          next: () => {
            this.guardando.set(false);
            this.cerrarModal();
            this.notify.exito('Proceso creado correctamente');
            this.cargarProcesos();
          },
          error: (err) => {
            this.guardando.set(false);
            this.errorFormulario.set(err.error?.detail || 'Error al crear el proceso');
          },
        });
    }
  }

  eliminar(p: Proceso): void {
    if (!confirm(`¿Eliminar proceso "${p.nombre}" (${p.codigo})?`)) return;
    this.procesosApi.eliminar(p.id).subscribe({
      next: () => {
        this.notify.info(`Proceso "${p.nombre}" eliminado.`);
        this.cargarProcesos();
      },
      error: (err) => {
        this.notify.error(err.error?.detail || 'No se pudo eliminar el proceso.');
      },
    });
  }
}
