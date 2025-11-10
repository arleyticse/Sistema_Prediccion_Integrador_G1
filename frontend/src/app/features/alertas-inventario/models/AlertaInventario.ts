import { TipoAlerta } from './TipoAlerta';
import { NivelCriticidad } from './NivelCriticidad';
import { EstadoAlerta } from './EstadoAlerta';

/**
 * Informacion basica de proveedor.
 */
export interface ProveedorBasico {
  proveedorId: number;
  nombreComercial: string;
  tiempoEntregaDias?: number;
  contacto?: string;
  telefono?: string;
}

/**
 * Informacion basica de un producto para AlertaInventario.
 */
export interface ProductoBasico {
  productoId: number;
  nombre: string;
  codigoSKU?: string;
  descripcion?: string;
  costoAdquisicion?: number;
  proveedor?: ProveedorBasico;
}

/**
 * Informacion basica de usuario asignado.
 */
export interface UsuarioBasico {
  usuarioId: number;
  nombre: string;
  email?: string;
}

/**
 * Modelo de Alerta de Inventario.
 * 
 * Representa una alerta generada automaticamente por el sistema
 * que requiere atencion del usuario para evitar quiebres de stock
 * o problemas en la gestion de inventario.
 */
export interface AlertaInventario {
  alertaId: number;
  tipoAlerta: TipoAlerta;
  nivelCriticidad: NivelCriticidad;
  mensaje: string;
  producto: ProductoBasico;
  stockActual?: number;
  stockMinimo?: number;
  cantidadSugerida?: number;
  usuarioAsignado?: UsuarioBasico;
  estado: EstadoAlerta;
  fechaGeneracion: Date;
  fechaResolucion?: Date;
  accionTomada?: string;
  observaciones?: string;
}

/**
 * Alerta agrupada por proveedor para el dashboard.
 * Extiende AlertaInventario con informacion del proveedor.
 */
export interface AlertaDashboard extends AlertaInventario {
  proveedorId?: number;
  proveedorNombreComercial?: string;
  proveedorTiempoEntrega?: number;
  costoAdquisicion?: number;
  codigoSKU?: string;
}

/**
 * Agrupacion de alertas por proveedor con totales.
 */
export interface AlertasProveedor {
  proveedorId: number;
  proveedorNombre: string;
  alertas: AlertaInventario[];
  totalAlertas: number;
  cantidadTotalSugerida: number;
  alertasPorCriticidad: Record<string, number>;
}
