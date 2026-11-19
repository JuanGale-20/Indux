import { describe, expect, it, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { autenticadoGuard, rolGuard } from './guards';
import { AuthService } from './auth.service';

describe('Route Guards', () => {
  let authServiceMock: {
    autenticado: () => boolean;
    tieneRol: (...roles: string[]) => boolean;
  };
  let routerMock: {
    createUrlTree: (commands: string[]) => any;
  };

  beforeEach(() => {
    authServiceMock = {
      autenticado: vi.fn(),
      tieneRol: vi.fn(),
    };
    routerMock = {
      createUrlTree: vi.fn((commands) => ({ path: commands.join('/') })),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  describe('autenticadoGuard', () => {
    it('permite navegación si el usuario está autenticado', () => {
      authServiceMock.autenticado = () => true;
      const res = TestBed.runInInjectionContext(() => autenticadoGuard({} as any, {} as any));
      expect(res).toBe(true);
    });

    it('redirige a /login si no está autenticado', () => {
      authServiceMock.autenticado = () => false;
      const res = TestBed.runInInjectionContext(() => autenticadoGuard({} as any, {} as any)) as any;
      expect(res.path).toBe('/login');
    });
  });

  describe('rolGuard', () => {
    it('redirige a /login si no está autenticado', () => {
      authServiceMock.autenticado = () => false;
      const guard = rolGuard('ADMIN');
      const res = TestBed.runInInjectionContext(() => guard({} as any, {} as any)) as any;
      expect(res.path).toBe('/login');
    });

    it('permite navegación si el usuario tiene el rol requerido', () => {
      authServiceMock.autenticado = () => true;
      authServiceMock.tieneRol = (...roles: string[]) => roles.includes('ADMIN');

      const guard = rolGuard('ADMIN');
      const res = TestBed.runInInjectionContext(() => guard({} as any, {} as any));
      expect(res).toBe(true);
    });

    it('redirige a /sin-permiso si el rol no coincide', () => {
      authServiceMock.autenticado = () => true;
      authServiceMock.tieneRol = () => false;

      const guard = rolGuard('ADMIN');
      const res = TestBed.runInInjectionContext(() => guard({} as any, {} as any)) as any;
      expect(res.path).toBe('/sin-permiso');
    });
  });
});
