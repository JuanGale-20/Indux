import { Component, ElementRef, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AsistenteApi, ReportesApi } from '../core/api';
import { AuthService } from '../core/auth.service';
import { MensajeHistorial, ReporteResumen, RespuestaAsistenteResponse } from '../core/models';
import { AppIconComponent } from '../core/icon.component';

interface MensajeChat {
  id: string;
  remitente: 'usuario' | 'asistente';
  texto: string;
  hora: string;
  modelo?: string;
  fuentes?: string[];
  esError?: boolean;
}

@Component({
  selector: 'app-chatbot',
  imports: [CommonModule, FormsModule, AppIconComponent],
  template: `
    <div class="max-w-5xl mx-auto space-y-4 h-[calc(100vh-130px)] flex flex-col animate-fade-in">
      <!-- Cabecera Ejecutiva del Asistente IA -->
      <div class="bg-white p-4 sm:p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 shrink-0">
        <div class="flex items-center gap-3.5">
          <div class="w-11 h-11 rounded-2xl bg-emerald-950 text-emerald-400 flex items-center justify-center shadow-xs border border-emerald-800 shrink-0">
            <app-icon name="brain-circuit" [size]="22" />
          </div>
          <div>
            <div class="flex items-center gap-2 flex-wrap">
              <h1 class="text-base font-extrabold text-slate-900 tracking-tight">Asistente Inteligente SteelForce</h1>
              
              <!-- Insignia Honesta del Motor Activo -->
              <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold border transition-colors"
                [class]="estadoProveedor().motor === 'GEMINI' ? 'bg-emerald-50 text-emerald-800 border-emerald-200' : 'bg-slate-100 text-slate-700 border-slate-200'">
                <span class="w-1.5 h-1.5 rounded-full"
                  [class]="estadoProveedor().motor === 'GEMINI' ? 'bg-emerald-600' : 'bg-slate-400'"></span>
                {{ estadoProveedor().motor === 'GEMINI' ? ('Gemini · ' + estadoProveedor().modelo) : 'Base de conocimiento SteelForce' }}
              </span>
            </div>
            <p class="text-xs text-slate-500 mt-0.5">
              Diagnóstico de cuellos de botella, interpretación de restricciones y orientación operativa industrial.
            </p>
          </div>
        </div>

        <div class="flex items-center gap-2 self-end sm:self-center w-full sm:w-auto justify-between sm:justify-end">
          <!-- Selector de Contexto de Reporte -->
          <div class="flex items-center gap-1.5">
            <label for="reporteSelect" class="text-[11px] font-bold text-slate-500 uppercase tracking-wider hidden md:inline">
              Contexto:
            </label>
            <select
              id="reporteSelect"
              [ngModel]="reporteSeleccionadoId()"
              (ngModelChange)="cambiarReporteContextual($event)"
              class="rounded-xl border border-slate-300 bg-white px-2.5 py-1.5 text-xs text-slate-800 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600 font-medium"
            >
              <option [ngValue]="null">General (Toda la Empresa)</option>
              @for (r of reportesDisponibles(); track r.id) {
                <option [ngValue]="r.id">Reporte #{{ r.id }} - {{ r.plantaArea }} ({{ r.estado }})</option>
              }
            </select>
          </div>

          <button
            type="button"
            (click)="reiniciarChat()"
            class="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900 px-3 py-1.5 rounded-xl border border-slate-200 hover:bg-slate-50 transition cursor-pointer shrink-0"
            title="Iniciar nueva sesión de conversación"
          >
            <app-icon name="refresh-cw" [size]="13" />
            <span>Limpiar</span>
          </button>
        </div>
      </div>

      <!-- Aviso discreto de configuración visible EXCLUSIVAMENTE para el ADMIN cuando el motor es LOCAL -->
      @if (auth.tieneRol('ADMIN') && estadoProveedor().motor === 'LOCAL') {
        <div class="px-3.5 py-2 bg-slate-50 border border-slate-200/80 rounded-xl text-xs text-slate-600 flex items-center justify-between gap-3 shrink-0 animate-fade-in shadow-2xs">
          <div class="flex items-center gap-2">
            <app-icon name="settings" [size]="14" class="text-slate-500 shrink-0" />
            <span>Modo local activo. Para habilitar Google Gemini, configura <code>GEMINI_API_KEY</code> en las variables de entorno del backend.</span>
          </div>
          <span class="text-[10px] font-bold text-slate-500 bg-slate-200/60 px-2 py-0.5 rounded-md uppercase tracking-wider shrink-0 hidden sm:inline">Configuración Admin</span>
        </div>
      }

      <!-- Ventana Principal de Mensajes -->
      <div #chatContainer class="flex-1 overflow-y-auto bg-slate-50/70 p-4 sm:p-5 rounded-2xl border border-slate-200 space-y-4">
        @for (m of mensajes(); track m.id) {
          <div
            class="flex gap-3 animate-fade-in"
            [class]="m.remitente === 'usuario' ? 'justify-end' : 'justify-start'"
          >
            @if (m.remitente === 'asistente') {
              <div class="w-8 h-8 rounded-xl bg-emerald-950 text-emerald-300 border border-emerald-800 flex items-center justify-center shrink-0 shadow-2xs mt-0.5">
                <app-icon name="brain-circuit" [size]="16" />
              </div>
            }

            <div
              class="max-w-2xl rounded-2xl p-4 text-xs shadow-xs space-y-2 leading-relaxed"
              [class]="m.remitente === 'usuario'
                ? 'bg-emerald-800 text-white rounded-br-xs'
                : (m.esError ? 'bg-rose-50 text-rose-900 border border-rose-200 rounded-bl-xs' : 'bg-white text-slate-800 border border-slate-200 rounded-bl-xs')"
            >
              <div class="whitespace-pre-line" [innerHTML]="formatearTexto(m.texto)"></div>

              <!-- Fuentes de contexto utilizadas -->
              @if (m.fuentes && m.fuentes.length > 0) {
                <div class="pt-2 border-t border-slate-100 flex items-center gap-1.5 flex-wrap">
                  <span class="text-[10px] font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1">
                    <app-icon name="layers" [size]="11" />
                    <span>Datos Reales:</span>
                  </span>
                  @for (f of m.fuentes; track f) {
                    <span class="px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-800 border border-emerald-200 text-[10px] font-mono font-semibold">
                      {{ f }}
                    </span>
                  }
                </div>
              }

              <div
                class="text-[10px] text-right flex items-center justify-end gap-2 pt-1"
                [class]="m.remitente === 'usuario' ? 'text-emerald-200' : 'text-slate-400'"
              >
                @if (m.modelo) {
                  <span class="font-mono text-[9px] opacity-80">{{ m.modelo }}</span>
                }
                <span>{{ m.hora }}</span>
              </div>
            </div>

            @if (m.remitente === 'usuario') {
              <div class="w-8 h-8 rounded-xl bg-emerald-700 text-white flex items-center justify-center text-xs font-bold uppercase shrink-0 shadow-2xs mt-0.5">
                {{ iniciales() }}
              </div>
            }
          </div>
        }

        <!-- Indicador de generación activa con micro-animación -->
        @if (cargando()) {
          <div class="flex gap-3 justify-start animate-fade-in">
            <div class="w-8 h-8 rounded-xl bg-emerald-950 text-emerald-300 border border-emerald-800 flex items-center justify-center shrink-0 shadow-2xs">
              <app-icon name="brain-circuit" [size]="16" />
            </div>
            <div class="bg-white border border-slate-200 rounded-2xl rounded-bl-xs p-4 shadow-xs flex items-center gap-3">
              <div class="flex items-center gap-1.5">
                <span class="w-2 h-2 rounded-full bg-emerald-600 animate-bounce"></span>
                <span class="w-2 h-2 rounded-full bg-emerald-600 animate-bounce [animation-delay:0.18s]"></span>
                <span class="w-2 h-2 rounded-full bg-emerald-600 animate-bounce [animation-delay:0.36s]"></span>
              </div>
              <span class="text-xs text-slate-500 font-medium">Analizando variables y datos de capacidad SteelForce…</span>
            </div>
          </div>
        }
      </div>

      <!-- Preguntas Frecuentes Sugeridas -->
      <div class="flex items-center gap-2 overflow-x-auto pb-1 shrink-0 text-xs">
        <span class="font-bold text-[10px] uppercase tracking-wider text-slate-400 shrink-0 flex items-center gap-1">
          <app-icon name="sparkles" [size]="12" class="text-emerald-600" />
          <span>Sugerencias:</span>
        </span>
        <button
          type="button"
          (click)="enviarPreguntaRapida('¿Por qué un proceso puede tener una utilización del 125%?')"
          class="shrink-0 px-3 py-1.5 rounded-xl bg-white border border-slate-200 hover:border-emerald-500 hover:text-emerald-800 text-[11px] font-medium transition shadow-2xs cursor-pointer"
        >
          Utilización del 125%
        </button>
        <button
          type="button"
          (click)="enviarPreguntaRapida('¿Qué significa tener holgura negativa y cómo se calcula?')"
          class="shrink-0 px-3 py-1.5 rounded-xl bg-white border border-slate-200 hover:border-emerald-500 hover:text-emerald-800 text-[11px] font-medium transition shadow-2xs cursor-pointer"
        >
          Holgura Negativa
        </button>
        <button
          type="button"
          (click)="enviarPreguntaRapida('¿Cómo prioriza SteelForce los cuellos de botella de la planta?')"
          class="shrink-0 px-3 py-1.5 rounded-xl bg-white border border-slate-200 hover:border-emerald-500 hover:text-emerald-800 text-[11px] font-medium transition shadow-2xs cursor-pointer"
        >
          Ranking de Cuello #1
        </button>
        <button
          type="button"
          (click)="diagnosticarReporteActual()"
          class="shrink-0 px-3 py-1.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 hover:bg-emerald-100 text-[11px] font-semibold transition shadow-2xs cursor-pointer"
        >
          Diagnosticar Reporte Actual
        </button>
      </div>

      <!-- Barra de Entrada de Texto Profesional -->
      <div class="bg-white p-2.5 sm:p-3 rounded-2xl border border-slate-200 shadow-xs flex items-center gap-2 shrink-0">
        <textarea
          [(ngModel)]=\"mensajeInput\"
          (keydown.enter)="manejarEnter($event)"
          rows="1"
          placeholder="Escribe tu consulta sobre capacidad, cuellos de botella o datos del reporte (Shift+Enter para nueva línea)..."
          class="flex-1 border-0 px-3 py-1.5 text-xs text-slate-900 focus:outline-none placeholder:text-slate-400 resize-none max-h-24 overflow-y-auto leading-relaxed"
        ></textarea>
        <button
          type="button"
          (click)="enviar()"
          [disabled]="!mensajeInput.trim() || cargando()"
          class="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-emerald-700 text-white font-bold text-xs hover:bg-emerald-800 transition disabled:opacity-40 shadow-xs cursor-pointer shrink-0"
        >
          <span>Enviar</span>
          <app-icon name="send" [size]="14" />
        </button>
      </div>
    </div>
  `,
})
export class Chatbot implements OnInit {
  @ViewChild('chatContainer') private readonly chatContainer?: ElementRef<HTMLDivElement>;

