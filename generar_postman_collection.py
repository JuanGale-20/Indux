import json
import os

collection = {
    "info": {
        "_postman_id": "c1f7b8e3-824a-4d2a-bc91-1de0a71a0001",
        "name": "Indux Plataforma - API Completa (SteelForce)",
        "description": "Colección completa de pruebas para todos los módulos de Indux Plataforma:\n\n1. **Autenticación y Roles:** Login por rol (ADMIN, ANALISTA, LIDER) guardando tokens en variables de colección.\n2. **Catálogos y Entidades:** CRUD de Empresas, Usuarios, Productos, Procesos con merma y Recursos.\n3. **Vínculos Proceso-Recurso:** Tiempos de referencia por proceso.\n4. **Reportes:** Creación de reportes con líneas, actualización a BORRADOR, filtrado de líneas para rol LIDER.\n5. **Validación y Análisis Persistido:** Detección de errores críticos vs advertencias, análisis persistido, control numérico de recurso compartido (360h requeridas, 112.5% utilización, SOBRECARGA, prioridad 1).\n6. **Recomendaciones y Causas:** Planes de acción por nivel de severidad y desglose cuantitativo de factores causantes.\n7. **Histórico y Tendencia:** Consulta de puntos depurados y tendencia de utilización.\n8. **Importación:** Plantillas y endpoints multipart para Excel y CSV.\n9. **Análisis al Vuelo:** Endpoint de cálculo instantáneo.\n10. **Casos Negativos y Seguridad:** Pruebas 403 (LIDER creando reporte), 422 (análisis con error crítico), 409 (reporte no analizado), 400 (JSON/enum inválido y parámetro inválido) y 401 (usuario desactivado).\n\nDiseñada para ejecutarse de principio a fin con el **Collection Runner** de Postman con asserts automáticos en cada petición.",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    "variable": [
        {"key": "baseUrl", "value": "http://localhost:8080", "type": "string"},
        {"key": "adminEmail", "value": "admin@indux.com", "type": "string"},
        {"key": "adminPassword", "value": "reemplaza_por_una_clave_segura", "type": "string"},
        {"key": "adminToken", "value": "", "type": "string"},
        {"key": "analistaEmail", "value": "analista@aceros.com", "type": "string"},
        {"key": "analistaPassword", "value": "Password123!", "type": "string"},
        {"key": "analistaToken", "value": "", "type": "string"},
        {"key": "liderEmail", "value": "lider@aceros.com", "type": "string"},
        {"key": "liderPassword", "value": "Password123!", "type": "string"},
        {"key": "liderToken", "value": "", "type": "string"},
        {"key": "empresaId", "value": "", "type": "string"},
        {"key": "empresaAjenaId", "value": "", "type": "string"},
        {"key": "analistaId", "value": "", "type": "string"},
        {"key": "liderId", "value": "", "type": "string"},
        {"key": "usuarioDesactivarEmail", "value": "temporal@aceros.com", "type": "string"},
        {"key": "usuarioDesactivarId", "value": "", "type": "string"},
        {"key": "usuarioDesactivarToken", "value": "", "type": "string"},
        {"key": "productoId", "value": "", "type": "string"},
        {"key": "producto2Id", "value": "", "type": "string"},
        {"key": "proceso1Id", "value": "", "type": "string"},
        {"key": "proceso2Id", "value": "", "type": "string"},
        {"key": "procesoP2Id", "value": "", "type": "string"},
        {"key": "recurso1Id", "value": "", "type": "string"},
        {"key": "recurso2Id", "value": "", "type": "string"},
        {"key": "recurso3Id", "value": "", "type": "string"},
        {"key": "reporteId", "value": "", "type": "string"},
        {"key": "reporte2Id", "value": "", "type": "string"},
        {"key": "reporteBorradorId", "value": "", "type": "string"},
        {"key": "reporteInvalidoId", "value": "", "type": "string"}
    ],
    "item": []
}

def crear_req(name, method, url_path, auth_token=None, body=None, tests=None, description=None, is_form_data=False, prerequest=None):
    headers = []
    if not is_form_data:
        headers.append({"key": "Content-Type", "value": "application/json"})
    if auth_token:
        headers.append({"key": "Authorization", "value": f"Bearer {auth_token}"})

    r = {
        "name": name,
        "request": {
            "method": method,
            "header": headers,
            "url": {
                "raw": "{{baseUrl}}" + url_path,
                "host": ["{{baseUrl}}"],
                "path": [p for p in url_path.split("?")[0].split("/") if p]
            }
        }
    }

    if description:
        r["request"]["description"] = description

    if "?" in url_path:
        query_str = url_path.split("?")[1]
        queries = []
        for pair in query_str.split("&"):
            if "=" in pair:
                k, v = pair.split("=", 1)
                queries.append({"key": k, "value": v})
            else:
                queries.append({"key": pair, "value": ""})
        r["request"]["url"]["query"] = queries

    if is_form_data:
        r["request"]["body"] = {
            "mode": "formdata",
            "formdata": [
                {
                    "key": "archivo",
                    "type": "file",
                    "src": "",
                    "description": "[MANUAL] Selecciona aquí el archivo .xlsx o .csv antes de ejecutar"
                }
            ]
        }
    elif body is not None:
        r["request"]["body"] = {
            "mode": "raw",
            "raw": json.dumps(body, indent=2) if isinstance(body, (dict, list)) else str(body),
            "options": {"raw": {"language": "json"}}
        }

    events = []
    if prerequest:
        events.append({
            "listen": "prerequest",
            "script": {
                "exec": prerequest,
                "type": "text/javascript"
            }
        })
    if tests:
        events.append({
            "listen": "test",
            "script": {
                "exec": tests,
                "type": "text/javascript"
            }
        })
    if events:
        r["event"] = events

    return r

# ==============================================================================
# FOLDER 01: AUTENTICACIÓN INICIAL (ADMIN)
# ==============================================================================
f01 = {
    "name": "01 - Autenticación Inicial (ADMIN)",
    "item": [
        crear_req(
            "1.1 Login ADMIN",
            "POST",
            "/api/auth/login",
            prerequest=[
                "var ts = Date.now();",
                "pm.collectionVariables.set('analistaEmail', 'analista_' + ts + '@aceros.com');",
                "pm.collectionVariables.set('liderEmail', 'lider_' + ts + '@aceros.com');",
                "pm.collectionVariables.set('usuarioDesactivarEmail', 'temporal_' + ts + '@aceros.com');"
            ],
            auth_token=None,
            body={"email": "{{adminEmail}}", "password": "{{adminPassword}}"},
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Respuesta contiene token JWT', function () { pm.expect(json.token).to.not.be.empty; });",
                "pm.test('Rol del usuario es ADMIN', function () { pm.expect(json.usuario.rol).to.eql('ADMIN'); });",
                "pm.collectionVariables.set('adminToken', json.token);"
            ],
            description="Inicia sesión con el administrador del sistema y almacena 'adminToken' para las peticiones administrativas."
        )
    ]
}

