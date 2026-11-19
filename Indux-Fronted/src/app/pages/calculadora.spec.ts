import { describe, expect, it, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Calculadora } from './calculadora';
import { AnalisisApi } from '../core/api';
import { NotificationService } from '../core/notification.service';
import { of } from 'rxjs';

describe('Calculadora Component', () => {
  let component: Calculadora;
  let analisisApiMock: { calcular: any };
  let notifyMock: { info: any; exito: any; advertencia: any; error: any };

  beforeEach(() => {
    analisisApiMock = {
      calcular: vi.fn().mockReturnValue(of({ recursos: [] })),
    };
    notifyMock = {
      info: vi.fn(),
      exito: vi.fn(),
      advertencia: vi.fn(),
      error: vi.fn(),
    };

    TestBed.configureTestingModule({
      imports: [Calculadora],
      providers: [
        { provide: AnalisisApi, useValue: analisisApiMock },
        { provide: NotificationService, useValue: notifyMock },
      ],
    });

    const fixture = TestBed.createComponent(Calculadora);
    component = fixture.componentInstance;
  });

  it('se inicializa con valores predeterminados y 4 estaciones', () => {
    expect(component).toBeTruthy();
    expect(component['form'].value.demanda).toBe(1000);
    expect(component.recursosArray.length).toBe(4);
  });

  it('permite agregar y remover estaciones', () => {
    component.agregarRecurso();
    expect(component.recursosArray.length).toBe(5);

    component.eliminarRecurso(0);
    expect(component.recursosArray.length).toBe(4);
  });

  it('carga ejemplo industrial con 5 estaciones y demanda 2500', () => {
    component.cargarEjemplo();
    expect(component['form'].value.demanda).toBe(2500);
    expect(component.recursosArray.length).toBe(5);
    expect(notifyMock.info).toHaveBeenCalled();
  });

  it('limpia el formulario adecuadamente', () => {
    component.limpiar();
    expect(component['form'].value.demanda).toBe(0);
    expect(component.recursosArray.length).toBe(1);
    expect(component['resultado']()).toBeNull();
  });

  it('cuenta correctamente los cuellos de botella (SOBRECARGA)', () => {
    const recursos: any[] = [
      { clasificacion: 'SOBRECARGA' },
      { clasificacion: 'UTILIZACION_ADECUADA' },
      { clasificacion: 'SOBRECARGA' },
    ];
    expect(component.contarCuellos(recursos)).toBe(2);
  });

  it('limita el ancho de la barra de utilización entre 0 y 100', () => {
    expect(component.calcularAnchoBarra(125)).toBe(100);
    expect(component.calcularAnchoBarra(75)).toBe(75);
    expect(component.calcularAnchoBarra(-10)).toBe(0);
  });
});
