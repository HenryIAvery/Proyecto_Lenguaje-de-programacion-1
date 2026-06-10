package com.example.tienda.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer cantidad;

    private String imagenUrl;

    @Column(nullable = false)
    private String slogan;

    private String artista;

    private Integer descuento;

    @Column(nullable = false)
    private Boolean enOferta;

    // --- RELACIÓN UNO A MUCHOS: UN PRODUCTO APARECE EN MUCHOS DETALLES DE PEDIDOS
    // ---
    // 'mappedBy = "producto"' enlaza esta propiedad con la variable 'producto'
    // definida en la clase DetallePedido.
    // 'cascade = CascadeType.ALL' propaga los cambios o actualizaciones de este
    // artículo hacia sus detalles en cascada.
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<DetallePedido> detalles; // Lista que rastrea en qué renglones de qué facturas se ha vendido este producto

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    public Producto() {
    }

    public Producto(String nombre, String descripcion, Double precio, Integer cantidad, String imagenUrl,
            String slogan, String artista, Integer descuento, Boolean enOferta) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.imagenUrl = imagenUrl;
        this.slogan = slogan;
        this.artista = artista;
        this.descuento = descuento;
        this.enOferta = enOferta;
    }

    public Long getId() {
        return id;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String categoria) {
        this.slogan = categoria;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String marca) {
        this.artista = marca;
    }

    public Integer getDescuento() {
        return descuento;
    }

    public void setDescuento(Integer descuento) {
        this.descuento = descuento;
    }

    public Boolean getEnOferta() {
        return enOferta;
    }

    public void setEnOferta(Boolean enOferta) {
        this.enOferta = enOferta;
    }

    public Boolean isEnOferta() {
        return enOferta;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    
    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    // MÉTODO AUXILIAR AUTOMÁTICO: CALCULA EL PRECIO APLICANDO REBAJAS
    // Lógica interna que analiza si el producto califica para descuento. Si cumple las condiciones,
    // procesa la resta aritmética del porcentaje y devuelve el valor neto final cobrado.
    public Double getPrecioEfectivo() {
        // Validación de seguridad: Verifica que la oferta esté activa, que exista descuento y que sea mayor a cero
        if (this.enOferta != null && this.enOferta && this.descuento != null && this.descuento > 0) {
            // Calcula el monto exacto de dinero que se va a restar del precio original
            double rebaja = this.precio * (this.descuento / 100.0);
            // Resta la rebaja al precio de lista y aplica un redondeo matemático estricto a 2 decimales
            return Math.round((this.precio - rebaja) * 100.0) / 100.0;
        }
        // Si no cumple las condiciones de oferta, devuelve directamente el precio normal sin alteración
        return this.precio;
    }

}