# ==============================================================================
# FOLDER 02: GESTIÓN DE EMPRESAS
# ==============================================================================
f02 = {
    "name": "02 - Empresas",
    "item": [
        crear_req(
            "2.1 Crear Empresa Principal (ADMIN)",
            "POST",
            "/api/empresas",
            auth_token="{{adminToken}}",
            body={"nombre": "Aceros del Norte S.A.S.", "sector": "Metalmecánica"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.test('Empresa creada correctamente', function () { pm.expect(json.nombre).to.include('Aceros'); });",
                "pm.collectionVariables.set('empresaId', json.id.toString());"
            ]
        ),
        crear_req(
            "2.2 Listar Empresas (ADMIN)",
            "GET",
            "/api/empresas",
            auth_token="{{adminToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Lista contiene al menos una empresa', function () { pm.expect(json.length).to.be.above(0); });"
            ]
        ),
        crear_req(
            "2.3 Obtener Empresa por ID (ADMIN)",
            "GET",
            "/api/empresas/{{empresaId}}",
            auth_token="{{adminToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "pm.test('ID coincide con variable', function () { pm.expect(pm.response.json().id).to.eql(parseInt(pm.collectionVariables.get('empresaId'))); });"
            ]
        ),
        crear_req(
            "2.4 Actualizar Empresa (ADMIN)",
            "PUT",
            "/api/empresas/{{empresaId}}",
            auth_token="{{adminToken}}",
            body={"nombre": "Aceros del Norte S.A.S. - Planta Central", "sector": "Metalmecánica"},
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "pm.test('Nombre actualizado', function () { pm.expect(pm.response.json().nombre).to.include('Planta Central'); });"
            ]
        ),
        crear_req(
            "2.5 Crear Empresa Ajena (para pruebas de aislamiento)",
            "POST",
            "/api/empresas",
            auth_token="{{adminToken}}",
            body={"nombre": "Textiles de Colombia S.A.", "sector": "Textil"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('empresaAjenaId', json.id.toString());"
            ]
        )
    ]
}

