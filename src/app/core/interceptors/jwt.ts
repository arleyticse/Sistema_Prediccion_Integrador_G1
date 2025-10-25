import { Injectable, inject } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  private authService = inject(AuthService);
  private refreshTokenInProgress = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  /**
   * URLs públicas que NO necesitan token
   */
  private publicUrls = [
    '/iniciar-sesion',
    '/registro'
  ];

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Agregar token a la solicitud si existe
    const token = this.authService.getToken();
    
    if (token && !this.isPublicUrl(request.url)) {
      request = this.addToken(request, token);
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !this.isPublicUrl(error.url || '')) {
          // Token expiró, intentar refresh
          return this.handle401Error(request, next);
        }
        return throwError(() => error);
      })
    );
  }

  /**
   * Agregar token JWT a la solicitud
   */
  private addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  /**
   * Manejar error 401
   */
  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.refreshTokenInProgress) {
      this.refreshTokenInProgress = true;
      this.refreshTokenSubject.next(null);

      // Si hay token, intentar refresh (esto es un stub para versión futura)
      const token = this.authService.getToken();
      if (token) {
        // Aquí iría la llamada para refrescar el token
        // Por ahora, logout
        this.authService.logout();
        this.refreshTokenInProgress = false;
        return throwError(() => new Error('Token expirado'));
      }
    }

    return this.refreshTokenSubject.pipe(
      filter(result => result !== null),
      take(1),
      switchMap((token: string | null) => {
        if (token) {
          return next.handle(this.addToken(request, token));
        }
        this.authService.logout();
        return throwError(() => new Error('No se pudo refrescar el token'));
      })
    );
  }

  /**
   * Verificar si la URL es pública
   */
  private isPublicUrl(url: string): boolean {
    return this.publicUrls.some(publicUrl => url.includes(publicUrl));
  }
}
