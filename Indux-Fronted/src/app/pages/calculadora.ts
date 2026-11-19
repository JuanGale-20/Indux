import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription, debounceTime, filter } from 'rxjs';
import { AnalisisApi } from '../core/api';
import { AnalisisResponse, RecursoCalculoRequest, RecursoCalculoResultado } from '../core/models';
import { NotificationService } from '../core/notification.service';
import { AppIconComponent } from '../core/icon.component';
import { obtenerClasificacionInfo } from '../core/steelforce-constants';

@Component({
  selector: 'app-calculadora',
  imports: [CommonModule, ReactiveFormsModule, AppIconComponent],
  template: `
    <div class="space-y-6 max-w-6xl mx-auto app-fade-in">
      <!-- Cabecera -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-wider bg-emerald-100 text-emerald-800 mb-2">
            <app-icon name="calculator" [size]="12" />
            <span>Simulador de Capacidad al Vuelo</span>
          </span>
          <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">
            Calculadora SteelForce Instantánea
          </h1>
          <p class="text-xs text-slate-500 mt-0.5">
            Evalúa restricciones y cuellos de botella sin necesidad de persistir datos en la base de datos.
          </p>
        </div>

        <div class="flex items-center gap-2">
          <button
            type="button"
            (click)="cargarEjemplo()"
            class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 transition shadow-2xs cursor-pointer"
          >
            <app-icon name="sparkles" [size]="14" class="text-emerald-600" />
            <span>Cargar Ejemplo Industrial</span>
          </button>
          <button
            type="button"
            (click)="limpiar()"
            class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold bg-slate-100 text-slate-600 hover:bg-slate-200 transition cursor-pointer"
          >
            <app-icon name="refresh-cw" [size]="13" />
            <span>Limpiar</span>
          </button>
        </div>
      </div>

      <!-- Formulario de Demanda y Recursos -->
      <form [formGroup]="form" (ngSubmit)="calcular()" class="space-y-6">
        <!-- Parámetro de Demanda -->
        <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 card-interactive">
          <div>
            <label class="block text-xs font-bold uppercase tracking-wider text-slate-700" for="calc-demanda">
              Demanda Requerida (Unidades) *
            </label>
            <p class="text-[11px] text-slate-500 mt-0.5">
              Volumen total de piezas o lotes a fabricar en el periodo analizado.
            </p>
          </div>
          <div class="w-full sm:w-64">
            <input
              id="calc-demanda"
              type="number"
              step="any"
              min="0"
              formControlName="demanda"
              placeholder="Ej. 1000"
              class="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm font-mono text-slate-900 font-bold focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600"
            />
            @if (form.controls.demanda.touched && form.controls.demanda.invalid) {
              <p class="mt-1 text-xs text-rose-600">La demanda debe ser mayor o igual a 0.</p>
            }
          </div>
        </div>

        <!-- Recursos / Puestos de Trabajo -->
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <div class="flex items-center justify-between border-b border-slate-100 pb-3">
            <div>
              <h2 class="text-xs font-bold uppercase tracking-wider text-slate-800 flex items-center gap-2">
                <app-icon name="factory" [size]="16" class="text-emerald-700" />
                <span>Estaciones o Recursos Productivos</span>
              </h2>
              <p class="text-[11px] text-slate-500 mt-0.5">
                Define el tiempo estándar y el régimen operativo disponible de cada estación.
              </p>
            </div>
            <button
              type="button"
              (click)="agregarRecurso()"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold bg-emerald-50 text-emerald-800 hover:bg-emerald-100 transition cursor-pointer"
            >
              <app-icon name="plus" [size]="14" />
              <span>Agregar Estación</span>
            </button>
          </div>

          <div class="space-y-4">
            @for (rec of recursosArray.controls; track $index) {
              <div
                [formGroup]="rec"
                class="p-4 rounded-xl border border-slate-200 bg-slate-50/50 hover:bg-slate-50 transition space-y-3"
              >
                <div class="flex items-center justify-between">
                  <span class="text-xs font-black uppercase text-slate-500 tracking-wider">
                    Estación #{{ $index + 1 }}
                  </span>
                  @if (recursosArray.length > 1) {
                    <button
                      type="button"
                      (click)="eliminarRecurso($index)"
                      class="inline-flex items-center gap-1 text-xs text-rose-600 hover:text-rose-800 font-semibold cursor-pointer"
                    >
                      <app-icon name="trash-2" [size]="13" />
                      <span>Eliminar</span>
                    </button>
                  }
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-6 gap-3">
                  <div class="lg:col-span-2">
                    <label class="block text-[11px] font-semibold text-slate-600 mb-1">Nombre *</label>
                    <input
                      type="text"
                      formControlName="nombre"
                      placeholder="Ej. Torno CNC"
                      class="w-full rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs focus:border-emerald-600 focus:outline-none"
                    />
                  </div>

                  <div>
                    <label class="block text-[11px] font-semibold text-slate-600 mb-1">
                      T. Ciclo (h/u) *
                    </label>
                    <input
                      type="number"
                      step="any"
                      min="0.0001"
                      formControlName="tiempoProcesamiento"
                      placeholder="0.1"
                      class="w-full rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-mono focus:border-emerald-600 focus:outline-none"
                    />
                  </div>

                  <div>
                    <label class="block text-[11px] font-semibold text-slate-600 mb-1">Turnos/Día *</label>
                    <input
                      type="number"
                      min="1"
                      formControlName="turnos"
                      placeholder="1"
                      class="w-full rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-mono focus:border-emerald-600 focus:outline-none"
                    />
                  </div>

                  <div>
                    <label class="block text-[11px] font-semibold text-slate-600 mb-1">Horas/Turno *</label>
                    <input
                      type="number"
                      step="any"
                      min="0.1"
                      max="24"
                      formControlName="horasPorTurno"
                      placeholder="8"
                      class="w-full rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-mono focus:border-emerald-600 focus:outline-none"
                    />
                  </div>

                  <div>
                    <label class="block text-[11px] font-semibold text-slate-600 mb-1">Días Oper. *</label>
                    <input
                      type="number"
                      min="1"
                      formControlName="diasOperacion"
                      placeholder="20"
                      class="w-full rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-mono focus:border-emerald-600 focus:outline-none"
                    />
                  </div>

                  <div class="sm:col-span-2 lg:col-span-1">
                    <label class="block text-[11px] font-semibold text-slate-600 mb-1">Cant. Máquinas *</label>
                    <input
                      type="number"
                      min="1"
                      formControlName="cantidadRecursos"
                      placeholder="1"
                      class="w-full rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-mono focus:border-emerald-600 focus:outline-none"
                    />
                  </div>
                </div>
              </div>
            }
          </div>

          <div class="pt-4 flex justify-end">
            <button
              type="submit"
              [disabled]="calculando() || form.invalid"
              class="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl bg-emerald-700 text-white text-xs font-bold hover:bg-emerald-800 transition shadow-xs disabled:opacity-50 cursor-pointer"
            >
              <app-icon name="calculator" [size]="15" />
              <span>{{ calculando() ? 'Simulando...' : 'Calcular Capacidad y Cuellos de Botella' }}</span>
            </button>
          </div>
        </div>
      </form>

      <!-- Resultados del Análisis al Vuelo -->
      @if (resultado(); as res) {
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-6 app-slide-up">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div>
              <h2 class="text-lg font-black text-slate-900 tracking-tight flex items-center gap-2">
                <app-icon name="chart-no-axes-combined" [size]="20" class="text-emerald-700" />
                <span>Diagnóstico SteelForce</span>
              </h2>
              <p class="text-xs text-slate-500 mt-0.5">
                Resultados generados para una demanda de <strong>{{ form.value.demanda }} unidades</strong>.
              </p>
            </div>
            <div class="flex items-center gap-2">
              @let cuellos = contarCuellos(res.recursos);
              <span
                class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold border"
                [class]="cuellos > 0 ? 'bg-rose-50 text-rose-800 border-rose-200' : 'bg-emerald-50 text-emerald-800 border-emerald-200'"
              >
                <app-icon [name]="cuellos > 0 ? 'alert-triangle' : 'check-circle'" [size]="14" />
                <span>{{ cuellos > 0 ? cuellos + ' Cuello(s) de Botella Detectado(s)' : 'Capacidad Equilibrada' }}</span>
              </span>
            </div>
          </div>

          <!-- Tabla de Resultados -->
          <div class="overflow-x-auto">
            <table class="w-full text-left text-xs border-collapse">
              <thead>
                <tr class="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase tracking-wider text-[10px]">
                  <th class="py-3 px-3 font-semibold text-center">Prioridad</th>
                  <th class="py-3 px-3 font-semibold">Estación / Recurso</th>
                  <th class="py-3 px-3 font-semibold text-right">Horas Requeridas</th>
                  <th class="py-3 px-3 font-semibold text-right">Horas Disponibles</th>
                  <th class="py-3 px-3 font-semibold">Utilización (%)</th>
                  <th class="py-3 px-3 font-semibold text-right">Holgura / Déficit</th>
                  <th class="py-3 px-3 font-semibold">Clasificación</th>
                  <th class="py-3 px-3 font-semibold">Diagnóstico</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                @for (rec of res.recursos; track rec.nombre) {
                  @let info = obtenerClasificacion(rec.clasificacion);
                  <tr class="hover:bg-slate-50/80 transition">
                    <td class="py-3 px-3 text-center">
                      @if (rec.prioridad === 1 && rec.clasificacion === 'SOBRECARGA') {
                        <span class="inline-block px-2 py-0.5 rounded-full text-[10px] font-black bg-rose-600 text-white animate-pulse">
                          #1 CUELLO
                        </span>
                      } @else {
                        <span class="font-mono text-slate-500 font-bold">#{{ rec.prioridad }}</span>
                      }
                    </td>
                    <td class="py-3 px-3 font-bold text-slate-900">{{ rec.nombre }}</td>
                    <td class="py-3 px-3 text-right font-mono text-slate-700">
                      {{ rec.horasRequeridas | number: '1.1-2' }} h
                    </td>
                    <td class="py-3 px-3 text-right font-mono text-slate-700">
                      {{ rec.horasDisponibles | number: '1.1-2' }} h
                    </td>
                    <td class="py-3 px-3">
                      <div class="space-y-1">
                        <div class="flex items-center justify-between text-[11px] font-bold">
                          <span>{{ rec.utilizacion | number: '1.1-2' }}%</span>
                        </div>
                        <div class="w-24 bg-slate-200 rounded-full h-1.5 overflow-hidden">
                          <div
                            class="h-full rounded-full transition-all duration-300"
                            [style.width.%]="calcularAnchoBarra(rec.utilizacion)"
                            [style.background-color]="info.barColor"
                          ></div>
                        </div>
                      </div>
                    </td>
                    <td class="py-3 px-3 text-right font-mono font-bold">
                      @if (rec.deficitHoras > 0) {
                        <span class="text-rose-600">-{{ rec.deficitHoras | number: '1.1-2' }} h</span>
                      } @else {
                        <span class="text-emerald-700">+{{ rec.holgura | number: '1.1-2' }} h</span>
                      }
                    </td>
                    <td class="py-3 px-3">
                      <span
                        class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-bold border"
                        [class]="info.badgeClass"
                      >
                        <span class="w-1.5 h-1.5 rounded-full" [class]="info.dotColor"></span>
                        {{ info.etiqueta }}
                      </span>
                    </td>
                    <td class="py-3 px-3 text-[11px] text-slate-600 max-w-xs">
                      {{ rec.alerta }}
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      }
    </div>
  `,
})
export class Calculadora implements OnInit, OnDestroy {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly analisisApi = inject(AnalisisApi);
  private readonly notify = inject(NotificationService);

