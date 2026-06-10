package com.example.tienda.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double envioPrecio;

    @Column(nullable = false)
    private Double impuestosIva;

    @Column(nullable = false)
    private Double total;

    private String direccionEnvio;
    private String ciudadProvincia;
    private String metodoPago;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetallePedido> detalles;

    public Pedido() {
    }

    public Pedido(Usuario usuario, LocalDateTime fecha, Double subtotal, Double envioPrecio, Double impuestosIva,
            Double total, String direccionEnvio, String ciudadProvincia, String metodoPago) {
        this.usuario = usuario;
        this.fecha = fecha;
        this.subtotal = subtotal;
        this.envioPrecio = envioPrecio;
        this.impuestosIva = impuestosIva;
        this.total = total;
        this.direccionEnvio = direccionEnvio;
        this.ciudadProvincia = ciudadProvincia;
        this.metodoPago = metodoPago;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getEnvioPrecio() {
        return envioPrecio;
    }

    public void setEnvioPrecio(Double envioPrecio) {
        this.envioPrecio = envioPrecio;
    }

    public Double getImpuestosIva() {
        return impuestosIva;
    }

    public void setImpuestosIva(Double impuestosIva) {
        this.impuestosIva = impuestosIva;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getCiudadProvincia() {
        return ciudadProvincia;
    }

    public void setCiudadProvincia(String ciudadProvincia) {
        this.ciudadProvincia = ciudadProvincia;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
}