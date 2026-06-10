package com.example.tienda.controller;

import com.example.tienda.model.Usuario; 
import com.example.tienda.repository.UsuarioRepository; 
import jakarta.servlet.http.HttpSession; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Controller; 
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 
import java.util.Optional; 

@Controller 
public class UsuarioController {

    @Autowired 
    private UsuarioRepository usuarioRepository;

    @GetMapping("/login") 
    public String mostrarLogin() {
        return "login"; 
    }

    @PostMapping("/login") 
    public String procesarLogin(@RequestParam("correo") String correo,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try { // Bloque de seguridad para capturar fallos sin interrumpir la web

            // Busca en MySQL si existe algún usuario con el correo ingresado
            Optional<Usuario> userOpt = usuarioRepository.findByCorreo(correo);

            // Verifica si el correo existe Y si la contraseña coincide exactamente con la guardada
            if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
                // Guarda los datos del usuario en la sesión del navegador para mantenerlo conectado
                session.setAttribute("usuarioLogueado", userOpt.get());
                return "redirect:/"; 
            } else {
                // Si el correo no existe o la clave no coincide, prepara un aviso de error en rojo
                redirectAttributes.addFlashAttribute("error", "Credenciales incorrectas. Verifique correo o clave.");
                return "redirect:/login"; 
            }
        } catch (Exception e) { // Captura problemas inesperados (como si se cae el servidor de la base de datos)
            // Prepara un aviso de error de conexión en la pantalla
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
        try { // Bloque de seguridad para el proceso de inserción en la base de datos

            // CONTROL DE SEGURIDAD: Verifica si el correo ingresado ya pertenece a otra cuenta en MySQL
            if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
                // Si ya existe, detiene el registro y prepara un aviso en rojo de correo  duplicado
                redirectAttributes.addFlashAttribute("error", "El correo electrónico ya se encuentra registrado.");
                return "redirect:/registro";
            }

            // Si el correo está libre, guarda el nuevo usuario de forma definitiva en la  base de datos
            usuarioRepository.save(usuario);

            // Prepara un mensaje de éxito flotante en verde para la siguiente pantalla
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Registro completado con éxito. Ya puede iniciar sesión.");
            return "redirect:/login"; 

        } catch (Exception e) { // Captura errores en caso de que falle la inserción física en MySQL
            // Prepara un mensaje de advertencia informando sobre el fallo técnico
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo completar el registro debido a un fallo en base de datos.");
            return "redirect:/registro"; 
        }
    }

    @GetMapping("/logout") 
    public String cerrarSesion(HttpSession session) {
        session.invalidate(); // Destruye y borra toda la información guardada en la sesión del navegador
        return "redirect:/";
    }
}