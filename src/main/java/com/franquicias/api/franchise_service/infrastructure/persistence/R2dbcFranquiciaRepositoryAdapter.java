package com.franquicias.api.franchise_service.infrastructure.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.franquicias.api.franchise_service.models.Franquicia;
import com.franquicias.api.franchise_service.models.Productos;
import com.franquicias.api.franchise_service.models.Sucursales;
import com.franquicias.api.franchise_service.repository.FranquiciaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcFranquiciaRepositoryAdapter implements FranquiciaRepository {
    private static final String SELECT_AGGREGATE = """
            SELECT f.id AS f_id, f.nombre AS f_nombre,
                   s.id AS s_id, s.nombre AS s_nombre,
                   p.id AS p_id, p.nombre AS p_nombre, p.stock AS p_stock
            FROM franquicias f
            LEFT JOIN sucursales s ON s.franquicia_id = f.id
            LEFT JOIN productos p ON p.sucursal_id = s.id
            """;

    private final DatabaseClient databaseClient;

    public R2dbcFranquiciaRepositoryAdapter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Franquicia> findById(String id) {
        return databaseClient.sql(SELECT_AGGREGATE + " WHERE f.id = :id ORDER BY s.id, p.id")
                .bind("id", id)
                .fetch().all().collectList()
                .filter(rows -> !rows.isEmpty())
                .map(this::toFranchises)
                .map(franchises -> franchises.get(0));
    }

    @Override
    public Flux<Franquicia> findAll() {
        return databaseClient.sql(SELECT_AGGREGATE + " ORDER BY f.id, s.id, p.id")
                .fetch().all().collectList()
                .flatMapMany(rows -> Flux.fromIterable(toFranchises(rows)));
    }

    @Override
    public Mono<Boolean> existsByNombreIgnoreCase(String nombre) {
        return databaseClient.sql("SELECT COUNT(*) AS total FROM franquicias WHERE LOWER(nombre) = LOWER(:nombre)")
                .bind("nombre", nombre)
                .fetch().one()
                .map(row -> ((Number) row.get("total")).longValue() > 0)
                .defaultIfEmpty(false);
    }

    @Override
    @Transactional
    public Mono<Franquicia> save(Franquicia franquicia) {
        Mono<Void> upsertFranquicia = databaseClient.sql("""
                INSERT INTO franquicias (id, nombre) VALUES (:id, :nombre)
                ON DUPLICATE KEY UPDATE nombre = :nombre
                """)
                .bind("id", franquicia.getId())
                .bind("nombre", franquicia.getNombre())
                .fetch().rowsUpdated().then();

        Mono<Void> replaceBranches = databaseClient.sql("DELETE FROM sucursales WHERE franquicia_id = :id")
                .bind("id", franquicia.getId())
                .fetch().rowsUpdated().then();

        Mono<Void> insertBranches = Flux.fromIterable(franquicia.getSucursal())
                .concatMap(sucursal -> insertSucursal(franquicia.getId(), sucursal)
                        .then(Flux.fromIterable(sucursal.getProducto())
                                .concatMap(producto -> insertProducto(sucursal.getId(), producto))
                                .then()))
                .then();

        return upsertFranquicia.then(replaceBranches).then(insertBranches).thenReturn(franquicia);
    }

    private Mono<Void> insertSucursal(String franquiciaId, Sucursales sucursal) {
        return databaseClient.sql("""
                INSERT INTO sucursales (id, franquicia_id, nombre)
                VALUES (:id, :franquiciaId, :nombre)
                """)
                .bind("id", sucursal.getId())
                .bind("franquiciaId", franquiciaId)
                .bind("nombre", sucursal.getNombre())
                .fetch().rowsUpdated().then();
    }

    private Mono<Void> insertProducto(String sucursalId, Productos producto) {
        return databaseClient.sql("""
                INSERT INTO productos (id, sucursal_id, nombre, stock)
                VALUES (:id, :sucursalId, :nombre, :stock)
                """)
                .bind("id", producto.getId())
                .bind("sucursalId", sucursalId)
                .bind("nombre", producto.getNombre())
                .bind("stock", producto.getStock())
                .fetch().rowsUpdated().then();
    }

    private List<Franquicia> toFranchises(List<Map<String, Object>> rows) {
        Map<String, Franquicia> franchises = new LinkedHashMap<>();
        Map<String, Map<String, Sucursales>> branchesByFranchise = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            String franchiseId = (String) row.get("f_id");
            Franquicia franchise = franchises.computeIfAbsent(franchiseId,
                    id -> new Franquicia(id, (String) row.get("f_nombre"), new ArrayList<>()));

            String branchId = (String) row.get("s_id");
            if (branchId == null) {
                continue;
            }

            Map<String, Sucursales> branchMap = branchesByFranchise.computeIfAbsent(franchiseId,
                    ignored -> new LinkedHashMap<>());
            Sucursales branch = branchMap.computeIfAbsent(branchId, id -> {
                Sucursales newBranch = new Sucursales(id, (String) row.get("s_nombre"), new ArrayList<>());
                franchise.getSucursal().add(newBranch);
                return newBranch;
            });

            String productId = (String) row.get("p_id");
            if (productId != null) {
                branch.getProducto().add(new Productos(productId, (String) row.get("p_nombre"),
                        ((Number) row.get("p_stock")).intValue()));
            }
        }
        return new ArrayList<>(franchises.values());
    }
}