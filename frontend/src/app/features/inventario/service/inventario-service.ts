import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { InventarioCreateRequest } from '../model/InventarioRequest';
import { PageInventarioResponse } from '../model/InventarioResponse';
import { environment } from '../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class InventarioService {
  private readonly URL = `${environment.apiUrl}/inventarios`;
  private readonly http = inject(HttpClient);


  obtenerInventarios(page: number, size: number) {
    return this.http.get<PageInventarioResponse>(`${this.URL}?page=${page}&size=${size}`);
  }

  crearInventario(inventario: InventarioCreateRequest){
    return this.http.post(this.URL, inventario);
  }
  actualizarInventario(inventarioId:number, inventario: InventarioCreateRequest){
    return this.http.put(`${this.URL}/${inventarioId}`, inventario);
  }
  eliminarInventario(inventarioId:number){
    return this.http.delete(`${this.URL}/${inventarioId}`);
  }
}
