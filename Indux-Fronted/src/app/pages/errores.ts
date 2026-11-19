import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-sin-permiso',
  imports: [RouterLink],
  template: `
    <section class="max-w-md mx-auto px-4 py-24 text-center">
      <h1 class="text-3xl font-bold text-slate-900">Sin permiso</h1>
      <p class="mt-3 text-slate-600">Tu rol no permite entrar a esta página.</p>
      <a routerLink="/app" class="mt-6 inline-block text-blue-600 underline">Volver al panel</a>
    </section>
  `,
})
export class SinPermiso {}

@Component({
  selector: 'app-no-encontrado',
  imports: [RouterLink],
  template: `
    <section class="max-w-md mx-auto px-4 py-24 text-center">
      <h1 class="text-3xl font-bold text-slate-900">Página no encontrada</h1>
      <p class="mt-3 text-slate-600">La dirección que escribiste no existe.</p>
      <a routerLink="/" class="mt-6 inline-block text-blue-600 underline">Ir al inicio</a>
    </section>
  `,
})
export class NoEncontrado {}