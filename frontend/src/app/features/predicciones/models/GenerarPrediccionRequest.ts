/**
 * Request DTO para generar una nueva predicción de demanda.
 * 
 * @property productoId - ID del producto a predecir
 * @property algoritmo - Código del algoritmo (SMA, SES, HOLT_WINTERS)
 * @property horizonteTiempo - Días a predecir (1-365)
 * @property parametros - Parámetros del algoritmo seleccionado
 * @property limpiarAnterior - Si se deben eliminar predicciones anteriores
 * @property incluirDetalles - Si se incluyen detalles en la respuesta
 */
export interface GenerarPrediccionRequest {
  productoId: number;
  algoritmo: string;
  horizonteTiempo: number;
  parametros?: { [key: string]: number };
  limpiarAnterior?: boolean;
  incluirDetalles?: boolean;
}

/**
 * Información sobre un algoritmo de predicción disponible.
 */
export interface AlgoritmoInfo {
  codigo: string;
  nombre: string;
  descripcion: string;
  parametros: ParametroAlgoritmo[];
  minimosDatos: number;
  usoCaso: string;
  recomendadoPara?: string[];
}

/**
 * Parámetro configurable de un algoritmo.
 */
export interface ParametroAlgoritmo {
  nombre: string;
  tipo: 'integer' | 'double' | 'boolean';
  descripcion: string;
  rango: string;
  valorDefecto: number;
  min?: number;
  max?: number;
  paso?: number;
}