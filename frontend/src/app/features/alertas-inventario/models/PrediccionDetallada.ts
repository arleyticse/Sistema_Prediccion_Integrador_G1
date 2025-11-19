/**
 * Predicción detallada de un producto individual.
 * Incluye datos históricos, predicciones, métricas de calidad y gráficos.
 */
export interface PrediccionProductoDTO {
  /** ID del producto */
  productoId: number;
  
  /** Nombre del producto */
  nombreProducto: string;
  
  /** Código SKU del producto (Formato: [INICIAL_CATEGORIA][0000][ID]) */
  codigoSKU: string;
  
  /** Código del producto */
  codigoProducto: string;
  
  /** ID de la predicción generada */
  prediccionId: number;
  
  /** Valores históricos de demanda */
  valoresHistoricos: number[];
  
  /** Valores predichos (demanda futura) */
  valoresPredichos: number[];
  
  /** Fechas correspondientes a los valores históricos */
  fechasHistoricas: string[];
  
  /** Fechas correspondientes a los valores predichos */
  fechasPredichas: string[];
  
  /** Error Absoluto Medio (MAE) */
  mae: number;
  
  /** Error Porcentual Absoluto Medio (MAPE) */
  mape: number;
  
  /** Raíz del Error Cuadrático Medio (RMSE) */
  rmse: number;
  
  /** Calidad de la predicción (EXCELENTE, BUENA, REGULAR, MALA) */
  calidadPrediccion: 'EXCELENTE' | 'BUENA' | 'REGULAR' | 'MALA';
  
  /** Horizonte de predicción usado (en días) */
  horizonteUsado: number;
  
  /** Algoritmo usado para la predicción */
  algoritmoUsado: string;
  
  /** Indica si la serie tiene tendencia detectada */
  tieneTendencia: boolean;
  
  /** Indica si la serie tiene estacionalidad detectada */
  tieneEstacionalidad: boolean;
  
  /** Cantidad óptima de pedido (EOQ) calculada */
  cantidadOptimaPedido: number;
  
  /** Punto de reorden (ROP) calculado */
  puntoReorden: number;
  
  /** Advertencias sobre la predicción */
  advertencias: string[];
  
  /** Recomendaciones basadas en la predicción */
  recomendaciones: string[];
}

/**
 * Métricas agregadas de un conjunto de predicciones.
 * Proporciona resumen estadístico de calidad.
 */
export interface MetricasAgregadasDTO {
  /** Error Absoluto Medio promedio */
  maePromedio: number;
  
  /** Error Porcentual Absoluto Medio promedio */
  mapePromedio: number;
  
  /** Raíz del Error Cuadrático Medio promedio */
  rmsePromedio: number;
  
  /** Calidad general del conjunto de predicciones */
  calidadGeneral: string;
  
  /** Total de productos procesados */
  totalProductos: number;
  
  /** Cantidad de predicciones excelentes (MAPE < 10%) */
  prediccionesExcelentes: number;
  
  /** Cantidad de predicciones buenas (MAPE 10-20%) */
  prediccionesBuenas: number;
  
  /** Cantidad de predicciones regulares (MAPE 20-50%) */
  prediccionesRegulares: number;
  
  /** Cantidad de predicciones malas (MAPE > 50%) */
  prediccionesMalas: number;
  
  /** Porcentaje de predicciones con calidad aceptable (≤20%) */
  porcentajeAceptable: number;
}

/**
 * Resumen de predicciones agrupadas por proveedor.
 * Estructura las predicciones y métricas por proveedor.
 */
export interface ResumenPrediccionPorProveedor {
  /** ID del proveedor */
  proveedorId: number;
  
  /** Nombre del proveedor */
  nombreProveedor: string;
  
  /** RUC del proveedor */
  rucProveedor: string;
  
  /** Contacto principal del proveedor */
  contactoProveedor: string;
  
  /** Email del proveedor */
  emailProveedor: string;
  
  /** Teléfono del proveedor */
  telefonoProveedor: string;
  
  /** Lista de predicciones de productos para este proveedor */
  predicciones: PrediccionProductoDTO[];
  
  /** Métricas agregadas del conjunto de predicciones */
  metricas: MetricasAgregadasDTO;
  
  /** Cantidad total de alertas procesadas para este proveedor */
  totalAlertas: number;
  
  /** Cantidad de predicciones exitosas */
  prediccionesExitosas: number;
  
  /** Cantidad de predicciones fallidas */
  prediccionesFallidas: number;
}
