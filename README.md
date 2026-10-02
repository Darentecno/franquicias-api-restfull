# API de franquicias

API para administrar franquicias, sucursales y productos. Usa Java 21, Spring Boot, Spring WebFlux, Project Reactor, R2DBC y MySQL 8.

## Requisitos

- JDK 21 para ejecutar Maven localmente.
- Docker Desktop con Docker Compose para levantar la aplicación y MySQL.
- Postman para probar los endpoints.

## Ejecución local con Docker

Desde PowerShell, en la raíz del repositorio:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose up --build -d
docker compose ps
docker compose logs -f api
```

La API queda en `http://localhost:8080`; Compose publica MySQL en `localhost:3307` para no chocar con una instalación local que use `3306`. Entre contenedores, la API se conecta a `mysql:3306`. Para detenerlos y conservar la base usa `docker compose down`; `docker compose down -v` también elimina el volumen de datos.

`.env.example` contiene valores solo para desarrollo. `.env` está excluido de Git; no reutilices esas contraseñas en producción.

## Base de datos y esquema

La base utilizada para las pruebas locales es MySQL 8.4, con base `franquicias_db`. El contenedor publica el puerto `3307` del host y escucha en el puerto interno `3306`; las credenciales locales son usuario `root` y contraseña `test`.

Antes de levantar Compose, copia `.env.example` a `.env` y verifica/define `DB_USER`, `DB_PASSWORD` y `MYSQL_PORT`. Compose utiliza `DB_PASSWORD` tanto para inicializar el usuario root local de MySQL como para conectar la API. No olvides colocar las credenciales en `.env`; no subas ese archivo a Git. `.env.example` es solo una plantilla de desarrollo.

Compose crea la base `franquicias_db` al inicializar un volumen nuevo mediante `MYSQL_DATABASE`. En cambio, `src/main/resources/schema.sql` ejecuta `CREATE TABLE IF NOT EXISTS` para crear las tablas `franquicias`, `sucursales` y `productos`, con claves foráneas, índices y validación de stock no negativo. El script no crea la base de datos ni modifica tablas preexistentes; si la base ya tiene un esquema anterior, respáldala y aplica una migración antes de iniciar la aplicación.

## Pruebas y ejecución con Maven

Las pruebas unitarias no necesitan una instancia de MySQL:

```powershell
.\mvnw.cmd test
```

Para iniciar la API desde Maven usando el MySQL de Compose, levántalo y apunta la aplicación al puerto host `3307`:

```powershell
docker compose up -d mysql
$env:R2DBC_URL = "r2dbc:mysql://localhost:3307/franquicias_db"
$env:DB_USER = "root"
$env:DB_PASSWORD = "test"
.\mvnw.cmd spring-boot:run
```

Al ejecutar Maven contra tu MySQL instalado en Windows, los valores por defecto apuntan a:

- `R2DBC_URL=r2dbc:mysql://localhost:3306/franquicias_db`
- `DB_USER=root`
- `DB_PASSWORD=test`

Compose usa `DB_USER` y `DB_PASSWORD` del archivo `.env`; Maven usa `root/test` para tu instancia local por defecto. Si cambias las credenciales, configura las variables equivalentes en el proceso que ejecuta Maven.

Si `franquicias_db` ya contiene las tablas de una versión anterior del proyecto, haz una copia de seguridad y verifica el esquema antes de iniciar la API: `schema.sql` crea tablas faltantes, pero no migra columnas ni tipos existentes.

## Probar con Postman

1. En Postman, importa `postman/Franchise API.postman_collection.json`.
2. Inicia la API y verifica que responda en `http://localhost:8080`.
3. Ejecuta las solicitudes de la colección en orden con **Run**. Cada creación guarda los IDs recibidos para las solicitudes siguientes.
4. La colección prueba creación y actualización de franquicia/sucursal/producto, stock, eliminación y consulta del máximo stock por sucursal.

La variable `baseUrl` se puede cambiar si usas otro host o puerto.

## Endpoints

Los IDs son UUID representados como cadenas.

| Método | Ruta | Acción |
| --- | --- | --- |
| `GET` | `/api/franquicias` | Listar franquicias |
| `POST` | `/api/franquicias` | Crear franquicia |
| `PUT` | `/api/franquicias/{franquiciaId}` | Cambiar nombre de franquicia |
| `GET` | `/api/sucursales` | Listar sucursales |
| `POST` | `/api/franquicias/{franquiciaId}/sucursales` | Crear sucursal |
| `PUT` | `/api/sucursales/{sucursalId}` | Cambiar nombre de sucursal |
| `POST` | `/api/v1/sucursales/{sucursalId}/productos` | Crear producto |
| `DELETE` | `/api/v1/productos/{productoId}` | Eliminar producto |
| `PUT` | `/api/v1/productos/{productoId}/stock` | Actualizar stock |
| `PUT` | `/api/v1/productos/{productoId}` | Cambiar nombre del producto |
| `GET` | `/api/v1/franquicias/{franquiciaId}/max-stock` | Producto con mayor stock por sucursal |

Ejemplos de cuerpos JSON:

```json
{ "nombre": "Franquicia Centro" }
```

```json
{ "nombre": "Cafe", "stock": 25 }
```

```json
{ "stock": 40 }
```

El reporte de máximo stock devuelve `sucursalId`, `sucursalNombre`, `productoId`, `productoNombre` y `stock`. Las sucursales sin productos no aparecen. Los nombres no pueden estar vacíos y el stock debe ser mayor o igual a cero.

## Terraform local

`infra/terraform` puede levantar MySQL localmente como alternativa a Compose. No ejecutes ambas opciones al tiempo porque publican el mismo puerto `3307`.

```powershell
terraform -chdir=infra/terraform init
terraform -chdir=infra/terraform plan
terraform -chdir=infra/terraform apply
```

Terraform usa por defecto el esquema `franquicias_db`, el puerto host `3307` y la contraseña local `test`. Al terminar:

```powershell
terraform -chdir=infra/terraform destroy
```

## Producción

1. Requisitos: servicio de contenedores, MySQL administrado, registro de imágenes y gestor de secretos.
2. Crea `franquicias_db` y ejecuta `schema.sql` como migración. Configura `SPRING_SQL_INIT_MODE=never` para evitar permisos DDL en el usuario de ejecución.
3. Inyecta `R2DBC_URL` (`r2dbc:mysql://<host>:3306/franquicias_db`), `DB_USER` y `DB_PASSWORD` desde secretos. Usa un usuario de mínimo privilegio, TLS y reglas de red restringidas.
4. Construye/publica la imagen con `docker build -t <registro>/<proyecto>/franchise-service:<tag> .` y despliega detrás de HTTPS.
5. Configura backups, monitoreo, logs y alertas. No uses credenciales de ejemplo ni publiques MySQL directamente.

La API todavía no implementa autenticación/autorización ni un endpoint de health check; agrégalos antes de exponerla públicamente.