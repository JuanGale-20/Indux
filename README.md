# Indux — Backend API

**Plataforma web empresarial para el análisis de capacidad operativa, detección de cuellos de botella y optimización de procesos bajo la metodología SteelForce.**

![Estado del proyecto](https://img.shields.io/badge/Estado-Producci%C3%B3n%20%2F%20Activo-brightgreen)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1)
![Arquitectura](https://img.shields.io/badge/Arquitectura-Hexagonal-purple)
![Licencia](https://img.shields.io/badge/Licencia-MIT-green)

---

## Índice

1. [Acerca del Proyecto](#acerca-del-proyecto)
2. [Metodología SteelForce y ODS 9](#metodología-steelforce-y-ods-9)
3. [Arquitectura del Sistema](#arquitectura-del-sistema)
4. [Módulos y Funcionalidades](#módulos-y-funcionalidades)
5. [Asistente Inteligente SteelForce (Google Gemini + Contingencia Local)](#asistente-inteligente-steelforce)
6. [Seguridad y Control de Acceso (RBAC)](#seguridad-y-control-de-acceso-rbac)
7. [Requisitos Previos](#requisitos-previos)
8. [Puesta en Marcha Rápida](#puesta-en-marcha-rápida)
9. [Variables de Entorno (.env)](#variables-de-entorno-env)
10. [Documentación de la API (Swagger / OpenAPI)](#documentación-de-la-api-swagger--openapi)
11. [Colección Postman y Pruebas Automatizadas](#colección-postman-y-pruebas-automatizadas)
12. [Estructura del Proyecto](#estructura-del-proyecto)

---

## Acerca del Proyecto

**Indux** es una plataforma orientada a la ingeniería industrial que permite diagnosticar la capacidad instalada, calcular horas requeridas y disponibles, identificar restricciones operativas (cuellos de botella) y proponer planes de acción estructurados.

La plataforma soporta empresas multi-tenant con aislamiento estricto de datos por empresa y perfiles de usuario basados en roles (`ADMIN`, `ANALISTA`, `LIDER`).

---

## Metodología SteelForce y ODS 9

El núcleo del motor analítico implementa la metodología industrial **SteelForce**, alineada con el **ODS 9 (Industria, Innovación e Infraestructura)** de las Naciones Unidas.

### Indicadores Clave:
- **Horas Requeridas ($H_{req}$):** Tiempo estándar necesario para satisfacer la demanda de producción considerando mermas del proceso.
- **Horas Disponibles ($H_{disp}$):** Capacidad temporal disponible según turnos, jornadas y demoras operativas.
- **Utilización ($U$):** Relación porcentual entre carga y capacidad:
  $$\text{Utilización} = \left(\frac{H_{req}}{H_{disp}}\right) \times 100\%$$
- **Holgura Operativa ($H$):** Margen de capacidad remanente ($H_{disp} - H_{req}$).
- **Déficit de Capacidad:** Horas faltantes en recursos sobrecargados ($\max(0, H_{req} - H_{disp})$).

### Clasificación Operativa de Recursos:
| Clasificación | Rango de Utilización | Significado Operativo |
| :--- | :---: | :--- |
| **Baja Utilización** | $< 80\%$ | Capacidad ociosa / subutilización |
| **Utilización Adecuada** | $80\% \le U \le 90\%$ | Rango óptimo y balanceado |
| **Capacidad Ajustada** | $90\% < U \le 100\%$ | Alerta operacional ante variabilidad |
| **Sobrecarga** | $> 100\%$ | Cuello de botella activo |

El recurso con mayor sobrecarga recibe automáticamente la **Prioridad 1 (Restricción Primaria)** de la planta.

---

## Arquitectura del Sistema

El backend sigue rigurosamente los principios de **Arquitectura Hexagonal (Puertos y Adaptadores)**, dividida en tres capas:

```
src/main/java/com/indux/induxbackend/
├── domain/                  # Lógica pura del negocio (sin dependencias de frameworks)
│   ├── model/               # Entidades del dominio inmutables (Records)
│   ├── service/             # Motor analítico SteelForce y detector causal
│   ├── validacion/          # Reglas de validación sintáctica y de integridad
│   └── exception/           # Excepciones de negocio y dominio
│
├── application/             # Casos de uso de la aplicación
│   ├── port/
│   │   ├── in/              # Casos de uso (Interfaces primarias)
│   │   └── out/             # Puertos hacia persistencia, seguridad y LLM
│   └── service/             # Orquestación de casos de uso y lógica de aplicación
│
└── infrastructure/          # Conexión con el exterior (frameworks y librerías)
    ├── adapter/
    │   ├── in/web/          # Controladores REST, DTOs y manejador global de errores
    │   └── out/
    │       ├── persistence/ # Adaptadores JPA, Repositorios Spring Data y Mappers
    │       ├── security/    # BCrypt y proveedor de hashing
    │       └── llm/         # Adaptador HTTP para Google Gemini (AI Studio)
    ├── config/              # Configuraciones de beans de Spring
    └── security/            # Filtros JWT, UserDetailsService y políticas de seguridad
```

---

## Módulos y Funcionalidades

- **Autenticación y Seguridad:** JWT con firma HMAC-SHA256, expiración configurable y control de revocación inmediata para usuarios inactivos.
- **Gestión Multi-Tenant:**
  - Empresas (`/api/empresas`): Registro y administración (solo `ADMIN`).
  - Usuarios (`/api/usuarios`): Creación con roles `ADMIN`, `ANALISTA` y `LIDER`.
  - Productos y Procesos (`/api/productos`, `/api/procesos`): Configuración de secuencias operativas, tiempos estándar y porcentajes de merma.
  - Recursos (`/api/recursos`): Estaciones de trabajo, capacidad instalada y costos horarios.
- **Reportes Operativos (`/api/reportes`):**
  - Ciclo de vida completo: `BORRADOR` $\to$ `VALIDADO` $\to$ `ANALIZADO`.
  - Validación sintáctica de Fase 2 (distinción estricta entre advertencias y errores críticos).
  - Cálculo instantáneo de utilización, holguras, déficit y recomendaciones automáticas.
  - Desglose causal del déficit (identificación de causas raíz: Demanda Excesiva, Tiempos de Ciclo Altos, etc.).
- **Importación Masiva (`/api/importaciones`):** Carga masiva de reportes estructurados.
- **Simulador What-If / Calculadora (`/api/analisis/calcular`):** Motor de cálculo instantáneo para escenarios predictivos.

---

## Asistente Inteligente SteelForce

El backend incorpora un asistente de IA conversacional (`/api/asistente`):
- **Motor Primario:** **Google Gemini** (`gemini-3.8-flash`) vía Google AI Studio API.
- **Seguridad de Credenciales:** La clave API se transmite únicamente mediante el encabezado `x-goog-api-key` y `Authorization: Bearer`. **Nunca** en parámetros de URL ni en logs del servidor.
- **Aislamiento por Rol:**
  - `ADMIN` y `ANALISTA`: Acceden a métricas globales de su empresa.
  - `LIDER`: El contexto inyectado al modelo **filtra automáticamente** solo las estaciones y recursos asignados formalmente a ese líder. Recursos ajenos nunca se envían al LLM.
- **Inferencia Estable y Anti-Alucinación:**
  - Temperatura fijada en **0.25** ($\le 0.3$) para máxima estabilidad técnica.
  - Límite de 2500 caracteres por pregunta y truncado de historial a los últimos 8 mensajes.
  - Prompt estricto con prohibición de LaTeX (fórmulas en texto plano) y regla de cero invención (*"NUNCA inventes números... Si falta un dato, di: 'No dispongo de ese dato'"*).
- **Contingencia Local Silenciosa:** Si no hay clave API configurada o el servicio externo falla (404, 429, timeout de 15 s, caída de red), el sistema conmuta instantáneamente al **Motor Local SteelForce** entregando respuestas diagnósticas sin exponer errores técnicos al usuario.

---

## Seguridad y Control de Acceso (RBAC)

| Endpoint | ADMIN | ANALISTA | LIDER |
| :--- | :---: | :---: | :---: |
| `POST /api/auth/login` | Público | Público | Público |
| `/api/empresas/**` | Lectura / Escritura | Denegado (403) | Denegado (403) |
| `/api/usuarios/**` | Lectura / Escritura | Denegado (403) | Denegado (403) |
| `/api/productos/**`, `/api/procesos/**`, `/api/recursos/**` | Lectura / Escritura | Lectura / Escritura (Su Empresa) | Solo Lectura |
| `/api/reportes/**` (Crear / Validar / Analizar) | Completo | Completo (Su Empresa) | Solo Lectura / Consultas |
| `/api/asistente/**` | Completo | Completo (Su Empresa) | Filtrado a sus estaciones |

---

## Requisitos Previos

- **Java JDK:** 21 LTS (OpenJDK / Eclipse Adoptium recomendado).
- **Docker:** Para ejecutar el contenedor de base de datos PostgreSQL.
- **Maven:** Incluido a través de `mvnw` y `mvnw.cmd`.

---

## Puesta en Marcha Rápida

### 1. Clonar el repositorio
```bash
git clone https://github.com/JuanGale-20/Indux.git
cd Indux
```

### 2. Configurar variables de entorno
Copia la plantilla y crea tu archivo `.env`:
```powershell
Copy-Item .env.example .env
```

### 3. Levantar la base de datos PostgreSQL con Docker
```bash
docker compose up -d
```
El contenedor inicializará PostgreSQL en el puerto `5432` con usuario `indux_user` y base de datos `indux`.

### 4. Ejecutar el Backend
En Windows (PowerShell):
```powershell
.\mvnw.cmd spring-boot:run
```
En Linux / macOS:
```bash
./mvnw spring-boot:run
```

Las migraciones de Flyway se aplicarán automáticamente al iniciar (V1 a V4). La API quedará disponible en `http://localhost:8080`.

---

## Variables de Entorno (.env)

| Variable | Descripción | Valor por Defecto / Ejemplo |
| :--- | :--- | :--- |
| `INDUX_DB_URL` | URL de conexión JDBC a PostgreSQL | `jdbc:postgresql://localhost:5432/indux` |
| `INDUX_DB_USERNAME` | Usuario de base de datos | `indux_user` |
| `INDUX_DB_PASSWORD` | Contraseña de base de datos | `<tu_contrasena>` |
| `INDUX_JWT_SECRET` | Clave secreta HMAC-SHA256 (Base64 $\ge$ 256 bits) | `<clave_base64>` |
| `INDUX_ADMIN_EMAIL` | Correo del administrador inicial | `admin@indux.com` |
| `INDUX_ADMIN_PASSWORD` | Contraseña del administrador inicial | `<tu_clave_segura>` |
| `INDUX_CORS_ORIGEN` | Origen frontend permitido | `http://localhost:4200` |
| `SWAGGER_ENABLED` | Habilitación de OpenAPI / Swagger | `true` |
| `GEMINI_API_KEY` | Clave API de Google AI Studio (opcional) | `AIzaSy...` |
| `GEMINI_MODEL` | Modelo de Gemini utilizado | `gemini-3.8-flash` |
| `GEMINI_BASE_URL` | Endpoint base oficial de Gemini | `https://generativelanguage.googleapis.com/v1beta/openai` |
| `INDUX_LLM_TIMEOUT` | Timeout máximo en segundos para el LLM | `15` |

---

## Documentación de la API (Swagger / OpenAPI)

Con la aplicación en ejecución:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Colección Postman y Pruebas Automatizadas

El repositorio incluye herramientas integrales de prueba:
- **Pruebas Automatizadas (178 tests pasando al 100%):**
  ```powershell
  .\mvnw.cmd test
  ```
- **Colección Postman Completa:**
  El archivo [`Indux_Plataforma_Postman_Collection.json`](./Indux_Plataforma_Postman_Collection.json) contiene todas las peticiones con variables dinámicas, autenticación automática JWT y escenarios de prueba de Fase 2.
- **Generador de Colección:** Script Python [`generar_postman_collection.py`](./generar_postman_collection.py) para actualizar o regenerar la colección Postman ante cambios en la API.

---

## Estructura del Proyecto

```
Indux/
├── .mvn/                               # Wrapper de Maven
├── src/
│   ├── main/
│   │   ├── java/com/indux/induxbackend/
│   │   └── resources/
│   │       ├── db/migration/           # Migraciones Flyway V1..V4
│   │       └── application.yml         # Configuración Spring Boot
│   └── test/
│       └── java/com/indux/induxbackend/# Batería de 178 pruebas unitarias e integración
├── .env.example                        # Plantilla de variables de entorno
├── .gitignore                          # Exclusiones de Git (seguridad y artefactos)
├── docker-compose.yml                  # Definición del contenedor PostgreSQL
├── generar_postman_collection.py       # Script generador de la suite Postman
├── Indux_Plataforma_Postman_Collection.json # Colección de pruebas de integración Postman
├── mvnw / mvnw.cmd                     # Binarios del Maven Wrapper
├── pom.xml                             # Configuración del proyecto Maven y dependencias
├── README.md                           # Documentación principal del backend
├── run-backend.cmd                     # Script rápido de ejecución en Windows
└── run-tests.ps1                       # Script de ejecución de pruebas en PowerShell
```

---

## Licencia

Distribuido bajo la Licencia MIT. Consulta el archivo `LICENSE` para más información.