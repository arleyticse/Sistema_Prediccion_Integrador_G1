import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { signal, effect } from '@angular/core';
import { AuthRequest, AuthResponse, UsuarioInfo } from '../models/auth.models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private apiUrl = `${environment.apiUrl}/auth`;

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
      const usuario = this.usuarioSignal();

      if (token && usuario) {
        localStorage.setItem('authToken', token);
        if (usuario.refreshToken) {
          localStorage.setItem('refreshToken', usuario.refreshToken);
        }
        localStorage.setItem('usuario', JSON.stringify(usuario));
        console.log(' Token guardado en localStorage');
      } else {
        localStorage.removeItem('authToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('usuario');
        console.log(' Token eliminado de localStorage');
      }
    });

    // Verificar token al iniciar
    const storedToken = this.getTokenFromStorage();
    if (storedToken) {
      console.log(' Token recuperado del localStorage al iniciar');
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
    console.log('Estableciendo datos de autenticación');
    this.tokenSignal.set(response.token);
    this.usuarioSignal.set({
      token: response.token,
      refreshToken: response.refreshToken,
      nombreCompleto: response.nombreCompleto,
      email: response.email,
      rol: response.rol
    });
  }

  /**
   * Logout: limpiar token y usuario y notificar al backend
   */
  logout(): boolean {
    console.log('Cerrando sesión');

    this.http.post(`${this.apiUrl}/cerrar-sesion`, {}).subscribe({
      next: () => console.log('Sesión cerrada en backend'),
      error: (err) => console.warn('Error al cerrar sesión en backend', err),
      complete: () => {
        this.tokenSignal.set(null);
        this.usuarioSignal.set(null);
        this.router.navigate(['/login']);
        return true;
      }
    });

    // Fallback por si la petición tarda mucho o falla
    setTimeout(() => {
      if (this.tokenSignal()) {
        this.tokenSignal.set(null);
        this.usuarioSignal.set(null);
        this.router.navigate(['/login']);
        return true;
      }
      return false;
    }, 1000);
    return false;
  }

  /**
   * Refrescar token
   */
  refreshToken() {
    const refreshToken = this.usuarioSignal()?.refreshToken || localStorage.getItem('refreshToken');
    if (!refreshToken) {
      this.logout();
      throw new Error('No refresh token available');
    }

    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh-token`, { refreshToken });
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
      if (usuario) {
        const parsedUsuario = JSON.parse(usuario);
        // Asegurar que refreshToken se recupere si está guardado aparte o en el objeto usuario
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken && !parsedUsuario.refreshToken) {
          parsedUsuario.refreshToken = refreshToken;
        }
        return parsedUsuario;
      }
    }
    return null;
  }

  /**
   * Solicitar código OTP para desbloquear cuenta bloqueada
   */
  solicitarDesbloqueo(email: string) {
    return this.http.post<{ success: boolean; message: string }>(`${this.apiUrl}/solicitar-desbloqueo`, { email });
  }

  /**
   * Desbloquear cuenta usando código OTP
   */
  desbloquearCuenta(email: string, code: string) {
    return this.http.post<{ success: boolean; message: string }>(`${this.apiUrl}/desbloquear-cuenta`, { email, code });
  }

  /**
   * Verificar estado de bloqueo de una cuenta
   */
  verificarEstadoCuenta(email: string) {
    return this.http.get<{ bloqueada: boolean; intentosRestantes: number; maxIntentos: number }>(
      `${this.apiUrl}/estado-cuenta?email=${encodeURIComponent(email)}`
    );
  }
}