import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EmpresasApi } from '../core/api';
import { Empresa } from '../core/models';
import { NotificationService } from '../core/notification.service';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-empresas',
  imports: [CommonModule, ReactiveFormsModule, AppIconComponent],
  template: `
    <div class="space-y-6 animate-fade-in">
      <!-- Cabecera -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">Gestión de Empresas</h1>
          <p class="text-xs text-slate-500 mt-1">
            Administración del catálogo de empresas registradas en Indux (Solo ADMIN).
          </p>
        </div>
        <button
          (click)="abrirModalCrear()"
          class="inline-flex items-center gap-2 rounded-xl bg-emerald-700 px-4 py-2.5 text-xs font-semibold text-white shadow-xs hover:bg-emerald-800 transition cursor-pointer"
        >
          <app-icon name="plus" [size]="14" />
          <span>Nueva Empresa</span>
        </button>
      </div>

      <!-- Barra de búsqueda y filtro -->
      <div class="flex items-center gap-4 bg-white p-3 rounded-2xl border border-slate-200 shadow-xs">
        <div class="relative flex-1">
          <input
            type="text"
            [value]="busqueda()"
            (input)="busqueda.set($any($event.target).value)"
            placeholder="Buscar por nombre o sector…"
            class="w-full rounded-xl border border-slate-200 pl-9 pr-4 py-2 text-xs focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
          />
          <span class="absolute left-3 top-2.5 text-slate-400">
            <app-icon name="search" [size]="14" />
          </span>
        </div>
        <span class="text-xs text-slate-500 font-medium whitespace-nowrap">
          Total: {{ empresasFiltradas().length }}
        </span>
      </div>

      <!-- Estado de carga -->
      @if (cargando()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-xs">
          <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p class="mt-3 text-xs text-slate-500 font-medium">Cargando empresas…</p>
        </div>
      } @else if (error()) {
        <div class="p-6 text-center bg-rose-50 rounded-2xl border border-rose-200 text-rose-700 text-xs">
          <app-icon name="alert-circle" [size]="24" class="mx-auto mb-2 text-rose-600" />
          <p class="font-bold">Error al cargar empresas</p>
          <p class="mt-1">{{ error() }}</p>
          <button
            (click)="cargar()"
            class="mt-3 px-3 py-1.5 rounded-lg bg-rose-600 text-white font-medium hover:bg-rose-700 transition cursor-pointer"
          >
            Reintentar
          </button>
        </div>
      } @else if (empresasFiltradas().length === 0) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-xs">
          <div class="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center mx-auto text-slate-400">
            <app-icon name="building-2" [size]="24" />
          </div>
          <h3 class="mt-3 text-sm font-bold text-slate-800">No se encontraron empresas</h3>
          <p class="text-xs text-slate-500 mt-1">
            {{ busqueda() ? 'No hay resultados para la búsqueda realizada.' : 'Aún no hay empresas registradas en la plataforma.' }}
          </p>
          @if (!busqueda()) {
            <button
              (click)="abrirModalCrear()"
              class="mt-4 inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-700 text-white text-xs font-semibold hover:bg-emerald-800 transition cursor-pointer"
            >
              <app-icon name="plus" [size]="14" />
              <span>Crear primera empresa</span>
            </button>
          }
        </div>
      } @else {
        <!-- Tabla de empresas -->
        <div class="overflow-hidden bg-white rounded-2xl border border-slate-200 shadow-xs">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase tracking-wider text-[10px]">
                <th class="py-3.5 px-4 font-semibold">ID</th>
                <th class="py-3.5 px-4 font-semibold">Nombre de la Empresa</th>
                <th class="py-3.5 px-4 font-semibold">Sector Industrial</th>
                <th class="py-3.5 px-4 font-semibold">Estado</th>
                <th class="py-3.5 px-4 font-semibold text-right">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              @for (emp of empresasFiltradas(); track emp.id) {
                <tr class="hover:bg-slate-50/80 transition">
                  <td class="py-3.5 px-4 text-slate-400 font-mono">#{{ emp.id }}</td>
                  <td class="py-3.5 px-4 font-bold text-slate-900">{{ emp.nombre }}</td>
                  <td class="py-3.5 px-4 text-slate-600">
                    {{ emp.sector || 'No especificado' }}
                  </td>
                  <td class="py-3.5 px-4">
                    <span
                      class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold border"
                      [class]="emp.activa ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-slate-100 text-slate-500 border-slate-200'"
                    >
                      <span class="w-1.5 h-1.5 rounded-full" [class]="emp.activa ? 'bg-emerald-500' : 'bg-slate-400'"></span>
                      {{ emp.activa ? 'Activa' : 'Inactiva' }}
                    </span>
                  </td>
                  <td class="py-3.5 px-4 text-right whitespace-nowrap">
                    <div class="inline-flex items-center gap-2">
                      <button
                        (click)="abrirModalEditar(emp)"
                        class="px-2.5 py-1 rounded-lg text-slate-600 bg-slate-100 hover:bg-slate-200 hover:text-slate-900 transition font-medium cursor-pointer"
                      >
                        Editar
                      </button>
                      @if (emp.activa) {
                        <button
                          (click)="desactivar(emp)"
                          class="px-2.5 py-1 rounded-lg text-rose-600 bg-rose-50 hover:bg-rose-100 transition font-medium cursor-pointer"
                        >
                          Desactivar
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

      <!-- Modal Crear / Editar -->
      @if (modalAbierto()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-fade-in">
          <div class="w-full max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <h2 class="text-sm font-bold text-slate-900">
                {{ empresaEnEdicion() ? 'Editar Empresa' : 'Registrar Nueva Empresa' }}
              </h2>
              <button (click)="cerrarModal()" class="text-slate-400 hover:text-slate-600 p-1 rounded-md transition cursor-pointer" aria-label="Cerrar">
                <app-icon name="x" [size]="16" />
              </button>
            </div>

            <form [formGroup]="form" (ngSubmit)="guardar()" class="p-6 space-y-4">
              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="emp-nombre">
                  Nombre de la Empresa *
                </label>
                <input
                  id="emp-nombre"
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Metálicas del Valle S.A.S."
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
                @if (form.controls.nombre.touched && form.controls.nombre.invalid) {
                  <p class="mt-1 text-xs text-rose-600">El nombre es obligatorio.</p>
                }
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="emp-sector">
                  Sector Industrial
                </label>
                <input
                  id="emp-sector"
                  type="text"
                  formControlName="sector"
                  placeholder="Ej. Metalmecánica, Automotriz, Textil…"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
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
                  class="px-4 py-2 rounded-xl text-xs font-medium text-slate-600 hover:bg-slate-100 transition cursor-pointer"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  [disabled]="guardando()"
                  class="px-4 py-2 rounded-xl text-xs font-semibold text-white bg-emerald-700 hover:bg-emerald-800 transition disabled:opacity-60 shadow-xs cursor-pointer"
                >
                  {{ guardando() ? 'Guardando…' : 'Guardar Empresa' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
})
export class Empresas implements OnInit {
  private readonly api = inject(EmpresasApi);
  private readonly notify = inject(NotificationService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly empresas = signal<Empresa[]>([]);
  protected readonly busqueda = signal('');
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly modalAbierto = signal(false);
  protected readonly empresaEnEdicion = signal<Empresa | null>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required]],
    sector: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.api.listar().subscribe({
      next: (data) => {
        this.empresas.set(data);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(e.error?.detail || 'No se pudo cargar la lista de empresas.');
      },
    });
  }

  empresasFiltradas(): Empresa[] {
    const q = this.busqueda().toLowerCase().trim();
    if (!q) return this.empresas();
    return this.empresas().filter(
      (e) =>
        e.nombre.toLowerCase().includes(q) ||
        (e.sector && e.sector.toLowerCase().includes(q)),
    );
  }

  abrirModalCrear(): void {
    this.empresaEnEdicion.set(null);
    this.form.reset({ nombre: '', sector: '' });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  abrirModalEditar(emp: Empresa): void {
    this.empresaEnEdicion.set(emp);
    this.form.patchValue({
      nombre: emp.nombre,
      sector: emp.sector || '',
    });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.empresaEnEdicion.set(null);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);
    const { nombre, sector } = this.form.getRawValue();
    const edicion = this.empresaEnEdicion();

    if (edicion) {
      this.api.actualizar(edicion.id, { nombre, sector: sector || null }).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.notify.exito('Empresa actualizada correctamente');
          this.cargar();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(err.error?.detail || 'Error al actualizar la empresa');
        },
      });
    } else {
      this.api.crear({ nombre, sector: sector || null }).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.notify.exito('Empresa creada correctamente');
          this.cargar();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(err.error?.detail || 'Error al crear la empresa');
        },
      });
    }
  }

  desactivar(emp: Empresa): void {
    if (!confirm(`¿Estás seguro de desactivar la empresa "${emp.nombre}"?`)) {
      return;
    }
    this.api.desactivar(emp.id).subscribe({
      next: () => {
        this.notify.info(`Empresa "${emp.nombre}" desactivada.`);
        this.cargar();
      },
      error: (err) => {
        this.notify.error(err.error?.detail || 'No se pudo desactivar la empresa.');
      },
    });
  }
}
