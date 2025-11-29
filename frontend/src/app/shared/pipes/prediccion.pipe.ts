import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'algoritmo',
  standalone: true
})
export class AlgoritmoPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return 'N/A';
    
    const mapeo: Record<string, string> = {
      'simpleMovingAverageAlgorithm': 'Media Móvil',
      'simpleExponentialSmoothingAlgorithm': 'Suavizado Exp.',
      'holtWintersAlgorithm': 'Holt-Winters',
      'AUTO': 'Automático',
      'SMA': 'Media Móvil',
      'SES': 'Suavizado Exp.',
      'HW': 'Holt-Winters'
    };
    
    return mapeo[value] || value;
  }
}

@Pipe({
  name: 'algoritmoSeverity',
  standalone: true
})
export class AlgoritmoSeverityPipe implements PipeTransform {
  transform(value: string | null | undefined): "success" | "secondary" | "info" | "warn" | "danger" | "contrast" | undefined {
    if (!value) return 'secondary';
    
    const mapeo: Record<string, "success" | "secondary" | "info" | "warn" | "danger" | "contrast"> = {
      'simpleMovingAverageAlgorithm': 'info',
      'simpleExponentialSmoothingAlgorithm': 'success',
      'holtWintersAlgorithm': 'warn',
      'AUTO': 'contrast',
      'SMA': 'info',
      'SES': 'success',
      'HW': 'warn'
    };
    
    return mapeo[value] || 'secondary';
  }
}

@Pipe({
  name: 'estadoPrediccion',
  standalone: true
})
export class EstadoPrediccionPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return 'N/A';
    
    const mapeo: Record<string, string> = {
      'ACTIVA': 'Activa',
      'OBSOLETA': 'Obsoleta',
      'FALLIDA': 'Fallida',
      'EN_PROCESO': 'En Proceso',
      'COMPLETADA': 'Completada'
    };
    
    return mapeo[value] || value;
  }
}

@Pipe({
  name: 'estadoPrediccionSeverity',
  standalone: true
})
export class EstadoPrediccionSeverityPipe implements PipeTransform {
  transform(value: string | null | undefined): "success" | "secondary" | "info" | "warn" | "danger" | "contrast" | undefined {
    if (!value) return 'secondary';
    
    const mapeo: Record<string, "success" | "secondary" | "info" | "warn" | "danger" | "contrast"> = {
      'ACTIVA': 'success',
      'OBSOLETA': 'warn',
      'FALLIDA': 'danger',
      'EN_PROCESO': 'info',
      'COMPLETADA': 'success'
    };
    
    return mapeo[value] || 'secondary';
  }
}

@Pipe({
  name: 'calidadPrediccion',
  standalone: true
})
export class CalidadPrediccionPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return 'N/A';
    
    const mapeo: Record<string, string> = {
      'EXCELENTE': 'Excelente',
      'BUENA': 'Buena',
      'ACEPTABLE': 'Aceptable',
      'BAJA': 'Baja',
      'MUY_BAJA': 'Muy Baja'
    };
    
    return mapeo[value] || value;
  }
}

@Pipe({
  name: 'calidadPrediccionSeverity',
  standalone: true
})
export class CalidadPrediccionSeverityPipe implements PipeTransform {
  transform(value: string | null | undefined): "success" | "secondary" | "info" | "warn" | "danger" | "contrast" | undefined {
    if (!value) return 'secondary';
    
    const mapeo: Record<string, "success" | "secondary" | "info" | "warn" | "danger" | "contrast"> = {
      'EXCELENTE': 'success',
      'BUENA': 'info',
      'ACEPTABLE': 'warn',
      'BAJA': 'danger',
      'MUY_BAJA': 'danger'
    };
    
    return mapeo[value] || 'secondary';
  }
}
