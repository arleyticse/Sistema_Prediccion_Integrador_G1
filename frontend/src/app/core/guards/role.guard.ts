import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

/**
 * Guard for GERENTE role (has all permissions including user management)
 */
export const gerenteGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.getUsuario();
  
  if (currentUser && currentUser.rol === 'GERENTE') {
    return true;
  }

  // Redirect to no permissions if not gerente
  router.navigate(['/sin-permisos']);
  return false;
};

/**
 * Role guard with hierarchy: GERENTE > OPERARIO
 * GERENTE has all permissions including user management
 * OPERARIO has all permissions except user management
 */
export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.getUsuario();
  const requiredRole = route.data?.['rol'];

  if (!currentUser) {
    router.navigate(['/login']);
    return false;
  }

  // Role hierarchy: GERENTE: 2, OPERARIO: 1
  const roleHierarchy = {
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