  private subscripcionCambios?: Subscription;

  protected readonly calculando = signal(false);
  protected readonly resultado = signal<AnalisisResponse | null>(null);
  protected readonly obtenerClasificacion = obtenerClasificacionInfo;

  protected readonly form = this.fb.group({
    demanda: [1000, [Validators.required, Validators.min(0)]],
    recursos: this.fb.array<FormGroup>([
      this.crearGrupoRecurso('Corte', 0.05, 2, 8, 20, 2),
      this.crearGrupoRecurso('Soldadura', 0.12, 2, 8, 20, 2),
      this.crearGrupoRecurso('Pintura', 0.08, 1, 8, 20, 1),
      this.crearGrupoRecurso('Ensamble', 0.04, 2, 8, 20, 2),
    ]),
  });

  ngOnInit(): void {
    this.subscripcionCambios = this.form.valueChanges
      .pipe(
        debounceTime(350),
        filter(() => this.form.valid && !this.calculando()),
      )
      .subscribe(() => {
        this.ejecutarCalculo(false);
      });
  }

  ngOnDestroy(): void {
    this.subscripcionCambios?.unsubscribe();
  }

  get recursosArray(): FormArray<FormGroup> {
    return this.form.controls.recursos;
  }

  crearGrupoRecurso(
    nombre = '',
    tiempoProcesamiento = 0.1,
    turnos = 1,
    horasPorTurno = 8,
    diasOperacion = 20,
    cantidadRecursos = 1,
  ): FormGroup {
    return this.fb.group({
      nombre: [nombre, [Validators.required]],
      tiempoProcesamiento: [tiempoProcesamiento, [Validators.required, Validators.min(0.0001)]],
      turnos: [turnos, [Validators.required, Validators.min(1)]],
      horasPorTurno: [horasPorTurno, [Validators.required, Validators.min(0.1)]],
      diasOperacion: [diasOperacion, [Validators.required, Validators.min(1)]],
      cantidadRecursos: [cantidadRecursos, [Validators.required, Validators.min(1)]],
    });
  }

