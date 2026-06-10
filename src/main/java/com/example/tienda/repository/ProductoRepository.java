package com.example.tienda.repository;

import com.example.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Filtra productos por categoría sin importar si escriben en mayúsculas o
    // minúsculas
    List<Producto> findByCategoriaIgnoreCase(String categoria);

    // Recupera únicamente los productos que tengan el campo 'enOferta' marcado como
    // True
    List<Producto> findByEnOfertaTrue();
}