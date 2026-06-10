package com.example.tienda.repository;

import com.example.tienda.model.Pedido;
import com.example.tienda.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Al heredar de JpaRepository ya incluye .save() para insertar la orden en
    // MySQL
    List<Pedido> findByUsuario(Usuario usuario);
}