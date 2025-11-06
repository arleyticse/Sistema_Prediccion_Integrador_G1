/**
 * Response DTO con los resultados de optimización EOQ/ROP.
 */
export interface OptimizacionResponse {
  // Identificación
  id?: number;
  prediccionId: number;
  productoId: number;
  productoNombre: string;
  codigoProducto: string;
  
  // Métricas EOQ
  cantidadEconomicaPedido: number;
  numeroOptimoPedidos: number;
  cicloOptimoDias: number;
  
  // Métricas ROP
  puntoReorden: number;
  stockSeguridad: number;
  stockMaximo: number;
  
  // Análisis de Costos
  costoTotalAnual: number;
  costoOrdenamiento: number;
  costoAlmacenamientoAnual: number;
  costoStockSeguridad: number;
  
  // Análisis de Demanda
  demandaAnual: number;
  demandaDiaria: number;
  desviacionEstandarDemanda: number;
  coeficienteVariacion: number;
  
  // Parámetros de entrada
  costoPedido: number;
  costoAlmacenamiento: number;
  costoUnitario: number;
  tiempoEntregaDias: number;
  nivelServicioDeseado: number;
  factorZ: number;
  
  // Recomendaciones
  recomendacion: string;
  advertencia: string;
  nivelConfianza: 'ALTO' | 'MEDIO' | 'BAJO';
  
  // Metadatos
  fechaCalculo: string;
  calculadoPor?: string;
}

/**
 * Request DTO para calcular optimización.
 * Los costos y tiempos son OPCIONALES - se obtienen automáticamente 
 * de la BD del producto si no se proporcionan.
 */
export interface CalcularOptimizacionRequest {
  prediccionId: number;
  nivelServicioDeseado: number;
  // Parámetros opcionales (si no se envían, se obtienen de la BD)
  costoPedido?: number;
  costoAlmacenamiento?: number;
  costoUnitario?: number;
  tiempoEntregaDias?: number;
  desviacionEstandarDemanda?: number;
}
