import { Component, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputOtp } from 'primeng/inputotp';
import { PasswordModule } from 'primeng/password';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PasswordRecoveryService } from '../../../../core/services/password-recovery.service';
import { PasswordRecoveryResponse } from '../../../../core/models/password-recovery.models';
import { MessageModule } from 'primeng/message';

type RecoveryStep = 'email' | 'otp' | 'password' | 'success';

@Component({
  selector: 'app-password-recovery',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    InputTextModule,
    
    InputOtp,
    PasswordModule,
    ToastModule,
    ProgressSpinnerModule,
    MessageModule
  ],
  templateUrl: './password-recovery.html',
  providers: [MessageService]
})
export class PasswordRecoveryComponent {
  // Señales de estado
  currentStep = signal<RecoveryStep>('email');
  loading = signal(false);
  email = signal('');
  otpCode = signal<string | null>(null);
  newPassword = signal('');
  confirmPassword = signal('');
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  expiryTime = signal<string | null>(null);

  // Computed para validaciones
  isEmailValid = computed(() => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(this.email());
  });

  isOtpComplete = computed(() => {
    const code = this.otpCode();
    return code !== null && code.length === 6;
  });

  isPasswordValid = computed(() => {
    const pwd = this.newPassword();
    return pwd.length >= 6;
  });

  passwordsMatch = computed(() => {
    return this.newPassword() === this.confirmPassword() && this.newPassword().length > 0;
  });

  canSubmitEmail = computed(() => !this.loading() && this.isEmailValid());
  canVerifyOtp = computed(() => !this.loading() && this.isOtpComplete());
  canResetPassword = computed(() => !this.loading() && this.isPasswordValid() && this.passwordsMatch());

  private passwordRecoveryService = inject(PasswordRecoveryService);
  private router = inject(Router);

  /**
   * Paso 1: Enviar código OTP al email
   */
  requestPasswordReset(): void {
    if (!this.canSubmitEmail()) return;

    this.loading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.passwordRecoveryService.requestPasswordReset(this.email()).subscribe({
      next: (response: PasswordRecoveryResponse) => {
        this.loading.set(false);
        if (response.success) {
          this.successMessage.set(response.message);
          this.expiryTime.set(response.expiresIn || '10 minutos');
          this.currentStep.set('otp');
        } else {
          this.errorMessage.set(response.message);
        }
      },
      error: (error: any) => {
        this.loading.set(false);
        this.errorMessage.set('Error al solicitar código. Intenta nuevamente.');
        console.error('Error requesting password reset:', error);
      }
    });
  }

  /**
   * Paso 2: Verificar código OTP
   */
  verifyOtpCode(): void {
    if (!this.canVerifyOtp()) return;

    const code = this.otpCode();
    if (!code) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.passwordRecoveryService.verifyOtp(this.email(), code).subscribe({
      next: (response: PasswordRecoveryResponse) => {
        this.loading.set(false);
        if (response.success && response.valid) {
          this.successMessage.set('Código verificado correctamente');
          this.currentStep.set('password');
        } else {
          this.errorMessage.set(response.message);
          this.otpCode.set(null);
        }
      },
      error: (error: any) => {
        this.loading.set(false);
        this.errorMessage.set('Error al verificar código. Intenta nuevamente.');
        this.otpCode.set(null);
        console.error('Error verifying OTP:', error);
      }
    });
  }

  /**
   * Paso 3: Restablecer contraseña
   */
  resetPassword(): void {
    if (!this.canResetPassword()) return;

    const code = this.otpCode();
    if (!code) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.passwordRecoveryService.resetPassword(
      this.email(),
      code,
      this.newPassword()
    ).subscribe({
      next: (response: PasswordRecoveryResponse) => {
        this.loading.set(false);
        if (response.success) {
          this.successMessage.set(response.message);
          this.currentStep.set('success');
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 3000);
        } else {
          this.errorMessage.set(response.message);
        }
      },
      error: (error: any) => {
        this.loading.set(false);
        this.errorMessage.set('Error al restablecer contraseña. Intenta nuevamente.');
        console.error('Error resetting password:', error);
      }
    });
  }

  /**
   * Reenviar código OTP
   */
  resendCode(): void {
    this.otpCode.set(null);
    this.currentStep.set('email');
    this.requestPasswordReset();
  }

  /**
   * Volver al login
   */
  goToLogin(): void {
    this.router.navigate(['/auth/login']);
  }

  /**
   * Reiniciar proceso
   */
  restart(): void {
    this.currentStep.set('email');
    this.email.set('');
    this.otpCode.set(null);
    this.newPassword.set('');
    this.confirmPassword.set('');
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.expiryTime.set(null);
  }

  /**
   * Obtener subtítulo según paso actual
   */
  getStepSubtitle(): string {
    const step = this.currentStep();
    const subtitles: Record<RecoveryStep, string> = {
      email: 'Ingresa tu correo electrónico para recibir un código de verificación',
      otp: 'Verifica tu identidad con el código que enviamos',
      password: 'Crea una nueva contraseña segura',
      success: 'Tu contraseña ha sido actualizada correctamente'
    };
    return subtitles[step];
  }
}
