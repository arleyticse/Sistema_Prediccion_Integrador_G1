export interface InventarioCreateRequest {
    productoId: number;
    stockDisponible: number;
    stockReservado?: number;
    stockEnTransito?: number;
    stockMinimo: number;
    stockMaximo?: number;
    puntoReorden: number;
    ubicacionAlmacen?: string;
    observaciones?: string;
}