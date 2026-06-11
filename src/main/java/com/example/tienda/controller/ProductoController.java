package com.example.tienda.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.tienda.model.Contacto;
import com.example.tienda.model.Producto;
import com.example.tienda.repository.ContactoRepository;
import com.example.tienda.repository.ProductoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ContactoRepository contactoRepository;

    @GetMapping("/")
    public String mostrarInicio(HttpSession session, Model model) {

        model.addAttribute(
                "usuarioLogueado",
                session.getAttribute("usuarioLogueado")
        );

        List<Producto> productos = productoRepository.findAll();

        if (!productos.isEmpty()) {

            Random random = new Random();

            Producto albumRecomendado
                    = productos.get(random.nextInt(productos.size()));

            model.addAttribute(
                    "albumRecomendado",
                    albumRecomendado
            );
        }

        return "index";
    }

    @GetMapping("/catalogo")
public String mostrarCatalogo(
        @RequestParam(required = false) Long categoriaId,
        HttpSession session,
        Model model) {

    model.addAttribute(
            "usuarioLogueado",
            session.getAttribute("usuarioLogueado"));

    if (categoriaId == null) {

        model.addAttribute(
                "productos",
                productoRepository.findAll());

    } else {

        model.addAttribute(
                "productos",
                productoRepository.findByCategoria_Id(categoriaId));
    }

    return "catalogo";
}

    @GetMapping("/categorias")
    public String verCategorias(HttpSession session, Model model) {
        // Envía el usuario de la sesión para mantener la barra de navegación personalizada
        model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));

        return "categorias";
    }



    @GetMapping("/ofertas")
    public String verOfertas(HttpSession session, Model model) {
        // Busca en MySQL únicamente los productos que tengan la columna "en_oferta" en TRUE (1)
        model.addAttribute("productos", productoRepository.findByEnOfertaTrue());
        // Envía los datos de control del usuario logueado
        model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));

        return "ofertas";
    }

    @GetMapping("/contacto")
    public String mostrarContacto(HttpSession session, Model model) {
        // Envía un objeto "Contacto" vacío para que el formulario de Thymeleaf sepa qué campos debe rellenar
        model.addAttribute("contacto", new Contacto());
        // Mantiene al usuario conectado en la barra de navegación
        model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));

        return "contacto";
    }

    @PostMapping("/contacto/enviar")
    public String procesarContacto(@ModelAttribute("contacto") Contacto contacto, HttpSession session, Model model) {
        try { // Bloque seguro para registrar el mensaje sin peligro de que colapse la web

            // Le inyecta automáticamente la fecha y hora actual en la que se envía el mensaje
            contacto.setFechaEnvio(LocalDateTime.now());
            // Guarda el objeto completo con nombre, correo, asunto y mensaje en la tabla de MySQL
            contactoRepository.save(contacto);
            // Envía un mensaje de éxito que Thymeleaf pintará en un recuadro verde
            model.addAttribute("mensajeExito", "¡Tu mensaje ha sido enviado con éxito a soporte!");

        } catch (Exception e) { // Si hay un error con el servidor o la base de datos, captura la falla
            // Envía una alerta que Thymeleaf pintará en un recuadro rojo de peligro
            model.addAttribute("error", "Error al procesar el mensaje. Intente más tarde.");
        }

        // Limpia el formulario enviando un nuevo objeto vacío para que no se queden los datos escritos en pantalla
        model.addAttribute("contacto", new Contacto());
        // Mantiene al usuario verificado en la sesión web
        model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));

        return "contacto";
    }
}
