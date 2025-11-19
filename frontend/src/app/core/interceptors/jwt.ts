import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError, switchMap } from 'rxjs';
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

  // Clonar request con token si existe
  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si es 401 y no es una petición de login o refresh, intentar refrescar
      if (error.status === 401 && !req.url.includes('/iniciar-sesion') && !req.url.includes('/refresh-token')) {
        console.log('🔄 Token expirado, intentando refrescar...');
        
        return authService.refreshToken().pipe(
          switchMap((response) => {
            console.log('✅ Token refrescado exitosamente');
            authService.setAuthData(response);
            
            // Reintentar la petición original con el nuevo token
            const newAuthReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${response.token}`
              }
            });
            return next(newAuthReq);
          }),
          catchError((refreshError) => {
            console.error('❌ Error al refrescar token, cerrando sesión', refreshError);
            authService.logout();
            return throwError(() => refreshError);
          })
        );
      }

      console.error('❌ Error HTTP:', {
        status: error.status,
        statusText: error.statusText,
        url: error.url
      });

      if (error.status === 403) {
        console.warn('Acceso denegado - verifica permisos del backend');
      }

      return throwError(() => error);
    })
  );
};