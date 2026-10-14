-- =====================================================
-- V3 · Bloque B1: recurso compartido, unidades, merma y análisis ampliado
-- =====================================================

-- 1. Catálogo de unidades de medida y sus equivalencias
CREATE TABLE unidad_medida (
                               codigo    VARCHAR(20) PRIMARY KEY,
                               nombre    VARCHAR(60) NOT NULL,
                               magnitud  VARCHAR(20) NOT NULL CHECK (magnitud IN ('MASA', 'CANTIDAD', 'TIEMPO', 'LOTE'))
);

INSERT INTO unidad_medida (codigo, nombre, magnitud) VALUES
                                                         ('kg',       'Kilogramo', 'MASA'),
                                                         ('ton',      'Tonelada',  'MASA'),
                                                         ('unidades', 'Unidades',  'CANTIDAD'),
                                                         ('piezas',   'Piezas',    'CANTIDAD'),
                                                         ('lotes',    'Lotes',     'LOTE'),
                                                         ('horas',    'Horas',     'TIEMPO'),
                                                         ('minutos',  'Minutos',   'TIEMPO'),
                                                         ('segundos', 'Segundos',  'TIEMPO');

-- 1 unidad_origen = factor unidad_destino. La conversión inversa se calcula en código.
-- empresa_id y producto_id nulos = equivalencia global.
-- Un lote exige una equivalencia propia (ej. 1 lote = 500 unidades) definida por empresa o producto.
CREATE TABLE equivalencia_unidad (
                                     id              BIGSERIAL PRIMARY KEY,
                                     empresa_id      BIGINT        REFERENCES empresa(id),
                                     producto_id     BIGINT        REFERENCES producto(id),
                                     unidad_origen   VARCHAR(20)   NOT NULL REFERENCES unidad_medida(codigo),
                                     unidad_destino  VARCHAR(20)   NOT NULL REFERENCES unidad_medida(codigo),
                                     factor          NUMERIC(18,6) NOT NULL CHECK (factor > 0),
                                     CONSTRAINT ck_equivalencia_distinta CHECK (unidad_origen <> unidad_destino)
);
CREATE UNIQUE INDEX uq_equivalencia
    ON equivalencia_unidad (COALESCE(empresa_id, 0), COALESCE(producto_id, 0), unidad_origen, unidad_destino);

INSERT INTO equivalencia_unidad (unidad_origen, unidad_destino, factor) VALUES
                                                                            ('ton',      'kg',       1000),
                                                                            ('horas',    'minutos',  60),
                                                                            ('horas',    'segundos', 3600),
                                                                            ('minutos',  'segundos', 60),
                                                                            ('unidades', 'piezas',   1);

-- 2. Merma por proceso (porcentaje de pérdida de la etapa)
--    entrada requerida = salida requerida / (1 - merma/100)
--    unidad_entrada nula = igual a unidad_procesada
ALTER TABLE proceso
    ADD COLUMN merma NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (merma >= 0 AND merma < 100),
    ADD COLUMN unidad_entrada VARCHAR(20);

-- 3. Recurso compartido: el recurso pasa a la empresa y se vincula a varios procesos
ALTER TABLE recurso ADD COLUMN empresa_id BIGINT REFERENCES empresa(id);

UPDATE recurso r
SET empresa_id = p.empresa_id
    FROM proceso pr
  JOIN producto p ON p.id = pr.producto_id
WHERE pr.id = r.proceso_id;

ALTER TABLE recurso ALTER COLUMN empresa_id SET NOT NULL;

CREATE TABLE proceso_recurso (
                                 proceso_id         BIGINT NOT NULL REFERENCES proceso(id) ON DELETE CASCADE,
                                 recurso_id         BIGINT NOT NULL REFERENCES recurso(id) ON DELETE CASCADE,
                                 tiempo_referencia  NUMERIC(12,6) CHECK (tiempo_referencia > 0),  -- h/unidad de referencia, para detectar el factor "tiempo"
                                 PRIMARY KEY (proceso_id, recurso_id)
);
CREATE INDEX idx_proceso_recurso_recurso ON proceso_recurso(recurso_id);

INSERT INTO proceso_recurso (proceso_id, recurso_id)
SELECT proceso_id, id FROM recurso;

ALTER TABLE recurso DROP CONSTRAINT uq_recurso_codigo;
ALTER TABLE recurso DROP COLUMN proceso_id;
ALTER TABLE recurso ADD CONSTRAINT uq_recurso_codigo UNIQUE (empresa_id, codigo);
CREATE INDEX idx_recurso_empresa ON recurso(empresa_id);

-- 4. Análisis: nuevas franjas, déficit en horas e impacto
DO $$
DECLARE c text;
BEGIN
SELECT conname INTO c
FROM pg_constraint
WHERE conrelid = 'analisis_recurso'::regclass
       AND contype = 'c'
       AND pg_get_constraintdef(oid) LIKE '%clasificacion%';
IF c IS NOT NULL THEN
        EXECUTE format('ALTER TABLE analisis_recurso DROP CONSTRAINT %I', c);
END IF;
END $$;

UPDATE analisis_recurso
SET clasificacion = CASE
                        WHEN utilizacion > 100 THEN 'SOBRECARGA'
                        WHEN utilizacion > 90  THEN 'CAPACIDAD_AJUSTADA'
                        WHEN utilizacion >= 80 THEN 'UTILIZACION_ADECUADA'
                        ELSE 'BAJA_UTILIZACION'
    END;

ALTER TABLE analisis_recurso
    ADD CONSTRAINT ck_analisis_clasificacion
        CHECK (clasificacion IN ('BAJA_UTILIZACION', 'UTILIZACION_ADECUADA', 'CAPACIDAD_AJUSTADA', 'SOBRECARGA'));

-- horas_requeridas pasa a ser el TOTAL del recurso (suma de todos los productos que lo usan);
-- aporte_horas es la parte que corresponde al producto de este reporte.
ALTER TABLE analisis_recurso
    ADD COLUMN aporte_horas  NUMERIC(14,4),
    ADD COLUMN deficit_horas NUMERIC(14,4) NOT NULL DEFAULT 0,
    ADD COLUMN impacto       NUMERIC(8,2)  NOT NULL DEFAULT 0;

UPDATE analisis_recurso
SET aporte_horas  = horas_requeridas,
    deficit_horas = GREATEST(0, horas_requeridas - horas_disponibles),
    impacto       = CASE WHEN horas_requeridas > 0
                             THEN ROUND(GREATEST(0, horas_requeridas - horas_disponibles) * 100 / horas_requeridas, 2)
                         ELSE 0 END;