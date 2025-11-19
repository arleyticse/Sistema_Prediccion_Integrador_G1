import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sin-permisos',
  imports: [CommonModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50">
      <div class="max-w-md w-full text-center">
        <div class="mb-8">
          <svg class="mx-auto h-24 w-24 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        </div>
        
        <h1 class="text-3xl font-bold text-gray-900 mb-4">
          Acceso Denegado
        </h1>
        
        <p class="text-lg text-gray-600 mb-8">
          No tienes permisos para acceder a esta sección del sistema.
        </p>
        
        <div class="space-y-4">
          <button 
            (click)="volverAlDashboard()"
            class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition duration-200">
            Volver al Dashboard
          </button>
          
          <button 
            (click)="volverAnterior()"
            class="w-full bg-gray-500 hover:bg-gray-600 text-white font-medium py-2 px-4 rounded-lg transition duration-200">
            Página Anterior
          </button>
        </div>
        
        <div class="mt-8 text-sm text-gray-500">
          <p>Si necesitas acceso a esta funcionalidad, contacta al administrador del sistema.</p>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class SinPermisosComponent {
  
  constructor(private router: Router) {}

  volverAlDashboard() {
    this.router.navigate(['/administracion/dashboard']);
  }

  volverAnterior() {
    window.history.back();
  }
}