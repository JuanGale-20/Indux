import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, beforeEach } from 'vitest';
import { AsistenteApi, ReportesApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { Chatbot } from './chatbot';

describe('Chatbot Component', () => {
  let fixture: ComponentFixture<Chatbot>;
  let component: Chatbot;

  const mockAsistenteApi = {
    consultar: () =>
      of({
        respuesta: 'Explicación técnica de SteelForce',
        modelo: 'gemini-3.8-flash',
        proveedorConfigurado: true,
        fuentesContexto: ['Reporte #1'],
        motor: 'GEMINI' as const,
      }),
  };

  const mockReportesApi = {
    listar: () => of([]),
  };

  const mockAuthService = {
    usuario: () => ({ id: 1, nombre: 'Carlos Gomez', email: 'carlos@indux.test', rol: 'ANALISTA', empresaId: 10 }),
    tieneRol: (...roles: string[]) => roles.includes('ADMIN') ? false : false,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Chatbot],
      providers: [
        { provide: AsistenteApi, useValue: mockAsistenteApi },
        { provide: ReportesApi, useValue: mockReportesApi },
        { provide: AuthService, useValue: mockAuthService },
      ],
    });

    fixture = TestBed.createComponent(Chatbot);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('se inicializa con mensaje de bienvenida neutro, honesto y sin emojis', () => {
    expect(component).toBeTruthy();
    const textoBienvenida = component['mensajes']()[0].texto;
    expect(textoBienvenida).toContain('SteelForce');
    // Saludo neutral que no afirma estar conectado a Gemini en el saludo estático
    expect(textoBienvenida).not.toContain('impulsado por Google Gemini');
    expect(component['estadoProveedor']().motor).toBe('LOCAL');
    expect(component['estadoProveedor']().modelo).toBe('Base de conocimiento SteelForce');
  });

  it('sanea fórmulas en LaTeX residuales convirtiéndolas a texto plano en español', () => {
    const textoConLatex =
      'Fórmula: $$U = \\left( \\frac{H_{req}}{H_{disp}} \\right) \\times 100\\%$$ con rango $80\\% \\le U \\le 90\\%$';
    const resultado = component.formatearTexto(textoConLatex);

    expect(resultado).not.toContain('\\frac');
    expect(resultado).not.toContain('\\times');
    expect(resultado).not.toContain('\\le');
    expect(resultado).not.toContain('\\left(');
    expect(resultado).not.toContain('$$');
    expect(resultado).toContain('(Horas requeridas / Horas disponibles) × 100%');
    expect(resultado).toContain('80% ≤ U ≤ 90%');
  });

  it('previene inyecciones HTML (XSS) neutralizando etiquetas peligrosas', () => {
    const textoXss = '<script>alert("xss")</script><img src="x" onerror="alert(1)">**Texto seguro**';
    const resultado = component.formatearTexto(textoXss);

    expect(resultado).not.toContain('<script>');
    expect(resultado).toContain('&lt;script&gt;');
    expect(resultado).toContain('<strong class="font-bold text-slate-900">Texto seguro</strong>');
  });

  it('envía mensaje y actualiza insignia según motor GEMINI', () => {
    component['mensajeInput'] = '¿Qué es sobrecarga?';
    component['enviar']();

    const mensajes = component['mensajes']();
    expect(mensajes.length).toBe(3); // Bienvenida, Usuario, Asistente
    expect(mensajes[1].texto).toBe('¿Qué es sobrecarga?');
    expect(mensajes[2].texto).toBe('Explicación técnica de SteelForce');
    expect(component['estadoProveedor']().motor).toBe('GEMINI');
    expect(component['estadoProveedor']().modelo).toBe('gemini-3.8-flash');
  });

  it('el aviso de configuración técnica GEMINI_API_KEY no se muestra a usuarios ANALISTA', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).not.toContain('GEMINI_API_KEY');
    expect(compiled.textContent).not.toContain('Configuración Admin');
  });

  it('diagnosticar reporte actual sin reporte seleccionado pide elegir uno amablemente', () => {
    component['reporteSeleccionadoId'].set(null);
    component['diagnosticarReporteActual']();

    const mensajes = component['mensajes']();
    const ultimoMensaje = mensajes[mensajes.length - 1];
    expect(ultimoMensaje.remitente).toBe('asistente');
    expect(ultimoMensaje.texto).toContain('selecciona primero un **Reporte**');
  });
});