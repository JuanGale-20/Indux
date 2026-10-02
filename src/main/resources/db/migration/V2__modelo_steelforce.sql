-- =====================================================
-- INDUX · Modelo de datos basado en la metodología SteelForce
-- =====================================================

-- Empresa (Fase 1: información de la empresa)
CREATE TABLE empresa (
                         id          BIGSERIAL PRIMARY KEY,
                         nombre      VARCHAR(150) NOT NULL,
                         sector      VARCHAR(80),            -- categórico; si es inválido solo genera advertencia
                         activa      BOOLEAN      NOT NULL DEFAULT TRUE,
                         created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Usuario y rol. empresa_id es nulo para el administrador de la plataforma
CREATE TABLE usuario (
                         id             BIGSERIAL PRIMARY KEY,
                         empresa_id     BIGINT REFERENCES empresa(id),
                         nombre         VARCHAR(120) NOT NULL,
                         email          VARCHAR(150) NOT NULL UNIQUE,
                         password_hash  VARCHAR(255) NOT NULL,
                         rol            VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'LIDER', 'ANALISTA')),
                         activo         BOOLEAN      NOT NULL DEFAULT TRUE,
                         created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_usuario_empresa ON usuario(empresa_id);

-- Producto
CREATE TABLE producto (
                          id              BIGSERIAL PRIMARY KEY,
                          empresa_id      BIGINT       NOT NULL REFERENCES empresa(id),
                          codigo          VARCHAR(40)  NOT NULL,
                          nombre          VARCHAR(150) NOT NULL,
                          unidad_medida   VARCHAR(20)  NOT NULL,     -- kg, ton, unidades...
                          CONSTRAINT uq_producto_codigo UNIQUE (empresa_id, codigo)
);

-- Proceso = etapa de la ruta productiva (une "proceso" y "etapa")
CREATE TABLE proceso (
                         id                BIGSERIAL PRIMARY KEY,
                         producto_id       BIGINT       NOT NULL REFERENCES producto(id),
                         lider_id          BIGINT       REFERENCES usuario(id),
                         codigo            VARCHAR(40)  NOT NULL,
                         nombre            VARCHAR(150) NOT NULL,
                         secuencia         INTEGER      NOT NULL CHECK (secuencia > 0),
                         unidad_procesada  VARCHAR(20)  NOT NULL,
                         CONSTRAINT uq_proceso_codigo    UNIQUE (producto_id, codigo),
                         CONSTRAINT uq_proceso_secuencia UNIQUE (producto_id, secuencia)
);
CREATE INDEX idx_proceso_lider ON proceso(lider_id);

-- Recurso (lo que realmente se analiza). cantidad_disponible = recursos equivalentes
CREATE TABLE recurso (
                         id                   BIGSERIAL PRIMARY KEY,
                         proceso_id           BIGINT       NOT NULL REFERENCES proceso(id),
                         codigo               VARCHAR(40)  NOT NULL,
                         nombre               VARCHAR(150) NOT NULL,
                         tipo                 VARCHAR(30),             -- máquina, línea, estación... (advertencia si es inválido)
                         cantidad_disponible  INTEGER      NOT NULL CHECK (cantidad_disponible > 0),
                         estado               VARCHAR(30)  NOT NULL DEFAULT 'DISPONIBLE',
                         CONSTRAINT uq_recurso_codigo UNIQUE (proceso_id, codigo)
);

-- Reporte del periodo: demanda y producción real del producto
CREATE TABLE reporte (
                         id               BIGSERIAL PRIMARY KEY,
                         producto_id      BIGINT        NOT NULL REFERENCES producto(id),
                         creado_por       BIGINT        NOT NULL REFERENCES usuario(id),
                         planta_area      VARCHAR(120)  NOT NULL,
                         frecuencia       VARCHAR(20)   NOT NULL,      -- diaria, semanal, mensual, anual
                         periodo_inicio   DATE          NOT NULL,
                         periodo_fin      DATE          NOT NULL,
                         demanda          NUMERIC(14,4) NOT NULL CHECK (demanda >= 0),
                         produccion_real  NUMERIC(14,4) NOT NULL CHECK (produccion_real >= 0),
                         estado           VARCHAR(20)   NOT NULL DEFAULT 'BORRADOR'
                             CHECK (estado IN ('BORRADOR', 'VALIDADO', 'CON_ERRORES', 'ANALIZADO')),
                         created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
                         CONSTRAINT ck_reporte_periodo CHECK (periodo_fin >= periodo_inicio)
);
CREATE INDEX idx_reporte_producto ON reporte(producto_id, periodo_inicio);

