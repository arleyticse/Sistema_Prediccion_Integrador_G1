import { Categoria } from "../../categorias/models/Categoria";
import { UnidadMedida } from "../../unidades-medida/models/UnidadMedida";

export interface ProductoResponse {
  productoId: number;
  nombre: string;
  categoria: Categoria;
  unidadMedida: UnidadMedida;
  costoAdquisicion: number;
  stockDisponible: number;
  estadoInventario: string;
  diasLeadTime: number;
  costoMantenimiento: number;
  costoPedido: number;
}

export interface PageInfo {
  size: number;          
  number: number;       
  totalElements: number;
  totalPages: number;
}

export interface PageProductoResponse {
  content: ProductoResponse[];
  page: PageInfo;
}