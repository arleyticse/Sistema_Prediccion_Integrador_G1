import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe para formatear el tipo de movimiento a formato legible.
 * Ej: ENTRADA_COMPRA → Entrada Compra, SALIDA_VENTA → Salida Venta
 */
@Pipe({
  name: 'tipoMovimiento',
  standalone: true
})
export class TipoMovimientoPipe implements PipeTransform {

  private readonly tipoMap: Record<string, string> = {
    'ENTRADA_COMPRA': 'Entrada Compra',
    'ENTRADA_DEVOLUCION': 'Entrada Devolución',
    'ENTRADA_AJUSTE': 'Entrada Ajuste',
    'ENTRADA_TRANSFERENCIA': 'Entrada Transferencia',
    'ENTRADA_PRODUCCION': 'Entrada Producción',
    'ENTRADA_INICIAL': 'Entrada Inicial',
    'SALIDA_VENTA': 'Salida Venta',
    'SALIDA_DEVOLUCION': 'Salida Devolución',
    'SALIDA_AJUSTE': 'Salida Ajuste',
    'SALIDA_TRANSFERENCIA': 'Salida Transferencia',
    'SALIDA_MERMA': 'Salida Merma',
    'SALIDA_VENCIMIENTO': 'Salida Vencimiento',
    'SALIDA_CONSUMO': 'Salida Consumo',
    'AJUSTE_POSITIVO': 'Ajuste Positivo',
    'AJUSTE_NEGATIVO': 'Ajuste Negativo'
  };

  transform(value: string | null | undefined): string {
    if (!value) return '';
    return this.tipoMap[value.toUpperCase()] ?? this.formatDefault(value);
  }

  private formatDefault(value: string): string {
    return value
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, char => char.toUpperCase());
  }
}

/**
 * Obtiene el severity de PrimeNG según el tipo de movimiento.
 */
@Pipe({
  name: 'tipoMovimientoSeverity',
  standalone: true
})
export class TipoMovimientoSeverityPipe implements PipeTransform {

  transform(value: string | null | undefined): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    if (!value) return 'secondary';
    
    const tipo = value.toUpperCase();
    
    if (tipo.startsWith('ENTRADA') || tipo === 'AJUSTE_POSITIVO') {
      return 'success';
    }
    
    if (tipo.startsWith('SALIDA') || tipo === 'AJUSTE_NEGATIVO') {
      return 'danger';
    }
    
    return 'info';
  }
}
