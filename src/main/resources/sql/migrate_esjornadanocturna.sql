-- ============================================
-- Migración: Agregar columna esJornadaNocturna
-- Fecha: 2025-12-21
-- Para: Soporte de turnos nocturnos (Fase 1)
-- ============================================

-- HSQLDB / MySQL / PostgreSQL compatible
ALTER TABLE AuditoriaRegistros ADD COLUMN esJornadaNocturna BOOLEAN DEFAULT FALSE;

-- Actualizar registros existentes (opcional - marcar como no nocturno)
UPDATE AuditoriaRegistros SET esJornadaNocturna = FALSE WHERE esJornadaNocturna IS NULL;
