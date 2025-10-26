import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { TipoDeMovimiento } from '../models/TipoDeMovimiento';
import { environment } from '../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class CatalogoService {
  private readonly URL = `${environment.apiUrl}/catalogos`;
  private readonly http = inject(HttpClient);

  getTiposDeMovimiento(){
    return this.http.get<TipoDeMovimiento[]>(`${this.URL}/tipos-movimiento`);
  }
}
