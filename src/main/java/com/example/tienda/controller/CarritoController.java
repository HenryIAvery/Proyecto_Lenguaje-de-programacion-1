package com.example.tienda.controller;

import com.example.tienda.model.*;
import com.example.tienda.repository.*;
import jakarta.servlet.http.HttpSession; // Permite manejar la memoria de la sesión del navegador
import org.springframework.beans.factory.annotation.Autowired; // Conecta componentes automáticamente
import org.springframework.stereotype.Controller; // Define que esta clase es un controlador web
import org.springframework.ui.Model; // Permite enviar datos desde Java hacia la plantilla HTML
import org.springframework.web.bind.annotation.*; // Habilita el uso de rutas URL (Get, Post, etc.)
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Envía mensajes flash entre pantallas

import java.time.LocalDateTime; // Permite capturar la fecha y hora exacta del servidor
import java.util.ArrayList; // Permite estructurar listas dinámicas en memoria
import java.util.List; // Define contratos para el uso de listas
import java.util.Optional; // Contenedor seguro para evitar valores nulos (null) al buscar en BD

@Controller 
public class CarritoController {

    @Autowired // Conecta de forma automática la tabla de productos de MySQL
    private ProductoRepository productoRepository;

    @Autowired // Conecta de forma automática la tabla de pedidos de MySQL
    
    private PedidoRepository pedidoRepository;

    // FUNCIÓN INTERNA: OBTIENE O CREA EL CARRITO EN LA SESIÓN DEL NAVEGADOR 
    private List<ItemCarrito> obtenerCarritoDeSesion(HttpSession session) {
        // Intenta buscar si ya existe un carrito guardado en la memoria de la sesión
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");

        // Si el usuario es nuevo o no tiene un carrito, la lista estará vacía (null)
        if (carrito == null) {
            carrito = new ArrayList<>(); // Crea una lista de compras totalmente nueva
            session.setAttribute("carrito", carrito); // Guarda esa nueva lista en la sesión web
        }
        return carrito; 
    }

    // FUNCIÓN INTERNA: SUMA LOS VALORES DE TODOS LOS PRODUCTOS DEL CARRITO
    private double calcularTotal(List<ItemCarrito> carrito) {
        double total = 0; // Inicializa el contador del dinero en cero

        // Recorre uno por uno todos los artículos guardados en el carrito
        for (ItemCarrito item : carrito) {
            // Multiplica el precio cobrado (que ya evalúa si tiene descuento) por la cantidad pedida
            total += item.getPrecioCobrado() * item.getCantidad();
        }
        // Devuelve la suma total redondeada a dos decimales para evitar problemas con
        // centavos flotantes
        return Math.round(total * 100.0) / 100.0;
    }

