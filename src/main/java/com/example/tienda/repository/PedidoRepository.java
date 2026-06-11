package com.example.tienda.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.tienda.model.Pedido;
import com.example.tienda.model.Usuario;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    // @EntityGraph le dice a Spring que cargue los 'detalles' en la misma consulta sin duplicar el 'Pedido'
    @EntityGraph(attributePaths = {"detalles"})
    @Query("SELECT p FROM Pedido p WHERE p.usuario = :usuario ORDER BY p.fecha DESC")
    List<Pedido> findByUsuario(@Param("usuario") Usuario usuario);
    
}