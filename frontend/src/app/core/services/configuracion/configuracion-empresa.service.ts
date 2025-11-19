import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ConfiguracionEmpresa,
  ActualizarLogoRequest,
  ValidacionLogoResponse
} from '../../../shared/models/configuracion/configuracion-empresa.model';

/**
 * Servicio para gestionar la configuración de la empresa.
 * 
 * Gestiona información básica de la empresa y logo en Base64.
 * Solo existe un registro (singleton pattern) en el backend.
 * 
 * @version 1.0
 * @since 2025-11-19
 */
@Injectable({
  providedIn: 'root'
})
export class ConfiguracionEmpresaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/configuracion/empresa`;

  /**
   * Obtiene la configuración actual de la empresa.
   * 
   * GET /api/configuracion/empresa
   * 
   * @returns Observable con la configuración
   */
  obtenerConfiguracion(): Observable<ConfiguracionEmpresa> {
    return this.http.get<ConfiguracionEmpresa>(this.baseUrl).pipe(
      catchError((error) => {
        console.error('Error al obtener configuración de empresa:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Actualiza la configuración de la empresa.
   * Solo accesible por usuarios con rol GERENTE.
   * 
   * PUT /api/configuracion/empresa
   * 
   * @param configuracion datos a actualizar
   * @returns Observable con la configuración actualizada
   */
  actualizarConfiguracion(configuracion: ConfiguracionEmpresa): Observable<ConfiguracionEmpresa> {
    return this.http.put<ConfiguracionEmpresa>(this.baseUrl, configuracion).pipe(
      catchError((error) => {
        console.error('Error al actualizar configuración de empresa:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Actualiza solo el logo de la empresa.
   * Solo accesible por usuarios con rol GERENTE.
   * 
   * PATCH /api/configuracion/empresa/logo
   * 
   * @param request objeto con logoBase64 y logoMimeType
   * @returns Observable con la configuración actualizada
   */
  actualizarLogo(request: ActualizarLogoRequest): Observable<ConfiguracionEmpresa> {
    return this.http.patch<ConfiguracionEmpresa>(`${this.baseUrl}/logo`, request).pipe(
      catchError((error) => {
        console.error('Error al actualizar logo:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Elimina el logo de la empresa.
   * Solo accesible por usuarios con rol GERENTE.
   * 
   * DELETE /api/configuracion/empresa/logo
   * 
   * @returns Observable con la configuración sin logo
   */
  eliminarLogo(): Observable<ConfiguracionEmpresa> {
    return this.http.delete<ConfiguracionEmpresa>(`${this.baseUrl}/logo`).pipe(
      catchError((error) => {
        console.error('Error al eliminar logo:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Valida si un logo cumple con las restricciones de tamaño y tipo.
   * Útil para validación en frontend antes de enviar.
   * 
   * POST /api/configuracion/empresa/logo/validar
   * 
   * @param request objeto con logoBase64 y logoMimeType
   * @returns Observable con resultado de validación
   */
  validarLogo(request: ActualizarLogoRequest): Observable<ValidacionLogoResponse> {
    return this.http.post<ValidacionLogoResponse>(`${this.baseUrl}/logo/validar`, request).pipe(
      catchError((error) => {
        console.error('Error al validar logo:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Convierte un archivo de imagen a Base64.
   * 
   * @param file archivo de imagen
   * @returns Promise con string Base64 (sin prefijo Data URL)
   */
  convertirImagenABase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      
      reader.onload = () => {
        const result = reader.result as string;
        // Remover prefijo "data:image/...;base64,"
        const base64 = result.split(',')[1];
        resolve(base64);
      };
      
      reader.onerror = (error) => {
        reject(error);
      };
      
      reader.readAsDataURL(file);
    });
  }

  /**
   * Obtiene el Data URL completo del logo para mostrarlo en <img>.
   * 
   * @param logoBase64 logo en Base64 (sin prefijo)
   * @param mimeType tipo MIME del logo
   * @returns Data URL completo
   */
  obtenerDataUrlLogo(logoBase64: string | null | undefined, mimeType: string | null | undefined): string | null {
    if (!logoBase64 || !mimeType) {
      return null;
    }
    
    return `data:${mimeType};base64,${logoBase64}`;
  }

  /**
   * Comprime y redimensiona una imagen antes de subirla.
   * Objetivo: max 800x600px y ~100KB.
   * 
   * @param file archivo de imagen original
   * @param maxWidth ancho máximo (default: 800)
   * @param maxHeight alto máximo (default: 600)
   * @param quality calidad de compresión (0-1, default: 0.8)
   * @returns Promise con archivo optimizado
   */
  async optimizarImagen(
    file: File,
    maxWidth: number = 800,
    maxHeight: number = 600,
    quality: number = 0.8
  ): Promise<File> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');

      if (!ctx) {
        reject(new Error('No se pudo obtener contexto de canvas'));
        return;
      }

      img.onload = () => {
        // Calcular nuevas dimensiones manteniendo aspect ratio
        let width = img.width;
        let height = img.height;

        if (width > maxWidth) {
          height = (height * maxWidth) / width;
          width = maxWidth;
        }

        if (height > maxHeight) {
          width = (width * maxHeight) / height;
          height = maxHeight;
        }

        // Redimensionar
        canvas.width = width;
        canvas.height = height;
        ctx.drawImage(img, 0, 0, width, height);

        // Convertir a Blob con compresión
        canvas.toBlob(
          (blob) => {
            if (blob) {
              // Crear nuevo File con mismo nombre
              const optimizedFile = new File([blob], file.name, {
                type: file.type,
                lastModified: Date.now()
              });
              resolve(optimizedFile);
            } else {
              reject(new Error('No se pudo generar imagen optimizada'));
            }
          },
          file.type,
          quality
        );
      };

      img.onerror = () => {
        reject(new Error('Error al cargar imagen'));
      };

      img.src = URL.createObjectURL(file);
    });
  }

  /**
   * Valida el tamaño del archivo antes de procesarlo.
   * 
   * @param file archivo a validar
   * @param maxSizeKB tamaño máximo en KB (default: 100)
   * @returns true si el tamaño es válido
   */
  validarTamanoArchivo(file: File, maxSizeKB: number = 100): boolean {
    const maxBytes = maxSizeKB * 1024;
    return file.size <= maxBytes;
  }

  /**
   * Valida el tipo MIME del archivo.
   * 
   * @param file archivo a validar
   * @returns true si el tipo es válido
   */
  validarTipoImagen(file: File): boolean {
    const tiposPermitidos = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp'];
    return tiposPermitidos.includes(file.type);
  }
}
