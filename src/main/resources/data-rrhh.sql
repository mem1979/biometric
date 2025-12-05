-- =========================================================================================
-- SCRIPT DE DATOS INICIALES - MODULO RRHH (ARGENTINA)
-- Motor: HSQLDB / Standard SQL
-- Versión Corregida 3: Incluye campos NOT NULL (booleanos primitivos y orden)
-- =========================================================================================

-- 1. CONCEPTOS DE RECIBO (REMUNERATIVOS)
-- 100: Basico
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c100', '100', 'SUELDO BASICO', 'REMUNERATIVO', NULL, FALSE, FALSE, 10);

-- 101: Jornal
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c101', '101', 'JORNAL NORMAL', 'REMUNERATIVO', NULL, FALSE, TRUE, 10);

-- 110: Antiguedad
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c110', '110', 'ANTIGUEDAD', 'REMUNERATIVO', 'BASICO * 0.01 * ANIOS', FALSE, FALSE, 20);

-- 111: Presentismo (Suele ser proporcional si falta)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c111', '111', 'PRESENTISMO', 'REMUNERATIVO', '(BASICO + ANTIGUEDAD) * 0.0833', TRUE, FALSE, 30);

-- 150: Feriado
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c150', '150', 'FERIADO TRABAJADO', 'REMUNERATIVO', NULL, FALSE, TRUE, 40);

-- 2. CONCEPTOS DE RECIBO (ADICIONALES/EXTRAS)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c201', '201', 'HS EXTRAS 50%', 'REMUNERATIVO', NULL, FALSE, TRUE, 50);

INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c202', '202', 'HS EXTRAS 100%', 'REMUNERATIVO', NULL, FALSE, TRUE, 50);

-- 3. CONCEPTOS DE RECIBO (DEDUCCIONES DE LEY)
-- Orden 100+ para que se calculen despues del bruto
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c501', '501', 'JUBILACION (SIPA)', 'DEDUCCION', 'BRUTO_REM * 0.11', FALSE, FALSE, 100);

INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c502', '502', 'LEY 19.032 (INSSJP)', 'DEDUCCION', 'BRUTO_REM * 0.03', FALSE, FALSE, 101);

INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c503', '503', 'OBRA SOCIAL', 'DEDUCCION', 'BRUTO_REM * 0.03', FALSE, FALSE, 102);

INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c504', '504', 'SINDICATO (SEC)', 'DEDUCCION', 'BRUTO_REM * 0.02', FALSE, FALSE, 103);

INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, scriptCalculo, esProporcionalAInasistencias, esVariableNovedad, ordenCalculo) 
VALUES ('c505', '505', 'FAECYS', 'DEDUCCION', 'BRUTO_REM * 0.005', FALSE, FALSE, 104);

-- 4. CONVENIOS COLECTIVOS (aplicaZonaDesfavorable es boolean primitivo tambien)
INSERT INTO ConvenioColectivo (id, codigo, nombre, sigla, aplicaZonaDesfavorable) VALUES ('cct1', '130/75', 'EMPLEADOS DE COMERCIO', 'CEC', FALSE);
INSERT INTO ConvenioColectivo (id, codigo, nombre, sigla, aplicaZonaDesfavorable) VALUES ('cct2', '76/75', 'INDUSTRIA DE LA CONSTRUCCION', 'UOCRA', TRUE);
INSERT INTO ConvenioColectivo (id, codigo, nombre, sigla, aplicaZonaDesfavorable) VALUES ('cct3', '000/00', 'FUERA DE CONVENIO', 'F/C', FALSE);

-- 5. CATEGORIAS (COMERCIO - ABRIL 2024 Referencia)
INSERT INTO Categoria (id, codigo, nombre, basicoMensual, valorHora, convenio_id) VALUES ('cat101', 'MAESTRANZA A', 'MAESTRANZA A', 700000.00, NULL, 'cct1');
INSERT INTO Categoria (id, codigo, nombre, basicoMensual, valorHora, convenio_id) VALUES ('cat102', 'ADMINISTRATIVO A', 'ADMINISTRATIVO A', 710000.00, NULL, 'cct1');
INSERT INTO Categoria (id, codigo, nombre, basicoMensual, valorHora, convenio_id) VALUES ('cat103', 'VENDEDOR B', 'VENDEDOR B', 750000.00, NULL, 'cct1');

-- 6. CATEGORIAS (UOCRA - ABRIL 2024 Referencia)
INSERT INTO Categoria (id, codigo, nombre, basicoMensual, valorHora, convenio_id) VALUES ('cat201', 'OFICIAL ESP', 'OFICIAL ESPECIALIZADO', NULL, 3500.00, 'cct2');
INSERT INTO Categoria (id, codigo, nombre, basicoMensual, valorHora, convenio_id) VALUES ('cat202', 'OFICIAL', 'OFICIAL', NULL, 3000.00, 'cct2');
INSERT INTO Categoria (id, codigo, nombre, basicoMensual, valorHora, convenio_id) VALUES ('cat203', 'AYUDANTE', 'AYUDANTE', NULL, 2500.00, 'cct2');

-- FIN SCRIPT
