import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { PageKardexResponse } from '../model/KardexResponse';
import { KardexCreateRequest } from '../model/KardexRequest';
import { environment } from '../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class MovimientoService {
  private readonly URL = `${environment.apiUrl}/movimientos`;
  private readonly http = inject(HttpClient);

  getKardex(page: number = 0, size: number = 10) {
    return this.http.get<PageKardexResponse>(`${this.URL}?page=${page}&size=${size}`);
  }

  createKardex(request: KardexCreateRequest) {
    return this.http.post(this.URL, request);
  }
  
  deleteKardex(kardexId: number) {
    return this.http.delete(`${this.URL}/${kardexId}`);
  }
}
