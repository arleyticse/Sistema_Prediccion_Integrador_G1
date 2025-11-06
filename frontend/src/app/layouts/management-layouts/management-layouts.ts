import { Component, computed, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MenuModule } from 'primeng/menu';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import NavbarComponent from '../../shared/components/navbar/navbar';
import { AuthService } from '../../core/services/auth';
import { Avatar } from "primeng/avatar";

@Component({
  selector: 'app-management-layouts',
  standalone: true,
  imports: [
    RouterOutlet,
    MenuModule,
    ButtonModule,
    CommonModule,
    Avatar
  ],
  template: `
    <div class="flex flex-col h-screen bg-surface-50 dark:bg-surface-950">

      <div class="flex flex-1 overflow-hidden">
        <!-- Sidebar Menu -->
        <aside class="w-80 bg-white dark:bg-surface-900 shadow-lg border-r border-surface-200 dark:border-surface-800 overflow-y-auto">
        <!-- Logo Section -->
        <div class="sticky top-0 bg-white dark:bg-surface-900 border-b border-surface-200 dark:border-surface-800 p-6 z-10">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-blue-700 rounded-lg flex items-center justify-center">
              <i class="pi pi-chart-line text-white text-xl"></i>
            </div>
            <div>
              <h1 class="text-xl font-bold text-surface-900 dark:text-white">Predicción</h1>
              <p class="text-xs text-surface-500 dark:text-surface-400">Sistema de Gestión</p>
            </div>
          </div>
        </div>

        <p-menu 
          [model]="menuItems()" 
          [styleClass]="'w-full border-none'"
          [style]="{'background': 'transparent', 'border': 'none'}" />
      </aside>

      <main class="flex-1 overflow-auto">
        <div class="bg-white dark:bg-surface-900 shadow-sm border-b border-surface-200 dark:border-surface-800 p-4 sticky top-0 z-20">
          <div class="max-w-7xl flex items-center justify-between mx-auto">
            <h2 class="text-2xl font-bold text-surface-900 dark:text-white">Sistema de Predicción de Demanda</h2>
            <!-- Usuario y Logout -->
  <div class="flex items-center gap-4">
    @if (usuario()) {
      <div class="flex items-center gap-2">
        <p-avatar
          [label]="initials()"
          class="bg-blue-400 text-white font-bold"
          size="large"
          shape="circle"
        ></p-avatar>
        <div class="hidden sm:block">
          <p class="text-sm font-semibold">{{ usuario()?.nombreCompleto }}</p>
          <p class="text-xs opacity-75">{{ usuario()?.rol }}</p>
        </div>
      </div>

      <button
        (click)="onLogout()"
        class="px-4 py-2 bg-red-500 hover:bg-red-600 rounded-lg text-white font-semibold transition"
      >
        Logout
      </button>
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
  `,
  styles: [`
    :host ::ng-deep {
      .p-menu {
        background: transparent;
        border: none;
        padding: 0;
      }

      .p-menu .p-menu-list {
        padding: 0.5rem 0;
      }

      .p-menu .p-menuitem {
        margin: 0;
        border-radius: 0;
      }

      .p-menu .p-menuitem-link {
        padding: 0.75rem 1.5rem;
        transition: all 0.2s ease;
        border-left: 3px solid transparent;
      }

      .p-menu .p-menuitem-link:hover {
        background-color: var(--surface-100);
        border-left-color: var(--primary-color);
      }

      .p-menu .p-menuitem.p-focus > .p-menuitem-link {
        background-color: var(--surface-100);
        border-left-color: var(--primary-color);
      }

      .p-menu .p-submenu-header {
        padding: 1rem 1.5rem 0.5rem 1.5rem;
        font-weight: 600;
        font-size: 0.875rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: var(--surface-600);
        margin-top: 0.5rem;
      }

      .p-menu .p-submenu-header:first-child {
        margin-top: 0;
      }

      .p-menu .p-menu-separator {
        margin: 0.5rem 0;
        border-top-color: var(--surface-200);
      }

      .p-menu .p-menuitem-icon {
        width: 1.25rem;
        margin-right: 0.75rem;
        font-size: 1rem;
      }

      .p-menu .p-menuitem-text {
        color: var(--surface-700);
        font-weight: 500;
      }

      .p-menu .p-menuitem:hover .p-menuitem-text {
        color: var(--primary-color);
      }
    }

    :host ::ng-deep .dark {
      .p-menu .p-menuitem-link:hover {
        background-color: var(--surface-800);
      }

      .p-menu .p-menuitem.p-focus > .p-menuitem-link {
        background-color: var(--surface-800);
      }
    }
  `]
})
export class ManagementLayouts {
  private authService = inject(AuthService);
  // Computed para obtener iniciales del usuario
  usuario = this.authService.usuario;
  initials = computed(() => {
    const user = this.usuario();
    if (!user) return '';
    const names = user.nombreCompleto.split(' ');
    return names.map(n => n[0]).join('').substring(0, 2).toUpperCase();
  });
  menuItems = signal<MenuItem[]>([
    {
      label: 'Dashboard',
      icon: 'pi pi-fw pi-home',
      routerLink: ['/administracion'],
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
        {
          label: 'Órdenes de Compra',
          icon: 'pi pi-fw pi-receipt',
          routerLink: ['/administracion/ordenes-compra']
        }
      ]
    },
    {
      label: 'Análisis y Predicción',
      icon: 'pi pi-fw pi-chart-line',
      items: [
        {
          label: 'Predicciones',
          icon: 'pi pi-fw pi-chart-bar',
          routerLink: ['/administracion/predicciones'],
          badgeSeverity: 'success'
        },
        {
          label: 'Reportes',
          icon: 'pi pi-fw pi-file-pdf',
          disabled: true
        }
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
    }
  ]);
  onLogout() {
    this.authService.logout();
  }
}
