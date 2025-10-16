import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { UnidadMedida } from '../models/UnidadMedida';

@Injectable({
  providedIn: 'root'
})
export class UnidaMedidaService {
  URL = "http://localhost:8080/api/unidades-medida";
  http = inject(HttpClient);

  obtenerUnidadesMedida() {
    return this.http.get<UnidadMedida[]>(this.URL);
  }

  crearUnidadMedida(unidadMedida: UnidadMedida) {
    return this.http.post<UnidadMedida>(this.URL, unidadMedida);
  }

  actualizarUnidadMedida(unidadMedida: UnidadMedida) {
    return this.http.put<UnidadMedida>(`${this.URL}/${unidadMedida.unidadMedidaId}`, unidadMedida);
  }

  eliminarUnidadMedida(id: number) {
    return this.http.delete(`${this.URL}/${id}`);
  }
}