  private readonly asistenteApi = inject(AsistenteApi);
  private readonly reportesApi = inject(ReportesApi);
  protected readonly auth = inject(AuthService);

  protected mensajeInput = '';
  protected readonly cargando = signal<boolean>(false);
  protected readonly reporteSeleccionadoId = signal<number | null>(null);
  protected readonly reportesDisponibles = signal<ReporteResumen[]>([]);
  protected readonly estadoProveedor = signal<{ configurado: boolean; modelo: string; motor: 'GEMINI' | 'LOCAL' }>({
    configurado: false,
    modelo: 'Base de conocimiento SteelForce',
    motor: 'LOCAL',
  });

  protected readonly mensajes = signal<MensajeChat[]>([
    {
      id: 'bienvenida',
      remitente: 'asistente',
      texto:
        '¡Hola! Soy tu **Asistente Inteligente SteelForce** en Indux.\n\n' +
        'Estoy capacitado para interpretar tus análisis de capacidad operativa, explicar los cuellos de botella detectados, diagnosticar los factores causales del déficit y asesorarte sobre estrategias de optimización de planta (ODS 9).\n\n' +
        'Puedes formularme consultas conceptuales sobre la metodología o seleccionar un **Reporte** en el menú contextual superior para diagnosticar los datos reales registrados en tu empresa.',
      hora: this.obtenerHoraActual(),
      modelo: 'Base de conocimiento SteelForce',
    },
  ]);

