import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import { AvatarModule } from 'primeng/avatar';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, MenubarModule, AvatarModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export default class NavbarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  usuario = this.authService.usuario;
  
  // Computed para obtener iniciales del usuario
  initials = computed(() => {
    const user = this.usuario();
    if (!user) return '';
    const names = user.nombreCompleto.split(' ');
    return names.map(n => n[0]).join('').substring(0, 2).toUpperCase();
  });

  items: MenuItem[] = [
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
  onLogout() {
    this.authService.logout();
  }
}
