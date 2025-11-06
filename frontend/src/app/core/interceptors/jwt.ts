import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError, tap } from 'rxjs';
import { AuthService } from '../services/auth';

/**
 * Interceptor funcional para agregar JWT token a las peticiones HTTP
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Endpoints públicos que NO necesitan token
  const publicEndpoints = [
    '/iniciar-sesion',
    '/registro',
    '/auth/login',
    '/auth/register',
    '/plantilla', // Descargas de plantillas CSV
    '/validar'    // Validación de CSV (puede ser pública)
  ];

  // Verificar si es endpoint público
  const isPublic = publicEndpoints.some(endpoint => req.url.includes(endpoint));
  
  if (isPublic) {
    return next(req);
  }

  // Obtener token
  const token = authService.getToken();

  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    
    return next(clonedRequest).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Error HTTP:', {
          status: error.status,
          statusText: error.statusText,
          url: error.url
        });

        // Si es 401, token inválido/expirado - cerrar sesión
        if (error.status === 401) {
          console.error('Token inválido o expirado - cerrando sesión');
          authService.logout();
        }
        
        if (error.status === 403) {
          console.warn('Acceso denegado - verifica permisos del backend');
        }

        return throwError(() => error);
      })
    );
  } else {
    return next(req);
  }
};