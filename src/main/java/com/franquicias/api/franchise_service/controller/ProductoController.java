package com.franquicias.api.franchise_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.franquicias.api.franchise_service.dto.MaxStockProductByBranchDTO;
import com.franquicias.api.franchise_service.dto.NombreRequest;
import com.franquicias.api.franchise_service.dto.ProductoRequest;
import com.franquicias.api.franchise_service.dto.StockRequest;
import com.franquicias.api.franchise_service.models.Productos;
import com.franquicias.api.franchise_service.service.ProductoService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class ProductoController {
    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @PostMapping("/sucursales/{sucursalId}/productos")
    public Mono<ResponseEntity<Productos>> create(@PathVariable String sucursalId,
            @Valid @RequestBody ProductoRequest request) {
        return service.createProducto(sucursalId, request.nombre(), request.stock())
                .map(producto -> ResponseEntity.status(HttpStatus.CREATED).body(producto));
    }

    @DeleteMapping("/productos/{productoId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String productoId) {
        return service.deleteProducto(productoId).thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/productos/{productoId}/stock")
    public Mono<Productos> updateStock(@PathVariable String productoId,
            @Valid @RequestBody StockRequest request) {
        return service.updateStock(productoId, request.stock());
    }

    @PutMapping("/productos/{productoId}")
    public Mono<Productos> updateName(@PathVariable String productoId,
            @Valid @RequestBody NombreRequest request) {
        return service.updateNombre(productoId, request.nombre());
    }

    @GetMapping("/franquicias/{franquiciaId}/max-stock")
    public Flux<MaxStockProductByBranchDTO> getMaxStock(@PathVariable String franquiciaId) {
        return service.getMaxStockProductsByFranchise(franquiciaId);
    }
}