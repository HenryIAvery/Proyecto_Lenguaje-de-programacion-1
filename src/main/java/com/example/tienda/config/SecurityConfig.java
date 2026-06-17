// Desarrollo de software
// Henry Mauricio Paredes Quintanilla 
// Lenguaje de programacion I

package com.example.tienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Este Bean es el que usaremos para encriptar y comparar contraseñas
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configuración para que no te bloquee las páginas actuales de la tienda
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desactivado temporalmente para tus formularios POST actuales
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permite el acceso a todas tus rutas actuales
            );
        return http.build();
    }
}
