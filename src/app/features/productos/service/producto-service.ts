import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { PageProductoResponse } from '../models/ProductoResponse';
import { ProductoRequest } from '../models/ProductoRequest';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
    URL = 'http://localhost:8080/api/productos';
  http = inject(HttpClient);

  obtenerProductos(page: number, size: number) {
    return this.http.get<PageProductoResponse>(`${this.URL}?page=${page}&size=${size}`);
  }
  crearProducto(producto: ProductoRequest) {
    return this.http.post<ProductoRequest>(this.URL, producto);
  }
  actualizarProducto(producto: ProductoRequest, id: number) {
    return this.http.put<ProductoRequest>(`${this.URL}/${id}`, producto);
  }
  eliminarProducto(id: number) {
    return this.http.delete(`${this.URL}/${id}`);
  }
}
