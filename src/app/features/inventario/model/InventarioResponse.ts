import { Page } from "../../../shared/models/Page";


export interface InventarioResponse {
    inventarioId: number;
    productoId: number;
    nombreProducto: string;
    stockDisponible: number;
    stockReservado?: number;
    stockEnTransito?: number;
    stockMinimo: number;
    stockMaximo?: number;
    puntoReorden: number;
    ubicacionAlmacen: string;
    estado: string;
    observaciones?: string;
}


export interface PageInventarioResponse extends Page<InventarioResponse> { }