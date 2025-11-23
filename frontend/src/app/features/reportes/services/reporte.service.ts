import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReportePrediccionDTO } from '../models/ReportePrediccion';
import { ReporteInventarioDTO } from '../models/ReporteInventario';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/reportes`;

  obtenerReportePredicciones(fechaInicio?: string, fechaFin?: string): Observable<ReportePrediccionDTO> {
    let params = new HttpParams();
    if (fechaInicio) params = params.set('fechaInicio', fechaInicio);
    if (fechaFin) params = params.set('fechaFin', fechaFin);

    return this.http.get<ReportePrediccionDTO>(`${this.apiUrl}/predicciones`, { params });
  }

  obtenerReporteInventario(categoriaId?: number): Observable<ReporteInventarioDTO> {
    let params = new HttpParams();
    if (categoriaId) params = params.set('categoriaId', categoriaId.toString());

    return this.http.get<ReporteInventarioDTO>(`${this.apiUrl}/inventario`, { params });
  }

  obtenerReportePrediccionesPorProducto(
    productoId: number,
    fechaInicio?: string,
    fechaFin?: string
  ): Observable<ReportePrediccionDTO> {
    let params = new HttpParams();
    if (fechaInicio) params = params.set('fechaInicio', fechaInicio);
    if (fechaFin) params = params.set('fechaFin', fechaFin);

    return this.http.get<ReportePrediccionDTO>(
      `${this.apiUrl}/predicciones/producto/${productoId}`,
      { params }
    );
  }
}
