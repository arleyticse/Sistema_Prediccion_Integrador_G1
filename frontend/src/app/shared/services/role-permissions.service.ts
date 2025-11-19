import { Injectable, inject, computed } from '@angular/core';
import { AuthService } from '../../core/services/auth';

export type UserRole = 'ADMIN' | 'GERENTE' | 'OPERARIO';

export interface RolePermissions {
  canAccessPredictions: boolean;
  canAccessOrders: boolean;
  canAccessReports: boolean;
  canAccessAdminPanel: boolean;
  canManageUsers: boolean;
  canManageParameters: boolean;
  canViewSensitiveData: boolean;
  canCreateOrders: boolean;
  canModifyPredictions: boolean;
  canDeleteData: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class RolePermissionsService {
  private authService = inject(AuthService);
  
  // Signal para el usuario actual
  private currentUser = computed(() => this.authService.usuario());
  
  // Signal para el rol actual
  currentRole = computed(() => {
    const user = this.currentUser();
    return user?.rol as UserRole || null;
  });
  
  // Signal para los permisos actuales
  currentPermissions = computed<RolePermissions>(() => {
    const role = this.currentRole();
    return this.getPermissionsForRole(role);
  });
  
  /**
   * Obtiene los permisos basados en el rol
   */
  private getPermissionsForRole(role: UserRole | null): RolePermissions {
    switch (role) {
      case 'ADMIN':
        return {
          canAccessPredictions: true,
          canAccessOrders: true,
          canAccessReports: true,
          canAccessAdminPanel: true,
          canManageUsers: true,
          canManageParameters: true,
          canViewSensitiveData: true,
          canCreateOrders: true,
          canModifyPredictions: true,
          canDeleteData: true
        };
      
      case 'GERENTE':
        return {
          canAccessPredictions: true,
          canAccessOrders: true,
          canAccessReports: true,
          canAccessAdminPanel: false,
          canManageUsers: false,
          canManageParameters: true,
          canViewSensitiveData: true,
          canCreateOrders: true,
          canModifyPredictions: true,
          canDeleteData: false
        };
      
      case 'OPERARIO':
        return {
          canAccessPredictions: false,
          canAccessOrders: false,
          canAccessReports: false,
          canAccessAdminPanel: false,
          canManageUsers: false,
          canManageParameters: false,
          canViewSensitiveData: false,
          canCreateOrders: false,
          canModifyPredictions: false,
          canDeleteData: false
        };
      
      default:
        // Sin rol o rol desconocido - permisos mínimos
        return {
          canAccessPredictions: false,
          canAccessOrders: false,
          canAccessReports: false,
          canAccessAdminPanel: false,
          canManageUsers: false,
          canManageParameters: false,
          canViewSensitiveData: false,
          canCreateOrders: false,
          canModifyPredictions: false,
          canDeleteData: false
        };
    }
  }
  
  /**
   * Verifica si el usuario actual tiene un permiso específico
   */
  hasPermission(permission: keyof RolePermissions): boolean {
    return this.currentPermissions()[permission];
  }
  
  /**
   * Verifica si el usuario actual tiene uno de los roles especificados
   */
  hasRole(roles: UserRole | UserRole[]): boolean {
    const currentRole = this.currentRole();
    if (!currentRole) return false;
    
    const allowedRoles = Array.isArray(roles) ? roles : [roles];
    return allowedRoles.includes(currentRole);
  }
  
  /**
   * Verifica si el usuario puede acceder a una ruta específica
   */
  canAccessRoute(route: string): boolean {
    const role = this.currentRole();
    
    switch (route) {
      case '/administracion/predicciones':
        return this.hasPermission('canAccessPredictions');
      
      case '/administracion/ordenes-compra':
        return this.hasPermission('canAccessOrders');
      
      case '/administracion/reportes':
        return this.hasPermission('canAccessReports');
      
      case '/administracion/usuarios':
        return this.hasPermission('canManageUsers');
      
      case '/administracion/parametros':
        return this.hasPermission('canManageParameters');
      
      // Rutas básicas accesibles para todos los roles autenticados
      case '/administracion/dashboard':
      case '/administracion/inventario':
      case '/administracion/productos':
      case '/administracion/categorias':
      case '/administracion/unidades-medida':
      case '/administracion/proveedores':
      case '/administracion/movimientos':
      case '/administracion/alertas-inventario':
        return true;
      
      default:
        return false;
    }
  }
  
  /**
   * Obtiene los elementos del menú filtrados por rol
   */
  getFilteredMenuItems() {
    const baseMenuItems = [
      {
        label: 'Categorías',
        icon: 'pi pi-tag',
        routerLink: '/administracion/categorias',
        visible: true
      },
      {
        label: 'Unidades de Medida',
        icon: 'pi pi-calculator',
        routerLink: '/administracion/unidades-medida',
        visible: true
      },
      {
        label: 'Productos',
        icon: 'pi pi-box',
        routerLink: '/administracion/productos',
        visible: true
      },
      {
        label: 'Inventario',
        icon: 'pi pi-warehouse',
        routerLink: '/administracion/inventario',
        visible: true
      },
      {
        label: 'Movimientos',
        icon: 'pi pi-history',
        routerLink: '/administracion/movimientos',
        visible: true
      },
      {
        label: 'Proveedores',
        icon: 'pi pi-users',
        routerLink: '/administracion/proveedores',
        visible: true
      },
      {
        label: 'Órdenes de Compra',
        icon: 'pi pi-shopping-cart',
        routerLink: '/administracion/ordenes-compra',
        visible: this.hasPermission('canAccessOrders')
      },
      {
        label: 'Predicciones',
        icon: 'pi pi-chart-line',
        routerLink: '/administracion/predicciones',
        visible: this.hasPermission('canAccessPredictions')
      },
      {
        label: 'Alertas Inventario',
        icon: 'pi pi-bell',
        routerLink: '/administracion/alertas-inventario',
        visible: true
      },
      {
        label: 'Reportes',
        icon: 'pi pi-file-pdf',
        routerLink: '/administracion/reportes',
        visible: this.hasPermission('canAccessReports'),
        disabled: !this.hasPermission('canAccessReports')
      },
      {
        label: 'Parámetros Algoritmos',
        icon: 'pi pi-cog',
        routerLink: '/administracion/parametros',
        visible: this.hasPermission('canManageParameters')
      },
      {
        label: 'Gestión de Usuarios',
        icon: 'pi pi-user',
        routerLink: '/administracion/usuarios',
        visible: this.hasPermission('canManageUsers')
      }
    ];
    
    return baseMenuItems.filter(item => item.visible);
  }
  
  /**
   * Obtiene información sobre las limitaciones del rol actual
   */
  getRoleInfo() {
    const role = this.currentRole();
    
    switch (role) {
      case 'ADMIN':
        return {
          name: 'Administrador',
          description: 'Acceso completo al sistema',
          color: 'red',
          icon: 'pi pi-shield'
        };
      
      case 'GERENTE':
        return {
          name: 'Gerente',
          description: 'Acceso a predicciones y órdenes',
          color: 'blue',
          icon: 'pi pi-briefcase'
        };
      
      case 'OPERARIO':
        return {
          name: 'Operario',
          description: 'Acceso de solo lectura a inventario',
          color: 'green',
          icon: 'pi pi-user'
        };
      
      default:
        return {
          name: 'Sin rol',
          description: 'Permisos limitados',
          color: 'gray',
          icon: 'pi pi-question'
        };
    }
  }
}