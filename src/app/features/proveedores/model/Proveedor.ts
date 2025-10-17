export interface Proveedor {
	proveedorId: number;
	razonSocial: string;
	nombreComercial: string;
	rucNit: string;
	telefono: string;
	email: string;
	direccion: string;
	ciudad: string;
	pais: string;
	personaContacto: string;
	tiempoEntregaDias: number;
	diasCredito: number;
	calificacion: number;
	estado: boolean;
	fechaRegistro: string;
	observaciones: string;
}