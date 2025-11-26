import { Component, inject, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { MenuModule } from 'primeng/menu';
import { AvatarModule } from 'primeng/avatar';
import { MenuItem } from 'primeng/api';
import { OrdenesCompraService } from '../../../features/ordenes-compra/service/ordenes-compra.service';
import { OrdenCompraResponse } from '../../../features/ordenes-compra/models/OrdenCompraResponse';
import { signal } from '@angular/core';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, MenubarModule, AvatarModule, ButtonModule, BadgeModule, MenuModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export default class NavbarComponent {
  private authService = inject(AuthService);
  router = inject(Router);

  usuario = this.authService.usuario;
  borradores = signal<OrdenCompraResponse[]>([]);
  borradoresCount = signal<number>(0);
  borradoresMenuItems: MenuItem[] = [];
  
  // Computed para obtener iniciales del usuario
  initials = computed(() => {
    const user = this.usuario();
    if (!user) return '';
    const names = user.nombreCompleto.split(' ');
    return names.map(n => n[0]).join('').substring(0, 2).toUpperCase();
  });

  items: MenuItem[] = [
    {
      label: 'Dashboard',
      icon: 'pi pi-home',
      routerLink: '/administracion/dashboard'
    },
    {
      label: 'Alertas',
      icon: 'pi pi-bell',
      routerLink: '/administracion/alertas-inventario',
      badge: '0'
    },
    {
      label: 'Productos',
      routerLink: '/administracion/productos'
    },
    {
      label: 'Inventario',
      routerLink: '/administracion/inventario'
    },
    {
      label: 'Movimientos',
      routerLink: '/administracion/movimientos'
    },
    {
      label: 'Predicciones',
      routerLink: '/administracion/predicciones'
    },
    {
      label: 'Órdenes de Compra',
      routerLink: '/administracion/ordenes-compra'
    },
    {
      label: 'Categorías',
      routerLink: '/administracion/categorias'
    },
    {
      label: 'Unidades de Medida',
      routerLink: '/administracion/unidades-medida'
    },
    {
      label: 'Proveedores',
      routerLink: '/administracion/proveedores'
    }
  ];

  /**
   * Cerrar sesión
   */
  private ordenesService = inject(OrdenesCompraService);

  constructor() {
    // Fetch borradores when the logged user changes to GERENTE
    effect(() => {
      const user = this.authService.usuario();
      if (user && user.rol === 'GERENTE') {
        this.refreshBorradores();
      } else {
        this.borradores.set([]);
        this.borradoresCount.set(0);
        // update items badge
        const idx = this.items.findIndex(i => i.routerLink === '/administracion/ordenes-compra');
        if (idx >= 0) {
          (this.items[idx] as any).badge = '0';
        }
      }
    });

    // Escuchar eventos globales de actualización de borradores
    document.addEventListener('borradores-updated', () => {
      this.refreshBorradores();
    });
  }

  refreshBorradores(): void {
    if (!this.authService.getUsuario()) return;
    if (this.authService.getUsuario()!.rol !== 'GERENTE') {
      this.borradores.set([]);
      this.borradoresCount.set(0);
      return;
    }
    this.ordenesService!.obtenerOrdenesBorrador().subscribe({
      next: (res) => {
        this.borradores.set(res || []);
        this.borradoresCount.set((res || []).length);
        this.borradoresMenuItems = (res || []).slice(0,5).map(o => ({
          label: `${o.numeroOrden} - ${o.proveedorNombre}`,
          command: () => {
            this.router.navigate(['/administracion/ordenes-compra']);
          }
        }));
        if ((res || []).length > 5) {
          this.borradoresMenuItems.push({
            label: 'Ver todas las órdenes',
            command: () => this.router.navigate(['/administracion/ordenes-compra'])
          });
        }
        // Update items badge
        const idx = this.items.findIndex(i => i.routerLink === '/administracion/ordenes-compra');
        if (idx >= 0) {
          (this.items[idx] as any).badge = String(this.borradoresCount());
        }
      }
    });
  }

  onLogout() {
    this.authService.logout();
  }
}
