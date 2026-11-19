import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EmpresasApi, ProductosApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { Empresa, Producto } from '../core/models';
import { NotificationService } from '../core/notification.service';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-productos',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 app-fade-in">
      <!-- Cabecera -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">Catálogo de Productos</h1>
          <p class="text-xs text-slate-500 mt-1">
            Gestión de artículos y referencias con sus unidades de medida para análisis de capacidad.
          </p>
        </div>
        @if (puedeEscribir()) {
          <button
            (click)="abrirModalCrear()"
            [disabled]="!empresaSeleccionadaId()"
            class="inline-flex items-center gap-2 rounded-xl bg-emerald-700 px-4 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-emerald-800 transition disabled:opacity-50 shadow-emerald-700/20"
          >
            <app-icon name="plus" [size]="15" />
            <span>Nuevo Producto</span>
          </button>
        }
      </div>

      <!-- Selector de Empresa (ADMIN) y Barra de búsqueda -->
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
                <option [value]="emp.id">{{ emp.nombre }} (ID: {{ emp.id }})</option>
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
            placeholder="Buscar por código o nombre…"
            class="w-full rounded-xl border border-slate-200 pl-9 pr-4 py-2 text-xs focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
          />
          <app-icon name="search" [size]="15" class="absolute left-3 top-2.5 text-slate-400" />
        </div>

        <div class="flex items-center justify-end text-xs text-slate-500 font-medium">
          Total: {{ productosFiltrados().length }} producto(s)
        </div>
      </div>

      <!-- Estados de carga / error / vacío -->
      @if (cargando()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p class="mt-3 text-xs text-slate-500">Cargando catálogo de productos…</p>
        </div>
      } @else if (error()) {
        <div class="p-6 text-center bg-rose-50 rounded-2xl border border-rose-200 text-rose-700 text-xs">
          <p class="font-bold">Error al cargar productos</p>
          <p class="mt-1">{{ error() }}</p>
          <button
            (click)="cargarProductos()"
            class="mt-3 px-3 py-1.5 rounded-lg bg-rose-600 text-white font-medium hover:bg-rose-700 transition"
          >
            Reintentar
          </button>
        </div>
      } @else if (!empresaSeleccionadaId()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <p class="text-xs text-slate-500">Selecciona una empresa para visualizar sus productos.</p>
        </div>
      } @else if (productosFiltrados().length === 0) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center mx-auto text-slate-400">
            <app-icon name="package" [size]="24" />
          </div>
          <h3 class="mt-3 text-sm font-bold text-slate-800">No hay productos registrados</h3>
          <p class="text-xs text-slate-500 mt-1">
            {{ busqueda() ? 'No hay resultados que coincidan con la búsqueda.' : 'Esta empresa aún no cuenta con productos en su catálogo.' }}
          </p>
          @if (puedeEscribir() && !busqueda()) {
            <button
              (click)="abrirModalCrear()"
              class="mt-4 inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-700 text-white text-xs font-semibold hover:bg-emerald-800 transition"
            >
              <app-icon name="plus" [size]="14" />
              <span>Crear primer producto</span>
            </button>
          }
        </div>
      } @else {
        <!-- Tabla -->
        <div class="overflow-hidden bg-white rounded-2xl border border-slate-200 shadow-sm">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase tracking-wider text-[10px]">
                <th class="py-3.5 px-4 font-semibold">Código</th>
                <th class="py-3.5 px-4 font-semibold">Nombre del Producto</th>
                <th class="py-3.5 px-4 font-semibold">Unidad de Medida</th>
                <th class="py-3.5 px-4 font-semibold">Procesos Productivos</th>
                <th class="py-3.5 px-4 font-semibold text-right">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              @for (p of productosFiltrados(); track p.id) {
                <tr class="hover:bg-slate-50/80 transition">
                  <td class="py-3.5 px-4 font-mono font-bold text-emerald-800 bg-emerald-50/30">
                    {{ p.codigo }}
                  </td>
                  <td class="py-3.5 px-4 font-bold text-slate-900">{{ p.nombre }}</td>
                  <td class="py-3.5 px-4 text-slate-600 font-mono">{{ p.unidadMedida }}</td>
                  <td class="py-3.5 px-4">
                    <a
                      [routerLink]="['/app/procesos']"
                      [queryParams]="{ productoId: p.id }"
                      class="inline-flex items-center gap-1.5 text-xs text-emerald-700 hover:text-emerald-900 font-semibold underline"
                    >
                      <app-icon name="workflow" [size]="13" />
                      <span>Ver etapas y secuencia</span>
                    </a>
                  </td>
                  <td class="py-3.5 px-4 text-right whitespace-nowrap">
                    @if (puedeEscribir()) {
                      <div class="inline-flex items-center gap-2">
                        <button
                          (click)="abrirModalEditar(p)"
                          class="px-2.5 py-1 rounded-lg text-slate-600 bg-slate-100 hover:bg-slate-200 hover:text-slate-900 transition font-medium"
                        >
                          Editar
                        </button>
                        <button
                          (click)="eliminar(p)"
                          class="px-2.5 py-1 rounded-lg text-rose-600 bg-rose-50 hover:bg-rose-100 transition font-medium"
                        >
                          Eliminar
                        </button>
                      </div>
                    } @else {
                      <span class="text-slate-400 italic text-[11px]">Solo lectura</span>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      <!-- Modal Crear / Editar -->
      @if (modalAbierto()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 app-fade-in">
          <div class="w-full max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden app-slide-up">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <h2 class="text-sm font-bold text-slate-900">
                {{ productoEnEdicion() ? 'Editar Producto' : 'Crear Nuevo Producto' }}
              </h2>
              <button (click)="cerrarModal()" class="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100 transition">
                <app-icon name="x" [size]="18" />
              </button>
            </div>

            <form [formGroup]="form" (ngSubmit)="guardar()" class="p-6 space-y-4">
              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="prod-codigo">
                  Código de Producto *
                </label>
                <input
                  id="prod-codigo"
                  type="text"
                  formControlName="codigo"
                  placeholder="Ej. PROD-001"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 uppercase font-mono focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
                @if (form.controls.codigo.touched && form.controls.codigo.invalid) {
                  <p class="mt-1 text-xs text-rose-600">El código es obligatorio.</p>
                }
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="prod-nombre">
                  Nombre del Producto *
                </label>
                <input
                  id="prod-nombre"
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Tubo Estructural 2x2 pulg"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
                @if (form.controls.nombre.touched && form.controls.nombre.invalid) {
                  <p class="mt-1 text-xs text-rose-600">El nombre es obligatorio.</p>
                }
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="prod-unidad">
                  Unidad de Medida *
                </label>
                <input
                  id="prod-unidad"
                  type="text"
                  formControlName="unidadMedida"
                  placeholder="Ej. UNIDAD, KG, METRO, LOTE…"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
                @if (form.controls.unidadMedida.touched && form.controls.unidadMedida.invalid) {
                  <p class="mt-1 text-xs text-rose-600">La unidad de medida es obligatoria.</p>
                }
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
                  {{ guardando() ? 'Guardando…' : 'Guardar Producto' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
})
export class Productos implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly productosApi = inject(ProductosApi);
  private readonly empresasApi = inject(EmpresasApi);
  private readonly notify = inject(NotificationService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly empresas = signal<Empresa[]>([]);
  protected readonly empresaSeleccionadaId = signal<number | null>(null);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly busqueda = signal('');
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly modalAbierto = signal(false);
  protected readonly productoEnEdicion = signal<Producto | null>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  protected readonly form = this.fb.group({
    codigo: ['', [Validators.required]],
    nombre: ['', [Validators.required]],
    unidadMedida: ['', [Validators.required]],
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
            this.cargarProductos();
          }
        },
        error: (e) => this.error.set(e.error?.detail || 'Error al cargar empresas'),
      });
    } else {
      const empId = this.auth.usuario()?.empresaId;
      if (empId) {
        this.empresaSeleccionadaId.set(empId);
        this.cargarProductos();
      }
    }
  }

  alCambiarEmpresa(val: string): void {
    const id = Number(val);
    if (!isNaN(id)) {
      this.empresaSeleccionadaId.set(id);
      this.cargarProductos();
    }
  }

  cargarProductos(): void {
    const empId = this.empresaSeleccionadaId();
    if (!empId) return;

    this.cargando.set(true);
    this.error.set(null);
    this.productosApi.listarPorEmpresa(empId).subscribe({
      next: (data) => {
        this.productos.set(data);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(e.error?.detail || 'No se pudieron cargar los productos');
      },
    });
  }

  productosFiltrados(): Producto[] {
    const q = this.busqueda().toLowerCase().trim();
    if (!q) return this.productos();
    return this.productos().filter(
      (p) =>
        p.codigo.toLowerCase().includes(q) ||
        p.nombre.toLowerCase().includes(q) ||
        p.unidadMedida.toLowerCase().includes(q),
    );
  }

  abrirModalCrear(): void {
    this.productoEnEdicion.set(null);
    this.form.reset({ codigo: '', nombre: '', unidadMedida: '' });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  abrirModalEditar(p: Producto): void {
    this.productoEnEdicion.set(p);
    this.form.patchValue({
      codigo: p.codigo,
      nombre: p.nombre,
      unidadMedida: p.unidadMedida,
    });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.productoEnEdicion.set(null);
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
    const { codigo, nombre, unidadMedida } = this.form.getRawValue();
    const edicion = this.productoEnEdicion();

    if (edicion) {
      this.productosApi.actualizar(edicion.id, { codigo, nombre, unidadMedida }).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.notify.exito('Producto actualizado correctamente');
          this.cargarProductos();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(err.error?.detail || 'Error al actualizar el producto');
        },
      });
    } else {
      this.productosApi.crear({ empresaId: empId, codigo, nombre, unidadMedida }).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.notify.exito('Producto creado correctamente');
          this.cargarProductos();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(err.error?.detail || 'Error al crear el producto');
        },
      });
    }
  }

  eliminar(p: Producto): void {
    if (!confirm(`¿Eliminar producto "${p.nombre}" (${p.codigo})?`)) return;
    this.productosApi.eliminar(p.id).subscribe({
      next: () => {
        this.notify.info(`Producto "${p.nombre}" eliminado.`);
        this.cargarProductos();
      },
      error: (err) => {
        this.notify.error(err.error?.detail || 'No se pudo eliminar el producto.');
      },
    });
  }
}
