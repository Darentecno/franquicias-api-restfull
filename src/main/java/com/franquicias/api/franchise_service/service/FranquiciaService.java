package com.franquicias.api.franchise_service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.franquicias.api.franchise_service.models.Franquicia;
import com.franquicias.api.franchise_service.repository.FranquiciaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FranquiciaService {
    private final FranquiciaRepository repository;

    public FranquiciaService(FranquiciaRepository repository) {
        this.repository = repository;
    }

    public Flux<Franquicia> getAllFranquicias() {
        return repository.findAll();
    }

    public Mono<Franquicia> createFranquicia(String nombre) {
        return repository.existsByNombreIgnoreCase(nombre)
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalArgumentException("Ya existe una franquicia con ese nombre."))
                        : repository.save(new Franquicia(UUID.randomUUID().toString(), nombre, null)));
    }

    public Mono<Franquicia> updateFranquicia(String id, String nombre) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Franquicia no encontrada: " + id)))
                .flatMap(franquicia -> repository.existsByNombreIgnoreCase(nombre)
                        .flatMap(exists -> exists && !nombre.equalsIgnoreCase(franquicia.getNombre())
                                ? Mono.error(new IllegalArgumentException("Ya existe una franquicia con ese nombre."))
                                : saveName(franquicia, nombre)));
    }

    private Mono<Franquicia> saveName(Franquicia franquicia, String nombre) {
        franquicia.setNombre(nombre);
        return repository.save(franquicia);
    }
}