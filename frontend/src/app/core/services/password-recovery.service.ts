import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ForgotPasswordRequest,
  VerifyOtpRequest,
  ResetPasswordRequest,
  PasswordRecoveryResponse
} from '../models/password-recovery.models';

/**
 * Servicio para gestionar recuperación de contraseñas.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Injectable({
  providedIn: 'root'
})
export class PasswordRecoveryService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/auth`;

  /**
   * Solicita código OTP para recuperación de contraseña
   */
  requestPasswordReset(email: string): Observable<PasswordRecoveryResponse> {
    const request: ForgotPasswordRequest = { email };
    return this.http.post<PasswordRecoveryResponse>(`${this.apiUrl}/forgot-password`, request);
  }

  /**
   * Verifica el código OTP sin cambiar la contraseña
   */
  verifyOtp(email: string, code: string): Observable<PasswordRecoveryResponse> {
    const request: VerifyOtpRequest = { email, code };
    return this.http.post<PasswordRecoveryResponse>(`${this.apiUrl}/verify-otp`, request);
  }

  /**
   * Restablece la contraseña usando código OTP
   */
  resetPassword(email: string, code: string, newPassword: string): Observable<PasswordRecoveryResponse> {
    const request: ResetPasswordRequest = { email, code, newPassword };
    return this.http.post<PasswordRecoveryResponse>(`${this.apiUrl}/reset-password`, request);
  }
}
