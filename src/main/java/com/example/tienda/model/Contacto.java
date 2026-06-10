package com.example.tienda.model;

import jakarta.persistence.*; // Importa las anotaciones estructurales (@Entity, @Table, @Id, etc.)
import java.time.LocalDateTime; // Importa la herramienta para manejar la fecha y hora de los mensajes

@Entity // Le avisa a Spring Boot y Hibernate que esta clase representa una tabla real
        // en la Base de Datos
@Table(name = "contactos") // Le asigna un nombre específico a la tabla en MySQL (se llamará "contactos")
public class Contacto {

    @Id // Define que la variable que va justo abajo será la Llave Primaria (Primary Key) de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura el ID como Autoincrementable (1, 2, 3...) en MySQL
    private Long id; // Variable para almacenar el identificador único del mensaje

    @Column(nullable = false) // Crea una columna en MySQL que no permite valores vacíos (NOT NULL)
    private String nombre; // Variable para guardar el nombre del usuario que escribe

    @Column(nullable = false) // Crea una columna que obliga a registrar un correo obligatoriamente
    private String correo; // Variable para guardar el correo electrónico del remitente

    @Column(nullable = false) // Crea una columna obligatoria para el título o tema del mensaje
    private String asunto; // Variable para guardar el asunto del mensaje de soporte

    // Crea una columna de tipo "TEXT" en MySQL para almacenar textos muy largos (párrafos grandes)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje; // Variable para guardar el cuerpo del reclamo, duda o sugerencia

    private LocalDateTime fechaEnvio; // Columna para registrar el día y la hora exacta del envío

    // CONSTRUCTOR VACÍO (OBLIGATORIO PARA HIBERNATE)
    // Sirve para que el motor de base de datos pueda crear un objeto vacío y luego rellenarlo con los datos de MySQL
    public Contacto() {
    }

    //  CONSTRUCTOR CON PARÁMETROS 
    // Sirve para crear un objeto de tipo Contacto pasándole todos los datos directamente en una sola línea de código
    public Contacto(String nombre, String correo, String asunto, String mensaje, LocalDateTime fechaEnvio) {
        this.nombre = nombre; 
        this.correo = correo; 
        this.asunto = asunto; 
        this.mensaje = mensaje; 
        this.fechaEnvio = fechaEnvio; 
    }

    public Long getId() {
        return id; 
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre; 
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo; 
    }

    public void setCorreo(String correo) {
        this.correo = correo; 
    }

    public String getAsunto() {
        return asunto; 
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto; 
    }

    public String getMensaje() {
        return mensaje; 
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje; 
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio; 
    }
}