import { Routes } from '@angular/router';
import { ManagementLayouts } from './layouts/management-layouts/management-layouts';
import { CategoriasComponent } from './features/categorias/page/categorias-component/categorias-component';
import { UnidadMedidaComponent } from './features/unidades-medida/page/unidad-medida-component/unidad-medida-component';
import { ProductosComponent } from './features/productos/page/productos-component/productos-component';
import { InventarioComponent } from './features/inventario/page/inventario-component/inventario-component';
import { ProveedorComponent } from './features/proveedores/page/proveedor-component/proveedor-component';

export const routes: Routes = [
    { path: '', redirectTo: '/administracion', pathMatch: 'full' },
    {
        path: 'administracion', component: ManagementLayouts,
        children:[
            { path: 'categorias', component: CategoriasComponent },
            { path: 'unidades-medida', component: UnidadMedidaComponent },
            { path: 'productos', component: ProductosComponent },
            {path: 'inventario', component: InventarioComponent},
            { path: 'proveedores', component: ProveedorComponent }
        ]
    }
];
