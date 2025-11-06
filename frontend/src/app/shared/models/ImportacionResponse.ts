export interface DetalleErrorImportacion {
  linea: number;
  identificador: string;
  campo: string;
  valorInvalido: string;
  mensajeError: string;
}

export interface ImportacionResponse {
  nombreArchivo: string;
  tipoImportacion: string;
  fechaImportacion: string;
  estado: 'COMPLETADA' | 'PARCIAL' | 'FALLIDA' | 'INVALIDO' | 'ERROR_LECTURA';
  registrosProcesados: number;
  registrosExitosos: number;
  registrosFallidos: number;
  errores: DetalleErrorImportacion[];
  duracionSegundos: number;
  importacionId?: number;
}
