// Desarrollo de software
// Henry Mauricio Paredes Quintanilla 
// Lenguaje de programacion I

package com.example.tienda.model;

public class ItemCarrito {

    private Producto producto;
    private Integer cantidad;
    private Double precioCobrado;

    public ItemCarrito() {
    }

    public ItemCarrito(Producto producto, Integer cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioCobrado = producto.getPrecioEfectivo();
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioCobrado() {
        return precioCobrado;
    }

    public void setPrecioCobrado(Double precioCobrado) {
        this.precioCobrado = precioCobrado;
    }
}