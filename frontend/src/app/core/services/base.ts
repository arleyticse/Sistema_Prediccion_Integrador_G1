import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

/**
 * Servicio base genérico para CRUD operations
 * Todos los servicios deben extender de este
 */
export class BaseService<T> {
  protected http = inject(HttpClient);

  /**
   * GET - Obtener lista de elementos
   */
  getAll<R>(endpoint: string) {
    return this.http.get<R[]>(endpoint);
  }

  /**
   * GET - Obtener un elemento por ID
   */
  getById<R>(endpoint: string, id: number) {
    return this.http.get<R>(`${endpoint}/${id}`);
  }

  /**
   * POST - Crear nuevo elemento
   */
  create<R>(endpoint: string, body: T) {
    return this.http.post<R>(endpoint, body);
  }

  /**
   * PUT - Actualizar elemento
   */
  update<R>(endpoint: string, id: number, body: T) {
    return this.http.put<R>(`${endpoint}/${id}`, body);
  }

  /**
   * DELETE - Eliminar elemento
   */
  delete(endpoint: string, id: number) {
    return this.http.delete(`${endpoint}/${id}`);
  }

  /**
   * GET - Método genérico
   */
  get<R>(endpoint: string) {
    return this.http.get<R>(endpoint);
  }

  /**
   * POST - Método genérico
   */
  post<R>(endpoint: string, body: any) {
    return this.http.post<R>(endpoint, body);
  }

  /**
   * PUT - Método genérico
   */
  put<R>(endpoint: string, body: any) {
    return this.http.put<R>(endpoint, body);
  }

  /**
   * DELETE - Método genérico
   */
  delete_generic<R>(endpoint: string) {
    return this.http.delete<R>(endpoint);
  }
}
