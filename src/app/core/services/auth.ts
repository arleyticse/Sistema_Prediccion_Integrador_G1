import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { signal, effect } from '@angular/core';
import { AuthRequest, AuthResponse, UsuarioInfo } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  
  private apiUrl = 'http://localhost:8080/api/auth';
  
  private tokenSignal = signal<string | null>(this.getTokenFromStorage());
  private usuarioSignal = signal<UsuarioInfo | null>(this.getUsuarioFromStorage());
  
  public token = this.tokenSignal.asReadonly();
  public usuario = this.usuarioSignal.asReadonly();
  
  get isAutenticado(): boolean {
    const isAuth = !!this.tokenSignal();
    return isAuth;
  }
  
  get nombreUsuario(): string {
    return this.usuarioSignal()?.nombreCompleto || 'Usuario';
  }

  constructor() {
    // Sincronizar con localStorage cuando cambien los signals
    effect(() => {
      const token = this.tokenSignal();
      if (token) {
        localStorage.setItem('authToken', token);
        localStorage.setItem('usuario', JSON.stringify(this.usuarioSignal()));
        console.log('✅ Token guardado en localStorage');
      } else {
        localStorage.removeItem('authToken');
        localStorage.removeItem('usuario');
        console.log('🗑️ Token eliminado de localStorage');
      }
    });

    // Verificar token al iniciar
    const storedToken = this.getTokenFromStorage();
    if (storedToken) {
      console.log('🔑 Token recuperado del localStorage al iniciar');
    }
  }

  /**
   * Login con email y contraseña
   */
  login(email: string, clave: string) {
    const request: AuthRequest = { email, clave };
    return this.http.post<AuthResponse>(`${this.apiUrl}/iniciar-sesion`, request);
  }

  /**
   * Establecer token y usuario después de login exitoso
   */
  setAuthData(response: AuthResponse) {
    console.log('📝 Estableciendo datos de autenticación');
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
    console.log('👋 Cerrando sesión');
    this.tokenSignal.set(null);
    this.usuarioSignal.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Obtener token actual (usado por el interceptor)
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
   * Obtener token del localStorage (solo al iniciar)
   */
  private getTokenFromStorage(): string | null {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      const token = localStorage.getItem('authToken');
      return token;
    }
    return null;
  }

  /**
   * Obtener usuario del localStorage (solo al iniciar)
   */
  private getUsuarioFromStorage(): UsuarioInfo | null {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      const usuario = localStorage.getItem('usuario');
      return usuario ? JSON.parse(usuario) : null;
    }
    return null;
  }
}