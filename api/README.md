# AutoParts API (Guía 9_2)

API REST propia en **Spring Boot 3 + Java 17** que consume la app Android `AutoParts`.
Sin autenticación (el login se agrega en la guía 9_3). Base de datos H2 en archivo, que se conserva en un volumen de Docker.

## Endpoints (`http://localhost:8080/api/`)

| Método | Ruta | Qué hace | Cuerpo |
|---|---|---|---|
| GET | `repuestos-servicios` | Lista | — |
| GET | `repuestos-servicios/{id}` | Detalle | — |
| POST | `repuestos-servicios` | Crear | `multipart/form-data`: parte `dto` (JSON) + parte `imagen` (opcional) |
| PUT | `repuestos-servicios/{id}` | Editar | Igual que POST; sin imagen se conserva la anterior |
| DELETE | `repuestos-servicios/{id}` | Eliminar | — |

Las fotos se sirven en `http://localhost:8080/images/{foto}` (fuera de `/api`).

Respuestas: POST y PUT devuelven `{"message": "...", "repuestoServicio": {...}}`; DELETE devuelve `{"message": "..."}`;
los errores salen como `{"message": "..."}` con 400, 404, 413 según el caso.

Al iniciar con la tabla vacía se cargan **8 registros**: 5 repuestos con foto y 3 servicios sin foto
(entre ellos *Pastillas de freno delanteras*, *Disco de frenos* y *Mantenimiento de frenos*, que son los 3 que salen al buscar `freno`).

## Levantarla

Desde WSL o PowerShell, dentro de esta carpeta (`api/`) y con Docker Desktop encendido:

```
docker compose up -d --build
docker ps
```

`docker ps` debe mostrar `0.0.0.0:8080->8080/tcp`. Para recompilar solo la API después de un cambio:

```
docker compose down
docker compose up -d --build --no-deps api
docker compose up -d
```

Ver los registros del servidor: `docker logs autoparts-api`

## Probar desde Windows

```powershell
Invoke-RestMethod http://localhost:8080/api/repuestos-servicios | Format-Table id, nombre, precio, tipo
(Invoke-WebRequest -Method Head -UseBasicParsing http://localhost:8080/images/08605714-820e-415b-9e31-fea1bdb00321.jpg).StatusCode
```

En el navegador: `http://localhost:8080/api/repuestos-servicios` muestra el JSON con los ocho registros.

POST y PUT se prueban mejor con Postman (Body → form-data): parte `dto` de tipo *Text* con el JSON
(`{"nombre":"Bujías","precio":15.0,"stock":12,"tipo":"REPUESTO"}`) y su Content-Type en `application/json`,
y parte `imagen` de tipo *File*.

## Conectar el teléfono o emulador

```
adb devices
adb reverse tcp:8080 tcp:8080
```

Y en Chrome del teléfono: `http://localhost:8080/api/repuestos-servicios` debe mostrar el JSON.
El enlace se pierde al desconectar el cable o reiniciar adb.

## Reiniciar los datos de ejemplo

```
docker compose down -v
docker compose up -d --build
```
