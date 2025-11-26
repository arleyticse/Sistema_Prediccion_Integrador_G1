import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { GenerarOrdenRequest } from '../models/GenerarOrdenRequest';
import { OrdenCompraResponse } from '../models/OrdenCompraResponse';
import { PrediccionResponse } from '../models/PrediccionResponse';
import { Page } from '../../../shared/models/Page';
import { environment } from '../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class OrdenesCompraService {
  
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ordenes`;
  private readonly prediccionUrl = `${environment.apiUrl}/predicciones`;

  generarOrden(request: GenerarOrdenRequest): Observable<OrdenCompraResponse> {
    return this.http.post<OrdenCompraResponse>(`${this.baseUrl}/generar/${request.prediccionId}`, request);
  }

  obtenerOrdenPorId(ordenId: number): Observable<OrdenCompraResponse> {
    return this.http.get<OrdenCompraResponse>(`${this.baseUrl}/${ordenId}`);
  }

  obtenerOrdenesPorProducto(productoId: number, page: number = 0, size: number = 10): Observable<Page<OrdenCompraResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<OrdenCompraResponse>>(`${this.baseUrl}/producto/${productoId}`, { params });
  }

  obtenerTodasLasOrdenes(page: number = 0, size: number = 10): Observable<Page<OrdenCompraResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<OrdenCompraResponse>>(`${this.baseUrl}`, { params });
  }

  obtenerUltimaOrden(productoId: number): Observable<OrdenCompraResponse> {
    return this.http.get<OrdenCompraResponse>(`${this.baseUrl}/ultima/${productoId}`);
  }

  confirmarOrden(ordenId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${ordenId}/confirmar`, {});
  }

  cancelarOrden(ordenId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${ordenId}`);
  }

  recibirOrden(ordenId: number, request: any): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${ordenId}/recibir`, request);
  }

  obtenerOrdenesBorrador(): Observable<OrdenCompraResponse[]> {
    return this.http.get<OrdenCompraResponse[]>(`${this.baseUrl}/borradores`);
  }

  aprobarOrdenesBorrador(ordenIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/aprobar-borrador`, ordenIds);
  }

  obtenerPredicciones(page: number = 0, size: number = 10): Observable<Page<PrediccionResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<PrediccionResponse>>(`${this.prediccionUrl}`, { params });
  }

  obtenerPrediccionPorId(prediccionId: number): Observable<PrediccionResponse> {
    return this.http.get<PrediccionResponse>(`${this.prediccionUrl}/${prediccionId}`);
  }
}