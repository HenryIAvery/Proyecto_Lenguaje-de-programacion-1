// Desarrollo de software
// Henry Mauricio Paredes Quintanilla 
// Lenguaje de programacion I

package com.example.tienda.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tienda.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca un usuario por su correo electrónico (Retorna un Optional para evita algun error)
    Optional<Usuario> findByCorreo(String correo);
}