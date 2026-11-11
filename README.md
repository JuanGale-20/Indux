# Indux

**Plataforma web empresarial para el análisis de capacidad y la detección de cuellos de botella en procesos organizacionales.**

![Estado del proyecto](https://img.shields.io/badge/Estado-En%20desarrollo-orange)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1)
![Arquitectura](https://img.shields.io/badge/Arquitectura-Hexagonal-purple)

## Acerca del proyecto

**Indux** es una plataforma web empresarial en desarrollo, orientada al monitoreo y análisis de procesos organizacionales. Su objetivo es ayudar a identificar restricciones de capacidad, detectar posibles cuellos de botella y proporcionar indicadores que faciliten la toma de decisiones.

La plataforma aplica la metodología **SteelForce**, diseñada con apoyo de ingenieros industriales, para analizar la capacidad operativa de procesos en sectores como manufactura, producción, logística, distribución y actividades administrativas repetitivas.

El proyecto busca contribuir al **Objetivo de Desarrollo Sostenible 9 (ODS 9)**: Industria, innovación e infraestructura.

> **Estado actual:** Indux se encuentra en desarrollo activo. El backend cuenta con funcionalidades implementadas y pruebas automatizadas. El frontend y otras capacidades de la plataforma se desarrollarán e integrarán progresivamente. Las funcionalidades descritas como planificadas todavía no deben considerarse terminadas.

## Objetivos

- Calcular las horas requeridas y disponibles de los procesos.
- Medir la utilización de la capacidad y la holgura operativa.
- Clasificar los procesos según su nivel de utilización.
- Identificar y priorizar procesos que puedan presentar restricciones.
- Proporcionar información que apoye las decisiones operativas.
- Incorporar progresivamente herramientas de análisis y recomendaciones asistidas por inteligencia artificial.

## Funcionalidades

### Implementadas en el backend

- Autenticación de usuarios mediante JWT.
- Control de acceso basado en roles: `ADMIN`, `LIDER` y `ANALISTA`.
- Gestión de empresas y usuarios.
- Gestión de productos y procesos.
- Validación de datos de empresas, productos y procesos según la fase 2 de SteelForce.
- Distinción entre errores críticos y advertencias de validación.
- Cálculo de indicadores de capacidad, utilización y holgura.
- Aislamiento de información por empresa.
- Migraciones de base de datos mediante Flyway.
- Pruebas automatizadas del dominio, los servicios, la persistencia y la API.

### En desarrollo o planificadas

- Frontend web con Angular y Tailwind CSS.
- Integración completa entre el frontend y la API REST.
- Gestión de recursos compartidos.
- Reportes operativos e históricos.
- Visualización de tendencias.
- Recomendaciones para apoyar la mejora de los procesos.
- Asistente de inteligencia artificial para explicar resultados y ofrecer recomendaciones.

Las funcionalidades se incorporarán progresivamente a medida que avance el desarrollo.

## Metodología de análisis

Indux calcula indicadores de capacidad para ayudar a interpretar la carga operativa de cada proceso.

Entre los indicadores contemplados se encuentran:

- **Horas requeridas:** estimación del tiempo necesario para atender la demanda.
- **Horas disponibles:** capacidad temporal disponible según los parámetros operativos.
- **Utilización:** relación entre las horas requeridas y las horas disponibles.
- **Holgura:** capacidad que permanece disponible después de atender la demanda.

La clasificación de procesos utiliza los siguientes umbrales provisionales:

| Clasificación | Utilización |
|---|---:|
| `SIN_RESTRICCION` | Menor al 80 % |
| `POSIBLE_RESTRICCION` | Desde el 80 % hasta el 100 % |
| `RESTRICCION_CAPACIDAD` | Mayor al 100 % |

Los parámetros operativos están sujetos a revisión y validación por parte de los ingenieros industriales responsables de la metodología.

## Arquitectura

El backend utiliza una arquitectura hexagonal basada en puertos y adaptadores, organizada en tres capas principales:

```text
Indux Backend
├── domain
│   ├── model
│   ├── service
│   ├── validacion
│   └── exception
│
├── application
│   ├── port
│   │   ├── in
│   │   └── out
│   └── service
│
└── infrastructure
    ├── adapter
    │   ├── in
    │   │   └── web
    │   └── out
    │       └── persistence
    ├── security
    └── config
```

La separación de responsabilidades permite mantener las reglas del negocio independientes de los detalles de infraestructura y facilita las pruebas, el mantenimiento y la evolución del sistema.

La arquitectura general contempla un frontend desacoplado que consumirá una API REST sin estado.

## Tecnologías

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA e Hibernate
- Spring Security
- JWT mediante JJWT
- BCrypt para el almacenamiento seguro de contraseñas
- Bean Validation
- Spring Boot Actuator
- Lombok
- Maven

### Base de datos e infraestructura

- PostgreSQL 16
- Docker y Docker Compose
- Flyway para las migraciones de base de datos

### Frontend previsto

- Angular
- TypeScript
- Tailwind CSS
- Componentes standalone
- Signals
- Rutas lazy loading
- Guards e interceptores funcionales
- Formularios reactivos

**Nota:** las tecnologías del frontend corresponden a la arquitectura prevista y no implican que su integración esté terminada.

## Requisitos del entorno de desarrollo

- JDK 21.
- Docker Desktop con Docker Compose, o una instalación compatible de PostgreSQL 16.
- Git.
- Conexión de red.
- Maven Wrapper, incluido en el repositorio.

Para desarrollar el frontend posteriormente también será necesario instalar Node.js y las herramientas correspondientes de Angular.

## Instalación y ejecución del backend

### 1. Clonar el repositorio

```bash
git clone https://github.com/JuanGale-20/Indux.git
cd Indux
```

### 2. Configurar las variables de entorno

Crea un archivo `.env` en la raíz del proyecto tomando como base `.env.example`:

En Windows (PowerShell):
```powershell
Copy-Item .env.example .env
```
En Linux / macOS:
```bash
cp .env.example .env
```

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


### 3. Iniciar PostgreSQL

Con las variables necesarias configuradas, ejecuta:

```bash
docker compose up -d
```

Este comando inicia el servicio PostgreSQL definido en `docker-compose.yml`.

### 4. Ejecutar el backend

En Windows:

```powershell
.\start-server.ps1
# O mediante CMD:
start.bat
# O directamente con Maven:
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`, siempre que el inicio finalice correctamente y no se haya modificado su configuración.

### 5. Ejecutar las pruebas

En Windows:

```powershell
.\run-tests.ps1
# O con Maven directamente:
.\mvnw.cmd test
```

En Linux o macOS:

```bash
./mvnw test
```

Las pruebas permiten comprobar el comportamiento de los componentes cubiertos por la suite automatizada.


#### Pruebas de integración con Newman / Postman

Puedes ejecutar la suite completa de 61 peticiones y 105 aserciones desde la terminal:

```bash
npx newman run Indux_Plataforma_Postman_Collection.json
```
O importando el archivo `Indux_Plataforma_Postman_Collection.json` en Postman con el **Collection Runner**.


---

## 6. Documentación Swagger / OpenAPI

Cuando la aplicación esté en ejecución y `SWAGGER_ENABLED=true`:
- **Interfaz Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Definición OpenAPI (JSON):** `http://localhost:8080/v3/api-docs`

Desde la interfaz web de Swagger UI puedes usar el botón **Authorize** para introducir el token JWT obtenido en el login (`Bearer <token>`).

---

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

---

## Base de datos y modelo de datos

Indux cuenta con una base de datos relacional desarrollada en **PostgreSQL 16**, diseñada para organizar la información empresarial, registrar los procesos operativos y almacenar los datos necesarios para analizar la capacidad y detectar posibles restricciones.

El modelo de datos contempla entidades relacionadas con la gestión empresarial, la configuración de procesos, la disponibilidad de recursos, el registro de reportes y el cálculo de indicadores. La estructura se gestiona mediante migraciones versionadas con Flyway.

### Modelo de datos implementado

La base de datos está organizada en las siguientes tablas:

| Área | Tablas |
|---|---|
| Gestión empresarial | `empresa`, `usuario` |
| Productos y procesos | `producto`, `proceso`, `proceso_recurso` |
| Recursos operativos | `recurso`, `unidad_medida`, `equivalencia_unidad` |
| Reportes operativos | `reporte`, `reporte_recurso` |
| Análisis de capacidad | `analisis_recurso` |
| Validación de información | `validacion_resultado` |
| Control de migraciones | `flyway_schema_history` |

### Capacidades del modelo

- **Gestión empresarial:** almacenamiento de empresas, usuarios, roles y estados de actividad.
- **Organización de procesos:** relación entre productos, procesos y recursos utilizados en las operaciones.
- **Administración de unidades de medida:** definición de unidades y factores de equivalencia para facilitar la consistencia de los datos.
- **Registro operativo:** almacenamiento de reportes con demanda, producción real, periodos de análisis y parámetros de operación.
- **Análisis de capacidad:** persistencia de horas requeridas y disponibles, capacidad, utilización, holgura, déficit, clasificación y prioridad.
- **Validación de datos:** registro de hallazgos asociados a reportes, con su grupo, variable, severidad y mensaje.
- **Evolución controlada del esquema:** versionamiento de los cambios estructurales mediante migraciones Flyway.

### Integridad y organización de la información

El esquema utiliza identificadores numéricos, columnas obligatorias y opcionales, tipos de datos adecuados para cantidades e indicadores, fechas y marcas temporales. Las relaciones entre entidades permiten organizar la información necesaria para las operaciones de la plataforma.

El diseño también contempla la asociación de productos y recursos con empresas, así como la relación de usuarios con su empresa correspondiente.

**Estado:** el esquema de base de datos forma parte del desarrollo de Indux. La implementación integral de cada funcionalidad que utiliza estas tablas continúa sujeta al avance del backend y de la integración con el frontend.


## Seguridad

Indux incorpora medidas de seguridad en el backend, entre ellas:

- Autenticación basada en JWT.
- Contraseñas protegidas mediante BCrypt.
- Autorización basada en roles.
- Separación de la información por empresa.
- Configuración de credenciales mediante variables de entorno.
- Restricción de CORS al origen configurado para el frontend.
- Manejo uniforme de errores de la API.

Estas medidas forman parte de la implementación actual y continúan sujetas a validación conforme evolucione el proyecto.

Antes de un despliegue productivo se deben revisar, entre otros aspectos, HTTPS, gestión de secretos, permisos, políticas de acceso, copias de seguridad y configuración del servidor.

---

## Perfiles de usuario y control de acceso

La plataforma contempla tres roles principales:

- **Administrador (`ADMIN`):** administra empresas y usuarios y supervisa la configuración global de la plataforma.
- **Líder de proceso (`LIDER`):** consulta los indicadores, análisis y alertas correspondientes a los procesos bajo su responsabilidad.
- **Analista (`ANALISTA`):** gestiona productos, procesos, recursos y genera reportes de producción con validación y análisis cuantitativo de capacidad.

### Matriz de permisos

| Rol | Descripción | Permisos Clave |
|


---

## 9. Parámetros de Dominio a Confirmar con Ingenieras Industriales

Los siguientes valores se encuentran centralizados como constantes de dominio debidamente documentadas:

1. `DetectorCausas.UMBRAL_TIEMPO = 1.10`: Umbral del 10 % sobre el tiempo de referencia para considerar que el tiempo de procesamiento es causa de déficit.
2. `CalculadoraTendencia.UMBRAL_PUNTOS = 5.00`: Umbral de 5 puntos porcentuales de utilización para clasificar la tendencia en `CRECIENTE` o `DECRECIENTE`.

---

## Clientes objetivo

Indux está orientado a organizaciones que necesitan mejorar la visibilidad de su capacidad operativa, especialmente en:

- Manufactura y producción.
- Logística y distribución.
- Operaciones industriales.
- Procesos administrativos repetitivos.

La plataforma busca ofrecer información estructurada para apoyar el análisis de procesos y la identificación de oportunidades de mejora.

## Consideraciones legales

El tratamiento de datos personales y el uso del software deben evaluarse de acuerdo con la normativa aplicable en Colombia, incluidas las disposiciones pertinentes sobre protección de datos personales y derechos de autor.

Las referencias normativas del proyecto deben verificarse antes de un despliegue institucional o comercial.

## Contribuciones

Durante la etapa de desarrollo, las mejoras, correcciones y nuevas funcionalidades deben mantener la separación de responsabilidades de la arquitectura, las convenciones de código y las pruebas automatizadas.

Antes de enviar cambios, se recomienda ejecutar la suite de pruebas y comprobar que no se incluyan credenciales, claves o información confidencial.

## Licencia

Consulta el archivo `LICENSE` incluido en este repositorio para conocer los términos de uso y distribución aplicables.

---

**Indux — En desarrollo.**

Plataforma orientada al análisis de capacidad, la identificación de restricciones operativas y el apoyo a la toma de decisiones empresariales.

**Coder - Juan Camilo Gale Muñoz**
