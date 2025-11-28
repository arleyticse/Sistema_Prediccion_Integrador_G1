import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MenuModule } from 'primeng/menu';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth';
import { RolePermissionsService } from '../../shared/services/role-permissions.service';
import { Avatar } from "primeng/avatar";
// Accordion removed: using Tailwind-based collapsible groups instead
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
  template: `
    <div class="flex flex-col h-screen bg-slate-50 dark:bg-slate-950">

      <div class="flex flex-1 overflow-hidden">
        <!-- Sidebar Menu -->
        <aside
          (mouseenter)="isExpanded.set(true)"
          (mouseleave)="isExpanded.set(false)"
          class="bg-white dark:bg-slate-900 shadow-lg border-r border-slate-200 dark:border-slate-800 overflow-y-auto overflow-x-hidden transition-all duration-300 ease-in-out"
          [class.w-20]="!isExpanded()"
          [class.w-80]="isExpanded()"
        >
        <!-- Logo Section -->
        <div class="bg-gradient-to-b from-white to-slate-50/50 dark:from-slate-900 dark:to-slate-900/50 border-b border-slate-200 dark:border-slate-800 p-6 backdrop-blur-sm">
          <div class="flex items-center justify-center">
            <div [class.w-16]="!isExpanded()" [class.w-64]="isExpanded()" class="h-16 transition-all duration-300">
              <img src="../../../assets/logo/Logo_NN.jpg" alt="Logo" class="w-full h-full object-contain" />
            </div>
          </div>
        </div>

        <div class="px-3 py-4">
           <a [routerLink]="['/administracion/dashboard']" 
             routerLinkActive="!bg-blue-100 !text-primary-100"
             [routerLinkActiveOptions]="{exact: true}"
             class="flex items-center gap-3 px-4 py-3 rounded-lg hover:bg-blue-50 dark:hover:bg-slate-800 transition-all duration-200 group">
            <div class="w-10 h-10 flex items-center justify-center rounded-md transition-colors">
              <i class="pi pi-fw pi-home text-slate-600 dark:text-slate-300 group-hover:text-blue-600 dark:group-hover:text-blue-400 text-lg"></i>
            </div>
            @if (isExpanded()) { <span class="text-slate-700 dark:text-slate-200 font-medium transition-colors">Dashboard</span> }
          </a>
        </div>

        <div class="space-y-2 px-2">
          <div *ngFor="let group of menuGroups()">
            <div
              class="flex items-center justify-between px-3 py-2 rounded-lg hover:bg-blue-50 dark:hover:bg-slate-800 cursor-pointer"
              (click)="toggleGroup(group.label)">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 flex items-center justify-center rounded-md transition-colors">
                  <i [class]="group.icon + ' text-blue-600 dark:text-blue-400 text-lg'"></i>
                </div>
                @if (isExpanded()) { <span class="text-slate-800 dark:text-slate-100 font-semibold">{{ group.label }}</span> }
              </div>
            </div>
            <ul *ngIf="activeAccordionIndex().includes(group.label)" class="mt-1 space-y-1 px-2">
              <li *ngFor="let item of group.items">
                <a [routerLink]="item.routerLink"
                   routerLinkActive="bg-blue-100 text-primary-100"
                   class="flex items-center gap-3 px-4 py-2.5 rounded-lg hover:bg-blue-50 dark:hover:bg-slate-800 transition-all duration-200 group relative overflow-hidden">
                  <div class="absolute inset-0 bg-gradient-to-r from-blue-600/0 to-blue-600/5 dark:from-blue-400/0 dark:to-blue-400/5 opacity-0 group-hover:opacity-100 transition-opacity"></div>
                  <div class="w-10 h-10 flex items-center justify-center shrink-0 rounded-md transition-colors relative z-10">
                    <i [class]="item.icon + ' text-slate-600 dark:text-slate-300 group-hover:text-blue-600 dark:group-hover:text-blue-400 text-base'"></i>
                  </div>
                  @if (isExpanded()) { <span class="text-slate-700 dark:text-slate-200 font-medium group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors relative z-10">{{ item.label }}</span> }
                </a>
              </li>
            </ul>
          </div>
        </div>
      </aside>

      <main class="flex-1 overflow-auto bg-bg-100 dark:bg-bg-dark-100">
        <div class="bg-white/95 dark:bg-gray-900 shadow-sm border-b border-bg-200 dark:border-bg-dark-300 p-6 sticky top-0 z-20 backdrop-blur-sm">
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
            <div class="flex items-center gap-4 relative">
              @if (usuario()) {
                <div class="flex items-center gap-3 cursor-pointer" (click)="menu.toggle($event)">
                      <p-avatar
                        [label]="initials()"
                        styleClass="!bg-gradient-to-br !from-blue-600 !to-blue-800 !text-white !font-bold !shadow-lg dark:!from-blue-400 dark:!to-blue-600"
                        size="large"
                        shape="circle"
                      ></p-avatar>
                      <div class="hidden sm:block">
                        <p class="text-sm font-semibold text-text-100 dark:text-text-dark-100">{{ usuario()?.nombreCompleto }}</p>
                        <p class="text-xs text-text-200 dark:text-text-dark-200">{{ usuario()?.rol }}</p>
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
        label: 'Ayuda y Documentación',
        icon: 'pi pi-fw pi-question-circle',
        items: [
          {
            label: 'Guía de Uso',
            icon: 'pi pi-fw pi-book',
            disabled: true
          },
          {
            label: 'Soporte Técnico',
            icon: 'pi pi-fw pi-phone',
            disabled: true
          }
        ]
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

    // Ayuda - para todos los roles pero con limitaciones
    baseMenu.push(
      {
        separator: true
      },
      {
        label: 'Ayuda y Documentación',
        icon: 'pi pi-fw pi-question-circle',
        items: [
          {
            label: 'Guía de Uso',
            icon: 'pi pi-fw pi-book',
            disabled: true
          },
          {
            label: 'Soporte Técnico',
            icon: 'pi pi-fw pi-phone',
            disabled: true
          }
        ]
      }
    );

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
      { label: 'Alertas Inventario', icon: 'pi pi-fw pi-bell', routerLink: ['/administracion/alertas-inventario'] },
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
    groups.push({
      label: 'Ayuda y Documentación',
      icon: 'pi pi-fw pi-question-circle',
      items: [
        { label: 'Guía de Uso', icon: 'pi pi-fw pi-book', routerLink: ['/administracion/ayuda'] },
        { label: 'Soporte Técnico', icon: 'pi pi-fw pi-phone', routerLink: ['/administracion/soporte'] }
      ]
    });
    return groups;
  });
  onLogout() {
    this.authService.logout();
  }

  toggleLogoutMenu() {
    this.logoutMenuVisible.set(!this.logoutMenuVisible());
  }

  // Toggle a group label in the activeAccordionIndex signal
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
}
