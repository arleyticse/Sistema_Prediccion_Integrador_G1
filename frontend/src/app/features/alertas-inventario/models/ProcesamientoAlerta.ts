/**
 * Request para procesar alertas seleccionadas de forma automatica.
 * 
 * Inicia el flujo completo:
 * 1. Predicciones automaticas
 * 2. Optimizacion EOQ/ROP
 * 3. Generacion de ordenes de compra
 * 4. Actualizacion de alertas
 */
export interface ProcesarAlertasRequest {
  /**
   * IDs de las alertas seleccionadas para procesar.
   */
  alertaIds: number[];

  /**
   * Horizonte de tiempo para las predicciones (en dias).
   * Default: 30 dias
   */
  horizonteTiempo?: number;

  /**
   * ID del usuario que ejecuta el procesamiento.
   */
  usuarioId?: number;

  /**
   * Observaciones adicionales sobre el procesamiento.
   */
  observaciones?: string;
}

/**
 * Respuesta del procesamiento batch de alertas.
 * 
 * Contiene el resumen completo del procesamiento incluyendo:
 * - Alertas procesadas
 * - Predicciones generadas
 * - Optimizaciones calculadas
 * - Ordenes de compra creadas
 * - Errores encontrados
 */
export interface ProcesamientoBatchResponse {
  fechaInicio: Date;
  fechaFin: Date;
  tiempoEjecucionMs: number;
  
  totalProcesadas: number;
  exitosos: number;
  fallidos: number;
  
  alertasExitosas: number[];
  alertasFallidas: number[];
  mensajesError: string[];
  
  observaciones?: string;
  exitoTotal: boolean;
  
  prediccionesGeneradas: number[];
  optimizacionesGeneradas: number[];
  ordenesGeneradas: number[];
}
