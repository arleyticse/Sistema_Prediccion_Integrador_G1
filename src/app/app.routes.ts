import { Routes } from '@angular/router';
import { ManagementLayouts } from './layouts/management-layouts/management-layouts';
import { CategoriasComponent } from './features/categorias/page/categorias-component/categorias-component';

export const routes: Routes = [
    { path: '', redirectTo: '/administracion', pathMatch: 'full' },
    {
        path: 'administracion', component: ManagementLayouts,
        children:[
            { path: 'categorias', component: CategoriasComponent },
        ]
    }
];
