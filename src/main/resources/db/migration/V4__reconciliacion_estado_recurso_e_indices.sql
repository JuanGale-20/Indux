-- =====================================================
-- V4 · Reconciliación de estado de recurso e índices de optimización
-- =====================================================

-- 1. Restricción CHECK de estados válidos de recurso (alineado con enum EstadoRecurso: DISPONIBLE, NO_DISPONIBLE)
ALTER TABLE recurso
    ADD CONSTRAINT ck_recurso_estado CHECK (estado IN ('DISPONIBLE', 'NO_DISPONIBLE'));

-- 2. Índice sobre reporte_recurso(recurso_id)
-- Justificación: optimiza las consultas de histórico de utilización por recurso y cálculo de recurso compartido,
-- evitando sequential scans sobre reporte_recurso que solo contaba con índice compuesto (reporte_id, recurso_id).
CREATE INDEX idx_reporte_recurso_recurso ON reporte_recurso(recurso_id);

-- 3. Índice sobre reporte para la búsqueda de candidatos a recurso compartido
-- Justificación: optimiza la consulta buscarCandidatosRecursoCompartido que filtra por estado, frecuencia y periodo.
CREATE INDEX idx_reporte_candidatos_compartido ON reporte(estado, frecuencia, periodo_fin, periodo_inicio);
