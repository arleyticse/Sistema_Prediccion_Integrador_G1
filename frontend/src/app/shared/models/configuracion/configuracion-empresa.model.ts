/**
 * Modelo que representa la configuración de la empresa.
 * 
 * Patrón Singleton: solo existe un registro (id = 1) en el backend.
 * Contiene información básica de la empresa y logo en Base64.
 * 
 * @version 1.0
 * @since 2025-11-19
 */
export interface ConfiguracionEmpresa {
  /**
   * ID fijo = 1 (singleton pattern)
   */
  id: number;

  /**
   * Nombre de la empresa
   */
  nombreEmpresa: string;

  /**
   * RUC o número de identificación fiscal
   */
  ruc?: string | null;

  /**
   * Dirección física de la empresa
   */
  direccion?: string | null;

  /**
   * Teléfono de contacto
   */
  telefono?: string | null;

  /**
   * Email de contacto
   */
  email?: string | null;

  /**
   * Logo en formato Base64 (sin prefijo Data URL)
   * Máximo: 100KB (~150000 caracteres)
   */
  logoBase64?: string | null;

  /**
   * Tipo MIME del logo
   * Ejemplos: "image/png", "image/jpeg", "image/webp"
   */
  logoMimeType?: string | null;

  /**
   * Nombre de la persona de contacto
   */
  nombreContacto?: string | null;

  /**
   * Cargo de la persona de contacto
   */
  cargoContacto?: string | null;

  /**
   * Fecha de creación del registro
   */
  fechaCreacion?: string;

  /**
   * Fecha de última modificación
   */
  fechaModificacion?: string;
}

/**
 * DTO para actualizar solo el logo
 */
export interface ActualizarLogoRequest {
  logoBase64: string;
  logoMimeType: string;
}

/**
 * Respuesta de validación de logo
 */
export interface ValidacionLogoResponse {
  valido: boolean;
  mensaje: string;
  tamanoActual?: number;
  tamano?: number;
}
