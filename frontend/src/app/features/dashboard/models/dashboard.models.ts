/**
 * Interfaces para los datos del dashboard del gerente.
 */

export interface DashboardEstadisticas {
  totalProductos: number;
  productosActivos: number;
  productosStockBajo: number;
  productosStockCritico: number;
  productosExceso: number;
  alertasPendientes: number;
  alertasCriticas: number;
  proveedoresActivos: number;
  ordenesPendientes: number;
  ordenesBorrador: number;
  valorInventarioTotal: number;
  stockTotalUnidades: number;
}

export interface DistribucionInventario {
  estado: string;
  cantidad: number;
}

export interface TendenciaMovimientos {
  fecha: string;
  entradas: number;
  salidas: number;
}

export interface ProductoMasVendido {
  productoId: number;
  nombre: string;
  cantidadVendida: number;
}

export interface ProductoStockBajo {
  productoId: number;
  nombre: string;
  stockActual: number;
  stockMinimo: number;
  puntoReorden: number;
  estado: string;
  categoriaNombre: string;
}

export interface DistribucionCategoria {
  categoriaId: number;
  nombre: string;
  cantidadProductos: number;
}

export interface DistribucionAlertas {
  tipo: string;
  cantidad: number;
}

export interface ResumenMovimientos {
  totalEntradas: number;
  totalSalidas: number;
  totalMermas: number;
  cantidadEntradas: number;
  cantidadSalidas: number;
  cantidadMermas: number;
}

export interface DashboardCompleto {
  estadisticas: DashboardEstadisticas;
  distribucionInventario: DistribucionInventario[];
  tendenciaMovimientos: TendenciaMovimientos[];
  productosMasVendidos: ProductoMasVendido[];
  productosStockBajo: ProductoStockBajo[];
  distribucionCategorias: DistribucionCategoria[];
  distribucionAlertas: DistribucionAlertas[];
  resumenMovimientos: ResumenMovimientos;
}
