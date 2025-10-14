package com.prediccion.apppredicciongm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.UnidadMedida;

@Repository
public interface IUnidadMedidaRepositorio extends JpaRepository<UnidadMedida, Integer> {

}
