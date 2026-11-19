import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { NotificationService } from '../core/notification.service';
import { Rol } from '../core/models';
import { AppIconComponent, IconName } from '../core/icon.component';

interface Enlace {
  ruta: string;
  texto: string;
  icono: IconName;
  roles: Rol[];
  seccion?: string;
}

@Component({
  selector: 'app-app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, AppIconComponent],
  template: `
    <div class="min-h-screen flex flex-col lg:flex-row bg-slate-50 font-sans text-slate-800">
      <!-- Barra superior móvil (visible solo en pantallas < lg) -->
      <header class="lg:hidden bg-emerald-950 text-white px-4 py-3 flex items-center justify-between border-b border-emerald-900 sticky top-0 z-40">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-lg bg-emerald-600 text-white font-extrabold flex items-center justify-center text-sm shadow-sm">
            Ix
          </div>
          <div>
            <span class="text-base font-black tracking-tight leading-none block">Indux</span>
            <span class="text-[9px] uppercase tracking-wider text-emerald-400 font-semibold">SteelForce v2</span>
          </div>
        </div>
        <button
          type="button"
          (click)="toggleMenuMovil()"
          class="p-2 rounded-lg bg-emerald-900 text-emerald-200 hover:text-white hover:bg-emerald-800 transition"
          aria-label="Abrir menú"
        >
          <app-icon [name]="menuMovilAbierto() ? 'x' : 'menu'" [size]="20" />
        </button>
      </header>

      <!-- Overlay oscuro para móvil -->
      @if (menuMovilAbierto()) {
        <div
          (click)="cerrarMenuMovil()"
          class="fixed inset-0 bg-slate-900/60 backdrop-blur-xs z-40 lg:hidden animate-fade-in"
        ></div>
      }

      <!-- Barra lateral (Sidebar escritorio y drawer móvil) -->
      <aside
        class="fixed inset-y-0 left-0 z-50 w-64 bg-emerald-950 text-slate-200 flex flex-col border-r border-emerald-900 shrink-0 transform transition-transform duration-300 ease-in-out lg:static lg:translate-x-0"
        [class.-translate-x-full]="!menuMovilAbierto()"
        [class.translate-x-0]="menuMovilAbierto()"
      >
        <!-- Logo y cabecera de la marca -->
        <div class="px-6 py-5 border-b border-emerald-900/80 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-xl bg-emerald-600 text-white font-extrabold flex items-center justify-center text-base shadow-sm">
              Ix
            </div>
            <div>
              <a routerLink="/app" (click)="cerrarMenuMovil()" class="text-lg font-black tracking-tight text-white block leading-tight">Indux</a>
              <span class="text-[10px] uppercase tracking-wider text-emerald-400 font-semibold">SteelForce v2</span>
            </div>
          </div>
          <button
            type="button"
            (click)="cerrarMenuMovil()"
            class="lg:hidden text-emerald-400 hover:text-white p-1"
            aria-label="Cerrar menú"
          >
            <app-icon name="x" [size]="18" />
          </button>
        </div>

        <!-- Navegación -->
        <nav class="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          @for (e of enlaces(); track e.ruta) {
            @if (e.seccion) {
              <div class="pt-4 pb-1 px-3 text-[10px] font-bold uppercase tracking-wider text-emerald-400/80">
                {{ e.seccion }}
              </div>
            }
            <a
              [routerLink]="e.ruta"
              (click)="cerrarMenuMovil()"
              routerLinkActive="bg-emerald-800 text-white font-semibold shadow-sm"
              [routerLinkActiveOptions]="{ exact: e.ruta === '/app' }"
              class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-xs text-slate-200 hover:bg-emerald-900/60 transition group"
            >
              <app-icon [name]="e.icono" [size]="17" class="text-emerald-400 group-hover:text-emerald-200 transition" />
              <span>{{ e.texto }}</span>
            </a>
          }
        </nav>

        <!-- Perfil del usuario activo -->
        <div class="border-t border-emerald-900/80 p-4 bg-emerald-950/60">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-full bg-emerald-800 text-emerald-200 flex items-center justify-center font-bold text-xs uppercase shadow-xs">
              {{ iniciales() }}
            </div>
            <div class="overflow-hidden flex-1">
              <p class="font-semibold text-white text-xs truncate">{{ auth.usuario()?.nombre }}</p>
              <div class="flex items-center gap-1.5 mt-0.5">
                <span class="inline-block px-1.5 py-0.5 text-[10px] font-bold uppercase rounded"
                  [class]="rolBadgeClass()">
                  {{ auth.usuario()?.rol }}
                </span>
                @if (auth.usuario()?.empresaId; as empId) {
                  <span class="text-[10px] text-emerald-400/80 truncate">Empresa #{{ empId }}</span>
                }
              </div>
            </div>
          </div>
          <button
            (click)="auth.logout()"
            class="mt-3 w-full flex items-center justify-center gap-2 rounded-lg border border-emerald-800/80 bg-emerald-900/40 px-3 py-2 text-xs text-emerald-200 hover:bg-emerald-900 hover:text-white transition cursor-pointer"
          >
            <app-icon name="log-out" [size]="14" />
            <span>Cerrar sesión</span>
          </button>
        </div>
      </aside>

      <!-- Contenedor principal -->
      <div class="flex-1 flex flex-col min-w-0">
        <!-- Notificaciones flotantes globales -->
        <div class="fixed top-4 right-4 z-50 flex flex-col gap-2 max-w-sm w-full pointer-events-none">
          @for (n of notify.notificaciones(); track n.id) {
            <div
              class="pointer-events-auto rounded-xl p-4 shadow-lg border text-xs flex items-start gap-3 transition-all animate-fade-in"
              [class]="notifClass(n.tipo)"
            >
              <div class="shrink-0 mt-0.5">
                @switch (n.tipo) {
                  @case ('exito') {
                    <app-icon name="check-circle" [size]="17" class="text-emerald-700" />
                  }
                  @case ('error') {
                    <app-icon name="alert-circle" [size]="17" class="text-rose-700" />
                  }
                  @case ('advertencia') {
                    <app-icon name="alert-triangle" [size]="17" class="text-amber-700" />
                  }
                  @default {
                    <app-icon name="info" [size]="17" class="text-sky-700" />
                  }
                }
              </div>
              <div class="flex-1">
                @if (n.titulo) {
                  <p class="font-bold mb-0.5">{{ n.titulo }}</p>
                }
                <p>{{ n.mensaje }}</p>
              </div>
              <button
                (click)="notify.remover(n.id)"
                class="text-slate-400 hover:text-slate-600 shrink-0 p-1 rounded-md hover:bg-black/5 transition"
                aria-label="Cerrar notificación"
              >
                <app-icon name="x" [size]="14" />
              </button>
            </div>
          }
        </div>

        <main class="flex-1 p-4 sm:p-6 md:p-8 max-w-7xl w-full mx-auto animate-fade-in">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class AppLayout {
  protected readonly auth = inject(AuthService);
  protected readonly notify = inject(NotificationService);

  protected readonly menuMovilAbierto = signal<boolean>(false);

  private readonly todos: Enlace[] = [
    { ruta: '/app', texto: 'Panel Resumen', icono: 'layout-dashboard', roles: ['ADMIN', 'LIDER', 'ANALISTA'] },

    // Operación de reportes y capacidad
    { ruta: '/app/reportes', texto: 'Reportes de Capacidad', icono: 'file-text', roles: ['ADMIN', 'LIDER', 'ANALISTA'], seccion: 'Análisis SteelForce' },
    { ruta: '/app/calculadora', texto: 'Calculadora al Vuelo', icono: 'calculator', roles: ['ADMIN', 'LIDER', 'ANALISTA'] },
    { ruta: '/app/importar', texto: 'Importar Excel / CSV', icono: 'upload', roles: ['ADMIN', 'ANALISTA'] },
    { ruta: '/app/historico', texto: 'Tendencia e Histórico', icono: 'chart-no-axes-combined', roles: ['ADMIN', 'LIDER', 'ANALISTA'] },

    // Catálogo industrial
    { ruta: '/app/empresas', texto: 'Empresas', icono: 'building-2', roles: ['ADMIN'], seccion: 'Catálogo Industrial' },
    { ruta: '/app/usuarios', texto: 'Usuarios', icono: 'users', roles: ['ADMIN'] },
    { ruta: '/app/productos', texto: 'Productos', icono: 'package', roles: ['ADMIN', 'LIDER', 'ANALISTA'], seccion: 'Estructura Productiva' },
    { ruta: '/app/procesos', texto: 'Procesos', icono: 'workflow', roles: ['ADMIN', 'LIDER', 'ANALISTA'] },
    { ruta: '/app/recursos', texto: 'Recursos y Vínculos', icono: 'factory', roles: ['ADMIN', 'LIDER', 'ANALISTA'] },

    // Asistente IA
    { ruta: '/app/chatbot', texto: 'Asistente IA SteelForce', icono: 'brain-circuit', roles: ['ADMIN', 'LIDER', 'ANALISTA'], seccion: 'Innovación' },
  ];

  protected readonly enlaces = computed(() =>
    this.todos.filter((e) => this.auth.tieneRol(...e.roles)),
  );

  protected readonly iniciales = computed(() => {
    const nombre = this.auth.usuario()?.nombre || 'Usuario';
    return nombre
      .split(' ')
      .map((p) => p[0])
      .slice(0, 2)
      .join('');
  });

  protected toggleMenuMovil(): void {
    this.menuMovilAbierto.update((v) => !v);
  }

  protected cerrarMenuMovil(): void {
    this.menuMovilAbierto.set(false);
  }

  protected rolBadgeClass(): string {
    const rol = this.auth.usuario()?.rol;
    switch (rol) {
      case 'ADMIN':
        return 'bg-purple-900/70 text-purple-200 border border-purple-700/50';
      case 'ANALISTA':
        return 'bg-emerald-900/70 text-emerald-200 border border-emerald-700/50';
      case 'LIDER':
        return 'bg-blue-900/70 text-blue-200 border border-blue-700/50';
      default:
        return 'bg-slate-800 text-slate-300';
    }
  }

  protected notifClass(tipo: string): string {
    switch (tipo) {
      case 'exito':
        return 'bg-emerald-50 text-emerald-900 border-emerald-200';
      case 'error':
        return 'bg-rose-50 text-rose-900 border-rose-200';
      case 'advertencia':
        return 'bg-amber-50 text-amber-900 border-amber-200';
      default:
        return 'bg-sky-50 text-sky-900 border-sky-200';
    }
  }
}
