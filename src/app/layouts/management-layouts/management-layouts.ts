import { Component } from '@angular/core';
import { RouterOutlet, RouterLinkWithHref } from '@angular/router';

@Component({
  selector: 'app-management-layouts',
  imports: [RouterOutlet, RouterLinkWithHref],
  template: `
    <div class="flex h-screen">
      <!-- Sidebar Menu (30% width) -->
      <aside class="w-3/10 bg-gray-100 dark:bg-black p-4">
        <!-- Add your menu items here, e.g., navigation links -->
        <h2 class="text-lg font-bold">Menu</h2>
        <ul class="mt-4 space-y-2">
          <li><a [routerLink]="['/administracion']" class="text-blue-600 hover:underline">Dashboard</a></li>
          <li><a [routerLink]="['/administracion/usuarios']" class="text-blue-600 hover:underline">Users</a></li>
          <li><a [routerLink]="['/administracion/categorias']" class="text-blue-600 hover:underline">Categorias</a></li>
        </ul>
      </aside>
      <!-- Main Content Area -->
      <main class="flex-1 p-4">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: ``
})
export class ManagementLayouts {

}
