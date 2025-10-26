export interface ProductoRequest {
	nombre: string;
	categoriaId: number;
	unidadMedidaId: number;
	diasLeadTime: number;
	costoAdquisicion: number;
	costoMantenimiento: number;
	costoPedido: number;
}