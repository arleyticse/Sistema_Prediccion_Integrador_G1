import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe para formatear el estado de inventario a formato legible.
 * Ej: NORMAL → Normal, BAJO → Bajo, CRITICO → Crítico
 */
@Pipe({
  name: 'estadoInventario',
  standalone: true
})
export class EstadoInventarioPipe implements PipeTransform {
  
  private readonly estadoMap: Record<string, string> = {
    'NORMAL': 'Normal',
    'BAJO': 'Bajo',
    'CRITICO': 'Crítico',
    'EXCESO': 'Exceso',
    'OBSOLETO': 'Obsoleto',
    'BLOQUEADO': 'Bloqueado'
  };

  transform(value: string | null | undefined): string {
    if (!value) return '';
    return this.estadoMap[value.toUpperCase()] ?? this.formatDefault(value);
  }

  private formatDefault(value: string): string {
    return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
  }
}

/**
 * Obtiene el severity de PrimeNG según el estado del inventario.
 */
@Pipe({
  name: 'estadoInventarioSeverity',
  standalone: true
})
export class EstadoInventarioSeverityPipe implements PipeTransform {

  transform(value: string | null | undefined): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    if (!value) return 'secondary';
    
    const estado = value.toUpperCase();
    switch (estado) {
      case 'NORMAL':
        return 'success';
      case 'BAJO':
        return 'warn';
      case 'CRITICO':
        return 'danger';
      case 'EXCESO':
        return 'info';
      case 'OBSOLETO':
        return 'secondary';
      case 'BLOQUEADO':
        return 'contrast';
      default:
        return 'secondary';
    }
  }
}
