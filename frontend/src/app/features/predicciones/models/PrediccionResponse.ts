/**
 * Estados posibles de una predicción.
 */
export enum EstadoPrediccion {
  ACTIVA = 'ACTIVA',
  OBSOLETA = 'OBSOLETA',
  FALLIDA = 'FALLIDA'
}

/**
 * Información básica del producto (para optimización).
 */
export interface ProductoBasico {
  productoId: number;
  nombre: string;
  costoAdquisicion?: number;
  costoPedido?: number;
  costoMantenimientoAnual?: number;
  diasLeadTime?: number;
}

/**
 * Response DTO con los resultados de una predicción generada.
 */
export interface PrediccionResponse {
  prediccionId: number;
  productoId: number;
  productoNombre: string;
  algoritmo: string;
  horizonteTiempo?: number;
  demandaPredichaTotal: number;
  precision?: number;
  fechaGeneracion: string;
  descripcion?: string;
  vigenciaHasta?: string;
  detallePronostico?: DetallePronostico[];
  valoresPredichos?: number[];
  datosHistoricos?: number[];
  mae?: number;
  mape?: number;
  rmse?: number;
  calidadPrediccion?: 'EXCELENTE' | 'BUENA' | 'REGULAR' | 'ACEPTABLE' | 'POBRE';
  advertencias?: string[];
  recomendaciones?: string[];
  tieneTendencia?: boolean;
  tieneEstacionalidad?: boolean;
  estado?: EstadoPrediccion;
  descripcionEstado?: string;
  producto?: ProductoBasico; // Datos del producto para optimización
}

/**
 * Detalle de pronóstico por día.
 */
export interface DetallePronostico {
  fecha: string;
  demandaPredicha: number;
  confianzaMin?: number;
  confianzaMax?: number;
}

/**
 * Resultado de comparación de algoritmos.
 */
export interface ComparacionAlgoritmos {
  productoId: number;
  productoNombre: string;
  resultados: ResultadoComparacion[];
  mejorAlgoritmo: string;
  fechaAnalisis: string;
}

/**
 * Resultado individual de un algoritmo en la comparación.
 */
export interface ResultadoComparacion {
  algoritmo: string;
  nombreAlgoritmo: string;
  mae: number;
  mape: number;
  rmse: number;
  calidadPrediccion: string;
  tiempoEjecucion: number;
  ranking: number;
}
