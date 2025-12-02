import { Routes } from '@angular/router';
import { ManagementLayouts } from './layouts/management-layouts/management-layouts';
import { authGuard } from './core/guards/auth';
import { gerenteGuard, roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
    { path: '', redirectTo: '/administracion/dashboard', pathMatch: 'full' },
    {
        path: 'login',
        loadComponent: () =>
            import('./features/auth/page/login/login').then(m => m.default)
    },
    {
        path: 'password-recovery',
        loadComponent: () =>
            import('./features/auth/page/password-recovery/password-recovery').then(m => m.default)
    },
    {
        path: 'administracion',
        component: ManagementLayouts,
        canActivate: [authGuard],
        children: [
            {
                path: '',
                redirectTo: 'dashboard',
                pathMatch: 'full'
            },
            {
                path: 'dashboard',
                loadComponent: () =>
                    import('./features/dashboard/page/dashboard-principal/dashboard-principal')
                        .then(m => m.default)
            },
            {
                path: 'categorias',
                loadComponent: () =>
                    import('./features/categorias/page/categorias-component/categorias-component')
                        .then(m => m.CategoriasComponent)
            },
            {
                path: 'unidades-medida',
                loadComponent: () =>
                    import('./features/unidades-medida/page/unidad-medida-component/unidad-medida-component')
                        .then(m => m.UnidadMedidaComponent)
            },
            {
                path: 'productos',
                loadComponent: () =>
                    import('./features/productos/page/productos-component/productos-component')
                        .then(m => m.ProductosComponent)
            },
            {
                path: 'inventario',
                loadComponent: () =>
                    import('./features/inventario/page/inventario-component/inventario-component')
                        .then(m => m.InventarioComponent)
            },
            {
                path: 'proveedores',
                loadComponent: () =>
                    import('./features/proveedores/page/proveedor-component/proveedor-component')
                        .then(m => m.ProveedorComponent)
            },
            {
                path: 'movimientos',
                loadComponent: () =>
                    import('./features/movimientos/page/movimiento-component/movimiento-component')
                        .then(m => m.MovimientoComponent)
            },
            {
                path: 'predicciones',
                loadComponent: () =>
                    import('./features/predicciones/page/predicciones/predicciones')
                        .then(m => m.PrediccionesComponent)
            },
            {
                path: 'ordenes-compra',
                loadComponent: () =>
                    import('./features/ordenes-compra/page/ordenes-compra/ordenes-compra')
                        .then(m => m.OrdenesCompraComponent)
            },
            {
                path: 'alertas-inventario',
                children: [
                    {
                        path: 'flujo-procesamiento',
                        loadComponent: () =>
                            import('./features/alertas-inventario/page/flujo-procesamiento/flujo-procesamiento')
                                .then(m => m.FlujoProcesamientoComponent)
                    }
                ]
            },
            {
                path: 'reportes',
                loadComponent: () =>
                    import('./features/reportes/page/reportes.component')
                        .then(m => m.default)
            },
            {
                path: 'admin',
                canActivate: [gerenteGuard],
                children: [
                    {
                        path: 'usuarios',
                        loadComponent: () =>
                            import('./features/usuarios-admin/gestion-usuarios.component')
                                .then(m => m.GestionUsuariosComponent)
                    },
                    {
                        path: 'configuracion-empresa',
                        loadComponent: () =>
                            import('./features/administracion/configuracion-empresa/configuracion-empresa.component')
                                .then(m => m.ConfiguracionEmpresaComponent)
                    },
                    {
                        path: '',
                        redirectTo: 'usuarios',
                        pathMatch: 'full'
                    }
                ]
            }
        ]
    },
    { path: '**', redirectTo: '/login' }
];
