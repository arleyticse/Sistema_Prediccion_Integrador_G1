/**
 * Nivel de criticidad de una alerta.
 * 
 * Define la prioridad con la que debe ser atendida una alerta,
 * afectando su visualizacion y ordenamiento en el dashboard.
 */
export enum NivelCriticidad {
  BAJA = 'BAJA',
  MEDIA = 'MEDIA',
  ALTA = 'ALTA',
  CRITICA = 'CRITICA'
}

/**
 * Configuracion de severidad para p-tag de PrimeNG.
 * Mapea criticidad a colores del design system.
 */
export const CriticidadSeverity: Record<NivelCriticidad, 'success' | 'info' | 'warn' | 'danger'> = {
  [NivelCriticidad.BAJA]: 'success',
  [NivelCriticidad.MEDIA]: 'info',
  [NivelCriticidad.ALTA]: 'warn',
  [NivelCriticidad.CRITICA]: 'danger'
};

/**
 * Iconos para cada nivel de criticidad.
 */
export const CriticidadIcon: Record<NivelCriticidad, string> = {
  [NivelCriticidad.BAJA]: 'pi pi-info-circle',
  [NivelCriticidad.MEDIA]: 'pi pi-exclamation-circle',
  [NivelCriticidad.ALTA]: 'pi pi-exclamation-triangle',
  [NivelCriticidad.CRITICA]: 'pi pi-times-circle'
};
