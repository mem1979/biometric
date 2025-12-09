-- ============================================================================
-- DATA SEED: PROVINCIAS ARGENTINAS
-- ============================================================================
-- Script universal para carga de datos de Provincias
-- Compatible con: HSQLDB, MySQL, PostgreSQL, H2, Oracle, SQL Server
--
-- Uso: Ejecutar una sola vez para cargar datos iniciales.
--      - Si la tabla ya tiene datos, verificar manualmente antes de ejecutar.
--      - El script NO verifica duplicados, asume tabla vacía.
--
-- Generado: 2025-12-09
-- ============================================================================

-- Limpiar datos existentes (DESCOMENTAR si es necesario)
-- DELETE FROM PROVINCIAS;

-- Insertar las 23 provincias + CABA
INSERT INTO PROVINCIAS (numero, nombre) VALUES (1, 'BUENOS AIRES');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (2, 'CATAMARCA');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (3, 'CHACO');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (4, 'CHUBUT');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (5, 'CABA');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (6, 'CORDOBA');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (7, 'CORRIENTES');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (8, 'ENTRE RIOS');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (9, 'FORMOSA');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (10, 'JUJUY');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (11, 'LA PAMPA');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (12, 'LA RIOJA');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (13, 'MENDOZA');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (14, 'MISIONES');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (15, 'NEUQUEN');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (16, 'RIO NEGRO');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (17, 'SALTA');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (18, 'SAN JUAN');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (19, 'SAN LUIS');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (20, 'SANTA FE');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (21, 'SANTIAGO DEL ESTERO');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (22, 'TIERRA DEL FUEGO');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (23, 'TUCUMAN');
INSERT INTO PROVINCIAS (numero, nombre) VALUES (24, 'SANTA CRUZ');

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
