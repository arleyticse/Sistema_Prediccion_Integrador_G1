
export interface KardexCreateRequest {
    productoId: number; // required
    tipoMovimiento: string; // required
    tipoDocumento?: string;
    numeroDocumento?: string;
    cantidad: number; // required, minimum 1
    costoUnitario?: number;
    lote?: string;
    fechaVencimiento?: string | Date;
    proveedorId?: number;
    motivo: string; // required
    referencia?: string;
    observaciones?: string;
    ubicacion?: string;
}