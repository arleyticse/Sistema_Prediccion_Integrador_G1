import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageKardexResponse } from '../models/KardexResponse';

@Injectable({
  providedIn: 'root'
})
export class KardexService {
  private readonly URL = `${environment.apiUrl}/movimientos`;
  private readonly http = inject(HttpClient);

  obtenerMovimientosPorProducto(productoId: number, pagina: number = 0, tamano: number = 10): Observable<PageKardexResponse> {
    return this.http.get<PageKardexResponse>(
      `${this.URL}/producto/${productoId}?pagina=${pagina}&tamano=${tamano}`
    );
  }

  obtenerUltimoMovimiento(productoId: number): Observable<any> {
    return this.http.get(`${this.URL}/producto/${productoId}/ultimo`);
  }
}
