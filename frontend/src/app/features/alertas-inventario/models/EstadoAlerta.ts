/**
 * Estado del ciclo de vida de una alerta.
 * 
 * Representa las diferentes etapas por las que pasa una alerta
 * desde su generacion hasta su resolucion o cierre.
 */
export enum EstadoAlerta {
  PENDIENTE = 'PENDIENTE',
  EN_PROCESO = 'EN_PROCESO',
  RESUELTA = 'RESUELTA',
  IGNORADA = 'IGNORADA',
  ESCALADA = 'ESCALADA'
}

/**
 * Labels amigables para mostrar en UI.
 */
export const EstadoAlertaLabels: Record<EstadoAlerta, string> = {
  [EstadoAlerta.PENDIENTE]: 'Pendiente',
  [EstadoAlerta.EN_PROCESO]: 'En Proceso',
  [EstadoAlerta.RESUELTA]: 'Resuelta',
  [EstadoAlerta.IGNORADA]: 'Ignorada',
  [EstadoAlerta.ESCALADA]: 'Escalada'
};

/**
 * Configuracion de severidad para p-tag de PrimeNG.
 */
export const EstadoAlertaSeverity: Record<EstadoAlerta, 'secondary' | 'info' | 'success' | 'warn' | 'danger'> = {
  [EstadoAlerta.PENDIENTE]: 'warn',
  [EstadoAlerta.EN_PROCESO]: 'info',
  [EstadoAlerta.RESUELTA]: 'success',
  [EstadoAlerta.IGNORADA]: 'secondary',
  [EstadoAlerta.ESCALADA]: 'danger'
};
