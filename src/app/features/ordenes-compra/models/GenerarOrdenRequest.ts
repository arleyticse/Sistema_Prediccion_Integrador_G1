export interface GenerarOrdenRequest {
  prediccionId: number;
  cantidadAdicional?: number;
  notasEspeciales?: string;
  fechaEntregaDeseada?: string;
}