# ==============================================================================
# FOLDER 03: GESTIÓN DE USUARIOS Y LOGIN POR ROLES
# ==============================================================================
f03 = {
    "name": "03 - Usuarios y Autenticación por Rol",
    "item": [
        crear_req(
            "3.1 Registrar Usuario ANALISTA (ADMIN)",
            "POST",
            "/api/usuarios",
            auth_token="{{adminToken}}",
            body={"empresaId": "{{empresaId}}", "nombre": "Carlos Gómez", "email": "{{analistaEmail}}", "password": "{{analistaPassword}}", "rol": "ANALISTA"},
            tests=[
                "pm.test('Status code es 201 o 409', function () { pm.expect(pm.response.code).to.be.oneOf([201, 409]); });",
                "if (pm.response.code === 201) { pm.collectionVariables.set('analistaId', pm.response.json().id.toString()); }"
            ]
        ),
        crear_req(
            "3.2 Registrar Usuario LIDER (ADMIN)",
            "POST",
            "/api/usuarios",
            auth_token="{{adminToken}}",
            body={"empresaId": "{{empresaId}}", "nombre": "Laura Méndez", "email": "{{liderEmail}}", "password": "{{liderPassword}}", "rol": "LIDER"},
            tests=[
                "pm.test('Status code es 201 o 409', function () { pm.expect(pm.response.code).to.be.oneOf([201, 409]); });",
                "if (pm.response.code === 201) { pm.collectionVariables.set('liderId', pm.response.json().id.toString()); }"
            ]
        ),
        crear_req(
            "3.3 Registrar Usuario Temporal para Desactivación (ADMIN)",
            "POST",
            "/api/usuarios",
            auth_token="{{adminToken}}",
            body={"empresaId": "{{empresaId}}", "nombre": "Usuario Temporal", "email": "{{usuarioDesactivarEmail}}", "password": "Password123!", "rol": "ANALISTA"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('usuarioDesactivarId', json.id.toString());"
            ]
        ),
        crear_req(
            "3.4 Listar Usuarios (ADMIN)",
            "GET",
            "/api/usuarios",
            auth_token="{{adminToken}}",
            tests=["pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });"]
        ),
        crear_req(
            "3.5 Actualizar Usuario (ADMIN)",
            "PUT",
            "/api/usuarios/{{analistaId}}",
            auth_token="{{adminToken}}",
            body={"empresaId": "{{empresaId}}", "nombre": "Carlos Gómez R.", "email": "{{analistaEmail}}", "password": None, "rol": "ANALISTA"},
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "pm.test('Nombre actualizado', function () { pm.expect(pm.response.json().nombre).to.eql('Carlos Gómez R.'); });"
            ]
        ),
        crear_req(
            "3.6 Login ANALISTA (guarda analistaToken)",
            "POST",
            "/api/auth/login",
            auth_token=None,
            body={"email": "{{analistaEmail}}", "password": "{{analistaPassword}}"},
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Rol es ANALISTA', function () { pm.expect(json.usuario.rol).to.eql('ANALISTA'); });",
                "pm.collectionVariables.set('analistaToken', json.token);"
            ]
        ),
        crear_req(
            "3.7 Login LIDER (guarda liderToken)",
            "POST",
            "/api/auth/login",
            auth_token=None,
            body={"email": "{{liderEmail}}", "password": "{{liderPassword}}"},
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Rol es LIDER', function () { pm.expect(json.usuario.rol).to.eql('LIDER'); });",
                "pm.collectionVariables.set('liderToken', json.token);"
            ]
        )
    ]
}

# ==============================================================================
# FOLDER 04: CATÁLOGO: PRODUCTOS Y PROCESOS
# ==============================================================================
f04 = {
    "name": "04 - Catálogo: Productos y Procesos",
    "item": [
        crear_req(
            "4.1 Crear Producto 1 (Eje de Transmisión)",
            "POST",
            "/api/productos",
            auth_token="{{adminToken}}",
            body={"empresaId": "{{empresaId}}", "codigo": "PROD-01", "nombre": "Eje de Transmisión 50mm", "unidadMedida": "piezas"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('productoId', json.id.toString());"
            ]
        ),
        crear_req(
            "4.2 Crear Producto 2 (Piñón de Acero - Comparte Recurso)",
            "POST",
            "/api/productos",
            auth_token="{{adminToken}}",
            body={"empresaId": "{{empresaId}}", "codigo": "PROD-02", "nombre": "Piñón de Acero Templado", "unidadMedida": "piezas"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('producto2Id', json.id.toString());"
            ]
        ),
        crear_req(
            "4.3 Listar Productos por Empresa",
            "GET",
            "/api/productos?empresaId={{empresaId}}",
            auth_token="{{analistaToken}}",
            tests=["pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });"]
        ),
        crear_req(
            "4.4 Crear Proceso 1 Producto 1 - Corte (Merma 5%)",
            "POST",
            "/api/procesos",
            auth_token="{{adminToken}}",
            body={"productoId": "{{productoId}}", "liderId": None, "codigo": "CORTE", "nombre": "Corte de Barra", "secuencia": 1, "unidadProcesada": "piezas", "merma": 5.0, "unidadEntrada": "piezas"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('proceso1Id', json.id.toString());"
            ]
        ),
        crear_req(
            "4.5 Crear Proceso 2 Producto 1 - Torneado (Asignado a LIDER, Merma 0%)",
            "POST",
            "/api/procesos",
            auth_token="{{adminToken}}",
            body={"productoId": "{{productoId}}", "liderId": "{{liderId}}", "codigo": "TORNO", "nombre": "Torneado CNC", "secuencia": 2, "unidadProcesada": "piezas", "merma": 0.0, "unidadEntrada": "piezas"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('proceso2Id', json.id.toString());"
            ]
        ),
        crear_req(
            "4.6 Crear Proceso Producto 2 - Torneado Piñón (Merma 0%)",
            "POST",
            "/api/procesos",
            auth_token="{{adminToken}}",
            body={"productoId": "{{producto2Id}}", "liderId": None, "codigo": "TORNO-P2", "nombre": "Torneado de Piñón", "secuencia": 1, "unidadProcesada": "piezas", "merma": 0.0, "unidadEntrada": "piezas"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('procesoP2Id', json.id.toString());"
            ]
        ),
        crear_req(
            "4.7 Listar Procesos Ordenados de Producto 1",
            "GET",
            "/api/productos/{{productoId}}/procesos",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Procesos ordenados por secuencia', function () { pm.expect(json[0].secuencia).to.be.below(json[1].secuencia); });"
            ]
        )
    ]
}

