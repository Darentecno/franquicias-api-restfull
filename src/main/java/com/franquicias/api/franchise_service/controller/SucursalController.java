package com.franquicias.api.franchise_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.franquicias.api.franchise_service.dto.NombreRequest;
import com.franquicias.api.franchise_service.models.Sucursales;
import com.franquicias.api.franchise_service.service.SucursalService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class SucursalController {
    private final SucursalService service;

    public SucursalController(SucursalService service) {
        this.service = service;
    }

    @GetMapping("/sucursales")
    public Flux<Sucursales> getAll() {
        return service.getAllSucursales();
    }

    @PostMapping("/franquicias/{franquiciaId}/sucursales")
    public Mono<ResponseEntity<Sucursales>> create(@PathVariable String franquiciaId,
            @Valid @RequestBody NombreRequest request) {
        return service.createSucursal(franquiciaId, request.nombre())
                .map(sucursal -> ResponseEntity.status(HttpStatus.CREATED).body(sucursal));
    }

    @PutMapping("/sucursales/{id}")
    public Mono<Sucursales> update(@PathVariable String id, @Valid @RequestBody NombreRequest request) {
        return service.updateSucursal(id, request.nombre());
    }
}