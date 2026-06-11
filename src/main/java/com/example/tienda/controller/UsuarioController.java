package com.example.tienda.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping; // Importación del encriptador
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tienda.model.Usuario;
import com.example.tienda.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

@Controller 
public class UsuarioController {

    @Autowired 
    private UsuarioRepository usuarioRepository;

    // CONEXIÓN AUTOMÁTICA: Inyectamos el encriptador registrado en SecurityConfig
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/login") 
    public String mostrarLogin() {
        return "login"; 
    }

    @PostMapping("/login") 
    public String procesarLogin(@RequestParam("correo") String correo,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try { 

            // Busca en MySQL si existe algún usuario con el correo ingresado
            Optional<Usuario> userOpt = usuarioRepository.findByCorreo(correo);

            // MODIFICACIÓN CLAVE 1: Comparamos la clave usando passwordEncoder.matches()
            if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
                // Guarda los datos del usuario en la sesión del navegador para mantenerlo conectado
                session.setAttribute("usuarioLogueado", userOpt.get());
                return "redirect:/"; 
            } else {
                // Si el correo no existe o la clave no coincide, prepara un aviso de error en rojo
                redirectAttributes.addFlashAttribute("error", "Credenciales incorrectas. Verifique correo o clave.");
                return "redirect:/login"; 
            }
        } catch (Exception e) { 
            redirectAttributes.addFlashAttribute("error", "Error de comunicación con el servicio de autenticación.");
            return "redirect:/login"; 
        }
    }

    @GetMapping("/registro") 
    public String mostrarRegistro() {
        return "registro"; 
    }

    @PostMapping("/registro") 
    public String procesarRegistro(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try { 

            // CONTROL DE SEGURIDAD: Verifica si el correo ingresado ya pertenece a otra cuenta en MySQL
            if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El correo electrónico ya se encuentra registrado.");
                return "redirect:/registro";
            }

            // MODIFICACIÓN CLAVE 2: Encriptamos la contraseña en texto plano antes de guardarla
            String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
            usuario.setPassword(passwordEncriptada);

            // Guarda el nuevo usuario (ahora con la contraseña oculta) de forma definitiva en MySQL
            usuarioRepository.save(usuario);

            // Prepara un mensaje de éxito flotante en verde para la siguiente pantalla
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Registro completado con éxito. Ya puede iniciar sesión.");
            return "redirect:/login"; 

        } catch (Exception e) { 
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo completar el registro debido a un fallo en base de datos.");
            return "redirect:/registro"; 
        }
    }

    @GetMapping("/logout") 
    public String cerrarSesion(HttpSession session) {
        session.invalidate(); 
        return "redirect:/";
    }
}