# ==============================================================================
# FOLDER 05: RECURSOS Y VÍNCULOS
# ==============================================================================
f05 = {
    "name": "05 - Recursos y Vínculos",
    "item": [
        crear_req(
            "5.1 Crear Recurso 1 (Torno CNC Haas - Cuello Compartido: 1 maquina)",
            "POST",
            "/api/recursos",
            auth_token="{{adminToken}}",
            body={"empresaId": "{{empresaId}}", "codigo": "REC-01", "nombre": "Torno CNC Haas ST-20", "tipo": "MÁQUINA", "cantidadDisponible": 1, "estado": "DISPONIBLE"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('recurso1Id', json.id.toString());"
            ]
        ),
        crear_req(
            "5.2 Crear Recurso 2 (Sierra Cinta Automática)",
            "POST",
            "/api/recursos",
            auth_token="{{adminToken}}",
            body={"empresaId": "{{empresaId}}", "codigo": "REC-02", "nombre": "Sierra Cinta Automática", "tipo": "MÁQUINA", "cantidadDisponible": 1, "estado": "DISPONIBLE"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('recurso2Id', json.id.toString());"
            ]
        ),
        crear_req(
            "5.3 Crear Recurso 3 (Prensa Hidráulica - 2 maquinas)",
            "POST",
            "/api/recursos",
            auth_token="{{adminToken}}",
            body={"empresaId": "{{empresaId}}", "codigo": "REC-03", "nombre": "Prensa Hidráulica 100T", "tipo": "MÁQUINA", "cantidadDisponible": 2, "estado": "DISPONIBLE"},
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('recurso3Id', json.id.toString());"
            ]
        ),
        crear_req(
            "5.4 Listar Recursos de la Empresa",
            "GET",
            "/api/recursos?empresaId={{empresaId}}",
            auth_token="{{analistaToken}}",
            tests=["pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });"]
        ),
        crear_req(
            "5.5 Vincular Recurso 1 a Proceso 2 (Torno -> Torneado Eje, tiempoRef 0.20)",
            "POST",
            "/api/recursos/{{recurso1Id}}/procesos",
            auth_token="{{adminToken}}",
            body={"procesoId": "{{proceso2Id}}", "tiempoReferencia": 0.20},
            tests=["pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });"]
        ),
        crear_req(
            "5.6 Vincular Recurso 1 a Proceso Producto 2 (Torno -> Torneado Piñón, tiempoRef 0.20)",
            "POST",
            "/api/recursos/{{recurso1Id}}/procesos",
            auth_token="{{adminToken}}",
            body={"procesoId": "{{procesoP2Id}}", "tiempoReferencia": 0.20},
            tests=["pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });"]
        ),
        crear_req(
            "5.7 Vincular Recurso 2 a Proceso 1 (Sierra -> Corte, tiempoRef 0.15)",
            "POST",
            "/api/recursos/{{recurso2Id}}/procesos",
            auth_token="{{adminToken}}",
            body={"procesoId": "{{proceso1Id}}", "tiempoReferencia": 0.15},
            tests=["pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });"]
        )
    ]
}

# ==============================================================================
# FOLDER 06: REPORTES DE PRODUCCIÓN
# ==============================================================================
f06 = {
    "name": "06 - Reportes de Producción",
    "item": [
        crear_req(
            "6.1 Crear Reporte Producto 2 (Demanda 800, tiempo 0.2 -> 160h)",
            "POST",
            "/api/reportes",
            auth_token="{{analistaToken}}",
            body={
                "productoId": "{{producto2Id}}",
                "plantaArea": "Planta Metalmecánica",
                "frecuencia": "MENSUAL",
                "periodoInicio": "2026-01-01",
                "periodoFin": "2026-01-31",
                "demanda": 800.0,
                "produccionReal": 800.0,
                "lineas": [
                    {
                        "recursoId": "{{recurso1Id}}",
                        "tiempoProcesamiento": 0.20,
                        "unidadReferencia": "piezas",
                        "tiempoCalendario": 720.0,
                        "tiempoNoDisponible": 0.0,
                        "numeroTurnos": 2,
                        "horasPorTurno": 8.0,
                        "diasOperacion": 20
                    }
                ]
            },
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('reporte2Id', json.id.toString());"
            ]
        ),
        crear_req(
            "6.2 Crear Reporte Producto 1 (Demanda 1000, tiempo 0.2 -> 200h)",
            "POST",
            "/api/reportes",
            auth_token="{{analistaToken}}",
            body={
                "productoId": "{{productoId}}",
                "plantaArea": "Planta Metalmecánica",
                "frecuencia": "MENSUAL",
                "periodoInicio": "2026-01-01",
                "periodoFin": "2026-01-31",
                "demanda": 1000.0,
                "produccionReal": 950.0,
                "lineas": [
                    {
                        "recursoId": "{{recurso1Id}}",
                        "tiempoProcesamiento": 0.20,
                        "unidadReferencia": "piezas",
                        "tiempoCalendario": 720.0,
                        "tiempoNoDisponible": 0.0,
                        "numeroTurnos": 2,
                        "horasPorTurno": 8.0,
                        "diasOperacion": 20
                    },
                    {
                        "recursoId": "{{recurso2Id}}",
                        "tiempoProcesamiento": 0.15,
                        "unidadReferencia": "piezas",
                        "tiempoCalendario": 720.0,
                        "tiempoNoDisponible": 0.0,
                        "numeroTurnos": 2,
                        "horasPorTurno": 8.0,
                        "diasOperacion": 20
                    }
                ]
            },
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('reporteId', json.id.toString());"
            ]
        ),
        crear_req(
            "6.3 Obtener Reporte 1 Completo (ANALISTA)",
            "GET",
            "/api/reportes/{{reporteId}}",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Analista visualiza las 2 lineas', function () { pm.expect(json.lineas.length).to.eql(2); });"
            ]
        ),
        crear_req(
            "6.4 Obtener Reporte 1 Filtrado (LIDER solo ve Recurso 1)",
            "GET",
            "/api/reportes/{{reporteId}}",
            auth_token="{{liderToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var lineas = pm.response.json().lineas;",
                "pm.test('Lider solo ve Recurso 1 de su proceso asignado', function () {",
                "    pm.expect(lineas.length).to.eql(1);",
                "    pm.expect(lineas[0].recursoId).to.eql(parseInt(pm.collectionVariables.get('recurso1Id')));",
                "});"
            ]
        )
    ]
}

