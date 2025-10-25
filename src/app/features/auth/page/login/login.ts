import { Component, inject, signal, computed } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { AuthService } from '../../../../core/services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ToastModule,
    CardModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    DividerModule
  ],
  providers: [MessageService],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export default class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);

  // Signals
  cargando = signal(false);
  mostrarContrasenia = signal(false);

  // Form
  loginForm: FormGroup;

  // URL para redirect después del login
  private returnUrl: string = '';

  constructor() {
    this.loginForm = this.fb.group({
      email: ['jose@gmail.com', [Validators.required, Validators.email]],
      clave: ['contraseña123', [Validators.required, Validators.minLength(6)]]
    });

    // Si ya está autenticado, redirigir
    if (this.authService.isAutenticado) {
      this.router.navigate(['/administracion']);
    }

    // Obtener returnUrl del query params
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/administracion';
    });
  }

  /**
   * Manejar submit del formulario
   */
  onLogin() {
    if (this.loginForm.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Validación',
        detail: 'Por favor completa todos los campos'
      });
      return;
    }

    this.cargando.set(true);
    const { email, clave } = this.loginForm.value;

    this.authService.login(email, clave).subscribe({
      next: (response) => {
        this.authService.setAuthData(response);
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: `¡Bienvenido ${response.nombreCompleto}!`
        });
        
        // Redirect después de 1 segundo
        setTimeout(() => {
          this.router.navigate([this.returnUrl]);
        }, 1000);
      },
      error: (error) => {
        this.cargando.set(false);
        
        let errorMsg = 'Error al iniciar sesión';
        
        if (error.status === 401 || error.status === 400) {
          errorMsg = 'Email o contraseña incorrectos';
        } else if (error.status === 0) {
          errorMsg = 'No se puede conectar al servidor';
        } else if (error.error) {
          errorMsg = error.error;
        }

        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: errorMsg,
          life: 5000
        });
      }
    });
  }

  /**
   * Obtener error de un campo
   */
  getFieldError(fieldName: string): string | null {
    const field = this.loginForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return 'Este campo es requerido';
      if (field.errors['email']) return 'Email inválido';
      if (field.errors['minlength']) return `Mínimo ${field.errors['minlength'].requiredLength} caracteres`;
    }
    return null;
  }

  /**
   * Verificar si hay error en un campo
   */
  hasFieldError(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
}
