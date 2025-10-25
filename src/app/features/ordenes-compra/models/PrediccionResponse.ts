export interface PrediccionResponse {
  prediccionId: number;
  producto: {
    productoId: number;
    nombre: string;
  };
  demandaPredichaTotal: number;
  fechaPrediccion: string;
  fechaProyeccion: string;
  precision: number;
  metodoUtilizado: string;
}