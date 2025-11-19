import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.getUsuario();
  
  if (currentUser && currentUser.rol === 'ADMIN') {
    return true;
  }

  // Redireccionar a sin permisos si no es admin
  router.navigate(['/sin-permisos']);
  return false;
};

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.getUsuario();
  const requiredRole = route.data?.['rol'];

  if (!currentUser) {
    router.navigate(['/login']);
    return false;
  }

  // Jerarquía de roles: ADMIN > GERENTE > OPERARIO
  const roleHierarchy = {
    'ADMIN': 3,
    'GERENTE': 2,
    'OPERARIO': 1
  };

  const userRoleLevel = roleHierarchy[currentUser.rol as keyof typeof roleHierarchy] || 0;
  const requiredRoleLevel = roleHierarchy[requiredRole as keyof typeof roleHierarchy] || 0;

  if (userRoleLevel >= requiredRoleLevel) {
    return true;
  }

  router.navigate(['/sin-permisos']);
  return false;
};