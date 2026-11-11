# Indux Plataforma — Backend

Indux Plataforma es un software universitario basado en la metodología **SteelForce**, diseñada en colaboración con ingenieros industriales (alineado al objetivo **ODS 9: Industria, Innovación e Infraestructura**). Detecta y analiza **cuellos de botella de capacidad** en plantas de producción: a partir de la demanda de un producto y de los datos operativos de cada recurso (máquina, línea, estación), calcula utilización, holgura, déficit e impacto, clasifica cada recurso, prioriza restricciones, diagnostica causas raíz y recomienda acciones operativas y estratégicas.

---

## 1. Stack Tecnológico

- **Lenguaje:** Java 21 (LTS)
- **Framework:** Spring Boot 4.1.x
- **Base de Datos:** PostgreSQL 16
- **Control de Versiones de BD:** Flyway
- **Seguridad:** Spring Security 6 con autenticación Bearer JWT sin estado
- **Documentación de API:** SpringDoc OpenAPI 3.x / Swagger UI
- **Construcción y Dependencias:** Apache Maven (Maven Wrapper incluido)
- **Procesamiento de Archivos:** Apache POI 5.4.0 (Excel `.xlsx`) y parser CSV nativo con detección automática de delimitador y BOM UTF-8

---

## 2. Variables de Entorno

