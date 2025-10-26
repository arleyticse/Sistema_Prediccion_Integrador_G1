package com.prediccion.apppredicciongm.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.prediccion.apppredicciongm.mappers.ProveedorMapper;
import com.prediccion.apppredicciongm.models.Proveedor;
import com.prediccion.apppredicciongm.repository.IProveedorRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorServicio implements IProveedorServicio{

    private final IProveedorRepositorio proveedorRepositorio;
    private final ProveedorMapper proveedorMapper;
    @Override
    public void actualizarProveedor(Integer id, Proveedor proveedor) {
        Proveedor proveedorExistente = proveedorRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + id));
        proveedorMapper.updateFromEntity(proveedor, proveedorExistente);
        proveedorRepositorio.save(proveedorExistente);
    }

    @Override
    public void crearProveedor(Proveedor proveedor) {
        proveedorRepositorio.save(proveedor); 
    }

    @Override
    public void eliminarProveedor(Integer id) {
        proveedorRepositorio.deleteById(id);
    }

    @Override
    public List<Proveedor> obtenerProveedores() {
        return proveedorRepositorio.findAll();
    }
}