  ngOnInit(): void {
    this.cargarReportesEmpresa();
  }

  protected iniciales(): string {
    const nombre = this.auth.usuario()?.nombre || 'Usuario';
    return nombre
      .split(' ')
      .map((p) => p[0])
      .slice(0, 2)
      .join('');
  }

  protected obtenerHoraActual(): string {
    const d = new Date();
    return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
  }

  protected cargarReportesEmpresa(): void {
    const empId = this.auth.usuario()?.empresaId;
    this.reportesApi.listar(empId ? { empresaId: empId } : undefined).subscribe({
      next: (reps) => {
        this.reportesDisponibles.set(reps);
        const ultimoAnalizado = reps.find((r) => r.estado === 'ANALIZADO');
        if (ultimoAnalizado) {
          this.reporteSeleccionadoId.set(ultimoAnalizado.id);
        }
      },
    });
  }

  protected cambiarReporteContextual(id: number | null): void {
    this.reporteSeleccionadoId.set(id);
  }

  protected enviarPreguntaRapida(pregunta: string): void {
    this.mensajeInput = pregunta;
    this.enviar();
  }

  protected diagnosticarReporteActual(): void {
    const reporteId = this.reporteSeleccionadoId();
    if (!reporteId) {
      this.mensajes.update((prev) => [
        ...prev,
        {
          id: `aviso-${Date.now()}`,
          remitente: 'asistente',
          texto:
            'Por favor, selecciona primero un **Reporte** en el menú contextual superior (o crea uno en el módulo de Reportes) para que pueda diagnosticar sus cuellos de botella e indicadores operativos.',
          hora: this.obtenerHoraActual(),
          modelo: this.estadoProveedor().modelo,
        },
      ]);
      this.desplazarAlFinal();
      return;
    }
    this.enviarPreguntaRapida('Analiza el cuello de botella y los indicadores del reporte seleccionado');
  }

