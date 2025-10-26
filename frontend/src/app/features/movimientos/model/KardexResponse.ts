import { Page } from "../../../shared/models/Page";

export interface KardexResponse {
    kardexId: number | null;
    productoId: number | null;
    nombreProducto: string | null;
    categoriaProducto: string | null;
    fechaMovimiento: string | null;
    tipoMovimiento: string | null;
    tipoDocumento: string | null;
    numeroDocumento: string | null;
    cantidad: number | null;
    saldoCantidad: number | null;
    costoUnitario: string | null;
    valorTotal: string | null;
    lote: string | null;
    fechaVencimiento: string | null;
    proveedorId: number | null;
    nombreProveedor: string | null;
    motivo: string | null;
    referencia: string | null;
    usuarioId: number | null;
    nombreUsuario: string | null;
    observaciones: string | null;
    ubicacion: string | null;
    fechaRegistro: string | null;
}
export interface PageKardexResponse extends Page<KardexResponse> { }