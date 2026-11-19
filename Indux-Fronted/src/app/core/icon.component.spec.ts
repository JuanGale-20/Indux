import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, expect, it, beforeEach } from 'vitest';
import { AppIconComponent } from './icon.component';

describe('AppIconComponent', () => {
  let fixture: ComponentFixture<AppIconComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppIconComponent],
    });
    fixture = TestBed.createComponent(AppIconComponent);
  });

  it('se crea correctamente y renderiza svg con el tamaño especificado', () => {
    fixture.componentRef.setInput('name', 'layout-dashboard');
    fixture.componentRef.setInput('size', 20);
    fixture.detectChanges();

    const svg = fixture.nativeElement.querySelector('svg');
    expect(svg).toBeTruthy();
    expect(svg.getAttribute('width')).toBe('20');
    expect(svg.getAttribute('height')).toBe('20');
  });

  it('renderiza icono de inteligencia artificial brain-circuit', () => {
    fixture.componentRef.setInput('name', 'brain-circuit');
    fixture.detectChanges();

    const svg = fixture.nativeElement.querySelector('svg');
    expect(svg).toBeTruthy();
  });
});
