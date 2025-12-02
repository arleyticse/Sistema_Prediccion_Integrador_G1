import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { PageProductoResponse, ProductoResponse, ProductoSimpleResponse } from '../models/ProductoResponse';
import { ProductoRequest } from '../models/ProductoRequest';
import { Observable, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
interface CacheEntry<T> {
  data: T;
  timestamp: number;
}
@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private readonly URL = `${environment.apiUrl}/productos`;
  private readonly http = inject(HttpClient);
  private readonly CACHE_DURATION = 5 * 60 * 1000;

  private cache = new Map<string, CacheEntry<unknown>>();

  obtenerProductos(page: number, size: number): Observable<PageProductoResponse> {
    const cacheKey = `productos_${page}_${size}`;
    const cached = this.getFromCache<PageProductoResponse>(cacheKey);

    if (cached) {
      return of(cached);
    }

    return this.http.get<PageProductoResponse>(`${this.URL}?page=${page}&size=${size}`).pipe(
      tap(response => this.setCache(cacheKey, response))
    );
  }

  buscarGlobal(nombre: string, page: number, size: number): Observable<PageProductoResponse> {
    const cacheKey = `productos_buscar_${nombre}_${page}_${size}`;
    const cached = this.getFromCache<PageProductoResponse>(cacheKey);

    if (cached) {
      return of(cached);
    }

    return this.http.get<PageProductoResponse>(`${this.URL}/buscar-global?nombre=${encodeURIComponent(nombre)}&pagina=${page}&tamanioPagina=${size}`).pipe(
      tap(response => this.setCache(cacheKey, response))
    );
  }

  crearProducto(producto: ProductoRequest): Observable<ProductoResponse> {
    return this.http.post<ProductoResponse>(this.URL, producto).pipe(
      tap(() => this.invalidateCache())
    );
  }

  actualizarProducto(producto: ProductoRequest, id: number): Observable<ProductoResponse> {
    return this.http.put<ProductoResponse>(`${this.URL}/${id}`, producto).pipe(
      tap(() => this.invalidateCache())
    );
  }

  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.URL}/${id}`).pipe(
      tap(() => this.invalidateCache())
    );
  }

  /** Versión optimizada para dropdowns - solo id, nombre y categoría */
  obtenerProductosSimple(): Observable<ProductoSimpleResponse[]> {
    const cacheKey = 'productos_simple';
    const cached = this.getFromCache<ProductoSimpleResponse[]>(cacheKey);

    if (cached) {
      return of(cached);
    }

    return this.http.get<ProductoSimpleResponse[]>(`${this.URL}/simple`).pipe(
      tap(response => this.setCache(cacheKey, response))
    );
  }

  /** @deprecated Usar obtenerProductosSimple() para mejor rendimiento */
  obtenerTodosProductos(): Observable<ProductoResponse[]> {
    const cacheKey = 'productos_todos';
    const cached = this.getFromCache<ProductoResponse[]>(cacheKey);

    if (cached) {
      return of(cached);
    }

    return this.http.get<ProductoResponse[]>(`${this.URL}/todos`).pipe(
      tap(response => this.setCache(cacheKey, response))
    );
  }

  private getFromCache<T>(key: string): T | null {
    const entry = this.cache.get(key) as CacheEntry<T> | undefined;

    if (!entry) {
      return null;
    }

    const now = Date.now();
    if (now - entry.timestamp > this.CACHE_DURATION) {
      this.cache.delete(key);
      return null;
    }

    return entry.data;
  }

  private setCache<T>(key: string, data: T): void {
    this.cache.set(key, {
      data,
      timestamp: Date.now()
    });
  }

  private invalidateCache(): void {
    this.cache.clear();
  }
}
