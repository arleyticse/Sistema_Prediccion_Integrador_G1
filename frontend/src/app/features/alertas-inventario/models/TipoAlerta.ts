/**
 * Tipos de alerta de inventario.
 * 
 * Define las categorias de alertas que el sistema puede generar
 * automaticamente basadas en el estado del inventario y predicciones.
 */
export enum TipoAlerta {
  STOCK_BAJO = 'STOCK_BAJO',
  PUNTO_REORDEN = 'PUNTO_REORDEN',
  STOCK_CRITICO = 'STOCK_CRITICO',
  SOBRESTOCK = 'SOBRESTOCK',
  PRODUCTO_OBSOLETO = 'PRODUCTO_OBSOLETO',
  VENCIMIENTO_PROXIMO = 'VENCIMIENTO_PROXIMO',
  VENCIMIENTO_VENCIDO = 'VENCIMIENTO_VENCIDO',
  DEMANDA_ANOMALA = 'DEMANDA_ANOMALA',
  COSTO_ELEVADO = 'COSTO_ELEVADO',
  MERMA_ALTA = 'MERMA_ALTA',
  PROVEEDOR_RETRASO = 'PROVEEDOR_RETRASO'
}

/**
 * Labels amigables para mostrar en UI.
 */
export const TipoAlertaLabels: Record<TipoAlerta, string> = {
  [TipoAlerta.STOCK_BAJO]: 'Stock Bajo',
  [TipoAlerta.PUNTO_REORDEN]: 'Punto de Reorden',
  [TipoAlerta.STOCK_CRITICO]: 'Stock Crítico',
  [TipoAlerta.SOBRESTOCK]: 'Sobrestock',
  [TipoAlerta.PRODUCTO_OBSOLETO]: 'Producto Obsoleto',
  [TipoAlerta.VENCIMIENTO_PROXIMO]: 'Vencimiento Próximo',
  [TipoAlerta.VENCIMIENTO_VENCIDO]: 'Vencimiento Vencido',
  [TipoAlerta.DEMANDA_ANOMALA]: 'Demanda Anómala',
  [TipoAlerta.COSTO_ELEVADO]: 'Costo Elevado',
  [TipoAlerta.MERMA_ALTA]: 'Merma Alta',
  [TipoAlerta.PROVEEDOR_RETRASO]: 'Proveedor con Retraso'
};