  protected reiniciarChat(): void {
    this.mensajes.set([
      {
        id: `b-${Date.now()}`,
        remitente: 'asistente',
        texto:
          'Conversación reiniciada. Puedes formularme preguntas teóricas sobre la metodología SteelForce o seleccionar un reporte para diagnosticar sus datos.',
        hora: this.obtenerHoraActual(),
        modelo: this.estadoProveedor().modelo,
      },
    ]);
  }

  protected manejarEnter(event: Event): void {
    const keyboardEvent = event as KeyboardEvent;
    if (!keyboardEvent.shiftKey) {
      keyboardEvent.preventDefault();
      this.enviar();
    }
  }

  protected enviar(): void {
    const texto = this.mensajeInput.trim();
    if (!texto || this.cargando()) return;

    // Agregar mensaje del usuario a la interfaz
    const mensajeUsuario: MensajeChat = {
      id: `u-${Date.now()}`,
      remitente: 'usuario',
      texto,
      hora: this.obtenerHoraActual(),
    };

    this.mensajes.update((prev) => [...prev, mensajeUsuario]);
    this.mensajeInput = '';
    this.cargando.set(true);
    this.desplazarAlFinal();

    // Construir historial para la API
    const historial: MensajeHistorial[] = this.mensajes()
      .slice(-6)
      .map((m) => ({
        rol: m.remitente === 'usuario' ? 'user' : 'assistant',
        contenido: m.texto,
      }));

    this.asistenteApi
      .consultar({
        mensaje: texto,
        reporteId: this.reporteSeleccionadoId(),
        historial,
      })
      .subscribe({
        next: (resp: RespuestaAsistenteResponse) => {
          this.cargando.set(false);
          const motorActivo = resp.motor || (resp.proveedorConfigurado ? 'GEMINI' : 'LOCAL');
          this.estadoProveedor.set({
            configurado: resp.proveedorConfigurado,
            modelo: resp.modelo,
            motor: motorActivo,
          });

          this.mensajes.update((prev) => [
            ...prev,
            {
              id: `a-${Date.now()}`,
              remitente: 'asistente',
              texto: resp.respuesta,
              hora: this.obtenerHoraActual(),
              modelo: resp.modelo,
              fuentes: resp.fuentesContexto,
            },
          ]);
          this.desplazarAlFinal();
        },
        error: (err) => {
          this.cargando.set(false);
          const detalle = err.error?.detail || 'No se pudo conectar con el servicio del asistente.';
          this.mensajes.update((prev) => [
            ...prev,
            {
              id: `err-${Date.now()}`,
              remitente: 'asistente',
              texto: `Ocurrió un error al procesar tu solicitud: ${detalle}`,
              hora: this.obtenerHoraActual(),
              esError: true,
            },
          ]);
          this.desplazarAlFinal();
        },
      });
  }

