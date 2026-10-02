package com.franquicias.api.franchise_service.dto;

public class MaxStockProductByBranchDTO {

    private String sucursalId;
    private String sucursalNombre;
    private String productoId;
    private String productoNombre;
    private Integer stock;

    public MaxStockProductByBranchDTO(String sucursalId, String sucursalNombre, String productoId, String productoNombre, Integer stock) {
        this.sucursalId = sucursalId;
        this.sucursalNombre = sucursalNombre;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.stock = stock;
    }

    // Getters y Setters
    public String getSucursalId() { return sucursalId; }
    public String getSucursalNombre() { return sucursalNombre; }
    public String getProductoId() { return productoId; }
    public String getProductoNombre() { return productoNombre; }
    public Integer getStock() { return stock; }
}