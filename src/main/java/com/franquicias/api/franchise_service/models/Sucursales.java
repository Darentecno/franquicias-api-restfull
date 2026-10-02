package com.franquicias.api.franchise_service.models;

import java.util.ArrayList;
import java.util.List;

public class Sucursales {
    private String id;
    private String nombre;
    private List<Productos> producto = new ArrayList<>();

    public Sucursales() {}

    public Sucursales(String id, String nombre, List<Productos> producto) {
        this.id = id;
        this.nombre = nombre;
        this.producto = producto == null ? new ArrayList<>() : producto;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public List<Productos> getProducto() {
        return producto;
    }
    public void setProducto(List<Productos> producto) {
        this.producto = producto;
    }
}