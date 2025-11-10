/**
 * Modelo para el resumen de una orden de compra generada.
 * 
 * Se utiliza para mostrar información resumida de las órdenes
 * generadas durante el procesamiento automático de alertas.
 */
export interface ResumenOrden {
  /**
   * ID único de la orden de compra.
   */
  ordenId: number;

  /**
   * Número de orden generado automáticamente.
   * Formato: OC-AUTO-YYYYMMDD-NNNN
   */
  numeroOrden: string;

  /**
   * Información básica del proveedor.
   */
  proveedor: ProveedorResumen;

  /**
   * Fecha de generación de la orden.
   */
  fechaOrden: string;

  /**
   * Fecha estimada de entrega basada en el lead time del proveedor.
   */
  fechaEntregaEsperada: string;

  /**
   * Monto total de la orden (suma de subtotales).
   */
  totalOrden: number;

  /**
   * Estado actual de la orden.
   * Valores: BORRADOR, PENDIENTE, APROBADA, ENVIADA, RECIBIDA_PARCIAL, RECIBIDA_COMPLETA, CANCELADA
   */
  estadoOrden: string;

  /**
   * Cantidad de productos diferentes en la orden.
   */
  cantidadProductos: number;

  /**
   * Lista resumida de productos incluidos en la orden.
   */
  productos: ProductoResumen[];

  /**
   * Indica si la orden fue generada automáticamente por el sistema.
   */
  generadaAutomaticamente: boolean;

  /**
   * Observaciones adicionales de la orden.
   */
  observaciones?: string;
}

/**
 * Información resumida del proveedor.
 */
export interface ProveedorResumen {
  proveedorId: number;
  nombreComercial: string;
  razonSocial: string;
  ruc: string;
  tiempoEntrega: number;
}

/**
 * Información resumida de productos en la orden.
 */
export interface ProductoResumen {
  productoId: number;
  nombre: string;
  codigoSKU: string;
  cantidadSolicitada: number;
  precioUnitario: number;
  subtotal: number;
}