    //  MUESTRA LA PÁGINA DE LA CANASTA DE COMPRAS 
    @GetMapping("/carrito") 
    public String verCarrito(HttpSession session, Model model) {
        // Obtiene los artículos que el usuario tiene acumulados en su sesión
        List<ItemCarrito> carrito = obtenerCarritoDeSesion(session);

        // Envía la información del usuario logueado a la pantalla HTML
        model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));
        // Envía el listado de productos para que Thymeleaf los dibuje en la tabla
        model.addAttribute("carrito", carrito);
        // Envía la suma total de dinero calculada al instante
        model.addAttribute("total", calcularTotal(carrito));

        return "carrito"; 
    }

    //  AGREGA UN PRODUCTO DESDE LA SECCIÓN DE OFERTAS (CON DESCUENTO)
    @PostMapping("/carrito/agregar") 
    public String agregarPorPost(@RequestParam("id") Long id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        // Llama a la lógica pasándole "true" para forzar que use el precio rebajado con
        // oferta
        procesarLogicaAgregar(id, true, session, redirectAttributes);
        return "redirect:/ofertas";
    }

    // AGREGA UN PRODUCTO DESDE EL CATÁLOGO GENERAL (PRECIO NORMAL)
    @GetMapping("/carrito/agregar/{id}") // Escucha clics directos en enlaces normales con el ID en la URL
    public String agregarPorGet(@PathVariable("id") Long id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        // Llama a la lógica pasándole "false" para que se procese con el precio de lista completo
        procesarLogicaAgregar(id, false, session, redirectAttributes);
        return "redirect:/"; 
    }

    // LÓGICA DE INSERCIÓN Y SEPARACIÓN DE FILAS EN EL CARRITO
    private void procesarLogicaAgregar(Long id, boolean esOferta, HttpSession session,
            RedirectAttributes redirectAttributes) {
        try { // Bloque seguro para evitar que la aplicación se caiga si ocurre un fallo
            List<ItemCarrito> carrito = obtenerCarritoDeSesion(session); // Pide el carrito actual
            Optional<Producto> prodOpt = productoRepository.findById(id); // Busca el producto en MySQL por su ID

            // Si el producto existe físicamente en la base de datos
            if (prodOpt.isPresent()) {
                Producto productoDB = prodOpt.get(); // Extrae los datos reales del producto

                // Si viene de ofertas calcula el precio efectivo, si no toma el precio normal completo
                double precioUnitarioFinal = esOferta ? productoDB.getPrecioEfectivo() : productoDB.getPrecio();

                boolean existe = false; // Variable para controlar si el artículo ya estaba repetido

                // Recorre el carrito para verificar si ya existe el mismo artículo al mismo precio
                for (ItemCarrito item : carrito) {
                    // Solo incrementa cantidad si coincide el ID Y el precio cobrado
                    if (item.getProducto().getId().equals(id) && item.getPrecioCobrado() == precioUnitarioFinal) {
                        item.setCantidad(item.getCantidad() + 1); // Suma uno a la cantidad de esa fila
                        existe = true; // Marca que sí se encontró repetido con el mismo precio
                        break; 
                    }
                }

                // Si no se encontró ninguna fila que tenga este mismo producto con este mismo precio
                if (!existe) {
                    ItemCarrito nuevoItem = new ItemCarrito(productoDB, 1); // Crea un registro de carrito con cantidad = 1
                    nuevoItem.setPrecioCobrado(precioUnitarioFinal); // Le inyecta el precio definitivo de compra
                    carrito.add(nuevoItem); // Añade la nueva fila independiente al carrito
                }
                // Prepara un aviso de éxito en color verde que aparecerá flotando en la web
                redirectAttributes.addFlashAttribute("mensajeExito", "Artículo agregado al carrito.");
            }
        } catch (Exception e) { // Si ocurre un error imprevisto (ej. caída de red o de base de datos)
            // Crea un mensaje de advertencia rojo para alertar al cliente sin colapsar el sitema
            redirectAttributes.addFlashAttribute("error", "Ocurrió un problema al intentar agregar el artículo.");
        }
    }

    // DISMINUYE CANTIDADES O BORRA UN PRODUCTO DE LA LISTA 
    @GetMapping("/carrito/eliminar/{index}") // Escucha clics en los botones de "Eliminar" usando el índice de fila
    public String eliminarProducto(@PathVariable("index") int index, HttpSession session) {
        List<ItemCarrito> carrito = obtenerCarritoDeSesion(session); // Pide el carrito actual

        // Verifica que la posición seleccionada sea válida y esté dentro de la lista
        if (index >= 0 && index < carrito.size()) {
            ItemCarrito item = carrito.get(index); // Selecciona la fila que se quiere reducir

            // Si el cliente tiene 2 o más unidades de ese mismo ítem
            if (item.getCantidad() > 1) {
                item.setCantidad(item.getCantidad() - 1); // Resta solo una unidad de la cantidad
            } else {
                carrito.remove(index); 
            }
        }
        return "redirect:/carrito"; 
    }

    // BORRA ABSOLUTAMENTE TODO EL CARRITO DE UN SOLO GOLPE 
    @GetMapping("/carrito/vaciar") 
    public String vaciarCarrito(HttpSession session) {
        obtenerCarritoDeSesion(session).clear(); // Limpia y deja la lista de compras en cero
        return "redirect:/carrito"; 
    }

    // PROCESA LA COMPRA FINAL, CALCULA IMPUESTOS Y GUARDA EN MYSQL
    @GetMapping("/carrito/pagar") // Realiza el proceso cuando el usuario le da al botón "Proceder al Pago"
    public String pagar(HttpSession session, Model model) {
        // Verifica si hay un cliente con sesión activa en el sistema
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            return "redirect:/login"; 
        }

        // Obtiene la lista de compras que está en la sesión
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        // Valida que el carrito contenga cosas
        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío."); // Crea mensaje de error
            return "carrito"; 
        }

        try { // Bloque de seguridad para el guardado transaccional en la Base de Datos

            // 1. CALCULO DEL SUBTOTAL REAL SUMANDO LAS FILAS SEPARADAS
            double subtotalAcumulado = 0.0; // Inicializa el subtotal de la factura en cero
            for (ItemCarrito item : carrito) {
                // Multiplica el precio guardado individualmente en cada fila por sus unidades
                subtotalAcumulado += item.getPrecioCobrado() * item.getCantidad();
            }

            // 2. CONFIGURACIÓN DE LA CABECERA PRINCIPAL DEL PEDIDO (TABLA PEDIDOS)
            Pedido pedido = new Pedido(); // Instancia un objeto vacío de tipo Pedido
            pedido.setUsuario(usuarioLogueado); // Conecta la orden con el ID del cliente que compra
            pedido.setFecha(LocalDateTime.now()); // Le clava la fecha y hora exacta del momento del pago
            pedido.setSubtotal(Math.round(subtotalAcumulado * 100.0) / 100.0); // Asigna el subtotal redondeado

            // 3. CÓMPUTO FINANCIERO DE IVA Y CARGOS LOGÍSTICOS
            double iva = subtotalAcumulado * 0.15; // Aplica la tasa legal del 15% de IVA (Ecuador)
            pedido.setImpuestosIva(Math.round(iva * 100.0) / 100.0); // Asigna el impuesto calculado
            pedido.setEnvioPrecio(15.0); // Tarifa plana por cargos logísticos o administrativos de ventanilla

            // Suma los 3 componentes (Subtotal + IVA + Envío) para estructurar el total definitivo
            double totalFinal = subtotalAcumulado + iva + 15.0;
            pedido.setTotal(Math.round(totalFinal * 100.0) / 100.0); // Asigna el monto final de cobro

            // Rellena campos descriptivos estándar solicitados para los comprobantes
            pedido.setDireccionEnvio("Retiro en Oficina / Aula ITQ");
            pedido.setCiudadProvincia("Quito");
            pedido.setMetodoPago("Efectivo / Caja");

            // 4. TRANSFORMACIÓN DE PRODUCTOS TEMPORALES A DETALLES FÍSICOS DE BASE DE DATOS
            List<DetallePedido> detalles = new ArrayList<>(); // Inicializa la lista intermedia de detalles
            for (ItemCarrito item : carrito) {
                DetallePedido detalle = new DetallePedido(); // Instancia una fila para la tabla "detalles_pedidos"
                detalle.setPedido(pedido); // Enlaza este detalle con la cabecera de pedido creada arriba
                detalle.setProducto(item.getProducto()); // Vincula el ID del producto comprado
                detalle.setCantidad(item.getCantidad()); // Registra las unidades adquiridas

                // REGISTRA EL PRECIO REAL INDIVIDUAL ADQUIRIDO (Respeta si uno fue normal y otro oferta)
                double precioUnitarioFila = item.getPrecioCobrado();
                detalle.setPrecioUnitario(precioUnitarioFila); // Guarda el valor exacto por unidad

                // Calcula el costo de la fila multiplicando su propio precio por la cantidad
                double subtotalItem = precioUnitarioFila * item.getCantidad();
                detalle.setSubtotalProducto(Math.round(subtotalItem * 100.0) / 100.0); // Guarda el subtotal de fila

                detalles.add(detalle); // 
            }
            pedido.setDetalles(detalles); // Agrega todo el grupo de detalles al Pedido en memoria

            // 5. INSERCIÓN ATÓMICA EN MYSQL EN CASCADA
            // Guarda el pedido y automáticamente rellena la tabla intermedia en MySQ
            Pedido pedidoGuardado = pedidoRepository.save(pedido);

            // 6. LIMPIEZA ABSOLUTA DEL ENTORNO
            carrito.clear(); // Borra todos los productos de la lista temporal del carrito
            session.setAttribute("carrito", carrito); // Guarda el carrito limpio en la sesión web para que quede vacío

            // Envía al cliente de forma directa a la URL de confirmación usando el ID que MySQL generó solo
            return "redirect:/pedido/confirmacion/" + pedidoGuardado.getId();

        } catch (Exception e) { // Si falla el motor de base de datos o el guardado relacional
            // Envía la alerta del error a la pantalla del carrito para que el usuario sepa que falló
            model.addAttribute("error", "Error al procesar la compra: " + e.getMessage());
            return "carrito"; 
        }
    }

    // PÁGINA DE ÉXITO QUE MUESTRA EL COMPROBANTE GENERADO 
    @GetMapping("/pedido/confirmacion/{id}") // Escucha la ruta dinámica "/pedido/confirmacion/ID"
    public String mostrarConfirmacion(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null)
            return "redirect:/login";

        // Busca en la base de datos el pedido guardado usando el ID dinámico de la URL
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);

        if (pedidoOpt.isPresent()) { // Si la orden de compra existe en la base de datos
            Pedido pedido = pedidoOpt.get(); // Extrae la orden completa

            // Control de Privacidad: Verifica que el pedido consultado pertenezca al usuario que está navegando
            if (!pedido.getUsuario().getId().equals(usuarioLogueado.getId())) {
                return "redirect:/"; 
            }
            // Envía el pedido completo a la plantilla HTML
            model.addAttribute("pedido", pedido);
            return "orden"; 
        }
        return "redirect:/"; 
    }

    //MUESTRA TODAS LAS COMPRAS HISTÓRICAS DEL USUARIO 
    @GetMapping("/historial") 
    public String verHistorial(HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            return "redirect:/login"; 
        }

        // Busca en la tabla de MySQL todos los pedidos vinculados al usuario logueado
        List<Pedido> misPedidos = pedidoRepository.findByUsuario(usuarioLogueado);

        // Envía la lista de compras del pasado al HTML
        model.addAttribute("pedidos", misPedidos);
        return "historial"; 
    }
}