# ==============================================================================
# FOLDER 07: VALIDACIÓN Y ANÁLISIS PERSISTIDO CON CONTROL NUMÉRICO
# ==============================================================================
f07 = {
    "name": "07 - Validación y Análisis Persistido",
    "item": [
        crear_req(
            "7.1 Validar Reporte Producto 2 (Pasa a VALIDADO)",
            "POST",
            "/api/reportes/{{reporte2Id}}/validar",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "pm.test('Estado queda VALIDADO', function () { pm.expect(pm.response.json().estado).to.eql('VALIDADO'); });",
                "pm.test('Sin errores criticos', function () { pm.expect(pm.response.json().errores).to.eql(0); });"
            ]
        ),
        crear_req(
            "7.2 Analizar Reporte Producto 2 (Pasa a ANALIZADO)",
            "POST",
            "/api/reportes/{{reporte2Id}}/analizar",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "pm.test('Estado queda ANALIZADO', function () { pm.expect(pm.response.json().estado).to.eql('ANALIZADO'); });"
            ]
        ),
        crear_req(
            "7.3 Validar Reporte Producto 1 (Pasa a VALIDADO)",
            "POST",
            "/api/reportes/{{reporteId}}/validar",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "pm.test('Estado queda VALIDADO', function () { pm.expect(pm.response.json().estado).to.eql('VALIDADO'); });"
            ]
        ),
        crear_req(
            "7.4 Consultar Validaciones Guardadas de Reporte 1",
            "GET",
            "/api/reportes/{{reporteId}}/validaciones",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "pm.test('Retorna lista de resultados', function () { pm.expect(pm.response.json()).to.be.an('array'); });"
            ]
        ),
        crear_req(
            "7.5 Analizar Reporte Producto 1 (Control Numérico Exacto SteelForce)",
            "POST",
            "/api/reportes/{{reporteId}}/analizar",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Reporte queda ANALIZADO', function () { pm.expect(json.estado).to.eql('ANALIZADO'); });",
                "var r1 = json.recursos.find(r => r.recursoId == pm.collectionVariables.get('recurso1Id'));",
                "pm.test('Recurso compartido encontrado', function () { pm.expect(r1).to.exist; });",
                "pm.test('CONTROL NUMÉRICO: horasRequeridas == 360', function () { pm.expect(Number(r1.horasRequeridas)).to.eql(360); });",
                "pm.test('CONTROL NUMÉRICO: horasDisponibles == 320', function () { pm.expect(Number(r1.horasDisponibles)).to.eql(320); });",
                "pm.test('CONTROL NUMÉRICO: utilizacion == 112.50 %', function () { pm.expect(Number(r1.utilizacion)).to.eql(112.5); });",
                "pm.test('CONTROL NUMÉRICO: clasificacion == SOBRECARGA', function () { pm.expect(r1.clasificacion).to.eql('SOBRECARGA'); });",
                "pm.test('CONTROL NUMÉRICO: deficitHoras == 40', function () { pm.expect(Number(r1.deficitHoras)).to.eql(40); });",
                "pm.test('CONTROL NUMÉRICO: impacto == 11.11 %', function () { pm.expect(Number(r1.impacto)).to.eql(11.11); });",
                "pm.test('CONTROL NUMÉRICO: prioridad == 1', function () { pm.expect(r1.prioridad).to.eql(1); });",
                "pm.test('Diagnostico menciona compartido', function () { pm.expect(r1.diagnostico).to.include('compartido'); });"
            ],
            description="Ejecuta el análisis persistido y comprueba los cálculos numéricos de SteelForce: aporte propio 200h + aporte compartido 160h = 360h totales sobre 320h disponibles -> 112.5% de utilización, sobrecarga y prioridad 1."
        ),
        crear_req(
            "7.6 Consultar Análisis Guardado (ANALISTA ve todos)",
            "GET",
            "/api/reportes/{{reporteId}}/analisis",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "pm.test('Analista ve todos los recursos analizados', function () { pm.expect(pm.response.json().recursos.length).to.eql(2); });"
            ]
        ),
        crear_req(
            "7.7 Consultar Análisis Guardado Filtrado (LIDER solo ve Recurso 1)",
            "GET",
            "/api/reportes/{{reporteId}}/analisis",
            auth_token="{{liderToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var recursos = pm.response.json().recursos;",
                "pm.test('Lider solo ve su recurso asignado', function () {",
                "    pm.expect(recursos.length).to.eql(1);",
                "    pm.expect(recursos[0].recursoId).to.eql(parseInt(pm.collectionVariables.get('recurso1Id')));",
                "});"
            ]
        )
    ]
}

