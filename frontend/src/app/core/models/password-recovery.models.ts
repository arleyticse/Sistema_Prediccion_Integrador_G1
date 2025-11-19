export interface ForgotPasswordRequest {
  email: string;
}

export interface VerifyOtpRequest {
  email: string;
  code: string;
}

export interface ResetPasswordRequest {
  email: string;
  code: string;
  newPassword: string;
}

export interface PasswordRecoveryResponse {
  success: boolean;
  message: string;
  expiresIn?: string;
  valid?: boolean;
}
