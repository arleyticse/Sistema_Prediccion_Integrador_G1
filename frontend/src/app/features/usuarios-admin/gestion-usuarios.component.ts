import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

// PrimeNG Imports
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { ToolbarModule } from 'primeng/toolbar';

import { MessageService, ConfirmationService } from 'primeng/api';

// Models
interface Usuario {
  id_usuario: number;
  nombre: string;
  email: string;
  rol: 'ADMIN' | 'GERENTE' | 'OPERARIO';
  activo?: boolean;
  fechaCreacion?: Date;
}

@Component({
  selector: 'app-gestion-usuarios',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    TagModule,
    CardModule,
    ToolbarModule
  ],
  providers: [MessageService, ConfirmationService],
  template: `
    <div class="card">
      <!-- Toolbar -->
      <p-toolbar class="mb-4">
        <div class="p-toolbar-group-left">
          <h1 class="text-2xl font-bold text-gray-800">Gestión de Usuarios</h1>
          <small class="text-gray-500 ml-2">RF001 - Registro y administración</small>
        </div>
        <div class="p-toolbar-group-right">
          <p-button
            label="Nuevo Usuario"
            icon="pi pi-plus"
            (onClick)="mostrarDialogNuevo()"
            class="p-button-success">
          </p-button>
        </div>
      </p-toolbar>

      <!-- Cards de estadísticas -->
      <div class="grid mb-4">
        <div class="col-12 lg:col-4">
          <p-card>
            <div class="text-center">
              <div class="text-3xl font-bold text-blue-600">{{ totalUsuarios() }}</div>
              <div class="text-sm text-gray-600">Total Usuarios</div>
            </div>
          </p-card>
        </div>
        
        <div class="col-12 lg:col-4">
          <p-card>
            <div class="text-center">
              <div class="text-3xl font-bold text-green-600">{{ usuariosActivos() }}</div>
              <div class="text-sm text-gray-600">Usuarios Activos</div>
            </div>
          </p-card>
        </div>

        <div class="col-12 lg:col-4">
          <p-card>
            <div class="text-center">
              <div class="text-3xl font-bold text-purple-600">{{ estadisticas().ADMIN }}</div>
              <div class="text-sm text-gray-600">Administradores</div>
            </div>
          </p-card>
        </div>
      </div>

      <!-- Tabla de usuarios -->
      <p-table 
        [value]="usuariosFiltrados()" 
        [loading]="cargando()"
        [paginator]="true"
        [rows]="10"
        responsiveLayout="scroll">
        
        <ng-template pTemplate="caption">
          <div class="flex justify-content-between align-items-center">
            <span class="p-input-icon-left">
              <i class="pi pi-search"></i>
              <input 
                pInputText 
                type="text" 
                (input)="aplicarFiltro($event)"
                placeholder="Buscar por nombre, email o rol...">
            </span>
          </div>
        </ng-template>

        <ng-template pTemplate="header">
          <tr>
            <th>Usuario</th>
            <th>Email</th>
            <th>Rol</th>
            <th>Estado</th>
            <th class="text-center">Acciones</th>
          </tr>
        </ng-template>

        <ng-template pTemplate="body" let-usuario>
          <tr>
            <td>
              <div class="flex align-items-center">
                <div class="font-medium">{{ usuario.nombre }}</div>
                <div class="text-sm text-gray-500 ml-2">ID: {{ usuario.id_usuario }}</div>
              </div>
            </td>
            <td>{{ usuario.email }}</td>
            <td>
              <p-tag 
                [value]="usuario.rol" 
                [severity]="obtenerSeveridadRol(usuario.rol)">
              </p-tag>
            </td>
            <td>
              <p-tag 
                [value]="usuario.activo ? 'Activo' : 'Inactivo'"
                [severity]="usuario.activo ? 'success' : 'danger'">
              </p-tag>
            </td>
            <td class="text-center">
              <p-button
                icon="pi pi-pencil"
                class="p-button-rounded p-button-text p-button-info mr-2"
                (onClick)="editarUsuario(usuario)">
              </p-button>
              <p-button
                icon="pi pi-trash"
                class="p-button-rounded p-button-text p-button-danger"
                (onClick)="confirmarEliminar(usuario)">
              </p-button>
            </td>
          </tr>
        </ng-template>

        <ng-template pTemplate="emptymessage">
          <tr>
            <td colspan="5" class="text-center py-4">
              <div>
                <i class="pi pi-users text-4xl text-gray-400"></i>
                <div class="text-lg font-medium text-gray-600 mt-2">No hay usuarios registrados</div>
                <div class="text-sm text-gray-400">Haz clic en "Nuevo Usuario" para agregar el primero</div>
              </div>
            </td>
          </tr>
        </ng-template>
      </p-table>

      <!-- Dialog para crear/editar usuario -->
      <p-dialog
        [(visible)]="dialogVisible"
        [modal]="true"
        [closable]="true"
        [resizable]="false"
        [style]="{width: '450px'}"
        [header]="esEdicion() ? 'Editar Usuario' : 'Nuevo Usuario'">
        
        <form [formGroup]="formularioUsuario" (ngSubmit)="guardarUsuario()">
          <div class="field">
            <label for="nombre" class="block text-sm font-medium mb-2">Nombre Completo *</label>
            <input 
              id="nombre"
              type="text" 
              pInputText 
              formControlName="nombre"
              class="w-full"
              placeholder="Ingresa el nombre completo">
            <small 
              *ngIf="formularioUsuario.get('nombre')?.invalid && formularioUsuario.get('nombre')?.touched"
              class="p-error">
              El nombre es requerido (mínimo 3 caracteres)
            </small>
          </div>

          <div class="field">
            <label for="email" class="block text-sm font-medium mb-2">Email *</label>
            <input 
              id="email"
              type="email" 
              pInputText 
              formControlName="email"
              class="w-full"
              placeholder="usuario@ejemplo.com">
            <small 
              *ngIf="formularioUsuario.get('email')?.invalid && formularioUsuario.get('email')?.touched"
              class="p-error">
              Email válido es requerido
            </small>
          </div>

          <div class="field" *ngIf="!esEdicion()">
            <label for="clave" class="block text-sm font-medium mb-2">Contraseña *</label>
            <input 
              id="clave"
              type="password" 
              pInputText 
              formControlName="clave"
              class="w-full"
              placeholder="Contraseña segura">
            <small 
              *ngIf="formularioUsuario.get('clave')?.invalid && formularioUsuario.get('clave')?.touched"
              class="p-error">
              Contraseña requerida (mínimo 6 caracteres)
            </small>
          </div>

          <div class="field">
            <label for="rol" class="block text-sm font-medium mb-2">Rol *</label>
            <select 
              id="rol"
              formControlName="rol"
              class="w-full p-2 border border-gray-300 rounded">
              <option value="">Selecciona un rol</option>
              <option value="ADMIN">ADMIN - Administrador</option>
              <option value="GERENTE">GERENTE - Gerente</option>
              <option value="OPERARIO">OPERARIO - Operario</option>
            </select>
          </div>

          <div class="flex justify-content-end gap-2 mt-4">
            <p-button
              label="Cancelar"
              icon="pi pi-times"
              class="p-button-text"
              (onClick)="cerrarDialog()">
            </p-button>
            <p-button
              label="Guardar"
              icon="pi pi-check"
              type="submit"
              [disabled]="formularioUsuario.invalid || guardando()"
              [loading]="guardando()">
            </p-button>
          </div>
        </form>
      </p-dialog>

      <!-- Toast para notificaciones -->
      <p-toast></p-toast>
      
      <!-- Confirmación para eliminación -->
      <p-confirmDialog></p-confirmDialog>
    </div>
  `,
  styles: [`
    .field {
      margin-bottom: 1rem;
    }
    
    .p-error {
      color: #e24c4c;
      font-size: 0.75rem;
      margin-top: 0.25rem;
    }

    .card {
      padding: 1rem;
    }

    select {
      border: 1px solid #d1d5db;
      border-radius: 0.375rem;
      padding: 0.5rem;
      font-size: 0.875rem;
      width: 100%;
    }

    select:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 1px #3b82f6;
    }
  `]
})
export class GestionUsuariosComponent implements OnInit {
  private fb = inject(FormBuilder);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);

  // Signals para estado reactivo
  usuarios = signal<Usuario[]>([]);
  cargando = signal(false);
  guardando = signal(false);
  dialogVisible = signal(false);
  esEdicion = signal(false);
  usuarioSeleccionado = signal<Usuario | null>(null);
  filtroGlobal = signal('');

  // Computed properties
  totalUsuarios = computed(() => this.usuarios().length);
  usuariosActivos = computed(() => this.usuarios().filter(u => u.activo !== false).length);
  usuariosFiltrados = computed(() => {
    const filtro = this.filtroGlobal().toLowerCase();
    if (!filtro) return this.usuarios();
    
    return this.usuarios().filter(usuario =>
      usuario.nombre.toLowerCase().includes(filtro) ||
      usuario.email.toLowerCase().includes(filtro) ||
      usuario.rol.toLowerCase().includes(filtro)
    );
  });

  estadisticas = computed(() => {
    const usuarios = this.usuarios();
    return {
      ADMIN: usuarios.filter(u => u.rol === 'ADMIN').length,
      GERENTE: usuarios.filter(u => u.rol === 'GERENTE').length,
      OPERARIO: usuarios.filter(u => u.rol === 'OPERARIO').length
    };
  });

  // Formulario reactivo
  formularioUsuario!: FormGroup;

  ngOnInit() {
    this.inicializarFormulario();
    this.cargarUsuarios();
  }

  inicializarFormulario() {
    this.formularioUsuario = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      clave: ['', [Validators.required, Validators.minLength(6)]],
      rol: ['', Validators.required]
    });
  }

  async cargarUsuarios() {
    this.cargando.set(true);
    try {
      // Simular datos de usuarios (reemplazar con servicio real)
      const usuariosSimulados: Usuario[] = [
        {
          id_usuario: 1,
          nombre: 'José Martínez',
          email: 'jose@gmail.com',
          rol: 'ADMIN',
          activo: true,
          fechaCreacion: new Date('2024-01-15')
        },
        {
          id_usuario: 2,
          nombre: 'María Gerente',
          email: 'gerente@minimarket.com',
          rol: 'GERENTE',
          activo: true,
          fechaCreacion: new Date('2024-02-01')
        },
        {
          id_usuario: 3,
          nombre: 'Carlos Operario',
          email: 'operario@minimarket.com',
          rol: 'OPERARIO',
          activo: true,
          fechaCreacion: new Date('2024-02-15')
        }
      ];
      
      this.usuarios.set(usuariosSimulados);
    } catch (error) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudieron cargar los usuarios'
      });
    } finally {
      this.cargando.set(false);
    }
  }

  mostrarDialogNuevo() {
    this.esEdicion.set(false);
    this.usuarioSeleccionado.set(null);
    this.formularioUsuario.reset();
    this.dialogVisible.set(true);
  }

  editarUsuario(usuario: Usuario) {
    this.esEdicion.set(true);
    this.usuarioSeleccionado.set(usuario);
    this.formularioUsuario.patchValue({
      nombre: usuario.nombre,
      email: usuario.email,
      rol: usuario.rol
    });
    this.dialogVisible.set(true);
  }

  async guardarUsuario() {
    if (this.formularioUsuario.valid) {
      this.guardando.set(true);
      try {
        const datosUsuario = this.formularioUsuario.value;
        
        if (this.esEdicion()) {
          // Simular actualización
          const usuarios = this.usuarios();
          const index = usuarios.findIndex(u => u.id_usuario === this.usuarioSeleccionado()?.id_usuario);
          if (index !== -1) {
            usuarios[index] = {
              ...usuarios[index],
              ...datosUsuario
            };
            this.usuarios.set([...usuarios]);
          }
          
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Usuario actualizado correctamente'
          });
        } else {
          // Simular creación
          const nuevoUsuario: Usuario = {
            id_usuario: Math.max(...this.usuarios().map(u => u.id_usuario)) + 1,
            ...datosUsuario,
            activo: true,
            fechaCreacion: new Date()
          };
          
          this.usuarios.set([...this.usuarios(), nuevoUsuario]);
          
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Usuario registrado correctamente'
          });
        }
        
        this.cerrarDialog();
      } catch (error) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo guardar el usuario'
        });
      } finally {
        this.guardando.set(false);
      }
    }
  }

  confirmarEliminar(usuario: Usuario) {
    this.confirmationService.confirm({
      message: `¿Estás seguro de eliminar al usuario "${usuario.nombre}"?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.eliminarUsuario(usuario);
      }
    });
  }

  async eliminarUsuario(usuario: Usuario) {
    try {
      const usuarios = this.usuarios().filter(u => u.id_usuario !== usuario.id_usuario);
      this.usuarios.set(usuarios);
      
      this.messageService.add({
        severity: 'success',
        summary: 'Éxito',
        detail: 'Usuario eliminado correctamente'
      });
    } catch (error) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo eliminar el usuario'
      });
    }
  }

  cerrarDialog() {
    this.dialogVisible.set(false);
    this.formularioUsuario.reset();
    this.usuarioSeleccionado.set(null);
  }

  aplicarFiltro(event: any) {
    this.filtroGlobal.set(event.target.value);
  }

  obtenerSeveridadRol(rol: string): 'success' | 'info' | 'warn' | 'danger' {
    switch (rol) {
      case 'ADMIN': return 'success';
      case 'GERENTE': return 'info';
      case 'OPERARIO': return 'warn';
      default: return 'info';
    }
  }
}