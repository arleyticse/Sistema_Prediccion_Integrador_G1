import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Categoria } from '../models/Categoria';

@Injectable({
  providedIn: 'root'
})
export class CategoriaServicio {
  URL = 'http://localhost:8080/api/categorias';
  http = inject(HttpClient);

  obtenerCategorias() {
    return this.http.get<Categoria[]>(this.URL);
  }
  crearCategoria(categoria: Categoria) {
    return this.http.post<Categoria>(this.URL, categoria);
  }
  actualizarCategoria(categoria: Categoria) {
    return this.http.put<Categoria>(`${this.URL}/${categoria.categoriaId}`, categoria);
  }
  eliminarCategoria(id: number) {
    return this.http.delete(`${this.URL}/${id}`);
  }
}
