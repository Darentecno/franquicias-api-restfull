package com.franquicias.api.franchise_service.service;

import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.franquicias.api.franchise_service.dto.MaxStockProductByBranchDTO;
import com.franquicias.api.franchise_service.models.Franquicia;
import com.franquicias.api.franchise_service.models.Productos;
import com.franquicias.api.franchise_service.models.Sucursales;
import com.franquicias.api.franchise_service.repository.FranquiciaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoService {
    private final FranquiciaRepository repository;

    public ProductoService(FranquiciaRepository repository) {
        this.repository = repository;
    }

    public Mono<Productos> createProducto(String sucursalId, String nombre, Integer stock) {
        return findFranchiseWithBranch(sucursalId).flatMap(result -> {
            Productos producto = new Productos(UUID.randomUUID().toString(), nombre, stock);
            result.sucursal().getProducto().add(producto);
            return repository.save(result.franquicia()).thenReturn(producto);
        });
    }

    public Mono<Void> deleteProducto(String productoId) {
        return repository.findAll()
                .filter(franquicia -> franquicia.getSucursal().stream()
                        .anyMatch(sucursal -> sucursal.getProducto().stream()
                                .anyMatch(producto -> producto.getId().equals(productoId))))
                .next()
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Producto no encontrado: " + productoId)))
                .flatMap(franquicia -> {
                    franquicia.getSucursal().forEach(sucursal ->
                            sucursal.getProducto().removeIf(producto -> producto.getId().equals(productoId)));
                    return repository.save(franquicia).then();
                });
    }

    public Mono<Productos> updateStock(String productoId, Integer stock) {
        return updateProduct(productoId, producto -> producto.setStock(stock));
    }

    public Mono<Productos> updateNombre(String productoId, String nombre) {
        return updateProduct(productoId, producto -> producto.setNombre(nombre));
    }

    public Flux<MaxStockProductByBranchDTO> getMaxStockProductsByFranchise(String franquiciaId) {
        return repository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMapMany(franquicia -> Flux.fromIterable(franquicia.getSucursal())
                        .filter(sucursal -> !sucursal.getProducto().isEmpty())
                        .map(this::toMaxStockDto));
    }

    private Mono<BranchResult> findFranchiseWithBranch(String branchId) {
        return repository.findAll()
                .filter(franquicia -> franquicia.getSucursal().stream()
                        .anyMatch(sucursal -> sucursal.getId().equals(branchId)))
                .next()
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Sucursal no encontrada: " + branchId)))
                .map(franquicia -> new BranchResult(franquicia, franquicia.getSucursal().stream()
                        .filter(sucursal -> sucursal.getId().equals(branchId)).findFirst().orElseThrow()));
    }

    private Mono<Productos> updateProduct(String productId, Consumer<Productos> update) {
        return repository.findAll()
                .filter(franquicia -> franquicia.getSucursal().stream().anyMatch(sucursal ->
                        sucursal.getProducto().stream().anyMatch(producto -> producto.getId().equals(productId))))
                .next()
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Producto no encontrado: " + productId)))
                .flatMap(franquicia -> {
                    Productos producto = franquicia.getSucursal().stream().flatMap(sucursal -> sucursal.getProducto().stream())
                            .filter(item -> item.getId().equals(productId)).findFirst().orElseThrow();
                    update.accept(producto);
                    return repository.save(franquicia).thenReturn(producto);
                });
    }

    private MaxStockProductByBranchDTO toMaxStockDto(Sucursales sucursal) {
        Productos producto = sucursal.getProducto().stream()
            .max((first, second) -> Integer.compare(first.getStock(), second.getStock())).orElseThrow();
        return new MaxStockProductByBranchDTO(sucursal.getId(), sucursal.getNombre(), producto.getId(),
                producto.getNombre(), producto.getStock());
    }

    private record BranchResult(Franquicia franquicia, Sucursales sucursal) {}
}