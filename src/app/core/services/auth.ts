import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { signal, effect } from '@angular/core';
import { AuthRequest, AuthResponse, UsuarioInfo } from '../models/auth.models';

export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  
  // URL del backend
  private apiUrl = 'http://localhost:8080/api/auth';
  
  // Signals para estado reactivo
  private tokenSignal = signal<string | null>(this.getTokenFromStorage());
  private usuarioSignal = signal<UsuarioInfo | null>(this.getUsuarioFromStorage());
  
  // Público: readonly signals
  public token = this.tokenSignal.asReadonly();
  public usuario = this.usuarioSignal.asReadonly();
  
  // Computed: si está autenticado
  get isAutenticado(): boolean {
    return !!this.tokenSignal();
  }
  
  get nombreUsuario(): string {
    return this.usuarioSignal()?.nombreCompleto || 'Usuario';
  }

  constructor() {
    // Sincronizar con localStorage cuando cambia el token
    effect(() => {
      const token = this.tokenSignal();
      if (token) {
        localStorage.setItem('authToken', token);
        localStorage.setItem('usuario', JSON.stringify(this.usuarioSignal()));
      } else {
        localStorage.removeItem('authToken');
        localStorage.removeItem('usuario');
      }
    });
  }

  /**
   * Login con email y contraseña
   */
  login(email: string, clave: string) {
    const request: AuthRequest = { email, clave };
    return this.http.post<AuthResponse>(`${this.apiUrl}/iniciar-sesion`, request);
  }

  /**
   * Registro de nuevo usuario
   */
  register(email: string, nombre: string, clave: string) {
    const request = { email, nombre, clave };
    return this.http.post<AuthResponse>(`${this.apiUrl}/registro`, request);
  }

  /**
   * Establecer token y usuario después de login exitoso
   */
  setAuthData(response: AuthResponse) {
    this.tokenSignal.set(response.token);
    this.usuarioSignal.set({
      token: response.token,
      nombreCompleto: response.nombreCompleto,
      email: response.email,
      rol: response.rol
    });
  }

  /**
   * Logout: limpiar token y usuario
   */
  logout() {
    this.tokenSignal.set(null);
    this.usuarioSignal.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Obtener token actual
   */
  getToken(): string | null {
    return this.tokenSignal();
  }

  /**
   * Obtener usuario actual
   */
  getUsuario(): UsuarioInfo | null {
    return this.usuarioSignal();
  }

  /**
   * Obtener token del localStorage
   */
  private getTokenFromStorage(): string | null {
    if (typeof localStorage !== 'undefined') {
      return localStorage.getItem('authToken');
    }
    return null;
  }

  /**
   * Obtener usuario del localStorage
   */
  private getUsuarioFromStorage(): UsuarioInfo | null {
    if (typeof localStorage !== 'undefined') {
      const usuario = localStorage.getItem('usuario');
      return usuario ? JSON.parse(usuario) : null;
    }
    return null;
  }
}
