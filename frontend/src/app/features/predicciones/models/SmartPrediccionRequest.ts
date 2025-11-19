/**
 * Modelos para predicciones inteligentes con Smile ML
 * Soporta algoritmos avanzados: ARIMA, RandomForest, GradientBoosting
 */

/**
 * Request para generar predicción inteligente con Smile ML
 */
export interface SmartPrediccionRequest {
  idProducto: number;
  algoritmoSeleccionado: 'AUTO' | 'ARIMA' | 'RANDOM_FOREST' | 'GRADIENT_BOOSTING';
  horizonteTiempo: number;
  detectarEstacionalidad?: boolean;
  generarOrdenCompra?: boolean;
  idUsuario?: number;
}

/**
 * Respuesta de predicción inteligente con métricas avanzadas
 */
export interface SmartPrediccionResponse {
  prediccionId: number;
  idProducto: number;
  nombreProducto: string;
  codigoProducto: string;
  
  // Algoritmo utilizado
  algoritmoUtilizado: 'ARIMA' | 'RANDOM_FOREST' | 'GRADIENT_BOOSTING';
  horizonteTiempo: number;
  
  // Datos para gráficos
  valoresHistoricos: number[];
  fechasHistoricas: string[];
  valoresPredichos: number[];
  fechasPredichas: string[];
  
  // Métricas de calidad
  metricas: MetricasPrediccion;
  
  // Análisis de datos
  tieneTendencia: boolean;
  tieneEstacionalidad: boolean;
  periodoEstacional?: number;
  
  // Optimización EOQ/ROP
  cantidadOptimaPedido?: number;
  puntoReorden?: number;
  stockSeguridad?: number;
  
  // Advertencias y recomendaciones
  advertencias: string[];
  recomendaciones: string[];
  
  // Metadata
  fechaEjecucion: string;
  tiempoEjecucionMs: number;
}

/**
 * Métricas avanzadas de calidad de predicción
 */
export interface MetricasPrediccion {
  mae: number;
  mape: number;
  rmse: number;
  r2?: number;
  calificacionCalidad: 'EXCELENTE' | 'BUENA' | 'REGULAR' | 'MALA';
  nivelConfianza: number;
  descripcion: string;
}

/**
 * Configuración automática de predicción
 */
export interface ConfiguracionAutomatica {
  algoritmoRecomendado: 'ARIMA' | 'RANDOM_FOREST' | 'GRADIENT_BOOSTING';
  horizonteRecomendado: number;
  razonamiento: string;
  confianza: number;
}

/**
 * Información de algoritmos Smile ML disponibles
 */
export interface AlgoritmoSmileInfo {
  codigo: 'ARIMA' | 'RANDOM_FOREST' | 'GRADIENT_BOOSTING' | 'AUTO';
  nombre: string;
  descripcion: string;
  ventajas: string[];
  desventajas: string[];
  usoCaso: string;
  complejidad: 'BAJA' | 'MEDIA' | 'ALTA';
  precisonTipica: string;
}
