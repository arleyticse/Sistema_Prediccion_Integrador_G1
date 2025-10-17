import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Proveedor } from '../model/Proveedor';

@Injectable({
  providedIn: 'root'
})
export class ProveedorService {
  private readonly URL = 'http://localhost:8080/api/proveedores';
  private readonly http = inject(HttpClient);

  getProveedores() {
    return this.http.get<Proveedor[]>(this.URL);
  }
  createProveedor(proveedor: Proveedor) {
    return this.http.post<Proveedor>(this.URL, proveedor);
  }
  updateProveedor(proveedorId: number, proveedor: Proveedor) {
    return this.http.put<Proveedor>(`${this.URL}/${proveedorId}`, proveedor);
  }
  deleteProveedor(proveedorId: number) {
    return this.http.delete<void>(`${this.URL}/${proveedorId}`);
  }
}
