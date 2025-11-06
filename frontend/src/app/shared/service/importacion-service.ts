import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ImportacionResponse } from '../models/ImportacionResponse';

/**
 * Servicio base para operaciones de importación CSV
 */
@Injectable({
  providedIn: 'root'
})
export class ImportacionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /**
   * Importa datos desde un archivo CSV
   */
  importar(archivo: File, endpoint: string): Observable<ImportacionResponse> {
    const formData = new FormData();
    formData.append('archivo', archivo);

    return this.http.post<ImportacionResponse>(
      `${this.apiUrl}${endpoint}/importar`,
      formData
    ).pipe(
      map(response => this.processResponse(response)),
      catchError(error => this.handleError(error))
    );
  }

  /**
   * Valida el formato de un archivo CSV sin importar
   */
  validar(archivo: File, endpoint: string): Observable<ImportacionResponse> {
    const formData = new FormData();
    formData.append('archivo', archivo);

    return this.http.post<any>(
      `${this.apiUrl}${endpoint}/validar`,
      formData
    ).pipe(
      map(response => {
        // Si la respuesta es un Map con {valido, errores, mensaje}
        if (typeof response === 'object' && 'valido' in response) {
          return this.createImportacionResponseFromValidation(response, archivo.name);
        }
        // Si ya es ImportacionResponse
        return this.processResponse(response);
      }),
      catchError(error => this.handleError(error))
    );
  }

  /**
   * Convierte respuesta de validación a ImportacionResponse
   */
  private createImportacionResponseFromValidation(validationResponse: any, fileName: string): ImportacionResponse {
    const erroresDetallados = (validationResponse.errores || []).map((errorMsg: string, index: number) => ({
      linea: index + 1,
      identificador: '',
      campo: '',
      valorInvalido: '',
      mensajeError: errorMsg
    }));

    return {
      nombreArchivo: fileName,
      tipoImportacion: 'VALIDACION',
      fechaImportacion: new Date().toISOString(),
      estado: validationResponse.valido ? 'COMPLETADA' : 'INVALIDO',
      registrosProcesados: 0,
      registrosExitosos: 0,
      registrosFallidos: validationResponse.errores?.length || 0,
      errores: erroresDetallados,
      duracionSegundos: 0
    };
  }

  /**
   * Descarga la plantilla CSV
   */
  descargarPlantilla(endpoint: string, nombreArchivo: string): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}${endpoint}/plantilla`,
      { responseType: 'blob' }
    ).pipe(
      catchError(error => this.handleError(error))
    );
  }

  /**
   * Procesa y normaliza la respuesta del servidor
   */
  private processResponse(response: any): ImportacionResponse {
    // Asegurar que la respuesta tiene la estructura esperada
    return {
      nombreArchivo: response.nombreArchivo || 'desconocido',
      tipoImportacion: response.tipoImportacion || 'desconocida',
      fechaImportacion: response.fechaImportacion || new Date().toISOString(),
      estado: response.estado || 'FALLIDA',
      registrosProcesados: response.registrosProcesados || 0,
      registrosExitosos: response.registrosExitosos || 0,
      registrosFallidos: response.registrosFallidos || 0,
      errores: response.errores || [],
      duracionSegundos: response.duracionSegundos || 0,
      importacionId: response.importacionId
    };
  }

  private handleError(error: HttpErrorResponse | any): Observable<never> {
    let errorMessage = 'Ocurrió un error durante la operación';
    
    if (error instanceof HttpErrorResponse) {
      if (error.error instanceof ErrorEvent) {
        // Error del cliente
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Error del servidor
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else {
          errorMessage = `Error ${error.status}: ${error.statusText}`;
        }
      }
    } else if (error instanceof Error) {
      errorMessage = error.message;
    }
    
    console.error('🔴 ImportacionService Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}

