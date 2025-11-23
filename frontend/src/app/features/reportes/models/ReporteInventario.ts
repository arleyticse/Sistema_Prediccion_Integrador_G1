export interface ReporteInventarioDTO {
  resumenGeneral: ResumenGeneralInventario;
  inventarios: InventarioDetalle[];
  estadisticas: EstadisticasInventario;
  productosCriticos: ProductoCritico[];
  valoracion: ValoracionInventario;
  fechaGeneracion: string;
}

export interface ResumenGeneralInventario {
  totalProductos: number;
  productosConStock: number;
  productosSinStock: number;
  productosBajoMinimo: number;
  productosEnReorden: number;
  productosObsoletos: number;
  periodoAnalisis: string;
}

export interface InventarioDetalle {
  inventarioId: number;
  nombreProducto: string;
  codigoProducto: string;
  categoria: string;
  stockDisponible: number;
  stockReservado: number;
  stockEnTransito: number;
  stockTotal: number;
  stockMinimo: number;
  stockMaximo: number;
  puntoReorden: number;
  ubicacionAlmacen: string;
  fechaUltimoMovimiento: string;
  diasSinVenta: number;
  estado: string;
  precioUnitario: number;
  valorStock: number;
  rotacion: number;
}

export interface EstadisticasInventario {
  stockTotalGeneral: number;
  stockDisponibleTotal: number;
  stockReservadoTotal: number;
  stockEnTransitoTotal: number;
  rotacionPromedio: number;
  diasPromedioSinVenta: number;
  categoriaConMasStock: string;
  categoriaConMenosStock: string;
}

export interface ProductoCritico {
  nombreProducto: string;
  codigoProducto: string;
  stockDisponible: number;
  stockMinimo: number;
  diasSinVenta: number;
  nivelCriticidad: string;
  razon: string;
  categoria: string;
}

export interface ValoracionInventario {
  valorTotalInventario: number;
  valorStockDisponible: number;
  valorStockReservado: number;
  valorStockEnTransito: number;
  valorPromedioPorProducto: number;
  categoriaConMayorValor: string;
  valorCategoriaMaxima: number;
}
