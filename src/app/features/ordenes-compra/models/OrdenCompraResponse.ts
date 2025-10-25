export interface OrdenCompraResponse {
  ordenCompraId: number;
  numeroOrden: string;
  proveedorNombre: string;
  productoId?: number;
  productoNombre?: string;
  cantidadSolicitada?: number;
  fechaOrden: string;
  fechaEntregaEsperada: string;
  estadoOrden: string;
  totalOrden: number;
  observaciones?: string;
  generadaAutomaticamente: boolean;
  detallesCalculo?: string;
  fechaCreacion: string;
  fechaActualizacion?: string;
}