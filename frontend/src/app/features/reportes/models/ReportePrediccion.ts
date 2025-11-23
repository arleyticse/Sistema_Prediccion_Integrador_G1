export interface ReportePrediccionDTO {
  resumenGeneral: ResumenGeneralPrediccion;
  predicciones: PrediccionDetalle[];
  estadisticas: EstadisticasPrediccion;
  topProductos: ProductoConPrediccion[];
  fechaGeneracion: string;
}

export interface ResumenGeneralPrediccion {
  totalPredicciones: number;
  prediccionesExcelentes: number;
  prediccionesBuenas: number;
  prediccionesRegulares: number;
  prediccionesMalas: number;
  porcentajeExito: number;
  periodoAnalisis: string;
}

export interface PrediccionDetalle {
  prediccionId: number;
  nombreProducto: string;
  codigoProducto: string;
  categoria: string;
  algoritmoUsado: string;
  fechaEjecucion: string;
  horizonteTiempo: number;
  demandaPredichaTotal: number;
  mape: number;
  rmse: number;
  mae: number;
  r2: number;
  nivelPrecision: string;
  nombreUsuario: string;
}

export interface EstadisticasPrediccion {
  mapePromedio: number;
  rmsePromedio: number;
  maePromedio: number;
  r2Promedio: number;
  algoritmoMasUsado: string;
  cantidadPorAlgoritmo: number;
  demandaTotalPredicha: number;
}

export interface ProductoConPrediccion {
  nombreProducto: string;
  codigoProducto: string;
  cantidadPredicciones: number;
  mapePromedio: number;
  demandaTotalPredicha: number;
  categoria: string;
}