  agregarRecurso(): void {
    this.recursosArray.push(this.crearGrupoRecurso());
  }

  eliminarRecurso(index: number): void {
    if (this.recursosArray.length > 1) {
      this.recursosArray.removeAt(index);
    }
  }

  cargarEjemplo(): void {
    this.form.patchValue({ demanda: 2500 });
    this.recursosArray.clear();
    this.recursosArray.push(this.crearGrupoRecurso('Corte Láser', 0.06, 2, 8, 22, 1));
    this.recursosArray.push(this.crearGrupoRecurso('Plegado CNC', 0.15, 2, 8, 22, 1));
    this.recursosArray.push(this.crearGrupoRecurso('Soldadura MIG', 0.22, 2, 8, 22, 2));
    this.recursosArray.push(this.crearGrupoRecurso('Cabina Pintura', 0.18, 1, 8, 22, 1));
    this.recursosArray.push(this.crearGrupoRecurso('Ensamble y Empaque', 0.08, 2, 8, 22, 2));
    this.notify.info('Ejemplo de línea de producción cargado.');
    this.calcular();
  }

  limpiar(): void {
    this.form.reset({ demanda: 0 });
    this.recursosArray.clear();
    this.recursosArray.push(this.crearGrupoRecurso());
    this.resultado.set(null);
  }