-- Datos de entrada por recurso en cada reporte (tiempos y capacidad)
CREATE TABLE reporte_recurso (
                                 id                      BIGSERIAL PRIMARY KEY,
                                 reporte_id              BIGINT        NOT NULL REFERENCES reporte(id) ON DELETE CASCADE,
                                 recurso_id              BIGINT        NOT NULL REFERENCES recurso(id),
                                 tiempo_procesamiento    NUMERIC(12,6) NOT NULL CHECK (tiempo_procesamiento > 0),  -- h/unidad
                                 unidad_referencia       VARCHAR(20)   NOT NULL,
                                 tiempo_calendario       NUMERIC(12,4) NOT NULL CHECK (tiempo_calendario >= 0),    -- h
                                 tiempo_no_disponible    NUMERIC(12,4) CHECK (tiempo_no_disponible >= 0),          -- h
                                 numero_turnos           INTEGER       NOT NULL CHECK (numero_turnos > 0),
                                 horas_por_turno         NUMERIC(5,2)  NOT NULL CHECK (horas_por_turno > 0 AND horas_por_turno <= 24),
                                 dias_operacion          INTEGER       NOT NULL CHECK (dias_operacion > 0),
                                 CONSTRAINT uq_reporte_recurso UNIQUE (reporte_id, recurso_id)
);

-- Fase 2: resultado de la validación (error crítico, advertencia)
CREATE TABLE validacion_resultado (
                                      id          BIGSERIAL PRIMARY KEY,
                                      reporte_id  BIGINT       NOT NULL REFERENCES reporte(id) ON DELETE CASCADE,
                                      grupo       VARCHAR(30)  NOT NULL,
                                      variable    VARCHAR(60)  NOT NULL,
                                      severidad   VARCHAR(20)  NOT NULL CHECK (severidad IN ('ERROR_CRITICO', 'ADVERTENCIA')),
                                      mensaje     VARCHAR(300) NOT NULL
);
CREATE INDEX idx_validacion_reporte ON validacion_resultado(reporte_id);

-- Fases 4 a 6: indicadores calculados, restricción y diagnóstico (histórico)
CREATE TABLE analisis_recurso (
                                  id                  BIGSERIAL PRIMARY KEY,
                                  reporte_recurso_id  BIGINT        NOT NULL UNIQUE REFERENCES reporte_recurso(id) ON DELETE CASCADE,
                                  horas_requeridas    NUMERIC(14,4) NOT NULL,   -- demanda × tiempo de procesamiento
                                  horas_disponibles   NUMERIC(14,4) NOT NULL,   -- turnos × horas × días × cantidad de recursos
                                  capacidad           NUMERIC(14,4) NOT NULL,   -- horas disponibles ÷ tiempo de procesamiento
                                  utilizacion         NUMERIC(8,2)  NOT NULL,   -- horas requeridas ÷ horas disponibles × 100
                                  holgura             NUMERIC(14,4) NOT NULL,   -- horas disponibles − horas requeridas
                                  deficit_capacidad   NUMERIC(14,4) NOT NULL DEFAULT 0,
                                  clasificacion       VARCHAR(30)   NOT NULL
                                      CHECK (clasificacion IN ('SIN_RESTRICCION', 'POSIBLE_RESTRICCION', 'RESTRICCION_CAPACIDAD')),
                                  prioridad           INTEGER,                  -- orden entre restricciones del mismo reporte
                                  diagnostico         TEXT,
                                  calculado_en        TIMESTAMPTZ   NOT NULL DEFAULT now()
);