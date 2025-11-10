import { PageInfo } from "../../../shared/models/PageInfo";
import { Categoria } from "../../categorias/models/Categoria";
import { UnidadMedida } from "../../unidades-medida/models/UnidadMedida";
import { Proveedor } from '../../proveedores/model/Proveedor';

export interface ProductoResponse {
  productoId: number;
  nombre: string;
  categoria: Categoria;
  unidadMedida: UnidadMedida;
  proveedorPrincipal?: Proveedor;
  costoAdquisicion: number;
  stockDisponible: number;
  estadoInventario: string;
  diasLeadTime: number;
  costoMantenimiento: number;
  costoPedido: number;
}


export interface PageProductoResponse {
  content: ProductoResponse[];
  page: PageInfo;
}