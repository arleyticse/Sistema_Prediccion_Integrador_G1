package com.prediccion.apppredicciongm.services;

import java.util.List;

import com.prediccion.apppredicciongm.models.Proveedor;

public interface IProveedorServicio {
    List<Proveedor> obtenerProveedores();
    void crearProveedor(Proveedor proveedor);
    void eliminarProveedor(Integer id);
    void actualizarProveedor(Integer id, Proveedor proveedor);
}
