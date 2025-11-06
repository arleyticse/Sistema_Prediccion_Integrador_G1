package com.prediccion.apppredicciongm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Proveedor;

import java.util.Optional;

@Repository
public interface IProveedorRepositorio extends JpaRepository<Proveedor, Integer> {
    
    /**
     * Busca un proveedor por nombre comercial ignorando mayúsculas/minúsculas
     * @param nombreComercial Nombre comercial del proveedor
     * @return Optional con el proveedor si existe
     */
    Optional<Proveedor> findByNombreComercialIgnoreCase(String nombreComercial);
    
    /**
     * Verifica si existe un proveedor con el nombre comercial especificado (case-insensitive)
     * @param nombreComercial Nombre comercial del proveedor
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreComercialIgnoreCase(String nombreComercial);
    
    /**
     * Busca un proveedor por RUC o NIT
     * @param rucNit Número de RUC o NIT del proveedor
     * @return Optional con el proveedor si existe
     */
    Optional<Proveedor> findByRucNit(String rucNit);
    
    /**
     * Busca un proveedor por razón social ignorando mayúsculas/minúsculas
     * @param razonSocial Razón social del proveedor
     * @return Optional con el proveedor si existe
     */
    Optional<Proveedor> findByRazonSocialIgnoreCase(String razonSocial);
    
    /**
     * Verifica si existe un proveedor con el RUC/NIT especificado
     * @param rucNit Número de RUC o NIT del proveedor
     * @return true si existe, false en caso contrario
     */
    boolean existsByRucNit(String rucNit);
}
