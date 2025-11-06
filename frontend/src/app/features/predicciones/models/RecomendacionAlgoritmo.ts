/**
 * Recomendación automática de algoritmo de predicción.
 * El backend analiza los datos históricos y sugiere la mejor configuración.
 */
export interface RecomendacionAlgoritmo {
  /** Código del algoritmo recomendado (bean name de Spring) */
  algoritmo: string;
  
  /** Parámetros optimizados calculados automáticamente */
  parametros: Record<string, number>;
  
  /** Explicación en lenguaje natural de por qué se eligió este algoritmo */
  justificacion: string;
  
  /** Nivel de confianza de la recomendación (0.0 - 1.0) */
  confianza: number;
}
