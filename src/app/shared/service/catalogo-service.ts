import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { TipoDeMovimiento } from '../models/TipoDeMovimiento';

@Injectable({
  providedIn: 'root'
})
export class CatalogoService {
  private readonly URL = "http://localhost:8080/api/catalogos";
  private readonly http = inject(HttpClient);

  getTiposDeMovimiento(){
    return this.http.get<TipoDeMovimiento[]>(`${this.URL}/tipos-movimiento`);
  }
}
