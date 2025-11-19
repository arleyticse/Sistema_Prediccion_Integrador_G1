// Interfaz para la solicitud de login
export interface AuthRequest {
  email: string;
  clave: string;
}

// Interfaz para la respuesta del servidor
export interface AuthResponse {
  token: string;
  refreshToken: string;
  nombreCompleto: string;
  email: string;
  rol: string;
}

// Interfaz para los datos del usuario en memoria
export interface UsuarioInfo {
  token: string;
  refreshToken?: string;
  nombreCompleto: string;
  email: string;
  rol: string;
}
