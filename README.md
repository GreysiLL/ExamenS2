# AgroAPI - Examen Parcial II, AgroSierra

Grease Duran Castro. Caso A, plataforma Android. Inicio real del proyecto: 27 de octubre de 2026.

## Ejecución en IntelliJ IDEA

Abrir esta carpeta como proyecto Maven. Usar JDK 21 o superior (en este equipo se verificó con el JBR 25 de Android Studio). Ejecutar AplicacionKt desde IntelliJ o, en PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\iniciar.ps1
```

El script compila si no existe un artefacto vigente y arranca el servicio. Puerto 8081, base URL `http://localhost:8081/api/v1`. La base relacional H2 se guarda en `data/agrosierra.mv.db`; no es una lista en memoria y conserva datos al reiniciar. No requiere arrancar PharmaSoft ni Oracle. En Linux/macOS con JDK: `./mvnw spring-boot:run`. Para recompilar explícitamente en Windows, agregar `-Compilar`.

## Usuarios semilla

Contraseña común: `Agro2026*` (BCrypt en la base).

| Usuario | Rol | Privilegios |
|---|---|---|
| tecnico1 | TECNICO | LOTE_LEER, LABOR_LEER, LABOR_CREAR, LABOR_EDITAR sobre propias durante 24 h |
| jefe1 | JEFE_CAMPO | LOTE_LEER, LABOR_LEER, LABOR_CREAR, LABOR_EDITAR global, LABOR_ELIMINAR, CAMPANIA_CERRAR |
| admin | ADMINISTRADOR | LOTE_LEER, LABOR_LEER, USUARIO_GESTIONAR |
| calidad1 | SUPERVISOR_CALIDAD (SC-B) | LOTE_LEER, LABOR_LEER, LABOR_EDITAR global |

ADMINISTRADOR tiene el privilegio definido por el examen; no se inventa un módulo de gestión de usuarios fuera del contrato obligatorio. Ocho lotes en Mantaro, Sagrado y Tambo; dos campañas cerradas. Veinte labores iniciales, con ejemplos de cosecha y de antigüedad superior a 24 horas. Las semillas se insertan una sola vez y los cambios SC-B/SC-C conservan la base existente.

## Contrato REST

Todas las rutas excepto el POST de login requieren `Authorization: Bearer <token>`.

| Verbo | Ruta | Privilegio / resultado |
|---|---|---|
| POST | /auth/login | Público; usuario, contrasena; 200 con JWT firmado, datos y privilegios; 401 si credenciales incorrectas |
| GET | /usuarios/me | Autenticado; 200 |
| GET | /lotes?pagina=0&tamanio=20&valle=Mantaro&campaniaCerrada=false | LOTE_LEER; 200 paginado |
| GET | /lotes/{id} | LOTE_LEER; 200/404 |
| GET | /lotes/{id}/labores?pagina=0&tamanio=20 | LABOR_LEER; 200 paginado, orden descendente de fecha e ID |
| POST | /labores | LABOR_CREAR; 201 con fila definitiva |
| PUT | /labores/{id} | LABOR_EDITAR; 200 con corrección |
| DELETE | /labores/{id} | LABOR_ELIMINAR; 204 sin cuerpo |
| POST | /lotes/{id}/cerrar-campania | CAMPANIA_CERRAR; 200 |

Petición de labor: `loteId: Long, tipo: RIEGO/FUMIGACION/COSECHA, fecha: AAAA-MM-DD, cantidad: Double, observacion: String, cantidadJornales: Double` (SC-C). El responsable lo asigna el servidor a partir del usuario autenticado.

Listados: `contenido, pagina, tamanio, totalElementos, totalPaginas, ultima`. Errores: `timestamp, status, error, message, path, validationErrors`. Tamaño permitido: 1 a 100; página no negativa.

## Arquitectura y seguridad

- `config`: JWT, filtro, configuración de privilegios, semillas y manejador común de errores.
- `seguridad`: Usuario, Rol, Privilegio, acceso a usuarios y login.
- `dominio`: entidades de lotes y labores.
- `repositorio`: SQL parametrizado mediante JdbcTemplate contra una base relacional real.
- `servicio`: reglas RN-01 a RN-04 y transacciones con TransactionTemplate.
- `web/dto`: contrato de entrada, paginación y errores; `web`: controladores delgados.

La contraseña se verifica con BCrypt. El JWT dura una hora y se verifica criptográficamente; su clave aleatoria persiste en la base y no se publica en Git. El token transporta ID y privilegios; el filtro recupera los privilegios vigentes de la base. No se implementa renovación. Un token ausente, alterado o vencido responde 401; la falta de privilegio responde 403. No hay comparaciones de rol en los controladores.

RN-01: campaña cerrada impide crear/corregir (409). RN-02: propietario y límite de 24 h para el alcance restringido (403); el alcance global está en rol.edicion_global. RN-03: cierre exige cosecha (409). RN-04: fumigación no excede dosis por hectárea por superficie (400 en cantidad). Se valida también fecha, tipo, cantidad, longitud de observación y jornales.

Los POST pueden recibir `Idempotency-Key`. La clave queda vinculada a la fila y al autor, permitiendo reconocer un reintento tras perder la respuesta sin volver a crear la labor. No se utilizan servicios simulados en producción; los datos de prueba se cargan solo en la base del servidor.

## Verificación y entrega

`./mvnw test` ejecuta pruebas de integración HTTP con Spring Boot y H2 en memoria aislada. Las peticiones ejercitan seguridad, CRUD, paginación, RN-01 a RN-04, cierre positivo, SC-B y SC-C. Los resultados verificables están en `evidencias/`; la colección importable, en `postman/`.

Repositorio: https://github.com/GreysiLL/ExamenS2
Aplicación: https://github.com/GreysiLL/exa-agrocampo

## Trazabilidad

Se usan main, develop, ramas feature, fix y sc-b-duran/sc-c-duran. Los commits tienen las fechas reales de esta jornada. Este proyecto se inició desde cero el día del examen: no acredita un historial distribuido a lo largo de la unidad y no se presenta como si lo tuviera. Las fusiones locales no equivalen a solicitudes de incorporación remotas; comprobar estas últimas en GitHub.
