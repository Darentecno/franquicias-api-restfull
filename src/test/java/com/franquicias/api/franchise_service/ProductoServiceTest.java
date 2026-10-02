package com.franquicias.api.franchise_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.franquicias.api.franchise_service.dto.MaxStockProductByBranchDTO;
import com.franquicias.api.franchise_service.models.Franquicia;
import com.franquicias.api.franchise_service.models.Productos;
import com.franquicias.api.franchise_service.models.Sucursales;
import com.franquicias.api.franchise_service.repository.FranquiciaRepository;
import com.franquicias.api.franchise_service.service.ProductoService;
import com.franquicias.api.franchise_service.service.ResourceNotFoundException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
    @Mock
    private FranquiciaRepository repository;

    @InjectMocks
    private ProductoService service;

    @Test
    void returnsHighestStockProductForEachBranch() {
        Franquicia franquicia = new Franquicia("f-1", "Acme", List.of(
                new Sucursales("s-1", "Centro", List.of(
                        new Productos("p-1", "Cafe", 10), new Productos("p-2", "Te", 25))),
                new Sucursales("s-2", "Norte", List.of(new Productos("p-3", "Agua", 8)))));
        when(repository.findById("f-1")).thenReturn(Mono.just(franquicia));

        StepVerifier.create(service.getMaxStockProductsByFranchise("f-1"))
                .expectNextMatches(dto -> matches(dto, "s-1", "p-2", 25))
                .expectNextMatches(dto -> matches(dto, "s-2", "p-3", 8))
                .verifyComplete();
    }

    @Test
    void createsProductInTheMatchingBranchAndPersistsAggregate() {
        Sucursales branch = new Sucursales("s-1", "Centro", null);
        Franquicia franchise = new Franquicia("f-1", "Acme", List.of(branch));
        when(repository.findAll()).thenReturn(Flux.just(franchise));
        when(repository.save(any(Franquicia.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.createProducto("s-1", "Cafe", 12))
                .assertNext(producto -> {
                    assertNotNull(producto.getId());
                    assertEquals("Cafe", producto.getNombre());
                    assertEquals(12, producto.getStock());
                })
                .verifyComplete();

        assertEquals(1, branch.getProducto().size());
        verify(repository).save(franchise);
    }

    @Test
    void updatesStockAndPersistsAggregate() {
        Productos product = new Productos("p-1", "Cafe", 12);
        Franquicia franchise = new Franquicia("f-1", "Acme", List.of(
                new Sucursales("s-1", "Centro", List.of(product))));
        when(repository.findAll()).thenReturn(Flux.just(franchise));
        when(repository.save(any(Franquicia.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.updateStock("p-1", 30))
                .expectNextMatches(updated -> updated.getStock() == 30)
                .verifyComplete();

        assertEquals(30, product.getStock());
        verify(repository).save(franchise);
    }

    @Test
    void reportsMissingBranchAsNotFound() {
        when(repository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(service.createProducto("missing", "Cafe", 5))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    private boolean matches(MaxStockProductByBranchDTO dto, String branchId, String productId, int stock) {
        return dto.getSucursalId().equals(branchId)
                && dto.getProductoId().equals(productId)
                && dto.getStock() == stock;
    }
}