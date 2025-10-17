import { Page } from "../../../shared/models/Page";


export interface InventarioResponse {
    inventarioId: number;
    productoId: number;
    cantidad: number;
    ubicacion: string;
    estado: string;
}


export interface PageInventarioResponse extends Page<InventarioResponse> { }