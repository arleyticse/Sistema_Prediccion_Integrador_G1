export interface ProductoRequest {
	nombre: string;
	categoriaId: number;
	unidadMedidaId: number;
	proveedorId?: number | null;
	diasLeadTime: number;
	costoAdquisicion: number;
	costoMantenimiento: number;
	costoPedido: number;
}