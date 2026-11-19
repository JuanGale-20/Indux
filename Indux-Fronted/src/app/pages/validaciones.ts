import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ValidacionesService } from '../services/validaciones.service';
import { RouterLink } from '@angular/router';
import { AppIconComponent } from '../core/icon.component';

@Component({
  selector: 'app-validaciones',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AppIconComponent],
  template: `
    <div class="space-y-6 max-w-4xl mx-auto app-fade-in">
      <div class="flex items-center justify-between pb-4 border-b border-slate-200">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">
            Validador de Reglas SteelForce (Fase 2)
          </h1>
          <p class="text-xs text-slate-500 mt-0.5">
            Comprobador instantáneo de consistencia para entidades del catálogo previo a la persistencia.
          </p>
        </div>
        <a
          routerLink="/app/reportes"
          class="text-xs font-semibold text-emerald-800 bg-emerald-50 hover:bg-emerald-100 px-3 py-1.5 rounded-xl transition"
        >
          Ver validaciones en Reportes →
        </a>
      </div>

      <!-- Selector de Tipo -->
      <div class="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4">
        <label class="text-xs font-bold uppercase tracking-wider text-slate-700">Entidad a Evaluar:</label>
        <div class="flex items-center gap-2">
          <button
            type="button"
            (click)="cambiarTipo('empresa')"
            class="px-3 py-1.5 rounded-xl text-xs font-semibold transition"
            [class]="tipo() === 'empresa' ? 'bg-emerald-700 text-white shadow-xs' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'"
          >
            Empresa
          </button>
          <button
            type="button"
            (click)="cambiarTipo('producto')"
            class="px-3 py-1.5 rounded-xl text-xs font-semibold transition"
            [class]="tipo() === 'producto' ? 'bg-emerald-700 text-white shadow-xs' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'"
          >
            Producto
          </button>
          <button
            type="button"
            (click)="cambiarTipo('proceso')"
            class="px-3 py-1.5 rounded-xl text-xs font-semibold transition"
            [class]="tipo() === 'proceso' ? 'bg-emerald-700 text-white shadow-xs' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'"
          >
            Proceso / Etapa
          </button>
        </div>
      </div>

      <!-- Formulario interactivo -->
      <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
        @if (tipo() === 'empresa') {
          <form [formGroup]="formEmpresa" (ngSubmit)="validarEmpresa()" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-semibold uppercase text-slate-700">Nombre de Empresa</label>
                <input
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Industrias Metálicas S.A."
                  class="mt-1 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs focus:border-emerald-600 focus:outline-none"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold uppercase text-slate-700">Sector Industrial</label>
                <input
                  type="text"
                  formControlName="sector"
                  placeholder="Ej. Metalmecánica"
                  class="mt-1 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs focus:border-emerald-600 focus:outline-none"
                />
              </div>
            </div>
            <div class="flex justify-end">
              <button
                type="submit"
                [disabled]="validando()"
                class="px-5 py-2 rounded-xl bg-emerald-700 text-white text-xs font-bold hover:bg-emerald-800 transition disabled:opacity-50"
              >
                {{ validando() ? 'Validando…' : 'Validar Empresa' }}
              </button>
            </div>
          </form>
        } @else if (tipo() === 'producto') {
          <form [formGroup]="formProducto" (ngSubmit)="validarProducto()" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-xs font-semibold uppercase text-slate-700">Código</label>
                <input
                  type="text"
                  formControlName="codigo"
                  placeholder="Ej. PRD-01"
                  class="mt-1 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs uppercase font-mono focus:border-emerald-600 focus:outline-none"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold uppercase text-slate-700">Nombre</label>
                <input
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Perfil de Acero"
                  class="mt-1 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs focus:border-emerald-600 focus:outline-none"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold uppercase text-slate-700">Unidad de Medida</label>
                <input
                  type="text"
                  formControlName="unidadMedida"
                  placeholder="Ej. UNIDAD, KG"
                  class="mt-1 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs focus:border-emerald-600 focus:outline-none"
                />
              </div>
            </div>
            <div class="flex justify-end">
              <button
                type="submit"
                [disabled]="validando()"
                class="px-5 py-2 rounded-xl bg-emerald-700 text-white text-xs font-bold hover:bg-emerald-800 transition disabled:opacity-50"
              >
                {{ validando() ? 'Validando…' : 'Validar Producto' }}
              </button>
            </div>
          </form>
        } @else {
          <form [formGroup]="formProceso" (ngSubmit)="validarProceso()" class="space-y-4">
            <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
              <div>
                <label class="block text-xs font-semibold uppercase text-slate-700">Código</label>
                <input
                  type="text"
                  formControlName="codigo"
                  placeholder="Ej. PROC-CORTE"
                  class="mt-1 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs uppercase font-mono focus:border-emerald-600 focus:outline-none"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold uppercase text-slate-700">Nombre</label>
                <input
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Corte"
                  class="mt-1 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs focus:border-emerald-600 focus:outline-none"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold uppercase text-slate-700">Secuencia</label>
                <input
                  type="number"
                  formControlName="secuencia"
                  placeholder="1"
                  class="mt-1 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs focus:border-emerald-600 focus:outline-none"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold uppercase text-slate-700">Unidad Procesada</label>
                <input
                  type="text"
                  formControlName="unidadProcesada"
                  placeholder="Ej. PIEZA"
                  class="mt-1 w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs focus:border-emerald-600 focus:outline-none"
                />
              </div>
            </div>
            <div class="flex justify-end">
              <button
                type="submit"
                [disabled]="validando()"
                class="px-5 py-2 rounded-xl bg-emerald-700 text-white text-xs font-bold hover:bg-emerald-800 transition disabled:opacity-50"
              >
                {{ validando() ? 'Validando…' : 'Validar Proceso' }}
              </button>
            </div>
          </form>
        }

        <!-- Resultado de la validación -->
        @if (resultado()) {
          @let res = resultado()!;
          <div class="pt-4 border-t border-slate-100">
            <h3 class="text-xs font-bold uppercase tracking-wider text-slate-700 mb-2">Resultado de la Regla:</h3>
            <div
              class="rounded-xl p-4 text-xs border"
              [class]="res.valido ? 'bg-emerald-50 border-emerald-200 text-emerald-900' : 'bg-rose-50 border-rose-200 text-rose-900'"
            >
              <div class="flex items-center gap-2 font-bold mb-1">
                <app-icon [name]="res.valido ? 'check-circle' : 'alert-triangle'" [size]="15" />
                <span>{{ res.valido ? 'Conforme a SteelForce' : 'Inconsistencia Detectada' }}</span>
              </div>
              @if (res.mensajes && res.mensajes.length > 0) {
                <ul class="list-disc list-inside mt-1 space-y-0.5">
                  @for (m of res.mensajes; track m) {
                    <li>{{ m }}</li>
                  }
                </ul>
              } @else {
                <p>Todos los campos cumplen los requisitos de integridad del modelo industrial.</p>
              }
            </div>
          </div>
        }
      </div>
    </div>
  `,
})
export class Validaciones {
  private readonly validacionesService = inject(ValidacionesService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly tipo = signal<'empresa' | 'producto' | 'proceso'>('empresa');
  protected readonly validando = signal(false);
  protected readonly resultado = signal<{ valido: boolean; mensajes?: string[] } | null>(null);

  protected readonly formEmpresa = this.fb.group({
    nombre: ['Metálicas del Norte', [Validators.required]],
    sector: ['Metalmecánica'],
  });

  protected readonly formProducto = this.fb.group({
    codigo: ['PRD-01', [Validators.required]],
    nombre: ['Tubo Industrial 1 pulg', [Validators.required]],
    unidadMedida: ['UNIDAD', [Validators.required]],
  });

  protected readonly formProceso = this.fb.group({
    codigo: ['PRC-CORTE', [Validators.required]],
    nombre: ['Corte Automático', [Validators.required]],
    secuencia: [1, [Validators.required]],
    unidadProcesada: ['PIEZA', [Validators.required]],
  });

  cambiarTipo(t: 'empresa' | 'producto' | 'proceso'): void {
    this.tipo.set(t);
    this.resultado.set(null);
  }

  validarEmpresa(): void {
    this.validando.set(true);
    const { nombre, sector } = this.formEmpresa.getRawValue();
    this.validacionesService.validarEmpresa({ nombre, sector }).subscribe({
      next: (res) => {
        this.validando.set(false);
        this.resultado.set(res);
      },
      error: () => this.validando.set(false),
    });
  }

  validarProducto(): void {
    this.validando.set(true);
    const { codigo, nombre, unidadMedida } = this.formProducto.getRawValue();
    this.validacionesService.validarProducto({ codigo, nombre, unidadMedida }).subscribe({
      next: (res) => {
        this.validando.set(false);
        this.resultado.set(res);
      },
      error: () => this.validando.set(false),
    });
  }

  validarProceso(): void {
    this.validando.set(true);
    const { codigo, nombre, secuencia, unidadProcesada } = this.formProceso.getRawValue();
    this.validacionesService
      .validarProceso({
        codigo,
        nombre,
        secuencia: Number(secuencia),
        unidadProcesada,
      })
      .subscribe({
        next: (res) => {
          this.validando.set(false);
          this.resultado.set(res);
        },
        error: () => this.validando.set(false),
      });
  }
}
