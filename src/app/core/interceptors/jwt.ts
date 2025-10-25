import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth';

/**
 * Interceptor funcional para agregar JWT token a las peticiones HTTP
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  console.log('Interceptando solicitud:', req.url);

  // Endpoints públicos que NO necesitan token
  const publicEndpoints = [
    '/iniciar-sesion',
    '/registro',
    '/auth/login',
    '/auth/register'
  ];

  // Verificar si es endpoint público
  const isPublic = publicEndpoints.some(endpoint => req.url.includes(endpoint));
  
  if (isPublic) {
    console.log('Endpoint público, sin token');
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
        console.error(' Error HTTP interceptado:', {
          status: error.status,
          statusText: error.statusText,
          url: error.url
        });

        // Si es 401, token inválido/expirado - cerrar sesión
        if (error.status === 401) {
          console.error(' Error 401: Token inválido o expirado');
          authService.logout();
        }
        
        if (error.status === 403) {
          console.warn('Error 403: Acceso denegado (puede ser problema de permisos del backend)');
        }

        return throwError(() => error);
      })
    );
  } else {
    console.warn(' No hay token disponible - enviando request sin Authorization');
    return next(req);
  }
};