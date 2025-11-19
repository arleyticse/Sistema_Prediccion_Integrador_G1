import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthResponse } from '../../../core/models/auth.models';
import { environment } from '../../../environments/environment';

/**
 * Modelo para solicitud de creación de usuario
 */
export interface UsuarioCreateRequest {
  nombre: string;
  email: string;
  claveHash: string;
  rol: 'ADMIN' | 'GERENTE' | 'OPERARIO';
}

/**
 * Servicio para gestión de usuarios por administradores
 * RF001: El sistema permite al administrador registrar usuarios
 */
@Injectable({
  providedIn: 'root'
})
export class UsuarioAdminService {
  
  private readonly baseUrl = `${environment.apiUrl}/api/admin/usuarios`;
  private readonly http = inject(HttpClient);

  /**
   * RF001: Registrar nuevo usuario (solo administradores)
   */
  registrarUsuario(usuario: UsuarioCreateRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/registrar`, usuario);
  }

  /**
   * Listar todos los usuarios del sistema
   */
  listarUsuarios(): Observable<AuthResponse[]> {
    return this.http.get<AuthResponse[]>(`${this.baseUrl}/listar`);
  }

  /**
   * Actualizar rol de un usuario
   */
  actualizarRol(usuarioId: number, nuevoRol: string): Observable<AuthResponse> {
    return this.http.put<AuthResponse>(
      `${this.baseUrl}/${usuarioId}/rol`,
      null,
      { params: { nuevoRol } }
    );
  }

  /**
   * Eliminar usuario del sistema
   */
  eliminarUsuario(usuarioId: number): Observable<string> {
    return this.http.delete<string>(`${this.baseUrl}/${usuarioId}`);
  }
}