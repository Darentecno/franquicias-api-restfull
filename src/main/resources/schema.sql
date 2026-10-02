CREATE TABLE IF NOT EXISTS franquicias (
    id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    CONSTRAINT uq_franquicias_nombre UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS sucursales (
    id VARCHAR(36) PRIMARY KEY,
    franquicia_id VARCHAR(36) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    CONSTRAINT fk_sucursales_franquicia
        FOREIGN KEY (franquicia_id) REFERENCES franquicias (id) ON DELETE CASCADE,
    INDEX idx_sucursales_franquicia (franquicia_id)
);

CREATE TABLE IF NOT EXISTS productos (
    id VARCHAR(36) PRIMARY KEY,
    sucursal_id VARCHAR(36) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    stock INT NOT NULL,
    CONSTRAINT ck_productos_stock CHECK (stock >= 0),
    CONSTRAINT fk_productos_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sucursales (id) ON DELETE CASCADE,
    INDEX idx_productos_sucursal_stock (sucursal_id, stock)
);