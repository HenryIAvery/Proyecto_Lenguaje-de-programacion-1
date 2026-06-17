// Desarrollo de software
// Henry Mauricio Paredes Quintanilla 
// Lenguaje de programacion I

package com.example.tienda.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 

import com.example.tienda.model.DetallePedido;
import com.example.tienda.model.ItemCarrito;
import com.example.tienda.model.Pedido;
import com.example.tienda.model.Producto;
import com.example.tienda.model.Usuario;
import com.example.tienda.repository.PedidoRepository;
import com.example.tienda.repository.ProductoRepository;

import jakarta.servlet.http.HttpSession;

@Controller 
public class CarritoController {

    @Autowired 
    private ProductoRepository productoRepository;

    @Autowired 
    private PedidoRepository pedidoRepository;

    // OBTIENE O CREA EL CARRITO EN LA SESIÓN
    private List<ItemCarrito> obtenerCarritoDeSesion(HttpSession session) {
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>(); 
            session.setAttribute("carrito", carrito); 
        }
        return carrito; 
    }

    // SUMA LOS VALORES DE TODOS LOS PRODUCTOS
    private double calcularTotal(List<ItemCarrito> carrito) {
        double total = 0; 
        for (ItemCarrito item : carrito) {
            total += item.getPrecioCobrado() * item.getCantidad();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    // MUESTRA LA PÁGINA DEL CARRITO 
    @GetMapping("/carrito") 
    public String verCarrito(HttpSession session, Model model) {
        List<ItemCarrito> carrito = obtenerCarritoDeSesion(session);
        model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", calcularTotal(carrito));
        return "carrito"; 
    }

    // AGREGA UN PRODUCTO DESDE OFERTAS
    @PostMapping("/carrito/agregar") 
    public String agregarPorPost(@RequestParam("id") Long id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        procesarLogicaAgregar(id, true, session, redirectAttributes);
        return "redirect:/ofertas";
    }

    // AGREGA UN PRODUCTO DESDE EL CATÁLOGO GENERAL
    @GetMapping("/carrito/agregar/{id}") 
    public String agregarPorGet(@PathVariable("id") Long id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        procesarLogicaAgregar(id, false, session, redirectAttributes);
        return "redirect:/"; 
    }

    // LÓGICA DE INSERCIÓN EN EL CARRITO
    private void procesarLogicaAgregar(Long id, boolean esOferta, HttpSession session,
            RedirectAttributes redirectAttributes) {
        try { 
            List<ItemCarrito> carrito = obtenerCarritoDeSesion(session); 
            Optional<Producto> prodOpt = productoRepository.findById(id); 

            if (prodOpt.isPresent()) {
                Producto productoDB = prodOpt.get(); 
                double precioUnitarioFinal = esOferta ? productoDB.getPrecioEfectivo() : productoDB.getPrecio();
                boolean existe = false; 

                for (ItemCarrito item : carrito) {
                    if (item.getProducto().getId().equals(id) && item.getPrecioCobrado() == precioUnitarioFinal) {
                        item.setCantidad(item.getCantidad() + 1); 
                        existe = true; 
                        break; 
                    }
                }

                if (!existe) {
                    ItemCarrito nuevoItem = new ItemCarrito(productoDB, 1); 
                    nuevoItem.setPrecioCobrado(precioUnitarioFinal); 
                    carrito.add(nuevoItem); 
                }
                redirectAttributes.addFlashAttribute("mensajeExito", "Artículo agregado al carrito.");
            }
        } catch (Exception e) { 
            redirectAttributes.addFlashAttribute("error", "Ocurrió un problema al intentar agregar el artículo.");
        }
    }

    // DISMINUYE CANTIDADES O BORRA UN PRODUCTO
    @GetMapping("/carrito/eliminar/{index}") 
    public String eliminarProducto(@PathVariable("index") int index, HttpSession session) {
        List<ItemCarrito> carrito = obtenerCarritoDeSesion(session); 
        if (index >= 0 && index < carrito.size()) {
            ItemCarrito item = carrito.get(index); 
            if (item.getCantidad() > 1) {
                item.setCantidad(item.getCantidad() - 1); 
            } else {
                carrito.remove(index); 
            }
        }
        return "redirect:/carrito"; 
    }

    // VACIAR CARRITO
    @GetMapping("/carrito/vaciar") 
    public String vaciarCarrito(HttpSession session) {
        obtenerCarritoDeSesion(session).clear(); 
        return "redirect:/carrito"; 
    }

    // PROCESA LA COMPRA FINAL (Música y Coleccionables con Control de Stock Activo)
    @GetMapping("/carrito/pagar") 
    public String pagar(HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            return "redirect:/login"; 
        }

        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío."); 
            return "carrito"; 
        }

        try { 
            // 1. CÁLCULO DEL SUBTOTAL
            double subtotalAcumulado = 0.0; 
            for (ItemCarrito item : carrito) {
                subtotalAcumulado += item.getPrecioCobrado() * item.getCantidad();
            }

            // 2. CONFIGURACIÓN DEL PEDIDO
            Pedido pedido = new Pedido(); 
            pedido.setUsuario(usuarioLogueado); 
            pedido.setFecha(LocalDateTime.now()); 
            pedido.setSubtotal(Math.round(subtotalAcumulado * 100.0) / 100.0); 

            // 3. CÓMPUTO DE IVA Y ENVÍO GRATIS (Ajustado para Vinyl House)
            double iva = subtotalAcumulado * 0.15; 
            pedido.setImpuestosIva(Math.round(iva * 100.0) / 100.0); 
            pedido.setEnvioPrecio(0.0); 

            double totalFinal = subtotalAcumulado + iva; 
            pedido.setTotal(Math.round(totalFinal * 100.0) / 100.0); 

            pedido.setDireccionEnvio("Retiro en Tienda - Vinyl House");
            pedido.setCiudadProvincia("Quito");
            pedido.setMetodoPago("Efectivo / Tarjeta en Caja");

            // 4. TRANSFORMACIÓN A DETALLES DE BASE DE DATOS Y REDUCCIÓN DE STOCK
            Set<DetallePedido> detalles = new HashSet<>(); 
            for (ItemCarrito item : carrito) {
                Producto productoDB = item.getProducto();
                
                // --- VALIDACIÓN Y CONTROL DE STOCK INCORPORADO ---
                int stockDisponible = productoDB.getCantidad();
                int cantidadComprada = item.getCantidad();
                int nuevoStockCalculado = stockDisponible - cantidadComprada;
                
                // Candado de seguridad preventivo
                if (nuevoStockCalculado < 0) {
                    throw new Exception("Stock insuficiente para el álbum: " + productoDB.getNombre() + 
                                        " (Disponible: " + stockDisponible + ")");
                }
                
                // Actualiza la entidad del producto y lo inyecta a MySQL (UPDATE)
                productoDB.setCantidad(nuevoStockCalculado);
                productoRepository.save(productoDB);
                // --------------------------------------------------

                DetallePedido detalle = new DetallePedido(); 
                detalle.setPedido(pedido); 
                detalle.setProducto(productoDB); 
                detalle.setCantidad(cantidadComprada); 

                double precioUnitarioFila = item.getPrecioCobrado();
                detalle.setPrecioUnitario(precioUnitarioFila); 

                double subtotalItem = precioUnitarioFila * cantidadComprada;
                detalle.setSubtotalProducto(Math.round(subtotalItem * 100.0) / 100.0); 

                detalles.add(detalle); 
            }
            pedido.setDetalles(detalles); 

            // 5. INSERCIÓN EN MYSQL
            Pedido pedidoGuardado = pedidoRepository.save(pedido);

            // 6. LIMPIEZA DEL ENTORNO
            carrito.clear(); 
            session.setAttribute("carrito", carrito); 

            return "redirect:/pedido/confirmacion/" + pedidoGuardado.getId();

        } catch (Exception e) { 
            model.addAttribute("error", "Error al procesar la compra: " + e.getMessage());
            return "carrito"; 
        }
    }

    // PÁGINA DE ÉXITO (TICKET)
    @GetMapping("/pedido/confirmacion/{id}") 
    public String mostrarConfirmacion(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null)
            return "redirect:/login";

        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);

        if (pedidoOpt.isPresent()) { 
            Pedido pedido = pedidoOpt.get(); 

            if (!pedido.getUsuario().getId().equals(usuarioLogueado.getId())) {
                return "redirect:/"; 
            }
            model.addAttribute("pedido", pedido);
            return "orden"; 
        }
        return "redirect:/"; 
    }

    // MUESTRA EL HISTORIAL DE COMPRAS (Filtro Anti-Duplicados Aplicado)
    @GetMapping("/historial") 
    public String verHistorial(HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            return "redirect:/login"; 
        }

        List<Pedido> misPedidos = pedidoRepository.findByUsuario(usuarioLogueado);
        List<Pedido> pedidosLimpios = misPedidos.stream().distinct().toList();

        model.addAttribute("pedidos", pedidosLimpios);
        return "historial"; 
    }
}