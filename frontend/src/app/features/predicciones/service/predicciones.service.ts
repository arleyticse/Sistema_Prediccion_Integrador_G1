import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { GenerarPrediccionRequest } from '../models/GenerarPrediccionRequest';
import { PrediccionResponse } from '../../ordenes-compra/models/PrediccionResponse';
import { ProductoResponse } from '../../productos/models/ProductoResponse';
import { Page } from '../../../shared/models/Page';
import { environment } from '../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class PrediccionesService {
  
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/predicciones`;
  private readonly productosUrl = `${environment.apiUrl}/productos`;

  generarPrediccion(request: GenerarPrediccionRequest): Observable<PrediccionResponse> {
    return this.http.post<PrediccionResponse>(`${this.baseUrl}/generar/${request.productoId}`, request);
  }

  obtenerPredicciones(page: number = 0, size: number = 10): Observable<Page<PrediccionResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<PrediccionResponse>>(`${this.baseUrl}`, { params });
  }

  obtenerPrediccionesPorProducto(productoId: number, page: number = 0, size: number = 10): Observable<PrediccionResponse[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PrediccionResponse[]>(`${this.baseUrl}/producto/${productoId}`, { params });
  }

  obtenerUltimaPrediccion(productoId: number): Observable<PrediccionResponse> {
    return this.http.get<PrediccionResponse>(`${this.baseUrl}/ultima/${productoId}`);
  }

  obtenerPrediccionPorId(prediccionId: number): Observable<PrediccionResponse> {
    return this.http.get<PrediccionResponse>(`${this.baseUrl}/${prediccionId}`);
  }

  eliminarPrediccion(prediccionId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${prediccionId}`);
  }

  obtenerProductos(page: number = 0, size: number = 50): Observable<Page<ProductoResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<ProductoResponse>>(`${this.productosUrl}`, { params });
  }
}