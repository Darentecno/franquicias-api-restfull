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
import com.franquicias.api.franchise_service.models.Franquicia;
import com.franquicias.api.franchise_service.service.FranquiciaService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/franquicias")
public class FranquiciaController {
    private final FranquiciaService service;

    public FranquiciaController(FranquiciaService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<Franquicia> getAll() {
        return service.getAllFranquicias();
    }

    @PostMapping
    public Mono<ResponseEntity<Franquicia>> create(@Valid @RequestBody NombreRequest request) {
        return service.createFranquicia(request.nombre())
                .map(franquicia -> ResponseEntity.status(HttpStatus.CREATED).body(franquicia));
    }

    @PutMapping("/{id}")
    public Mono<Franquicia> update(@PathVariable String id, @Valid @RequestBody NombreRequest request) {
        return service.updateFranquicia(id, request.nombre());
    }
}