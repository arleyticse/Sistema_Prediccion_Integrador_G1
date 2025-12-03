import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MenuModule } from 'primeng/menu';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth';
import { RolePermissionsService } from '../../shared/services/role-permissions.service';
import { Avatar } from "primeng/avatar";
import { Menu as PrimeMenu } from 'primeng/menu';

@Component({
  selector: 'app-management-layouts',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MenuModule,
    ButtonModule,
    CommonModule,
    Avatar,
    PrimeMenu
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
    /* Animación suave para submenús */
    .submenu-container {
      display: grid;
      grid-template-rows: 0fr;
      opacity: 0;
      transition: grid-template-rows 300ms cubic-bezier(0.4, 0, 0.2, 1),
                  opacity 200ms cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .submenu-container.open {
      grid-template-rows: 1fr;
      opacity: 1;
    }
    
    .submenu-content {
      overflow: hidden;
    }
    
    /* Efecto de entrada escalonada para items */
    .submenu-item {
      opacity: 0;
      transform: translateX(-8px);
      transition: opacity 200ms ease-out, transform 200ms ease-out;
    }
    
    .submenu-container.open .submenu-item {
      opacity: 1;
      transform: translateX(0);
    }
    
    /* Delay escalonado para cada item */
    .submenu-container.open .submenu-item:nth-child(1) { transition-delay: 50ms; }
    .submenu-container.open .submenu-item:nth-child(2) { transition-delay: 100ms; }
    .submenu-container.open .submenu-item:nth-child(3) { transition-delay: 150ms; }
    .submenu-container.open .submenu-item:nth-child(4) { transition-delay: 200ms; }
    .submenu-container.open .submenu-item:nth-child(5) { transition-delay: 250ms; }
    
    /* Icono de chevron rotación */
    .chevron-icon {
      transition: transform 300ms cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .chevron-icon.rotated {
      transform: rotate(180deg);
    }
    
    /* Efecto hover mejorado en grupo */
    .menu-group-header {
      position: relative;
      overflow: hidden;
    }
    
    .menu-group-header::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      height: 100%;
      width: 3px;
      background: linear-gradient(180deg, #3b82f6, #60a5fa);
      transform: scaleY(0);
      transition: transform 300ms cubic-bezier(0.4, 0, 0.2, 1);
      border-radius: 0 4px 4px 0;
    }
    
    .menu-group-header.active::before {
      transform: scaleY(1);
    }
  `],
  template: `
    <div class="flex flex-col h-screen bg-slate-50 dark:bg-[#18181b]">

      <div class="flex flex-1 overflow-hidden">
        <!-- Sidebar Menu -->
        <aside
          (mouseenter)="onSidebarEnter()"
          (mouseleave)="onSidebarLeave()"
          class="bg-white dark:bg-[#18181b] shadow-lg border-r border-slate-200 dark:border-[#2a2a2b] overflow-y-auto overflow-x-hidden transition-all duration-300 ease-out"
          [class.w-20]="!isExpanded()"
          [class.w-80]="isExpanded()"
        >
        <!-- Logo Section -->
        <div class="bg-gradient-to-b from-white to-slate-50/50 dark:from-[#18181b] dark:to-[#18181b]/50 border-b border-slate-200 dark:border-[#2a2a2b] p-6 backdrop-blur-sm">
          <div class="flex items-center justify-center">
            <div [class.w-16]="!isExpanded()" [class.w-64]="isExpanded()" class="h-16 transition-all duration-300 ease-out">
              <img src="../../../assets/logo/Logo_Transparente.png" alt="Logo" class="w-full h-full object-contain" />
            </div>
          </div>
        </div>

        <div class="px-3 py-4">
           <a [routerLink]="['/administracion/dashboard']" 
             routerLinkActive="bg-blue-500 !text-primary-100"
             [routerLinkActiveOptions]="{exact: true}"
             class="flex items-center gap-3 px-4 py-3 rounded-lg hover:bg-blue-50 dark:hover:bg-[#222225] transition-all duration-200 ease-out group dark:text-[#e4e4e7] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#0ea5e9] focus-visible:ring-offset-2 focus-visible:ring-offset-[#18181b]">
            <div class="w-10 h-10 flex items-center justify-center rounded-md transition-all duration-200 ease-out">
              <i class="pi pi-fw pi-home text-slate-600 dark:text-[#e4e4e7] group-hover:text-blue-600 dark:group-hover:text-blue-400 text-lg transition-colors duration-200 ease-out"></i>
            </div>
            <span 
              class="text-slate-700 dark:text-[#e4e4e7] font-medium whitespace-nowrap transition-all duration-200 ease-out"
              [class.opacity-100]="isExpanded()"
              [class.opacity-0]="!isExpanded()"
              [class.w-0]="!isExpanded()"
              [class.overflow-hidden]="!isExpanded()">
              Dashboard
            </span>
          </a>
        </div>

        <div class="space-y-1 px-2">
          <div *ngFor="let group of menuGroups(); trackBy: trackByLabel"
               (mouseenter)="onGroupMouseEnter(group.label)"
               (mouseleave)="onGroupMouseLeave(group.label)"
               class="menu-group">
            <!-- Header del grupo -->
            <div
              class="menu-group-header flex items-center justify-between px-3 py-2 rounded-lg hover:bg-blue-50 dark:hover:bg-[#222225] cursor-pointer dark:text-[#e4e4e7] transition-all duration-200 ease-out"
              [class.active]="activeAccordionIndex().includes(group.label)"
              (click)="toggleGroup(group.label)">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 flex items-center justify-center rounded-md transition-colors duration-200 ease-out">
                  <i [class]="group.icon + ' text-blue-600 dark:text-blue-400 text-lg transition-colors duration-200 ease-out'"></i>
                </div>
                <span 
                  class="text-slate-800 dark:text-[#e4e4e7] font-semibold whitespace-nowrap transition-all duration-200 ease-out"
                  [class.opacity-100]="isExpanded()"
                  [class.opacity-0]="!isExpanded()"
                  [class.w-0]="!isExpanded()"
                  [class.overflow-hidden]="!isExpanded()">
                  {{ group.label }}
                </span>
              </div>
              <!-- Chevron indicador -->
              <i *ngIf="isExpanded()" 
                 class="pi pi-chevron-down text-slate-400 text-xs chevron-icon"
                 [class.rotated]="activeAccordionIndex().includes(group.label)"></i>
            </div>
            
            <!-- Contenedor del submenú con animación CSS -->
            <div class="submenu-container" [class.open]="activeAccordionIndex().includes(group.label) && isExpanded()">
              <ul class="submenu-content mt-1 space-y-1 px-2">
                <li *ngFor="let item of group.items" class="submenu-item">
                  <a [routerLink]="item.routerLink"
                     routerLinkActive="!bg-blue-100 dark:!bg-blue-900/30 !text-blue-700 dark:!text-blue-300"
                     class="flex items-center gap-3 px-4 py-2.5 rounded-lg hover:bg-blue-50 dark:hover:bg-[#222225] transition-all duration-200 ease-out group relative overflow-hidden dark:text-[#e4e4e7]">
                    <div class="absolute inset-0 bg-gradient-to-r from-blue-600/0 to-blue-600/5 dark:from-blue-400/0 dark:to-blue-400/5 opacity-0 group-hover:opacity-100 transition-opacity duration-200 ease-out"></div>
                    <div class="w-10 h-10 flex items-center justify-center shrink-0 rounded-md transition-colors duration-200 ease-out relative z-10">
                      <i [class]="item.icon + ' text-slate-500 dark:text-[#a1a1aa] group-hover:text-blue-600 dark:group-hover:text-blue-400 text-base transition-colors duration-200 ease-out'"></i>
                    </div>
                    <span class="text-slate-600 dark:text-[#a1a1aa] font-medium group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors duration-200 ease-out relative z-10 whitespace-nowrap">
                      {{ item.label }}
                    </span>
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </aside>

      <main class="flex-1 overflow-auto bg-bg-100 dark:bg-[#18181b] dark:text-[#e4e4e7]">
        <div class="bg-white/95 dark:bg-[#18181b] shadow-sm border-b border-bg-200 dark:border-[#2a2a2b] p-6 sticky top-0 z-20 backdrop-blur-sm">
          <div class="max-w-7xl flex items-center justify-between mx-auto">
            <div>
              <h2 class="text-2xl font-bold bg-gradient-to-r from-[#0077c2] to-[#59a5f5] bg-clip-text text-transparent dark:from-[#59a5f5] dark:to-[#0077c2]">Sistema de Predicción de Demanda</h2>
              @if (currentRole()) {
                <div class="flex items-center gap-2 mt-2">
                  <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 dark:bg-blue-950 border-blue-200">
                    <i [class]="'text-xs ' + roleService.getRoleInfo().icon + ' text-[#0077c2] dark:text-[#59a5f5]'"></i>
                    <span class="text-xs font-medium text-[#0077c2] dark:text-[#59a5f5]">
                      {{ roleService.getRoleInfo().name }}
                    </span>
                  </span>
                </div>
              }
            </div>
            <!-- Usuario y Logout -->
            <div class="flex items-center gap-6 relative">
              @if (usuario()) {
                <div class="flex gap-3 cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#0ea5e9] focus-visible:ring-offset-2 focus-visible:ring-offset-[#18181b]" (click)="menu.toggle($event)" tabindex="0">
                      <p-avatar
                        [label]="initials()"
                        Class="!bg-gradient-to-br !from-blue-600 !to-blue-800 !text-white !font-bold !shadow-lg dark:!from-blue-400 dark:!to-blue-600"
                        size="large"
                        shape="circle"
                      ></p-avatar>
                      <div class="hidden sm:flex sm:flex-col items-center">
                        <p class="text-sm font-semibold text-text-100 dark:text-text-dark-100">{{ usuario()?.nombreCompleto }}</p>
                        <span class="text-xs text-text-200 dark:text-text-dark-200 px-2 py-0.5 rounded-full border border-slate-200 dark:border-[#2a2a2b]">{{ usuario()?.rol }}</span>
                      </div>
                    </div>
                    <p-menu #menu [model]="profileMenu()" [popup]="true" appendTo="body"></p-menu>
              }
            </div>
          </div>
        </div>
        <div class="p-6">
          <router-outlet></router-outlet>
        </div>
      </main>
    </div>
    </div>
  `
})
export class ManagementLayouts {
  private authService = inject(AuthService);
  public roleService = inject(RolePermissionsService);
  isExpanded = signal(false);

  activeAccordionIndex = signal<string[]>([]);
  usuario = this.authService.usuario;
  currentRole = this.roleService.currentRole;
  permissions = this.roleService.currentPermissions;

  logoutMenuVisible = signal(false);
  hoverTimeouts = new Map<string, any>();

  constructor() {
    effect(() => {
      if (!this.isExpanded()) {
        this.activeAccordionIndex.set([]);
      }
    });
  }

  initials = computed(() => {
    const user = this.usuario();
    if (!user) return '';
    const names = user.nombreCompleto.split(' ');
    return names.map(n => n[0]).join('').substring(0, 2).toUpperCase();
  });

  // Menu items dinámicos basados en roles
  menuItems = computed<MenuItem[]>(() => {
    const role = this.currentRole();
    const perms = this.permissions();

    const baseMenu: MenuItem[] = [
      {
        label: 'Dashboard',
        icon: 'pi pi-fw pi-home',
        routerLink: ['/administracion/dashboard'],
        routerLinkActiveOptions: { exact: true }
      },
      {
        separator: true
      },
      {
        label: 'Configuración',
        icon: 'pi pi-fw pi-cog',
        items: [
          {
            label: 'Categorías',
            icon: 'pi pi-fw pi-folder',
            routerLink: ['/administracion/categorias']
          },
          {
            label: 'Unidades de Medida',
            icon: 'pi pi-fw pi-list',
            routerLink: ['/administracion/unidades-medida']
          }
        ]
      },
      {
        label: 'Gestión de Inventario',
        icon: 'pi pi-fw pi-box',
        items: [
          {
            label: 'Productos',
            icon: 'pi pi-fw pi-shopping-bag',
            routerLink: ['/administracion/productos']
          },
          {
            label: 'Inventario',
            icon: 'pi pi-fw pi-database',
            routerLink: ['/administracion/inventario']
          },
          {
            label: 'Movimientos',
            icon: 'pi pi-fw pi-arrow-right-arrow-left',
            routerLink: ['/administracion/movimientos']
          }
        ]
      },
      {
        label: 'Compras y Proveedores',
        icon: 'pi pi-fw pi-shopping-cart',
        items: [
          {
            label: 'Proveedores',
            icon: 'pi pi-fw pi-truck',
            routerLink: ['/administracion/proveedores']
          },
          // Órdenes de compra - solo para roles con permiso
          ...(perms.canAccessOrders ? [{
            label: 'Órdenes de Compra',
            icon: 'pi pi-fw pi-receipt',
            routerLink: ['/administracion/ordenes-compra']
          }] : [])
        ]
      },
      {
        label: 'Análisis y Predicción',
        icon: 'pi pi-fw pi-chart-line',
        items: [
          // Predicciones - solo para ADMIN y GERENTE
          ...(perms.canAccessPredictions ? [{
            label: 'Predicciones',
            icon: 'pi pi-fw pi-chart-bar',
            routerLink: ['/administracion/predicciones'],
            badgeSeverity: 'success' as const
          }] : []),
          // Reportes - solo para ADMIN y GERENTE
          ...(perms.canAccessReports ? [{
            label: 'Reportes',
            icon: 'pi pi-fw pi-file-pdf',
            disabled: !perms.canAccessReports
          }] : [])
        ]
      },
      {
        separator: true
      },
      {
        label: 'Administración',
        icon: 'pi pi-fw pi-cog',
        items: [
          ...(perms.canManageUsers ? [{
            label: 'Gestión de Usuarios',
            icon: 'pi pi-fw pi-users',
            routerLink: ['/administracion/admin/usuarios']
          }, {
            label: 'Configuración Empresa',
            icon: 'pi pi-fw pi-building',
            routerLink: ['/administracion/admin/configuracion-empresa']
          }] : [])
        ]
      },
      {
        separator: true
      },
      {
        label: 'Cerrar Sesión',
        icon: 'pi pi-fw pi-sign-out',
        command: () => this.onLogout()
      }
    ];

    if (perms.canAccessPredictions || true) { // Mantener alertas para todos
      baseMenu.push({
        label: 'Análisis y Predicción',
        icon: 'pi pi-fw pi-chart-line',
        items: [
          // Predicciones - solo para ADMIN y GERENTE
          ...(perms.canAccessPredictions ? [{
            label: 'Predicciones',
            icon: 'pi pi-fw pi-chart-bar',
            routerLink: ['/administracion/predicciones'],
            badgeSeverity: 'success' as const
          }] : []),
          // Reportes - solo para ADMIN y GERENTE
          ...(perms.canAccessReports ? [{
            label: 'Reportes',
            icon: 'pi pi-fw pi-file-pdf',
            disabled: !perms.canAccessReports
          }] : [])
        ]
      });
    }

    // Administración - solo para ADMIN
    if (perms.canManageParameters || perms.canManageUsers) {
      baseMenu.push(
        {
          separator: true
        },
        {
          label: 'Administración',
          icon: 'pi pi-fw pi-cog',
          items: [
            ...(perms.canManageUsers ? [{
              label: 'Gestión de Usuarios',
              icon: 'pi pi-fw pi-users',
              routerLink: ['/administracion/admin/usuarios']
            }, {
              label: 'Configuración Empresa',
              icon: 'pi pi-fw pi-building',
              routerLink: ['/administracion/admin/configuracion-empresa']
            }] : [])
          ]
        }
      );
    }
    return baseMenu;
  });

  // Menu específico del perfil (solo Cerrar Sesión)
  profileMenu = computed<MenuItem[]>(() => [{
    label: 'Cerrar Sesión',
    icon: 'pi pi-fw pi-sign-out',
    command: () => this.onLogout()
  }]);

  menuGroups = computed<any[]>(() => {
    const perms = this.permissions();
    const groups: any[] = [];
    groups.push({
      label: 'Configuración',
      icon: 'pi pi-fw pi-cog',
      items: [
        { label: 'Categorías', icon: 'pi pi-fw pi-folder', routerLink: ['/administracion/categorias'] },
        { label: 'Unidades de Medida', icon: 'pi pi-fw pi-list', routerLink: ['/administracion/unidades-medida'] }
      ]
    });
    groups.push({
      label: 'Gestión de Inventario',
      icon: 'pi pi-fw pi-box',
      items: [
        { label: 'Productos', icon: 'pi pi-fw pi-shopping-bag', routerLink: ['/administracion/productos'] },
        { label: 'Inventario', icon: 'pi pi-fw pi-database', routerLink: ['/administracion/inventario'] },
        { label: 'Movimientos', icon: 'pi pi-fw pi-arrow-right-arrow-left', routerLink: ['/administracion/movimientos'] }
      ]
    });
    groups.push({
      label: 'Compras y Proveedores',
      icon: 'pi pi-fw pi-shopping-cart',
      items: [
        { label: 'Proveedores', icon: 'pi pi-fw pi-truck', routerLink: ['/administracion/proveedores'] },
        ...(perms.canAccessOrders ? [{ label: 'Órdenes de Compra', icon: 'pi pi-fw pi-receipt', routerLink: ['/administracion/ordenes-compra'] }] : [])
      ]
    });
    const predItems = [
      ...(perms.canAccessPredictions ? [{ label: 'Predicciones', icon: 'pi pi-fw pi-chart-bar', routerLink: ['/administracion/predicciones'] }] : []),
      ...(perms.canAccessReports ? [{ label: 'Reportes', icon: 'pi pi-fw pi-file-pdf', routerLink: ['/administracion/reportes'] }] : [])
    ];
    groups.push({
      label: 'Análisis y Predicción',
      icon: 'pi pi-fw pi-chart-line',
      items: predItems
    });
    if (perms.canManageUsers) {
      groups.push({
        label: 'Administración',
        icon: 'pi pi-fw pi-cog',
        items: [
          { label: 'Gestión de Usuarios', icon: 'pi pi-fw pi-users', routerLink: ['/administracion/admin/usuarios'] },
          { label: 'Configuración Empresa', icon: 'pi pi-fw pi-building', routerLink: ['/administracion/admin/configuracion-empresa'] }
        ]
      });
    }
    return groups;
  });
  onLogout() {
    this.authService.logout();
  }

  toggleLogoutMenu() {
    this.logoutMenuVisible.set(!this.logoutMenuVisible());
  }

  trackByLabel(index: number, group: any): string {
    return group.label;
  }

  toggleGroup(label: string) {
    const current = this.activeAccordionIndex();
    const idx = current.indexOf(label);
    if (idx > -1) {
      const next = [...current];
      next.splice(idx, 1);
      this.activeAccordionIndex.set(next);
    } else {
      this.activeAccordionIndex.set([...current, label]);
    }
  }

  private sidebarTimeout: any = null;

  onSidebarEnter() {
    if (this.sidebarTimeout) {
      clearTimeout(this.sidebarTimeout);
      this.sidebarTimeout = null;
    }
    this.isExpanded.set(true);
  }

  onSidebarLeave() {
    // Delay al cerrar para dar tiempo al usuario
    this.sidebarTimeout = setTimeout(() => {
      this.isExpanded.set(false);
      this.sidebarTimeout = null;
    }, 150);
  }

  // Abrir grupo al hacer hover con transición suave
  onGroupMouseEnter(label: string) {
    // Limpiar timeout existente si hay uno
    if (this.hoverTimeouts.has(label)) {
      clearTimeout(this.hoverTimeouts.get(label));
      this.hoverTimeouts.delete(label);
    }

    // Solo abrir si el sidebar está expandido
    if (this.isExpanded()) {
      // Pequeño delay para evitar aperturas accidentales
      const timeout = setTimeout(() => {
        const current = this.activeAccordionIndex();
        if (!current.includes(label)) {
          this.activeAccordionIndex.set([...current, label]);
        }
        this.hoverTimeouts.delete(label);
      }, 100);

      this.hoverTimeouts.set(label, timeout);
    }
  }

  // Cerrar grupo al quitar hover con transición suave
  onGroupMouseLeave(label: string) {
    // Limpiar timeout de apertura si existe
    if (this.hoverTimeouts.has(label)) {
      clearTimeout(this.hoverTimeouts.get(label));
    }

    // Delay más largo para cerrar - permite navegar entre items
    const timeout = setTimeout(() => {
      const current = this.activeAccordionIndex();
      if (current.includes(label)) {
        this.activeAccordionIndex.set(current.filter(x => x !== label));
      }
      this.hoverTimeouts.delete(label);
    }, 300);

    this.hoverTimeouts.set(label, timeout);
  }
}
