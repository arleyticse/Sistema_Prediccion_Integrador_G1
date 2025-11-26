import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { GenerarPrediccionRequest, AlgoritmoInfo } from '../models/GenerarPrediccionRequest';
import { PrediccionResponse } from '../models/PrediccionResponse';
import { OptimizacionResponse, CalcularOptimizacionRequest } from '../models/OptimizacionResponse';
import { ProductoResponse } from '../../productos/models/ProductoResponse';
import { Page } from '../../../shared/models/Page';
import { environment } from '../../../environments/environment';
import { SmartPrediccionRequest, SmartPrediccionResponse } from '../models/SmartPrediccionRequest';

@Injectable({
  providedIn: 'root'
})
export class PrediccionesService {
  
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/predicciones`;
  private readonly smartBaseUrl = `${environment.apiUrl}/v2/predicciones`;
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

  // ========== MÉTODOS PARA PREDICCIONES INTELIGENTES CON SMILE ML ==========

  /**
   * Genera predicción inteligente usando algoritmos avanzados de Smile ML.
   * Soporta: ARIMA, RandomForest, GradientBoosting, o selección AUTO.
   * 
   * @param request Configuración de predicción inteligente
   * @returns Observable<SmartPrediccionResponse> con métricas avanzadas
   */
  generarPrediccionInteligente(request: SmartPrediccionRequest): Observable<SmartPrediccionResponse> {
    return this.http.post<SmartPrediccionResponse>(`${this.smartBaseUrl}/inteligente`, request);
  }

  /**
   * Genera predicción automática con configuración inteligente.
   * Usa selección automática de algoritmo y horizonte calculado con autocorrelación.
   * 
   * @param productoId ID del producto
   * @returns Observable con predicción y configuración usada
   */
  generarPrediccionAutomatica(productoId: number): Observable<{
    productoId: number;
    timestamp: string;
    modo: string;
    configuracionUsada: {
      algoritmo: string;
      horizonte: number;
      horizonteCalculadoConSmile: boolean;
      estacionalidad: boolean;
      ordenCompra: boolean;
    };
    resultado: SmartPrediccionResponse;
  }> {
    return this.http.post<any>(`${this.smartBaseUrl}/automatico/${productoId}`, {});
  }

  /**
   * Calcula horizonte de predicción óptimo basado en análisis ML.
   * 
   * @param productoId ID del producto
   * @returns Observable con horizonte recomendado y justificación
   */
  calcularHorizonteAutomatico(productoId: number): Observable<{
    productoId: number;
    horizonteRecomendado: number;
    justificacion: string;
    rangoValido: string;
    metodoCalculo: string;
    factoresConsiderados: any;
  }> {
    return this.http.get<any>(`${this.smartBaseUrl}/horizonte-automatico/${productoId}`);
  }

  /**
   * Obtiene información sobre algoritmos Smile ML disponibles.
   * 
   * @returns Observable con detalles de algoritmos ML
   */
  obtenerAlgoritmosSmile(): Observable<{
    algoritmos: string[];
    framework: string;
    seleccionAutomatica: string;
    caracteristicas: string[];
    tiposDeModelos: any;
    metricas: string[];
  }> {
    return this.http.get<any>(`${this.smartBaseUrl}/algoritmos-smile`);
  }

  /**
   * Valida si un producto tiene suficientes datos históricos para predicción ML.
   * 
   * @param productoId ID del producto
   * @param minimoRegistros Mínimo de registros requeridos
   * @returns Observable con resultado de validación
   */
  validarDatosProducto(productoId: number, minimoRegistros: number = 30): Observable<{
    productoId: number;
    datosValidos: boolean;
    minimoRequerido: number;
    recomendacion: string;
  }> {
    const params = new HttpParams().set('minimoRegistros', minimoRegistros.toString());
    return this.http.get<any>(`${this.smartBaseUrl}/validar-datos/${productoId}`, { params });
  }



  /**
   * Obtiene el estado del servicio de predicción inteligente.
   * 
   * @returns Observable con estado y capacidades del servicio
   */
  obtenerEstadoServicio(): Observable<{
    servicio: string;
    estado: string;
    framework: string;
    algoritmosDisponibles: number;
    algoritmos: string[];
    version: string;
    funcionalidades: string[];
  }> {
    return this.http.get<any>(`${this.smartBaseUrl}/estado`);
  }
}