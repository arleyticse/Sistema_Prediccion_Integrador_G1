export interface KardexResponse {
  kardexId: number;
  productoId: number;
  nombreProducto: string;
  categoriaProducto: string;
  fechaMovimiento: string;
  tipoMovimiento: TipoMovimiento;
  tipoDocumento: string;
  numeroDocumento: string;
  cantidad: number;
  saldoCantidad: number;
  costoUnitario: number;
  valorTotal: number;
  lote: string;
  fechaVencimiento: string;
  proveedorId: number;
  nombreProveedor: string;
  motivo: string;
  referencia: string;
  usuarioId: number;
  nombreUsuario: string;
  observaciones: string;
  ubicacion: string;
  fechaRegistro: string;
}

export type TipoMovimiento = 
  | 'COMPRA' 
  | 'VENTA' 
  | 'DEVOLUCION_CLIENTE' 
  | 'DEVOLUCION_PROVEEDOR' 
  | 'AJUSTE_ENTRADA' 
  | 'AJUSTE_SALIDA' 
  | 'PRODUCCION' 
  | 'CONSUMO' 
  | 'MERMA' 
  | 'TRANSFERENCIA_ENTRADA' 
  | 'TRANSFERENCIA_SALIDA';

export interface PageKardexResponse {
  content: KardexResponse[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export function esEntrada(tipo: TipoMovimiento): boolean {
  return ['COMPRA', 'DEVOLUCION_CLIENTE', 'AJUSTE_ENTRADA', 'PRODUCCION', 'TRANSFERENCIA_ENTRADA'].includes(tipo);
}

export function esSalida(tipo: TipoMovimiento): boolean {
  return ['VENTA', 'DEVOLUCION_PROVEEDOR', 'AJUSTE_SALIDA', 'CONSUMO', 'MERMA', 'TRANSFERENCIA_SALIDA'].includes(tipo);
}
