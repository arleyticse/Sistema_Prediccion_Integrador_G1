import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { GenerarPrediccionRequest, AlgoritmoInfo } from '../models/GenerarPrediccionRequest';
import { PrediccionResponse } from '../models/PrediccionResponse';
import { OptimizacionResponse, CalcularOptimizacionRequest } from '../models/OptimizacionResponse';
import { ProductoResponse } from '../../productos/models/ProductoResponse';
import { Page } from '../../../shared/models/Page';
import { environment } from '../../../environments/environment';
import type { RecomendacionAlgoritmo } from '../models/RecomendacionAlgoritmo';

@Injectable({
  providedIn: 'root'
})
export class PrediccionesService {
  
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/predicciones`;
  private readonly productosUrl = `${environment.apiUrl}/productos`;
  private readonly optimizacionUrl = `${environment.apiUrl}/optimizacion`;

  /**
   * Genera una nueva predicción de demanda.
   */
  generarPrediccion(request: GenerarPrediccionRequest): Observable<PrediccionResponse> {
    return this.http.post<PrediccionResponse>(`${this.baseUrl}/generar`, request);
  }

  /**
   * Obtiene todas las predicciones con paginación.
   */
  obtenerPredicciones(page: number = 0, size: number = 10): Observable<Page<PrediccionResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<PrediccionResponse>>(`${this.baseUrl}`, { params });
  }

  /**
   * Obtiene predicciones de un producto específico.
   */
  obtenerPrediccionesPorProducto(productoId: number, page: number = 0, size: number = 10): Observable<PrediccionResponse[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PrediccionResponse[]>(`${this.baseUrl}/producto/${productoId}`, { params });
  }

  /**
   * Obtiene la última predicción generada para un producto.
   */
  obtenerUltimaPrediccion(productoId: number): Observable<PrediccionResponse> {
    return this.http.get<PrediccionResponse>(`${this.baseUrl}/ultima/${productoId}`);
  }

  /**
   * Obtiene una predicción por su ID.
   */
  obtenerPrediccionPorId(prediccionId: number): Observable<PrediccionResponse> {
    return this.http.get<PrediccionResponse>(`${this.baseUrl}/${prediccionId}`);
  }

  /**
   * Elimina una predicción.
   */
  eliminarPrediccion(prediccionId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${prediccionId}`);
  }

  /**
   * Obtiene la lista de algoritmos disponibles.
   */
  obtenerAlgoritmosDisponibles(): Observable<{ [key: string]: string }> {
    return this.http.get<{ [key: string]: string }>(`${this.baseUrl}/algoritmos`);
  }

  /**
   * Obtiene información detallada de todos los algoritmos.
   */
  obtenerInfoAlgoritmos(): Observable<{ [key: string]: any }> {
    return this.http.get<{ [key: string]: any }>(`${this.baseUrl}/algoritmos/info`);
  }

  /**
   * Obtiene productos disponibles para predicción.
   */
  obtenerProductos(page: number = 0, size: number = 50): Observable<Page<ProductoResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<ProductoResponse>>(`${this.productosUrl}`, { params });
  }

  /**
   * Obtiene una recomendación automática de algoritmo basada en el análisis de datos históricos.
   */
  obtenerRecomendacion(productoId: number): Observable<RecomendacionAlgoritmo> {
    return this.http.get<RecomendacionAlgoritmo>(`${this.baseUrl}/recomendar/${productoId}`);
  }

  /**
   * Calcula la optimización EOQ/ROP para una predicción.
   */
  calcularOptimizacion(request: CalcularOptimizacionRequest): Observable<OptimizacionResponse> {
    return this.http.post<OptimizacionResponse>(`${this.optimizacionUrl}/calcular`, request);
  }

  /**
   * Obtiene la última optimización calculada para una predicción.
   */
  obtenerOptimizacionPorPrediccion(prediccionId: number): Observable<OptimizacionResponse> {
    return this.http.get<OptimizacionResponse>(`${this.optimizacionUrl}/prediccion/${prediccionId}`);
  }
}