# Indux — Plataforma Empresarial SteelForce

**Plataforma web empresarial orientada al diagnóstico de capacidad instalada, detección de cuellos de botella y optimización de procesos operativos industriales bajo la metodología SteelForce.**

![Estado del proyecto](https://img.shields.io/badge/Estado-Producci%C3%B3n%20%2F%20Activo-brightgreen)
![Java](https://img.shields.io/badge/Backend-Java%2021%20%7C%20Spring%20Boot%204.1.1-blue)
![Frontend](https://img.shields.io/badge/Frontend-Angular%2021%20%7C%20Tailwind%20CSS%204-red)
![PostgreSQL](https://img.shields.io/badge/Base%20de%20Datos-PostgreSQL%2016-4169E1)
![Arquitectura](https://img.shields.io/badge/Arquitectura-Hexagonal%20%2B%20Standalone%20Signals-purple)
![Licencia](https://img.shields.io/badge/Licencia-MIT-green)

---

## Índice

1. [Visión General del Proyecto](#1-visión-general-del-proyecto)
2. [Estructura del Monorepo](#2-estructura-del-monorepo)
3. [Metodología SteelForce y ODS 9](#3-metodología-steelforce-y-ods-9)
4. [Diseño y Arquitectura de la Base de Datos](#4-diseño-y-arquitectura-de-la-base-de-datos)
   - [Diagrama Entidad-Relación (Mermaid)](#41-diagrama-entidad-relación-er)
   - [Flujo del Ciclo de Vida del Reporte](#42-flujo-del-ciclo-de-vida-del-reporte-state-diagram)
   - [Diccionario de Tablas y Restricciones](#43-diccionario-de-tablas-y-restricciones)
5. [Arquitectura del Sistema](#5-arquitectura-del-sistema)
   - [Arquitectura Hexagonal del Backend](#51-backend-arquitectura-hexagonal)
   - [Arquitectura Frontend (Angular 21 + Signals)](#52-frontend-angular-21--signals)
6. [Asistente Inteligente SteelForce (Google Gemini + Contingencia)](#6-asistente-inteligente-steelforce)
7. [Matriz de Seguridad y Roles (RBAC)](#7-matriz-de-seguridad-y-roles-rbac)
8. [Puesta en Marcha Rápida](#8-puesta-en-marcha-rápida)
9. [Variables de Entorno (.env)](#9-variables-de-entorno-env)
10. [Batería de Pruebas Automatizadas](#10-batería-de-pruebas-automatizadas)
11. [Documentación de API y Colección Postman](#11-documentación-de-api-y-colección-postman)

---

## 1. Visión General del Proyecto

**Indux** es una plataforma SaaS industrial concebida para balancear cargas operativas, detectar restricciones primarias (cuellos de botella) y sugerir acciones de mejora en plantas de producción, manufactura y operaciones logísticas. 

Alineada con el **ODS 9 (Industria, Innovación e Infraestructura)**, permite simular escenarios predictivos de capacidad mediante modelos matemáticos rigurosos y ofrece asistencia en lenguaje natural impulsada por **Google Gemini** con respaldo determinístico local.

---

## 2. Estructura del Monorepo

El proyecto está organizado como un monorepo que integra backend y frontend desacoplados:

```
Indux/
├── src/                                # Código fuente Backend (Java 21 / Spring Boot)
│   ├── main/java/com/indux/induxbackend/
│   │   ├── domain/                     # Núcleo de lógica pura (Models, Service, Validaciones)
│   │   ├── application/                # Casos de uso y puertos (In/Out)
│   │   └── infrastructure/             # Adaptadores Web REST, Persistencia JPA y LLM
│   └── main/resources/
│       ├── db/migration/               # Migraciones Flyway de base de datos (V1..V4)
│       └── application.yml             # Configuración Spring Boot
│
├── Indux-Fronted/                      # Código fuente Frontend (Angular 21)
│   ├── src/app/
│   │   ├── core/                       # Servicios API, interceptor JWT, modelos, constantes
│   │   ├── layouts/                    # AppLayout (Sidebar, Navbar) y PublicLayout
│   │   ├── pages/                      # Componentes Standalone (Dashboard, Reportes, Chatbot, etc.)
│   │   └── services/                   # Servicios reactivos con Signals
│   ├── package.json                    # Dependencias frontend (Tailwind CSS 4, Lucide Icons)
│   └── angular.json                    # Configuración de compilación Angular
│
├── .env.example                        # Plantilla de variables de entorno (segura, sin credenciales)
├── docker-compose.yml                  # Orquestación de base de datos PostgreSQL 16
├── pom.xml                             # Configuración Maven del backend
├── Indux_Plataforma_Postman_Collection.json # Suite completa de pruebas de integración Postman
└── generar_postman_collection.py       # Script generador de la suite Postman
```

---

## 3. Metodología SteelForce y ODS 9

### Fórmulas Matemáticas Centrales:
1. **Horas Requeridas ($H_{req}$):**
   $$H_{req} = \text{Demanda} \times \text{Tiempo Estándar de Procesamiento} \times \left(\frac{1}{1 - \text{Merma}}\right)$$
2. **Horas Disponibles ($H_{disp}$):**
   $$H_{disp} = \text{Turnos} \times \frac{\text{Horas}}{\text{Turno}} \times \text{Días Operación} \times \text{Cantidad de Recursos} - \text{Tiempo No Disponible}$$
3. **Índice de Utilización ($U$):**
   $$U = \left(\frac{H_{req}}{H_{disp}}\right) \times 100\%$$
4. **Holgura Operativa ($H$):**
   $$H = H_{disp} - H_{req}$$
5. **Déficit de Capacidad:**
   $$\text{Déficit} = \max(0, H_{req} - H_{disp})$$

### Clasificación de Recursos:
```mermaid
graph TD
    U["Utilización (U)"] -->|< 80%| BU["Baja Utilización (Capacidad Ociosa)"]
    U -->|80% <= U <= 90%| UA["Utilización Adecuada (Rango Óptimo)"]
    U -->|90% < U <= 100%| CA["Capacidad Ajustada (Alerta de Variabilidad)"]
    U -->|> 100%| SC["Sobrecarga (Cuello de Botella Activo)"]
    SC --> P1["Prioridad 1: Restricción Primaria de la Planta"]
```

---

## 4. Diseño y Arquitectura de la Base de Datos

La base de datos utiliza **PostgreSQL 16** gestionada con migraciones evolutivas e inmutables mediante **Flyway (V1 a V4)**.

### 4.1. Diagrama Entidad-Relación (ER)

```mermaid
erDiagram
    EMPRESA ||--o{ USUARIO : "pertenece_a"
    EMPRESA ||--o{ PRODUCTO : "fabrica"
    EMPRESA ||--o{ RECURSO : "posee"
    EMPRESA ||--o{ EQUIVALENCIA_UNIDAD : "define"

    PRODUCTO ||--o{ PROCESO : "compuesto_por"
    PRODUCTO ||--o{ REPORTE : "evaluado_en"

    USUARIO ||--o{ REPORTE : "crea"
    USUARIO ||--o{ PROCESO : "supervisa (LIDER)"

    PROCESO ||--o{ PROCESO_RECURSO : "requiere"
    RECURSO ||--o{ PROCESO_RECURSO : "asociado_a"

    REPORTE ||--o{ REPORTE_RECURSO : "contiene_datos_de"
    RECURSO ||--o{ REPORTE_RECURSO : "participa_en"

    REPORTE ||--o{ VALIDACION_RESULTADO : "registra_hallazgos"
    REPORTE_RECURSO ||--o| ANALISIS_RECURSO : "produce_metricas"

    UNIDAD_MEDIDA ||--o{ EQUIVALENCIA_UNIDAD : "unidad_origen"
    UNIDAD_MEDIDA ||--o{ EQUIVALENCIA_UNIDAD : "unidad_destino"

    EMPRESA {
        bigint id PK
        varchar nombre
        varchar sector
        boolean activa
        timestamptz created_at
    }

    USUARIO {
        bigint id PK
        bigint empresa_id FK
        varchar nombre
        varchar email UK
        varchar password_hash
        varchar rol
        boolean activo
    }

    PRODUCTO {
        bigint id PK
        bigint empresa_id FK
        varchar codigo
        varchar nombre
        varchar unidad_medida
    }

    PROCESO {
        bigint id PK
        bigint producto_id FK
        bigint lider_id FK
        varchar codigo
        varchar nombre
        int secuencia
        numeric merma
        varchar unidad_procesada
    }

    RECURSO {
        bigint id PK
        bigint empresa_id FK
        varchar codigo
        varchar nombre
        varchar tipo
        int cantidad_disponible
        varchar estado
    }

    REPORTE {
        bigint id PK
        bigint producto_id FK
        bigint creado_por FK
        varchar planta_area
        varchar frecuencia
        date periodo_inicio
        date periodo_fin
        numeric demanda
        numeric produccion_real
        varchar estado
    }

    REPORTE_RECURSO {
        bigint id PK
        bigint reporte_id FK
        bigint recurso_id FK
        numeric tiempo_procesamiento
        numeric tiempo_calendario
        numeric tiempo_no_disponible
        int numero_turnos
        numeric horas_por_turno
        int dias_operacion
    }

    ANALISIS_RECURSO {
        bigint id PK
        bigint reporte_recurso_id FK
        numeric horas_requeridas
        numeric horas_disponibles
        numeric capacidad
        numeric utilizacion
        numeric holgura
        numeric deficit_capacidad
        varchar clasificacion
        int prioridad
        text diagnostico
    }

    VALIDACION_RESULTADO {
        bigint id PK
        bigint reporte_id FK
        varchar grupo
        varchar variable
        varchar severidad
        varchar mensaje
    }
```

### 4.2. Flujo del Ciclo de Vida del Reporte (State Diagram)

```mermaid
stateDiagram-v2
    [*] --> BORRADOR : Creación de Reporte (POST /api/reportes)
    BORRADOR --> VALIDADO : Validación Fase 2 Exitosa (POST /api/reportes/{id}/validar)
    BORRADOR --> CON_ERRORES : Errores Críticos Detectados
    CON_ERRORES --> BORRADOR : Corrección de Datos
    VALIDADO --> ANALIZADO : Ejecución Motor SteelForce (POST /api/reportes/{id}/analizar)
    ANALIZADO --> [*] : Diagnósticos, Causas y Recomendaciones Listas
```

### 4.3. Diccionario de Tablas y Restricciones
- **`empresa`:** Multi-tenancy isolation. Sector industrial categórico con advertencias no bloqueantes.
- **`usuario`:** Cuentas con roles `ADMIN`, `LIDER`, `ANALISTA`. Restricción `CHECK (rol IN ('ADMIN', 'LIDER', 'ANALISTA'))`. Contraseñas protegidas mediante BCrypt con sal aleatoria.
- **`producto`:** Catálogo de productos. Restricción única compuesta `(empresa_id, codigo)`.
- **`proceso`:** Etapas de manufactura. Ordenadas por `secuencia > 0` única por producto. Admite porcentaje de merma para cálculo de masa/volumen bruto requerido.
- **`recurso`:** Estaciones, hornos, tornos y líneas. Desacopladas a nivel de empresa en `V3` para permitir modelar **recursos compartidos** entre múltiples líneas productivas (`proceso_recurso`).
- **`reporte`:** Contenedor de análisis periódico. Estados controlados: `BORRADOR`, `VALIDADO`, `CON_ERRORES`, `ANALIZADO`.
- **`reporte_recurso`:** Parámetros operativos concretos en el periodo evaluado (turnos, horas/turno, días, demoras).
- **`analisis_recurso`:** Indicadores consolidados post-análisis. Almacena histórico inmutable para gráficos de tendencias y comparativas temporales.
- **`validacion_resultado`:** Errores críticos (impiden analizar) vs Advertencias (alertas de calidad de datos).

---

## 5. Arquitectura del Sistema

### 5.1. Backend: Arquitectura Hexagonal

```mermaid
flowchart LR
    subgraph Infrastructure_In ["Infrastructure (In)"]
        REST["Controladores REST<br/>/api/**"]
        JWT_Filter["Filtro JWT<br/>Spring Security"]
    end

    subgraph Application ["Application Layer"]
        UseCaseIn["Puertos de Entrada<br/>(Use Cases)"]
        AppService["Servicios de Aplicación<br/>Orquestadores"]
        UseCaseOut["Puertos de Salida<br/>(Repositories & Clients)"]
    end

    subgraph Domain ["Domain Layer (Pure Java)"]
        Model["Modelos (Records)"]
        Engine["Motor Analítico<br/>SteelForce"]
        Causal["Detector Causal<br/>de Déficit"]
        Val["Validadores Fase 2"]
    end

    subgraph Infrastructure_Out ["Infrastructure (Out)"]
        JPA["Spring Data JPA<br/>PostgreSQL 16"]
        LLM["GeminiLlmAdapter<br/>Google AI Studio"]
    end

    REST --> UseCaseIn
    JWT_Filter --> REST
    UseCaseIn --> AppService
    AppService --> Model
    AppService --> Engine
    AppService --> Causal
    AppService --> Val
    AppService --> UseCaseOut
    UseCaseOut --> JPA
    UseCaseOut --> LLM
```

### 5.2. Frontend: Angular 21 + Signals

- **Componentes Standalone:** Cero NgModules. Menor tamaño de empaquetado y carga perezosa (`loadComponent`) en todas las rutas.
- **Reactividad con Signals:** Manejo de estado predecible y eficiente con `signal()`, `computed()` y `effect()`.
- **Diseño SaaS con Tailwind CSS 4:** Paleta profesional empresarial, iconografía unificada con Lucide Icons (cero emojis), animaciones fluidas y soporte responsivo completo.
- **Seguridad en Cliente:** `AuthInterceptor` inyecta automáticamente el token Bearer en peticiones salientes y captura errores 401 para expiración de sesión.

---

## 6. Asistente Inteligente SteelForce

El asistente conversacional (`/app/chatbot`) interactúa con el backend en `/api/asistente`:

```mermaid
sequenceDiagram
    autonumber
    actor Usuario
    participant Frontend as Angular (Chatbot)
    participant Service as AsistenteIaService
    participant BD as PostgreSQL
    participant LLM as Google Gemini (gemini-3.8-flash)

    Usuario->>Frontend: Consulta operativa o de reporte
    Frontend->>Service: POST /api/asistente/consultar {mensaje, reporteId, historial}
    Service->>BD: Valida tenant (empresa_id) y rol
    alt Usuario es LIDER
        Service->>BD: recursosDelLider(reporteId, usuarioId)
        Note over Service: Filtra estrictamente y excluye estaciones ajenas
    end
    alt Gemini Configurado y Disponible
        Service->>LLM: POST /chat/completions (x-goog-api-key, temp: 0.25)
        LLM-->>Service: Respuesta técnica sin LaTeX
        Service-->>Frontend: {motor: 'GEMINI', respuesta: '...'}
    else Falla de API (404, 429, timeout) o sin clave
        Note over Service: Conmutación silenciosa de contingencia
        Service-->>Frontend: {motor: 'LOCAL', respuesta: 'Diagnóstico SteelForce local...'}
    end
    Frontend->>Usuario: Muestra respuesta con insignia honesta (Gemini vs Base Local)
```

---

## 7. Matriz de Seguridad y Roles (RBAC)

| Módulo / Recurso | ADMIN | ANALISTA | LIDER |
| :--- | :---: | :---: | :---: |
| Autenticación (`/api/auth/login`) | Público | Público | Público |
| Empresas (`/api/empresas/**`) | Total (CRUD) | Denegado (403) | Denegado (403) |
| Usuarios (`/api/usuarios/**`) | Total (CRUD) | Denegado (403) | Denegado (403) |
| Catálogo de Productos y Procesos | Total | CRUD (Su Empresa) | Solo Lectura |
| Recursos y Estaciones | Total | CRUD (Su Empresa) | Solo Lectura |
| Reportes (Crear / Validar / Analizar) | Total | Total (Su Empresa) | Solo Lectura |
| Asistente SteelForce | Global | Su Empresa | Filtrado a sus Estaciones |

---

## 8. Puesta en Marcha Rápida

### Paso 1: Clonar el Repositorio Monorepo
```bash
git clone https://github.com/JuanGale-20/Indux.git
cd Indux
```

### Paso 2: Configurar Variables de Entorno del Backend
```powershell
Copy-Item .env.example .env
```

### Paso 3: Iniciar PostgreSQL con Docker
```bash
docker compose up -d
```

### Paso 4: Iniciar el Backend (Spring Boot)
En Windows:
```powershell
.\run-backend.cmd
```
O usando Maven Wrapper:
```bash
./mvnw spring-boot:run
```
*El backend compilará e iniciará en `http://localhost:8080` aplicando automáticamente las migraciones Flyway.*

### Paso 5: Iniciar el Frontend (Angular)
En otra terminal:
```bash
cd Indux-Fronted
npm install
npm start
```
*El frontend estará disponible de inmediato en `http://localhost:4200`.*

---

## 9. Variables de Entorno (.env)

| Variable | Descripción | Valor de Ejemplo |
| :--- | :--- | :--- |
| `INDUX_DB_URL` | URL JDBC de conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/indux` |
| `INDUX_DB_USERNAME` | Usuario de base de datos | `indux_user` |
| `INDUX_DB_PASSWORD` | Contraseña de base de datos | `<tu_contrasena>` |
| `INDUX_JWT_SECRET` | Secreto HMAC-SHA256 en Base64 ($\ge 256$ bits) | `<clave_base64>` |
| `INDUX_ADMIN_EMAIL` | Correo del administrador inicial | `admin@indux.com` |
| `INDUX_ADMIN_PASSWORD` | Contraseña del administrador inicial | `<tu_clave_segura>` |
| `INDUX_CORS_ORIGEN` | Origen frontend permitido | `http://localhost:4200` |
| `SWAGGER_ENABLED` | Activar Swagger UI y OpenAPI docs | `true` |
| `GEMINI_API_KEY` | Clave API de Google AI Studio (opcional) | `AIzaSy...` |
| `GEMINI_MODEL` | Modelo de Gemini para el asistente | `gemini-3.8-flash` |
| `GEMINI_BASE_URL` | Endpoint base oficial de Google AI Studio | `https://generativelanguage.googleapis.com/v1beta/openai` |
| `INDUX_LLM_TIMEOUT` | Timeout máximo en segundos | `15` |

---

## 10. Batería de Pruebas Automatizadas

La plataforma cuenta con pruebas unitarias, de integración y de seguridad que garantizan cero regresiones:

- **Backend (178 pruebas pasando al 100%):**
  ```powershell
  .\mvnw.cmd test
  ```
- **Frontend (30 pruebas Vitest pasando al 100%):**
  ```bash
  cd Indux-Fronted
  npm test
  ```
- **Compilación de Producción:**
  ```bash
  cd Indux-Fronted
  npm run build
  ```

---

## 11. Documentación de API y Colección Postman

- **Swagger UI Interactivo:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI v3 JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Colección Postman:** [`Indux_Plataforma_Postman_Collection.json`](./Indux_Plataforma_Postman_Collection.json) incluye todas las peticiones con autenticación automática, variables de entorno y validación de respuestas.

---

## Licencia

Distribuido bajo la Licencia MIT. Consulta el archivo `LICENSE` para más información.