Las credenciales y secretos nunca deben escribirse en el código. Configúralos en tu entorno o mediante un archivo `.env` local (basado en [.env.example](file:///C:/Users/juang/OneDrive/Documentos/SEMESTRES%20ING%20SISTEMAS/Proyecto/Indux/Indux/.env.example)):

| Variable | Descripción | Valor por defecto / Ejemplo |
|---|---|---|
| `INDUX_DB_URL` | URL JDBC de conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/indux` |
| `INDUX_DB_USERNAME` | Usuario de la base de datos | `indux_user` |
| `INDUX_DB_PASSWORD` | Contraseña de la base de datos | `indux_pass` |
| `INDUX_JWT_SECRET` | Clave secreta HMAC en Base64 (mínimo 256 bits) | `/zK8g7pwYlb7fM1JVbgstRmuISrgXKcmel6Mn7uQCBY=` |
| `INDUX_ADMIN_EMAIL` | Correo del usuario administrador inicial | `admin@indux.com` |
| `INDUX_ADMIN_PASSWORD` | Contraseña inicial del administrador | `reemplaza_por_una_clave_segura` |
| `INDUX_CORS_ORIGEN` | Origen permitido para CORS (frontend) | `http://localhost:4200` |
| `SWAGGER_ENABLED` | Activar o desactivar OpenAPI y Swagger UI | `true` |

---

## 3. Puesta en Marcha de la Base de Datos

### Opción A: Docker

Ejecuta el siguiente comando para levantar un contenedor de PostgreSQL 16 en el puerto 5432:

```bash
docker run -d \
  --name indux-postgres \
  -e POSTGRES_DB=indux \
  -e POSTGRES_USER=indux_user \
  -e POSTGRES_PASSWORD=indux_pass \
  -p 5432:5432 \
  postgres:16-alpine
```

### Opción B: WSL / Linux Nativo

```bash
sudo service postgresql start
sudo -u postgres psql -c "CREATE USER indux_user WITH PASSWORD 'indux_pass';"
sudo -u postgres psql -c "CREATE DATABASE indux OWNER indux_user;"
```

---

## 4. Ejecución de Tests

Asegúrate de que PostgreSQL esté activo en `localhost:5432` y ejecuta:

### En Windows (PowerShell / CMD):

```cmd
set INDUX_DB_URL=jdbc:postgresql://localhost:5432/indux
set INDUX_DB_USERNAME=indux_user
set INDUX_DB_PASSWORD=indux_pass
set INDUX_JWT_SECRET=/zK8g7pwYlb7fM1JVbgstRmuISrgXKcmel6Mn7uQCBY=
set INDUX_ADMIN_PASSWORD=reemplaza_por_una_clave_segura
.\mvnw.cmd test
```

### En Linux / macOS:

```bash
export INDUX_DB_URL="jdbc:postgresql://localhost:5432/indux"
export INDUX_DB_USERNAME="indux_user"
export INDUX_DB_PASSWORD="indux_pass"
export INDUX_JWT_SECRET="/zK8g7pwYlb7fM1JVbgstRmuISrgXKcmel6Mn7uQCBY="
export INDUX_ADMIN_PASSWORD="reemplaza_por_una_clave_segura"
./mvnw test
```

---

## 5. Ejecución del Servidor

```bash
./mvnw spring-boot:run
```

En Windows:
```cmd
.\mvnw.cmd spring-boot:run
```

El servidor iniciará en `http://localhost:8080`.

---

## 6. Documentación Swagger / OpenAPI

Cuando la aplicación esté en ejecución y `SWAGGER_ENABLED=true`:
- **Interfaz Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Definición OpenAPI (JSON):** `http://localhost:8080/v3/api-docs`

Desde la interfaz web de Swagger UI puedes usar el botón **Authorize** para introducir el token JWT obtenido en el login (`Bearer <token>`).

---

## 7. Roles y Control de Acceso

| Rol | Descripción | Permisos Clave |
|---|---|---|
| `ADMIN` | Administrador de la plataforma (sin empresa asignada). | Acceso total a todas las empresas, gestión de usuarios, productos, procesos, recursos, reportes e importaciones. |
| `ANALISTA` | Analista de planta asignado a una empresa. | Creación, actualización, importación, validación y análisis de reportes de su empresa. Solo lectura de catálogos de su empresa. |
| `LIDER` | Líder de proceso de una empresa. | Perfil de solo consulta. En las consultas de reportes y análisis, únicamente visualiza las líneas y recursos de los procesos asignados a su cargo. |

---

## 8. Catálogo Completo de Endpoints REST

### Autenticación
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/login` | Público | Autenticación con correo y contraseña. Devuelve token JWT. |

### Empresas
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/empresas` | ADMIN | Registrar una nueva empresa. |
| `GET` | `/api/empresas` | ADMIN | Listar todas las empresas registradas. |
| `GET` | `/api/empresas/{id}` | Autenticado | Consultar una empresa (aislado por empresa). |
| `PUT` | `/api/empresas/{id}` | ADMIN | Actualizar datos de una empresa. |
| `DELETE` | `/api/empresas/{id}` | ADMIN | Desactivar una empresa. |

### Usuarios
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/usuarios` | ADMIN | Registrar un nuevo usuario y rol. |
| `GET` | `/api/usuarios` | ADMIN | Listar todos los usuarios. |
| `PUT` | `/api/usuarios/{id}` | ADMIN | Actualizar datos, empresa o rol de un usuario. |
| `DELETE` | `/api/usuarios/{id}` | ADMIN | Desactivar a un usuario. |

### Productos
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/productos` | ADMIN | Crear un nuevo producto. |
| `GET` | `/api/productos/{id}` | Autenticado | Consultar un producto por ID. |
| `GET` | `/api/productos?empresaId={id}` | Autenticado | Listar productos de una empresa. |
| `PUT` | `/api/productos/{id}` | ADMIN | Actualizar datos de un producto. |
| `DELETE` | `/api/productos/{id}` | ADMIN | Eliminar un producto. |

### Procesos
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/procesos` | ADMIN | Crear una etapa de proceso productivo con merma y unidad. |
| `GET` | `/api/procesos/{id}` | Autenticado | Consultar un proceso por ID. |
| `GET` | `/api/productos/{id}/procesos` | Autenticado | Listar procesos ordenados de un producto. |
| `PUT` | `/api/procesos/{id}` | ADMIN | Actualizar proceso (secuencia, líder, merma, unidades). |
| `DELETE` | `/api/procesos/{id}` | ADMIN | Eliminar una etapa de proceso. |

### Recursos y Vínculos
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/recursos` | ADMIN | Crear un recurso (máquina, línea, estación). |
| `GET` | `/api/recursos/{id}` | Autenticado | Consultar un recurso por ID. |
| `GET` | `/api/recursos?empresaId={id}` | Autenticado | Listar recursos de una empresa. |
| `PUT` | `/api/recursos/{id}` | ADMIN | Actualizar datos y cantidad de un recurso. |
| `DELETE` | `/api/recursos/{id}` | ADMIN | Eliminar un recurso. |
| `POST` | `/api/recursos/{id}/procesos` | ADMIN | Vincular recurso a proceso con tiempo de referencia. |
| `DELETE` | `/api/recursos/{id}/procesos/{procesoId}` | ADMIN | Desvincular recurso de un proceso. |
| `GET` | `/api/recursos/{id}/procesos` | Autenticado | Listar procesos vinculados al recurso. |
| `GET` | `/api/procesos/{id}/recursos` | Autenticado | Listar recursos vinculados al proceso. |
| `GET` | `/api/recursos/{id}/historico?limite=12` | Autenticado | Histórico de utilización depurado y tendencia calculada. |

### Reportes de Producción y Análisis
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/reportes` | ADMIN, ANALISTA | Crear un reporte con sus líneas de recursos. |
| `GET` | `/api/reportes` | Autenticado | Listar reportes por producto o empresa. |
| `GET` | `/api/reportes/{id}` | Autenticado | Consultar detalle de un reporte (filtrado por líder). |
| `PUT` | `/api/reportes/{id}` | ADMIN, ANALISTA | Actualizar reporte y líneas (vuelve a `BORRADOR`). |
| `DELETE` | `/api/reportes/{id}` | ADMIN, ANALISTA | Eliminar un reporte. |
| `POST` | `/api/reportes/{id}/validar` | ADMIN, ANALISTA | Validar coherencia del reporte (estado `VALIDADO` o `CON_ERRORES`). |
| `GET` | `/api/reportes/{id}/validaciones` | Autenticado | Listar resultados detallados de validación. |
| `POST` | `/api/reportes/{id}/analizar` | ADMIN, ANALISTA | Validar y analizar persistiendo resultados (200 o 422). |
| `GET` | `/api/reportes/{id}/analisis` | Autenticado | Consultar el análisis persistido (409 si no está analizado). |
| `GET` | `/api/reportes/{id}/recomendaciones` | Autenticado | Obtener recomendaciones operativas clasificadas por nivel. |
| `GET` | `/api/reportes/{id}/causas` | Autenticado | Desglose cuantitativo de factores causantes del déficit. |

### Importación Masiva
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/importaciones/reportes` | ADMIN, ANALISTA | Importar reportes vía Excel (`.xlsx`) o CSV (`confirmar=false` para vista previa, `confirmar=true` para guardar). |
| `GET` | `/api/importaciones/reportes/plantilla` | ADMIN, ANALISTA | Descargar plantilla oficial en formato `.xlsx` o `.csv`. |

### Análisis al Vuelo
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/analisis/calcular` | Autenticado | Cálculo instantáneo sin persistencia. |

---

## 9. Parámetros de Dominio a Confirmar con Ingenieras Industriales

Los siguientes valores se encuentran centralizados como constantes de dominio debidamente documentadas:

1. `DetectorCausas.UMBRAL_TIEMPO = 1.10`: Umbral del 10 % sobre el tiempo de referencia para considerar que el tiempo de procesamiento es causa de déficit.
2. `CalculadoraTendencia.UMBRAL_PUNTOS = 5.00`: Umbral de 5 puntos porcentuales de utilización para clasificar la tendencia en `CRECIENTE` o `DECRECIENTE`.
