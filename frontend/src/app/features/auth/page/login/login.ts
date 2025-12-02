import { Component, inject, signal, computed } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { DialogModule } from 'primeng/dialog';
import { InputOtpModule } from 'primeng/inputotp';
import { AuthService } from '../../../../core/services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterLink,
    ToastModule,
    CardModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    DividerModule,
    DialogModule,
    InputOtpModule
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

  anio = new Date().getFullYear();
  
  // Signals
  cargando = signal(false);
  mostrarContrasenia = signal(false);
  
  // Signals para desbloqueo de cuenta
  mostrarDialogoDesbloqueo = signal(false);
  emailBloqueado = signal('');
  codigoOtp = signal('');
  enviandoCodigo = signal(false);
  verificandoCodigo = signal(false);
  codigoEnviado = signal(false);
  intentosRestantes = signal(5);

  // Form
  loginForm: FormGroup;

  // URL para redirect después del login
  private returnUrl: string = '';

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      clave: ['', [Validators.required, Validators.minLength(6)]]
    });

    // Si ya está autenticado, redirigir
    if (this.authService.isAutenticado) {
      this.router.navigate(['/administracion']);
    }

    // Obtener returnUrl del query params
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/administracion/dashboard';
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
          const queryParams = response.rol === 'GERENTE' ? { showBorradores: true } : {};
          this.router.navigate([this.returnUrl], { queryParams: queryParams });
        }, 1000);
      },
      error: (error) => {
        this.cargando.set(false);
        
        // Manejar cuenta bloqueada (HTTP 423)
        if (error.status === 423) {
          this.emailBloqueado.set(email);
          this.mostrarDialogoDesbloqueo.set(true);
          this.messageService.add({
            severity: 'error',
            summary: 'Cuenta Bloqueada',
            detail: 'Tu cuenta ha sido bloqueada. Usa el código OTP para desbloquearla.',
            life: 5000
          });
          return;
        }
        
        // Manejar credenciales inválidas con intentos restantes
        if (error.status === 401 && error.error?.intentosRestantes !== undefined) {
          this.intentosRestantes.set(error.error.intentosRestantes);
          this.messageService.add({
            severity: 'warn',
            summary: 'Credenciales Inválidas',
            detail: `Contraseña incorrecta. Te quedan ${error.error.intentosRestantes} intentos.`,
            life: 5000
          });
          return;
        }
        
        let errorMsg = 'Error al iniciar sesión';
        
        if (error.status === 401 || error.status === 400) {
          errorMsg = 'Email o contraseña incorrectos';
        } else if (error.status === 0) {
          errorMsg = 'No se puede conectar al servidor';
        } else if (error.error?.message) {
          errorMsg = error.error.message;
        } else if (typeof error.error === 'string') {
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
   * Solicitar código OTP para desbloquear cuenta
   */
  solicitarCodigoDesbloqueo() {
    this.enviandoCodigo.set(true);
    
    this.authService.solicitarDesbloqueo(this.emailBloqueado()).subscribe({
      next: (response: any) => {
        this.enviandoCodigo.set(false);
        if (response.success) {
          this.codigoEnviado.set(true);
          this.messageService.add({
            severity: 'success',
            summary: 'Código Enviado',
            detail: 'Revisa tu correo electrónico para obtener el código.',
            life: 5000
          });
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'No se pudo enviar el código.',
            life: 5000
          });
        }
      },
      error: (error) => {
        this.enviandoCodigo.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al enviar el código. Intenta nuevamente.',
          life: 5000
        });
      }
    });
  }

  /**
   * Verificar código OTP y desbloquear cuenta
   */
  desbloquearCuenta() {
    if (!this.codigoOtp() || this.codigoOtp().length !== 6) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Validación',
        detail: 'Ingresa el código de 6 dígitos.',
        life: 3000
      });
      return;
    }

    this.verificandoCodigo.set(true);

    this.authService.desbloquearCuenta(this.emailBloqueado(), this.codigoOtp()).subscribe({
      next: (response: any) => {
        this.verificandoCodigo.set(false);
        if (response.success) {
          this.mostrarDialogoDesbloqueo.set(false);
          this.codigoOtp.set('');
          this.codigoEnviado.set(false);
          this.messageService.add({
            severity: 'success',
            summary: 'Cuenta Desbloqueada',
            detail: 'Tu cuenta ha sido desbloqueada. Ya puedes iniciar sesión.',
            life: 5000
          });
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'Código incorrecto.',
            life: 5000
          });
        }
      },
      error: (error) => {
        this.verificandoCodigo.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al verificar el código. Intenta nuevamente.',
          life: 5000
        });
      }
    });
  }

  /**
   * Cerrar diálogo de desbloqueo
   */
  cerrarDialogoDesbloqueo() {
    this.mostrarDialogoDesbloqueo.set(false);
    this.codigoOtp.set('');
    this.codigoEnviado.set(false);
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