  calcular(): void {
    this.ejecutarCalculo(true);
  }

  private ejecutarCalculo(notificar: boolean): void {
    if (this.form.invalid) {
      if (notificar) {
        this.form.markAllAsTouched();
      }
      return;
    }
    const val = this.form.getRawValue();
    const data: { demanda: number; recursos: RecursoCalculoRequest[] } = {
      demanda: Number(val.demanda),
      recursos: val.recursos.map((r: any) => ({
        nombre: String(r.nombre),
        tiempoProcesamiento: Number(r.tiempoProcesamiento),
        turnos: Number(r.turnos),
        horasPorTurno: Number(r.horasPorTurno),
        diasOperacion: Number(r.diasOperacion),
        cantidadRecursos: Number(r.cantidadRecursos),
      })),
    };

    this.calculando.set(true);
    this.analisisApi.calcular(data).subscribe({
      next: (res) => {
        this.resultado.set(res);
        this.calculando.set(false);
        if (notificar) {
          const cuellos = this.contarCuellos(res.recursos);
          if (cuellos > 0) {
            this.notify.advertencia(`Se detectaron ${cuellos} cuellos de botella en la simulación.`);
          } else {
            this.notify.exito('Capacidad calculada con éxito. No hay restricciones activas.');
          }
        }
      },
      error: (err) => {
        this.calculando.set(false);
        if (notificar) {
          this.notify.error(err.error?.detail || 'No se pudo realizar el cálculo de capacidad.');
        }
      },
    });
  }

  contarCuellos(recursos: RecursoCalculoResultado[]): number {
    return recursos.filter((r) => r.clasificacion === 'SOBRECARGA').length;
  }

  calcularAnchoBarra(utilizacion: number): number {
    return Math.min(Math.max(utilizacion, 0), 100);
  }
}