# ==============================================================================
# FOLDER 08: RECOMENDACIONES OPERATIVAS
# ==============================================================================
f08 = {
    "name": "08 - Recomendaciones Operativas",
    "item": [
        crear_req(
            "8.1 Obtener Recomendaciones de Reporte 1 (ANALISTA)",
            "GET",
            "/api/reportes/{{reporteId}}/recomendaciones",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "var r1 = json.find(r => r.recursoId == pm.collectionVariables.get('recurso1Id'));",
                "pm.test('Recomendacion CRITICA para restriccion principal', function () {",
                "    pm.expect(r1.nivel).to.eql('CRITICA');",
                "    pm.expect(r1.prioridad).to.eql(1);",
                "    pm.expect(r1.resumen).to.include('Sobrecarga');",
                "    pm.expect(r1.acciones.some(a => a.includes('restricción principal'))).to.be.true;",
                "    pm.expect(r1.acciones.some(a => a.includes('comparten este recurso'))).to.be.true;",
                "});"
            ]
        ),
        crear_req(
            "8.2 Obtener Recomendaciones Filtradas (LIDER)",
            "GET",
            "/api/reportes/{{reporteId}}/recomendaciones",
            auth_token="{{liderToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Lider solo ve su recurso', function () {",
                "    pm.expect(json.length).to.eql(1);",
                "    pm.expect(json[0].recursoId).to.eql(parseInt(pm.collectionVariables.get('recurso1Id')));",
                "});"
            ]
        )
    ]
}

# ==============================================================================
# FOLDER 09: CAUSAS DEL DÉFICIT
# ==============================================================================
f09 = {
    "name": "09 - Causas del Déficit",
    "item": [
        crear_req(
            "9.1 Desglose de Factores del Déficit (ANALISTA)",
            "GET",
            "/api/reportes/{{reporteId}}/causas",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "var r1 = json.find(c => c.recursoId == pm.collectionVariables.get('recurso1Id'));",
                "pm.test('Recurso 1 tiene desglose de factores', function () {",
                "    pm.expect(r1).to.exist;",
                "    pm.expect(Number(r1.deficitHoras)).to.eql(40);",
                "    pm.expect(r1.causaPrincipal).to.eql('RECURSO_COMPARTIDO');",
                "    pm.expect(r1.factores.length).to.be.above(0);",
                "});"
            ]
        ),
        crear_req(
            "9.2 Desglose de Factores Filtrado (LIDER)",
            "GET",
            "/api/reportes/{{reporteId}}/causas",
            auth_token="{{liderToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Lider solo ve causas de sus recursos', function () {",
                "    pm.expect(json.every(c => c.recursoId == pm.collectionVariables.get('recurso1Id'))).to.be.true;",
                "});"
            ]
        )
    ]
}

# ==============================================================================
# FOLDER 10: HISTÓRICO Y TENDENCIAS
# ==============================================================================
f10 = {
    "name": "10 - Histórico y Tendencias",
    "item": [
        crear_req(
            "10.1 Consultar Histórico y Tendencia de Utilización (Recurso 1)",
            "GET",
            "/api/recursos/{{recurso1Id}}/historico?limite=12",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Recurso ID coincide', function () { pm.expect(json.recursoId).to.eql(parseInt(pm.collectionVariables.get('recurso1Id'))); });",
                "pm.test('Puntos historicos presentes', function () { pm.expect(json.puntos).to.be.an('array'); });",
                "pm.test('Tendencia calculada', function () { pm.expect(json.tendencia).to.be.an('object'); });"
            ]
        )
    ]
}

# ==============================================================================
# FOLDER 11: IMPORTACIÓN MASIVA
# ==============================================================================
f11 = {
    "name": "11 - Importación Masiva (Excel y CSV)",
    "item": [
        crear_req(
            "11.1 Descargar Plantilla Oficial XLSX",
            "GET",
            "/api/importaciones/reportes/plantilla?formato=xlsx",
            auth_token="{{analistaToken}}",
            tests=["pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });"]
        ),
        crear_req(
            "11.2 Descargar Plantilla Oficial CSV",
            "GET",
            "/api/importaciones/reportes/plantilla?formato=csv",
            auth_token="{{analistaToken}}",
            tests=["pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });"]
        ),
        crear_req(
            "11.3 Importar Reportes - Vista Previa (confirmar=false) [REQUIERE ADJUNTAR ARCHIVO]",
            "POST",
            "/api/importaciones/reportes?confirmar=false&empresaId={{empresaId}}",
            auth_token="{{analistaToken}}",
            is_form_data=True,
            tests=[
                "// Nota: Esta prueba devolverá 200 si adjuntaste un archivo válido, o 400 si la ejecutas sin archivo.",
                "pm.test('Petición ejecutada (200 con archivo o 400 sin archivo)', function () {",
                "    pm.expect(pm.response.code).to.be.oneOf([200, 400]);",
                "});"
            ],
            description="[MANUAL EN POSTMAN]\n1. Abre la pestaña 'Body'.\n2. En la clave 'archivo', pasa el cursor sobre el campo Value y selecciona 'File'.\n3. Elige tu archivo .xlsx o .csv generado con la plantilla.\n4. Al enviar con confirmar=false, devuelve la vista previa agrupada sin guardar en base de datos."
        ),
        crear_req(
            "11.4 Importar Reportes - Confirmar Creación (confirmar=true) [REQUIERE ADJUNTAR ARCHIVO]",
            "POST",
            "/api/importaciones/reportes?confirmar=true&empresaId={{empresaId}}",
            auth_token="{{analistaToken}}",
            is_form_data=True,
            tests=[
                "pm.test('Petición ejecutada (200 guardado, 422 con errores, o 400 sin archivo)', function () {",
                "    pm.expect(pm.response.code).to.be.oneOf([200, 422, 400]);",
                "});"
            ],
            description="[MANUAL EN POSTMAN]\n1. Abre la pestaña 'Body'.\n2. En la clave 'archivo', selecciona tu archivo .xlsx o .csv.\n3. Al enviar con confirmar=true, si no hay errores se crean los reportes en BORRADOR dentro de una sola transacción (todo o nada)."
        )
    ]
}

