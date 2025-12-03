import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  DashboardCompleto,
  DashboardEstadisticas,
  DistribucionInventario,
  DistribucionCategoria,
  DistribucionAlertas,
  ProductoStockBajo
} from '../models/dashboard.models';

/**
 * Servicio para obtener datos del dashboard del gerente.
 * Consume los endpoints de /api/dashboard del backend.
 */
@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/dashboard`;

  /**
   * Obtiene el dashboard completo con todas las métricas y gráficos.
   * Este es el endpoint principal que retorna toda la información en una sola llamada.
   */
  obtenerDashboardCompleto(): Observable<DashboardCompleto> {
    return this.http.get<DashboardCompleto>(`${this.apiUrl}/completo`);
  }

  /**
   * Obtiene solo las estadísticas generales del negocio.
   */
  obtenerEstadisticas(): Observable<DashboardEstadisticas> {
    return this.http.get<DashboardEstadisticas>(`${this.apiUrl}/estadisticas`);
  }

  /**
   * Obtiene la distribución del inventario por estado.
   */
  obtenerDistribucionInventario(): Observable<DistribucionInventario[]> {
    return this.http.get<DistribucionInventario[]>(`${this.apiUrl}/distribucion-inventario`);
  }

  /**
   * Obtiene la distribución de productos por categoría.
   */
  obtenerDistribucionCategorias(): Observable<DistribucionCategoria[]> {
    return this.http.get<DistribucionCategoria[]>(`${this.apiUrl}/distribucion-categorias`);
  }

  /**
   * Obtiene la distribución de alertas por tipo.
   */
  obtenerDistribucionAlertas(): Observable<DistribucionAlertas[]> {
    return this.http.get<DistribucionAlertas[]>(`${this.apiUrl}/distribucion-alertas`);
  }

  /**
   * Obtiene los productos con stock bajo.
   */
  obtenerProductosStockBajo(): Observable<ProductoStockBajo[]> {
    return this.http.get<ProductoStockBajo[]>(`${this.apiUrl}/productos-stock-bajo`);
  }
}
