import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-public-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="bg-white border-b border-slate-200 sticky top-0 z-40">
      <nav class="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
        <a routerLink="/" class="flex items-center gap-2">
          <span class="w-8 h-8 rounded-lg bg-emerald-800 text-white font-extrabold flex items-center justify-center text-sm shadow-sm">
            Ix
          </span>
          <span class="text-xl font-black tracking-tight text-emerald-950">Indux</span>
        </a>

        <div class="flex items-center gap-6 text-sm font-medium text-slate-600">
          <a
            routerLink="/"
            routerLinkActive="text-emerald-800 font-semibold"
            [routerLinkActiveOptions]="{ exact: true }"
            class="hover:text-emerald-800 transition"
            >Inicio</a
          >
          <a
            routerLink="/planes"
            routerLinkActive="text-emerald-800 font-semibold"
            class="hover:text-emerald-800 transition"
            >Membresías</a
          >
          @if (auth.autenticado()) {
            <a
              routerLink="/app"
              class="rounded-xl bg-emerald-700 px-4 py-2 text-white hover:bg-emerald-800 transition shadow-sm font-semibold"
              >Ir a la plataforma</a
            >
          } @else {
            <a
              routerLink="/login"
              class="rounded-xl bg-emerald-700 px-4 py-2 text-white hover:bg-emerald-800 transition shadow-sm font-semibold"
              >Iniciar sesión</a
            >
          }
        </div>
      </nav>
    </header>

    <main class="min-h-[calc(100vh-8rem)]">
      <router-outlet />
    </main>

    <footer class="border-t border-slate-200 bg-white py-8 text-center text-xs text-slate-500">
      <div class="max-w-6xl mx-auto px-4">
        <p class="font-medium text-slate-700">Indux Plataforma · Proyecto Universitario ODS 9</p>
        <p class="mt-1">Metodología SteelForce para detección y diagnóstico de cuellos de botella industriales.</p>
      </div>
    </footer>
  `,
})
export class PublicLayout {
  protected readonly auth = inject(AuthService);
}
