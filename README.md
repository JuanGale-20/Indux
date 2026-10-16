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

El proyecto utiliza variables de entorno para evitar almacenar credenciales y claves sensibles en el código fuente.

Configura las siguientes variables en tu entorno local:

| Variable | Propósito |
|---|---|
| `INDUX_DB_URL` | URL JDBC de PostgreSQL; opcional si se utiliza el valor local predeterminado |
| `INDUX_DB_USERNAME` | Usuario de PostgreSQL |
| `INDUX_DB_PASSWORD` | Contraseña de PostgreSQL |
| `INDUX_JWT_SECRET` | Clave secreta para firmar tokens JWT |
| `INDUX_ADMIN_PASSWORD` | Contraseña inicial del administrador |

La aplicación también admite `INDUX_ADMIN_EMAIL`, cuyo valor predeterminado es `admin@indux.com`.

Utiliza valores locales propios y una clave JWT criptográficamente segura. No publiques credenciales reales, archivos `.env` ni claves privadas en GitHub.

**Importante:** Spring Boot no carga automáticamente un archivo `.env` por el simple hecho de existir. Debes configurar las variables en el entorno de ejecución, por ejemplo, en IntelliJ IDEA o en PowerShell. Docker Compose puede leerlas de un archivo `.env` situado junto al archivo `docker-compose.yml`.

### 3. Iniciar PostgreSQL

Con las variables necesarias configuradas, ejecuta:

```bash
docker compose up -d
```

Este comando inicia el servicio PostgreSQL definido en `docker-compose.yml`.

### 4. Ejecutar el backend

En Windows:

```powershell
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
.\mvnw.cmd test
```

En Linux o macOS:

```bash
./mvnw test
```

Las pruebas permiten comprobar el comportamiento de los componentes cubiertos por la suite automatizada.

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

## Perfiles de usuario

La plataforma contempla tres roles principales:

- **Administrador (`ADMIN`):** administra empresas y usuarios y supervisa la configuración de la plataforma.
- **Líder de proceso (`LIDER`):** consulta los indicadores y las alertas correspondientes a los procesos autorizados.
- **Analista (`ANALISTA`):** registra y analiza información de productos y procesos, y revisa los resultados de las validaciones.

El acceso efectivo a cada operación depende de las reglas de autorización implementadas en el backend.

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