  private desplazarAlFinal(): void {
    setTimeout(() => {
      if (this.chatContainer) {
        this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
      }
    }, 50);
  }

  /**
   * Sanea LaTeX residual a texto plano en español y renderiza Markdown de forma segura (sin inyección XSS).
   */
  public formatearTexto(texto: string): string {
    if (!texto) return '';

    // 1. Saneado de LaTeX residual a texto plano técnico
    let limpio = texto
      .replace(/H_\{?req\}?/g, 'Horas requeridas')
      .replace(/H_\{?disp\}?/g, 'Horas disponibles')
      .replace(/\\left\(\s*\\frac\{([^}]+)\}\{([^}]+)\}\s*\\right\)/g, '($1 / $2)')
      .replace(/\\frac\{([^}]+)\}\{([^}]+)\}/g, '($1 / $2)')
      .replace(/\\times/g, '×')
      .replace(/\\(le|leq)\b/g, '≤')
      .replace(/\\(ge|geq)\b/g, '≥')
      .replace(/\\approx\b/g, '≈')
      .replace(/\\text\{([^}]+)\}/g, '$1')
      .replace(/\\left\(/g, '(')
      .replace(/\\right\)/g, ')')
      .replace(/\\%/g, '%')
      .replace(/\$\$([\s\S]*?)\$\$/g, '$1')
      .replace(/\$([^\$\n]+)\$/g, '$1');

    // 2. Escape estricto de HTML para neutralizar cualquier intento de XSS
    limpio = limpio
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');

    // 3. Renderizar Markdown seguro en HTML estilizado
    return limpio
      .replace(/### (.*?)(?:\n|$)/g, '<h4 class="font-bold text-slate-900 text-xs mt-2.5 mb-1">$1</h4>')
      .replace(/\*\*(.*?)\*\*/g, '<strong class="font-bold text-slate-900">$1</strong>')
      .replace(/`([^`]+)`/g, '<code class="bg-slate-100 text-slate-800 px-1 py-0.5 rounded text-[11px] font-mono border border-slate-200">$1</code>')
      .replace(/^\* (.*?)(?:\n|$)/gm, '<div class="flex items-start gap-1.5 ml-2 my-0.5"><span class="text-emerald-700 font-bold">•</span><span>$1</span></div>')
      .replace(/^- (.*?)(?:\n|$)/gm, '<div class="flex items-start gap-1.5 ml-2 my-0.5"><span class="text-slate-400 font-bold">•</span><span>$1</span></div>');
  }
}