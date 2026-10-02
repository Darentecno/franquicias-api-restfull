package com.franquicias.api.franchise_service.repository;

import com.franquicias.api.franchise_service.models.Franquicia;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranquiciaRepository {
    Mono<Franquicia> findById(String id);
    Flux<Franquicia> findAll();
    Mono<Boolean> existsByNombreIgnoreCase(String nombre);
    Mono<Franquicia> save(Franquicia franquicia);
}