# ==============================================================================
# FOLDER 12: ANÁLISIS AL VUELO
# ==============================================================================
f12 = {
    "name": "12 - Análisis al Vuelo (Instantáneo)",
    "item": [
        crear_req(
            "12.1 Calcular Cuello de Botella al Vuelo",
            "POST",
            "/api/analisis/calcular",
            auth_token="{{analistaToken}}",
            body={
                "demanda": 1000.0,
                "recursos": [
                    {"nombre": "Torno Haas", "tiempoProcesamiento": 0.20, "turnos": 2, "horasPorTurno": 8.0, "diasOperacion": 20, "cantidadRecursos": 1},
                    {"nombre": "Sierra Cinta", "tiempoProcesamiento": 0.10, "turnos": 1, "horasPorTurno": 8.0, "diasOperacion": 20, "cantidadRecursos": 1}
                ]
            },
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "var json = pm.response.json();",
                "pm.test('Identifica cuello de botella', function () { pm.expect(json.recursos).to.not.be.empty; });"
            ]
        )
    ]
}

# ==============================================================================
# FOLDER 13: CASOS NEGATIVOS Y VALIDACIONES DE SEGURIDAD
# ==============================================================================
f13 = {
    "name": "13 - Casos Negativos y Seguridad",
    "item": [
        crear_req(
            "13.1 Crear Reporte en Borrador (para probar 409)",
            "POST",
            "/api/reportes",
            auth_token="{{analistaToken}}",
            body={
                "productoId": "{{productoId}}",
                "plantaArea": "Planta Experimental",
                "frecuencia": "MENSUAL",
                "periodoInicio": "2026-02-01",
                "periodoFin": "2026-02-28",
                "demanda": 500.0,
                "produccionReal": 450.0,
                "lineas": [
                    {
                        "recursoId": "{{recurso2Id}}",
                        "tiempoProcesamiento": 0.15,
                        "unidadReferencia": "piezas",
                        "tiempoCalendario": 720.0,
                        "tiempoNoDisponible": 0.0,
                        "numeroTurnos": 2,
                        "horasPorTurno": 8.0,
                        "diasOperacion": 20
                    }
                ]
            },
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('reporteBorradorId', json.id.toString());"
            ]
        ),
        crear_req(
            "13.2 Obtener Análisis de Reporte NO Analizado (Espera 409 Conflict)",
            "GET",
            "/api/reportes/{{reporteBorradorId}}/analisis",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 409 Conflict', function () { pm.response.to.have.status(409); });",
                "var json = pm.response.json();",
                "pm.test('Mensaje indica que no ha sido analizado', function () { pm.expect(json.detail).to.include('no ha sido analizado'); });"
            ]
        ),
        crear_req(
            "13.3 Crear Reporte con Error Crítico (Turnos*Horas > 24)",
            "POST",
            "/api/reportes",
            auth_token="{{analistaToken}}",
            body={
                "productoId": "{{productoId}}",
                "plantaArea": "Planta Error",
                "frecuencia": "MENSUAL",
                "periodoInicio": "2026-03-01",
                "periodoFin": "2026-03-31",
                "demanda": 500.0,
                "produccionReal": 450.0,
                "lineas": [
                    {
                        "recursoId": "{{recurso1Id}}",
                        "tiempoProcesamiento": 0.20,
                        "unidadReferencia": "piezas",
                        "tiempoCalendario": 720.0,
                        "tiempoNoDisponible": 0.0,
                        "numeroTurnos": 3,
                        "horasPorTurno": 10.0,
                        "diasOperacion": 20
                    }
                ]
            },
            tests=[
                "pm.test('Status code es 201 Created', function () { pm.response.to.have.status(201); });",
                "var json = pm.response.json();",
                "pm.collectionVariables.set('reporteInvalidoId', json.id.toString());"
            ]
        ),
        crear_req(
            "13.4 Analizar Reporte con Errores Críticos (Espera 422 Unprocessable Entity)",
            "POST",
            "/api/reportes/{{reporteInvalidoId}}/analizar",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 422 Unprocessable Entity', function () { pm.response.to.have.status(422); });",
                "var json = pm.response.json();",
                "pm.test('Estado queda CON_ERRORES', function () { pm.expect(json.estado).to.eql('CON_ERRORES'); });",
                "pm.test('Contiene validaciones de severidad ERROR_CRITICO', function () {",
                "    pm.expect(json.validaciones.some(v => v.severidad === 'ERROR_CRITICO')).to.be.true;",
                "});"
            ]
        ),
        crear_req(
            "13.5 Rol LIDER Intentando Crear Reporte (Espera 403 Forbidden)",
            "POST",
            "/api/reportes",
            auth_token="{{liderToken}}",
            body={
                "productoId": "{{productoId}}",
                "plantaArea": "Planta Lider",
                "frecuencia": "MENSUAL",
                "periodoInicio": "2026-01-01",
                "periodoFin": "2026-01-31",
                "demanda": 100.0,
                "produccionReal": 100.0,
                "lineas": []
            },
            tests=[
                "pm.test('Status code es 403 Forbidden', function () { pm.response.to.have.status(403); });",
                "pm.test('ProblemDetail devuelto', function () { pm.expect(pm.response.json().status).to.eql(403); });"
            ]
        ),
        crear_req(
            "13.6 Rol ANALISTA Accediendo a Empresa Ajena (Espera 403 Forbidden)",
            "GET",
            "/api/empresas/{{empresaAjenaId}}",
            auth_token="{{analistaToken}}",
            tests=[
                "pm.test('Status code es 403 Forbidden', function () { pm.response.to.have.status(403); });"
            ]
        ),
        crear_req(
            "13.7 Login con Usuario Temporal para Prueba de Desactivación",
            "POST",
            "/api/auth/login",
            auth_token=None,
            body={"email": "{{usuarioDesactivarEmail}}", "password": "Password123!"},
            tests=[
                "pm.test('Status code es 200 OK', function () { pm.response.to.have.status(200); });",
                "pm.collectionVariables.set('usuarioDesactivarToken', pm.response.json().token);"
            ]
        ),
        crear_req(
            "13.8 Desactivar Usuario Temporal (ADMIN)",
            "DELETE",
            "/api/usuarios/{{usuarioDesactivarId}}",
            auth_token="{{adminToken}}",
            tests=[
                "pm.test('Status code es 204 No Content', function () { pm.response.to.have.status(204); });"
            ]
        ),
        crear_req(
            "13.9 Petición con Token de Usuario Desactivado (Espera 401 Unauthorized Inmediato)",
            "GET",
            "/api/empresas/{{empresaId}}",
            auth_token="{{usuarioDesactivarToken}}",
            tests=[
                "pm.test('Status code es 401 Unauthorized', function () { pm.response.to.have.status(401); });",
                "var json = pm.response.json();",
                "pm.test('Detalle explica desactivacion', function () { pm.expect(json.detail).to.include('desactivado'); });"
            ]
        ),
        crear_req(
            "13.10 Parámetro de Ruta Inválido tipo alfanumérico en vez de Long (Espera 400 Bad Request)",
            "GET",
            "/api/empresas/abc",
            auth_token="{{adminToken}}",
            tests=[
                "pm.test('Status code es 400 Bad Request', function () { pm.response.to.have.status(400); });",
                "var json = pm.response.json();",
                "pm.test('Titulo indica parámetro inválido', function () { pm.expect(json.title).to.include('Parámetro'); });"
            ]
        ),
        crear_req(
            "13.11 JSON con Enum Inválido (Espera 400 Bad Request)",
            "POST",
            "/api/reportes",
            auth_token="{{analistaToken}}",
            body={
                "productoId": "{{productoId}}",
                "plantaArea": "Planta",
                "frecuencia": "SEMANA_INVALIDA",
                "periodoInicio": "2026-01-01",
                "periodoFin": "2026-01-31",
                "demanda": 100.0,
                "produccionReal": 100.0,
                "lineas": []
            },
            tests=[
                "pm.test('Status code es 400 Bad Request', function () { pm.response.to.have.status(400); });",
                "pm.test('ProblemDetail devuelto', function () { pm.expect(pm.response.json().status).to.eql(400); });"
            ]
        ),
        crear_req(
            "13.12 Método HTTP No Permitido (Espera 405 Method Not Allowed)",
            "POST",
            "/api/usuarios/1",
            auth_token="{{adminToken}}",
            body={},
            tests=[
                "pm.test('Status code es 405 Method Not Allowed', function () { pm.response.to.have.status(405); });"
            ]
        ),
        crear_req(
            "13.13 Ruta Inexistente (Espera 404 Not Found)",
            "GET",
            "/api/ruta-que-no-existe-en-el-sistema",
            auth_token="{{adminToken}}",
            tests=[
                "pm.test('Status code es 404 Not Found', function () { pm.response.to.have.status(404); });"
            ]
        )
    ]
}

collection["item"] = [
    f01,
    f02,
    f03,
    f04,
    f05,
    f06,
    f07,
    f08,
    f09,
    f10,
    f11,
    f12,
    f13
]

output_path = os.path.join(os.getcwd(), "Indux_Plataforma_Postman_Collection.json")
with open(output_path, "w", encoding="utf-8") as f:
    json.dump(collection, f, indent=2, ensure_ascii=False)

print(f"Postman collection successfully generated at: {output_path}")
