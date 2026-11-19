import { Routes } from '@angular/router';
import { autenticadoGuard, rolGuard } from './core/guards';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layouts/public-layout').then((m) => m.PublicLayout),
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () => import('./pages/inicio').then((m) => m.Inicio),
      },
      {
        path: 'planes',
        loadComponent: () => import('./pages/planes').then((m) => m.Planes),
      },
      {
        path: 'compra/:plan',
        loadComponent: () => import('./pages/compra').then((m) => m.Compra),
      },
      {
        path: 'login',
        loadComponent: () => import('./pages/login').then((m) => m.Login),
      },
      {
        path: 'sin-permiso',
        loadComponent: () => import('./pages/errores').then((m) => m.SinPermiso),
      },
    ],
  },
  {
    path: 'app',
    canActivate: [autenticadoGuard],
    loadComponent: () => import('./layouts/app-layout').then((m) => m.AppLayout),
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/panel').then((m) => m.Panel),
      },
      {
        path: 'calculadora',
        canActivate: [rolGuard('ADMIN', 'LIDER', 'ANALISTA')],
        loadComponent: () => import('./pages/calculadora').then((m) => m.Calculadora),
      },
      {
        path: 'empresas',
        canActivate: [rolGuard('ADMIN')],
        loadComponent: () => import('./pages/empresas').then((m) => m.Empresas),
      },
      {
        path: 'usuarios',
        canActivate: [rolGuard('ADMIN')],
        loadComponent: () => import('./pages/usuarios').then((m) => m.Usuarios),
      },
      {
        path: 'productos',
        canActivate: [rolGuard('ADMIN', 'LIDER', 'ANALISTA')],
        loadComponent: () => import('./pages/productos').then((m) => m.Productos),
      },
      {
        path: 'procesos',
        canActivate: [rolGuard('ADMIN', 'LIDER', 'ANALISTA')],
        loadComponent: () => import('./pages/procesos').then((m) => m.Procesos),
      },
      {
        path: 'recursos',
        canActivate: [rolGuard('ADMIN', 'LIDER', 'ANALISTA')],
        loadComponent: () => import('./pages/recursos').then((m) => m.Recursos),
      },
      {
        path: 'reportes',
        canActivate: [rolGuard('ADMIN', 'LIDER', 'ANALISTA')],
        loadComponent: () => import('./pages/reportes').then((m) => m.Reportes),
      },
      {
        path: 'reportes/nuevo',
        canActivate: [rolGuard('ADMIN', 'ANALISTA')],
        loadComponent: () => import('./pages/reporte-form').then((m) => m.ReporteForm),
      },
      {
        path: 'reportes/editar/:id',
        canActivate: [rolGuard('ADMIN', 'ANALISTA')],
        loadComponent: () => import('./pages/reporte-form').then((m) => m.ReporteForm),
      },
      {
        path: 'reportes/:id',
        canActivate: [rolGuard('ADMIN', 'LIDER', 'ANALISTA')],
        loadComponent: () => import('./pages/reporte-detalle').then((m) => m.ReporteDetalle),
      },
      {
        path: 'importar',
        canActivate: [rolGuard('ADMIN', 'ANALISTA')],
        loadComponent: () => import('./pages/importar').then((m) => m.Importar),
      },
      {
        path: 'historico',
        canActivate: [rolGuard('ADMIN', 'LIDER', 'ANALISTA')],
        loadComponent: () => import('./pages/historico').then((m) => m.Historico),
      },
      {
        path: 'validaciones',
        canActivate: [rolGuard('ADMIN', 'LIDER', 'ANALISTA')],
        loadComponent: () => import('./pages/validaciones').then((m) => m.Validaciones),
      },
      {
        path: 'chatbot',
        canActivate: [rolGuard('ADMIN', 'LIDER', 'ANALISTA')],
        loadComponent: () => import('./pages/chatbot').then((m) => m.Chatbot),
      },
    ],
  },
  {
    path: '**',
    loadComponent: () => import('./pages/errores').then((m) => m.NoEncontrado),
  },
];
