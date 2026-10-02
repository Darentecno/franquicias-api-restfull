package com.franquicias.api.franchise_service.models;

import java.util.ArrayList;
import java.util.List;

public class Franquicia {
    private String id;
    private String nombre;
    private List<Sucursales> sucursal = new ArrayList<>();

    public Franquicia() {}

    public Franquicia(String id, String nombre, List<Sucursales> sucursal) {
        this.id = id;
        this.nombre = nombre;
        this.sucursal = sucursal == null ? new ArrayList<>() : sucursal;
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

    public List<Sucursales> getSucursal() {
        return sucursal;
    }

    public void setSucursal(List<Sucursales> sucursal) {
        this.sucursal = sucursal;
    }
}