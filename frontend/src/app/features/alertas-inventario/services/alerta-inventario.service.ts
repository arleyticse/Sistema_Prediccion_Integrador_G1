import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BaseService } from '../../../core/services/base';
import { 
  AlertaInventario,
  AlertaDashboard,
  AlertasProveedor 
} from '../models/AlertaInventario';
import { 
  ProcesarAlertasRequest, 
  ProcesamientoBatchResponse 
} from '../models/ProcesamientoAlerta';
import { ResumenOrden } from '../models/ResumenOrden';
import { ResumenPrediccionPorProveedor } from '../models/PrediccionDetallada';
import { NivelCriticidad } from '../models/NivelCriticidad';
import { TipoAlerta } from '../models/TipoAlerta';
import { EstadoAlerta } from '../models/EstadoAlerta';
import { Page } from '../../../shared/models/Page';

/**
 * Servicio para gestion de Alertas de Inventario.
 * 
 * Proporciona metodos para:
 * - Consultar alertas con filtros y paginacion
 * - Agrupar alertas por proveedor
 * - Procesar alertas de forma automatica (batch)
 * - Actualizar estados de alertas
 * - Operaciones batch (marcar, resolver, ignorar)
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 */
@Injectable({
  providedIn: 'root'
})
export class AlertaInventarioService extends BaseService<AlertaInventario> {
  
  private readonly baseUrl = `${environment.apiUrl}/alertas-inventario`;
  
