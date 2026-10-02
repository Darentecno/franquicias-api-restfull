package com.franquicias.api.franchise_service.service;

import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.franquicias.api.franchise_service.models.Franquicia;
import com.franquicias.api.franchise_service.models.Sucursales;
import com.franquicias.api.franchise_service.repository.FranquiciaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SucursalService {
    private final FranquiciaRepository repository;

    public SucursalService(FranquiciaRepository repository) {
        this.repository = repository;
    }

    public Flux<Sucursales> getAllSucursales() {
        return repository.findAll().flatMapIterable(franquicia -> franquicia.getSucursal());
    }

    public Mono<Sucursales> createSucursal(String franquiciaId, String nombre) {
        return repository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMap(franquicia -> {
                    Sucursales sucursal = new Sucursales(UUID.randomUUID().toString(), nombre, null);
                    franquicia.getSucursal().add(sucursal);
                    return repository.save(franquicia).thenReturn(sucursal);
                });
    }

    public Mono<Sucursales> updateSucursal(String id, String nombre) {
        return updateBranch(id, sucursal -> sucursal.setNombre(nombre));
    }

    private Mono<Sucursales> updateBranch(String id, Consumer<Sucursales> update) {
        return repository.findAll().filter(franquicia -> franquicia.getSucursal().stream()
                        .anyMatch(sucursal -> sucursal.getId().equals(id)))
                .next()
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Sucursal no encontrada: " + id)))
                .flatMap(franquicia -> {
                    Sucursales sucursal = franquicia.getSucursal().stream()
                            .filter(item -> item.getId().equals(id)).findFirst().orElseThrow();
                    update.accept(sucursal);
                    return repository.save(franquicia).thenReturn(sucursal);
                });
    }
}