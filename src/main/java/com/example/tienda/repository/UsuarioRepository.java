package com.example.tienda.repository;

import com.example.tienda.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca un usuario por su correo electrónico (Retorna un Optional para evita algun error)
    Optional<Usuario> findByCorreo(String correo);
}