  /**
   * Obtiene todas las alertas con filtros y paginacion.
   * 
   * @param filtros Parametros de filtrado
   * @param page Numero de pagina (base 0)
   * @param size Tamaño de pagina
   * @returns Observable<Page<AlertaInventario>>
   */
  listarAlertas(
    filtros: {
      estado?: EstadoAlerta;
      criticidad?: NivelCriticidad;
      tipoAlerta?: TipoAlerta;
      productoId?: number;
      proveedorId?: number;
      fechaDesde?: string;
      fechaHasta?: string;
    } = {},
    page: number = 0,
    size: number = 20
  ): Observable<Page<AlertaInventario>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'fechaGeneracion,desc');

    if (filtros.estado) {
      params = params.set('estado', filtros.estado);
    }
    if (filtros.criticidad) {
      params = params.set('criticidad', filtros.criticidad);
    }
    if (filtros.tipoAlerta) {
      params = params.set('tipoAlerta', filtros.tipoAlerta);
    }
    if (filtros.productoId) {
      params = params.set('productoId', filtros.productoId.toString());
    }
    if (filtros.proveedorId) {
      params = params.set('proveedorId', filtros.proveedorId.toString());
    }
    if (filtros.fechaDesde) {
      params = params.set('fechaDesde', filtros.fechaDesde);
    }
    if (filtros.fechaHasta) {
      params = params.set('fechaHasta', filtros.fechaHasta);
    }

    return this.http.get<Page<AlertaInventario>>(this.baseUrl, { params });
  }

  /**
   * Obtiene una alerta especifica por ID.
   * 
   * @param alertaId ID de la alerta
   * @returns Observable<AlertaInventario>
   */
  obtenerAlerta(alertaId: number): Observable<AlertaInventario> {
    return this.http.get<AlertaInventario>(`${this.baseUrl}/${alertaId}`);
  }

  /**
   * Obtiene alertas para el dashboard con informacion completa.
   * Incluye datos de proveedor, stock actual y minimo desde inventario.
   * 
   * @returns Observable<AlertaDashboard[]>
   */
  obtenerAlertasDashboard(): Observable<AlertaDashboard[]> {
    return this.http.get<AlertaDashboard[]>(`${this.baseUrl}/dashboard`);
  }

  /**
   * Obtiene alertas agrupadas por proveedor.
   * Incluye totales y metricas por proveedor.
   * 
   * @returns Observable con estructura de proveedores y alertas
   */
  obtenerAlertasAgrupadasPorProveedor(): Observable<{
    proveedores: Record<number, {
      alertas: AlertaInventario[];
      totalAlertas: number;
      cantidadTotalSugerida: number;
      alertasPorCriticidad: Record<string, number>;
    }>;
    totalProveedores: number;
  }> {
    return this.http.get<any>(`${this.baseUrl}/agrupadas-proveedor`);
  }

  /**
   * Obtiene estadisticas generales de alertas.
   * 
   * @returns Observable con estadisticas agregadas
   */
  obtenerEstadisticas(): Observable<{
    totalAlertas: number;
    alertasPorEstado: Record<string, number>;
    alertasPorCriticidad: Record<string, number>;
    alertasPorTipo: Record<string, number>;
    alertasMasAntiguas: AlertaInventario[];
  }> {
    return this.http.get<any>(`${this.baseUrl}/estadisticas`);
  }

  /**
   * Actualiza el estado de una alerta.
   * 
   * @param alertaId ID de la alerta
   * @param nuevoEstado Nuevo estado
   * @param observaciones Observaciones opcionales
   * @param usuarioId ID del usuario opcional
   * @returns Observable<AlertaInventario>
   */
  actualizarEstado(
    alertaId: number,
    nuevoEstado: EstadoAlerta,
    observaciones?: string,
    usuarioId?: number
  ): Observable<AlertaInventario> {
    return this.http.put<AlertaInventario>(
      `${this.baseUrl}/${alertaId}/estado`,
      { nuevoEstado, observaciones, usuarioId }
    );
  }

  /**
   * Marca multiples alertas como EN_PROCESO en batch.
   * 
   * @param alertaIds IDs de alertas
   * @param usuarioId ID del usuario responsable
   * @param observaciones Observaciones opcionales
   * @returns Observable con resultado del batch
   */
  marcarEnProcesoBatch(
    alertaIds: number[],
    usuarioId: number,
    observaciones?: string
  ): Observable<{
    exitoso: boolean;
    alertasActualizadas: AlertaInventario[];
    totalActualizadas: number;
    mensaje: string;
  }> {
    return this.http.post<any>(
      `${this.baseUrl}/batch/marcar-en-proceso`,
      { alertaIds, usuarioId, observaciones }
    );
  }

  /**
   * Resuelve multiples alertas en batch.
   * 
   * @param alertaIds IDs de alertas
   * @param accionTomada Descripcion de la accion
   * @param usuarioId ID del usuario opcional
   * @returns Observable con resultado del batch
   */
  resolverAlertasBatch(
    alertaIds: number[],
    accionTomada: string,
    usuarioId?: number
  ): Observable<{
    exitoso: boolean;
    alertasResueltas: AlertaInventario[];
    totalResueltas: number;
    mensaje: string;
  }> {
    return this.http.post<any>(
      `${this.baseUrl}/batch/resolver`,
      { alertaIds, accionTomada, usuarioId }
    );
  }

  /**
   * Ignora multiples alertas en batch.
   * 
   * @param alertaIds IDs de alertas
   * @param motivo Motivo de ignorar
   * @param usuarioId ID del usuario opcional
   * @returns Observable con resultado del batch
   */
  ignorarAlertasBatch(
    alertaIds: number[],
    motivo: string,
    usuarioId?: number
  ): Observable<{
    exitoso: boolean;
    alertasIgnoradas: AlertaInventario[];
    totalIgnoradas: number;
    mensaje: string;
  }> {
    return this.http.post<any>(
      `${this.baseUrl}/batch/ignorar`,
      { alertaIds, motivo, usuarioId }
    );
  }

  /**
   * Procesa alertas seleccionadas de forma automatica.
   * 
   * Ejecuta el flujo completo:
   * 1. Predicciones automaticas
   * 2. Optimizacion EOQ/ROP
   * 3. Generacion de ordenes de compra
   * 4. Actualizacion de alertas
   * 
   * @param request Datos del procesamiento
   * @returns Observable<ProcesamientoBatchResponse>
   */
  procesarAlertasAutomatico(
    request: ProcesarAlertasRequest
  ): Observable<ProcesamientoBatchResponse> {
    return this.http.post<ProcesamientoBatchResponse>(
      `${this.baseUrl}/procesar/automatico`,
      request
    );
  }

  /**
   * Procesa alertas y devuelve predicciones detalladas agrupadas por proveedor.
   * 
   * Este endpoint ejecuta predicciones para las alertas seleccionadas y devuelve
   * los resultados completos incluyendo:
   * - Predicciones con datos históricos y predichos (para gráficos)
   * - Métricas de calidad (MAE, MAPE, RMSE)
   * - Métricas agregadas por proveedor
   * - Información completa de productos y proveedores
   * 
   * @param request Datos del procesamiento (alertaIds y horizonteTiempo)
   * @returns Observable<Map<number, ResumenPrediccionPorProveedor>>
   */
  procesarAlertasConDetalles(
    request: ProcesarAlertasRequest
  ): Observable<Record<number, ResumenPrediccionPorProveedor>> {
    return this.http.post<Record<number, ResumenPrediccionPorProveedor>>(
      `${this.baseUrl}/procesar/con-detalles`,
      request
    );
  }

  /**
   * Verifica si las alertas son procesables.
   * 
   * @param alertaIds IDs de alertas a verificar
   * @returns Observable con resultado de validacion
   */
  verificarAlertasProcesables(alertaIds: number[]): Observable<{
    alertasValidas: number;
    alertasInvalidas: number;
    procesable: boolean;
    mensaje: string;
  }> {
    const params = new HttpParams().set('alertaIds', alertaIds.join(','));
    return this.http.get<any>(
      `${this.baseUrl}/procesar/verificar`,
      { params }
    );
  }

  /**
   * Obtiene el resumen detallado de ordenes de compra generadas.
   * 
   * Se utiliza para mostrar los detalles de las ordenes generadas
   * durante el procesamiento automatico de alertas en el frontend.
   * 
   * @param ordenIds IDs de ordenes de compra a consultar
   * @returns Observable<ResumenOrden[]>
   */
  obtenerResumenOrdenes(ordenIds: number[]): Observable<ResumenOrden[]> {
    const params = new HttpParams().set('ordenIds', ordenIds.join(','));
    return this.http.get<ResumenOrden[]>(
      `${this.baseUrl}/procesar/resumen-ordenes`,
      { params }
    );
  }
}
