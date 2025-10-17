export interface Column<T> {
    field: keyof T | 'acciones';
    header: string;
}