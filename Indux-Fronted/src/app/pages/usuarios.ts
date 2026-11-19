import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EmpresasApi, UsuariosApi } from '../core/api';
import { Empresa, Rol, Usuario } from '../core/models';
import { NotificationService } from '../core/notification.service';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-usuarios',
  imports: [CommonModule, ReactiveFormsModule, AppIconComponent],
  template: `
    <div class="space-y-6 app-fade-in">
      <!-- Cabecera -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">Gestión de Usuarios</h1>
          <p class="text-xs text-slate-500 mt-1">
            Administración de cuentas y asignación de roles (ADMIN, ANALISTA, LÍDER).
          </p>
        </div>
        <button
          (click)="abrirModalCrear()"
          class="inline-flex items-center gap-2 rounded-xl bg-emerald-700 px-4 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-emerald-800 transition shadow-emerald-700/20"
        >
          <app-icon name="plus" [size]="15" />
          <span>Nuevo Usuario</span>
        </button>
      </div>

      <!-- Barra de filtros -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 bg-white p-3 rounded-2xl border border-slate-200 shadow-sm">
        <div class="relative">
          <input
            type="text"
            [value]="busqueda()"
            (input)="busqueda.set($any($event.target).value)"
            placeholder="Buscar por nombre o correo…"
            class="w-full rounded-xl border border-slate-200 pl-9 pr-4 py-2 text-xs focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
          />
          <app-icon name="search" [size]="15" class="absolute left-3 top-2.5 text-slate-400" />
        </div>

        <div>
          <select
            [value]="filtroRol()"
            (change)="filtroRol.set($any($event.target).value)"
            class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs text-slate-700 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
          >
            <option value="">Todos los roles</option>
            <option value="ADMIN">ADMIN</option>
            <option value="ANALISTA">ANALISTA</option>
            <option value="LIDER">LÍDER</option>
          </select>
        </div>

        <div class="flex items-center justify-end text-xs text-slate-500 font-medium">
          Total: {{ usuariosFiltrados().length }} usuario(s)
        </div>
      </div>

      <!-- Estados de carga / error / vacío -->
      @if (cargando()) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="inline-block w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p class="mt-3 text-xs text-slate-500">Cargando usuarios…</p>
        </div>
      } @else if (error()) {
        <div class="p-6 text-center bg-rose-50 rounded-2xl border border-rose-200 text-rose-700 text-xs">
          <p class="font-bold">Error al cargar usuarios</p>
          <p class="mt-1">{{ error() }}</p>
          <button
            (click)="cargar()"
            class="mt-3 px-3 py-1.5 rounded-lg bg-rose-600 text-white font-medium hover:bg-rose-700 transition"
          >
            Reintentar
          </button>
        </div>
      } @else if (usuariosFiltrados().length === 0) {
        <div class="p-12 text-center bg-white rounded-2xl border border-slate-200 shadow-sm">
          <div class="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center mx-auto text-slate-400">
            <app-icon name="users" [size]="24" />
          </div>
          <h3 class="mt-3 text-sm font-bold text-slate-800">No se encontraron usuarios</h3>
          <p class="text-xs text-slate-500 mt-1">
            {{ busqueda() || filtroRol() ? 'No hay usuarios que coincidan con los filtros aplicados.' : 'No hay usuarios registrados.' }}
          </p>
        </div>
      } @else {
        <!-- Tabla -->
        <div class="overflow-hidden bg-white rounded-2xl border border-slate-200 shadow-sm">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase tracking-wider text-[10px]">
                <th class="py-3.5 px-4 font-semibold">Usuario</th>
                <th class="py-3.5 px-4 font-semibold">Correo</th>
                <th class="py-3.5 px-4 font-semibold">Rol</th>
                <th class="py-3.5 px-4 font-semibold">Empresa Asociada</th>
                <th class="py-3.5 px-4 font-semibold">Estado</th>
                <th class="py-3.5 px-4 font-semibold text-right">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              @for (u of usuariosFiltrados(); track u.id) {
                <tr class="hover:bg-slate-50/80 transition">
                  <td class="py-3.5 px-4 font-bold text-slate-900">
                    <div class="flex items-center gap-2.5">
                      <div class="w-7 h-7 rounded-full bg-slate-100 text-slate-700 flex items-center justify-center text-[10px] font-bold uppercase">
                        {{ u.nombre.charAt(0) }}
                      </div>
                      <span>{{ u.nombre }}</span>
                    </div>
                  </td>
                  <td class="py-3.5 px-4 text-slate-600 font-mono">{{ u.email }}</td>
                  <td class="py-3.5 px-4">
                    <span
                      class="inline-block px-2 py-0.5 rounded-md text-[10px] font-bold uppercase"
                      [class]="rolBadgeClass(u.rol)"
                    >
                      {{ u.rol }}
                    </span>
                  </td>
                  <td class="py-3.5 px-4 text-slate-600">
                    @if (u.empresaId) {
                      <span class="font-medium text-slate-800">{{ obtenerNombreEmpresa(u.empresaId) }}</span>
                    } @else {
                      <span class="text-slate-400 italic">Sin empresa (ADMIN global)</span>
                    }
                  </td>
                  <td class="py-3.5 px-4">
                    <span
                      class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold"
                      [class]="u.activo ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-slate-100 text-slate-500 border border-slate-200'"
                    >
                      <span class="w-1.5 h-1.5 rounded-full" [class]="u.activo ? 'bg-emerald-500' : 'bg-slate-400'"></span>
                      {{ u.activo ? 'Activo' : 'Inactivo' }}
                    </span>
                  </td>
                  <td class="py-3.5 px-4 text-right whitespace-nowrap">
                    <div class="inline-flex items-center gap-2">
                      <button
                        (click)="abrirModalEditar(u)"
                        class="px-2.5 py-1 rounded-lg text-slate-600 bg-slate-100 hover:bg-slate-200 hover:text-slate-900 transition font-medium"
                      >
                        Editar
                      </button>
                      @if (u.activo) {
                        <button
                          (click)="desactivar(u)"
                          class="px-2.5 py-1 rounded-lg text-rose-600 bg-rose-50 hover:bg-rose-100 transition font-medium"
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
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 app-fade-in">
          <div class="w-full max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden app-slide-up">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <h2 class="text-sm font-bold text-slate-900">
                {{ usuarioEnEdicion() ? 'Editar Usuario' : 'Registrar Nuevo Usuario' }}
              </h2>
              <button (click)="cerrarModal()" class="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100 transition">
                <app-icon name="x" [size]="18" />
              </button>
            </div>

            <form [formGroup]="form" (ngSubmit)="guardar()" class="p-6 space-y-4">
              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="u-nombre">
                  Nombre Completo *
                </label>
                <input
                  id="u-nombre"
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Ing. Laura Gómez"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
                @if (form.controls.nombre.touched && form.controls.nombre.invalid) {
                  <p class="mt-1 text-xs text-rose-600">El nombre es obligatorio.</p>
                }
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="u-email">
                  Correo Electrónico *
                </label>
                <input
                  id="u-email"
                  type="email"
                  formControlName="email"
                  placeholder="laura@empresa.com"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
                @if (form.controls.email.touched && form.controls.email.invalid) {
                  <p class="mt-1 text-xs text-rose-600">Ingresa un correo electrónico válido.</p>
                }
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="u-password">
                  {{ usuarioEnEdicion() ? 'Nueva Contraseña (opcional)' : 'Contraseña *' }}
                </label>
                <input
                  id="u-password"
                  type="password"
                  formControlName="password"
                  placeholder="••••••••"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                />
                @if (form.controls.password.touched && form.controls.password.invalid) {
                  <p class="mt-1 text-xs text-rose-600">La contraseña es obligatoria al registrar.</p>
                }
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="u-rol">
                  Rol en la Plataforma *
                </label>
                <select
                  id="u-rol"
                  formControlName="rol"
                  (change)="alCambiarRol()"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                >
                  <option value="ANALISTA">ANALISTA (Gestión y análisis de reportes de una empresa)</option>
                  <option value="LIDER">LÍDER (Supervisión solo lectura de procesos asignados)</option>
                  <option value="ADMIN">ADMIN (Administrador general de la plataforma)</option>
                </select>
              </div>

              @if (form.controls.rol.value !== 'ADMIN') {
                <div>
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-700" for="u-empresa">
                    Empresa Asociada *
                  </label>
                  <select
                    id="u-empresa"
                    formControlName="empresaId"
                    class="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
                  >
                    <option [ngValue]="null">Selecciona una empresa…</option>
                    @for (emp of empresas(); track emp.id) {
                      <option [ngValue]="emp.id">{{ emp.nombre }} (ID: {{ emp.id }})</option>
                    }
                  </select>
                  @if (form.controls.empresaId.touched && !form.controls.empresaId.value) {
                    <p class="mt-1 text-xs text-rose-600">Los roles ANALISTA y LÍDER deben estar vinculados a una empresa.</p>
                  }
                </div>
              }

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
                  {{ guardando() ? 'Guardando…' : 'Guardar Usuario' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
})
export class Usuarios implements OnInit {
  private readonly usuariosApi = inject(UsuariosApi);
  private readonly empresasApi = inject(EmpresasApi);
  private readonly notify = inject(NotificationService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly empresas = signal<Empresa[]>([]);
  protected readonly busqueda = signal('');
  protected readonly filtroRol = signal('');
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly modalAbierto = signal(false);
  protected readonly usuarioEnEdicion = signal<Usuario | null>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
    rol: ['ANALISTA' as Rol, [Validators.required]],
    empresaId: [null as number | null],
  });

  ngOnInit(): void {
    this.cargar();
    this.cargarEmpresas();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.usuariosApi.listar().subscribe({
      next: (data) => {
        this.usuarios.set(data);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(e.error?.detail || 'No se pudo cargar la lista de usuarios.');
      },
    });
  }

  cargarEmpresas(): void {
    this.empresasApi.listar().subscribe({
      next: (data) => this.empresas.set(data),
      error: () => {},
    });
  }

  obtenerNombreEmpresa(empresaId: number): string {
    const e = this.empresas().find((emp) => emp.id === empresaId);
    return e ? e.nombre : `Empresa #${empresaId}`;
  }

  usuariosFiltrados(): Usuario[] {
    const q = this.busqueda().toLowerCase().trim();
    const rol = this.filtroRol();
    return this.usuarios().filter((u) => {
      const coincideTexto = !q || u.nombre.toLowerCase().includes(q) || u.email.toLowerCase().includes(q);
      const coincideRol = !rol || u.rol === rol;
      return coincideTexto && coincideRol;
    });
  }

  rolBadgeClass(rol: Rol): string {
    switch (rol) {
      case 'ADMIN':
        return 'bg-purple-100 text-purple-800 border border-purple-200';
      case 'ANALISTA':
        return 'bg-emerald-100 text-emerald-800 border border-emerald-200';
      case 'LIDER':
        return 'bg-blue-100 text-blue-800 border border-blue-200';
      default:
        return 'bg-slate-100 text-slate-700';
    }
  }

  alCambiarRol(): void {
    if (this.form.controls.rol.value === 'ADMIN') {
      this.form.patchValue({ empresaId: null });
    }
  }

  abrirModalCrear(): void {
    this.usuarioEnEdicion.set(null);
    this.form.reset({
      nombre: '',
      email: '',
      password: '',
      rol: 'ANALISTA',
      empresaId: this.empresas().length > 0 ? this.empresas()[0].id : null,
    });
    this.form.controls.password.setValidators([Validators.required]);
    this.form.controls.password.updateValueAndValidity();
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  abrirModalEditar(u: Usuario): void {
    this.usuarioEnEdicion.set(u);
    this.form.patchValue({
      nombre: u.nombre,
      email: u.email,
      password: '',
      rol: u.rol,
      empresaId: u.empresaId,
    });
    this.form.controls.password.clearValidators();
    this.form.controls.password.updateValueAndValidity();
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.usuarioEnEdicion.set(null);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nombre, email, password, rol, empresaId } = this.form.getRawValue();

    if (rol !== 'ADMIN' && !empresaId) {
      this.errorFormulario.set('Debes seleccionar una empresa para los roles ANALISTA y LÍDER.');
      return;
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);
    const edicion = this.usuarioEnEdicion();

    if (edicion) {
      this.usuariosApi
        .actualizar(edicion.id, {
          nombre,
          email,
          password: password ? password : null,
          rol,
          empresaId: rol === 'ADMIN' ? null : empresaId,
        })
        .subscribe({
          next: () => {
            this.guardando.set(false);
            this.cerrarModal();
            this.notify.exito('Usuario actualizado correctamente');
            this.cargar();
          },
          error: (err) => {
            this.guardando.set(false);
            this.errorFormulario.set(err.error?.detail || 'Error al actualizar el usuario');
          },
        });
    } else {
      if (!password) {
        this.errorFormulario.set('La contraseña es obligatoria para nuevos usuarios.');
        this.guardando.set(false);
        return;
      }
      this.usuariosApi
        .registrar({
          nombre,
          email,
          password,
          rol,
          empresaId: rol === 'ADMIN' ? null : empresaId,
        })
        .subscribe({
          next: () => {
            this.guardando.set(false);
            this.cerrarModal();
            this.notify.exito('Usuario registrado correctamente');
            this.cargar();
          },
          error: (err) => {
            this.guardando.set(false);
            this.errorFormulario.set(err.error?.detail || 'Error al registrar el usuario');
          },
        });
    }
  }

  desactivar(u: Usuario): void {
    if (!confirm(`¿Estás seguro de desactivar al usuario "${u.nombre}" (${u.email})?`)) {
      return;
    }
    this.usuariosApi.desactivar(u.id).subscribe({
      next: () => {
        this.notify.info(`Usuario "${u.nombre}" desactivado.`);
        this.cargar();
      },
      error: (err) => {
        this.notify.error(err.error?.detail || 'No se pudo desactivar el usuario.');
      },
    });
  }
}
