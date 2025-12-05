-- ============================================================================
-- SCRIPT DE MIGRACIÓN: PERSONAL_JORNADASASIGNADAS → JORNADA_ASIGNADA
-- ============================================================================
-- Fecha: 2025-12-05
-- Propósito: Migrar datos de la tabla @ElementCollection a la nueva tabla @Entity
-- 
-- IMPORTANTE: Ejecutar este script ANTES de iniciar la aplicación con los cambios.
-- La aplicación creará automáticamente la nueva tabla jornada_asignada.
-- ============================================================================

-- 1. Si la nueva tabla existe y está vacía, migrar los datos
-- (Ejecutar después de que Hibernate cree la tabla automáticamente)

INSERT INTO JORNADA_ASIGNADA (ID, PERSONAL_ID, TURNO_ID, FECHAINICIO, FECHAFIN)
SELECT 
    RANDOM_UUID(),
    PERSONAL_ID,
    TURNO_ID,
    FECHAINICIO,
    FECHAFIN
FROM PERSONAL_JORNADASASIGNADAS
WHERE NOT EXISTS (SELECT 1 FROM JORNADA_ASIGNADA);

-- 2. Verificar migración
SELECT COUNT(*) AS registros_migrados FROM JORNADA_ASIGNADA;
SELECT COUNT(*) AS registros_originales FROM PERSONAL_JORNADASASIGNADAS;

-- 3. (OPCIONAL) Eliminar tabla vieja después de verificar que todo funciona
-- DROP TABLE PERSONAL_JORNADASASIGNADAS;

-- ============================================================================
-- NOTAS:
-- - La nueva tabla usa UUID como ID (generado por Identifiable de OpenXava)
-- - Si usas HSQLDB, RANDOM_UUID() genera el UUID
-- - Si usas MySQL: UUID()
-- - Si usas PostgreSQL: gen_random_uuid()
-- ============================================================================
