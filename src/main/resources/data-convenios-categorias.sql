-- =========================================================================================
-- SCRIPT DE CONVENIOS Y CATEGORIAS - ARGENTINA (AMPLIADO)
-- Motor: HSQLDB / Standard SQL
-- Entidades: ConvenioColectivo, Categoria
-- =========================================================================================

-- =========================================================================================
-- 1. CONVENIOS COLECTIVOS (CCT)
-- =========================================================================================

-- COMERCIO (130/75)
-- Antiguedad: 1%, Presentismo: 8.33%, Zona: No por defecto
INSERT INTO ConvenioColectivo (id, codigo, nombre, sigla, porcentajeAntiguedadPorAno, porcentajePresentismo, aplicaZonaDesfavorable, porcentajeZona) 
VALUES ('cct1', '130/75', 'EMPLEADOS DE COMERCIO', 'CEC', 1.00, 8.33, FALSE, 0.00);

-- UOCRA (76/75)
-- Antiguedad: 1% (Variable segun zona), Presentismo: 20% (Premio asist), Zona: Si
INSERT INTO ConvenioColectivo (id, codigo, nombre, sigla, porcentajeAntiguedadPorAno, porcentajePresentismo, aplicaZonaDesfavorable, porcentajeZona) 
VALUES ('cct2', '76/75', 'INDUSTRIA DE LA CONSTRUCCION', 'UOCRA', 1.00, 20.00, TRUE, 20.00);

-- SANIDAD (122/75) - CLÍNICAS Y SANATORIOS
INSERT INTO ConvenioColectivo (id, codigo, nombre, sigla, porcentajeAntiguedadPorAno, porcentajePresentismo, aplicaZonaDesfavorable, porcentajeZona) 
VALUES ('cct3', '122/75', 'SANIDAD (CLINICAS Y SANATORIOS)', 'FATSA', 2.00, 0.00, FALSE, 0.00);

-- GASTRONOMICOS (389/04) - HOTELES Y RESTAURANTES
INSERT INTO ConvenioColectivo (id, codigo, nombre, sigla, porcentajeAntiguedadPorAno, porcentajePresentismo, aplicaZonaDesfavorable, porcentajeZona) 
VALUES ('cct4', '389/04', 'GASTRONOMICOS (UTHGRA)', 'UTHGRA', 1.00, 10.00, FALSE, 0.00);

-- UOM (260/75) - METALURGICOS
INSERT INTO ConvenioColectivo (id, codigo, nombre, sigla, porcentajeAntiguedadPorAno, porcentajePresentismo, aplicaZonaDesfavorable, porcentajeZona) 
VALUES ('cct5', '260/75', 'UNION OBRERA METALURGICA', 'UOM', 1.00, 0.00, FALSE, 0.00);

-- FUERA DE CONVENIO (PERSONAL JERARQUICO)
INSERT INTO ConvenioColectivo (id, codigo, nombre, sigla, porcentajeAntiguedadPorAno, porcentajePresentismo, aplicaZonaDesfavorable, porcentajeZona) 
VALUES ('cct0', '000/00', 'FUERA DE CONVENIO', 'F/C', 0.00, 0.00, FALSE, 0.00);


-- =========================================================================================
-- 2. CATEGORIAS PROFESIONALES (Valores Referencia 2024 - NO CONTRACTUALES)
-- =========================================================================================

-- --- COMERCIO (CCT 130/75) ---
-- MAESTRANZA
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat100', 'cct1', 'M-A', 'MAESTRANZA A', 700000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat101', 'cct1', 'M-B', 'MAESTRANZA B', 710000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat102', 'cct1', 'M-C', 'MAESTRANZA C', 720000.00, NULL);

-- ADMINISTRATIVO
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat110', 'cct1', 'ADM-A', 'ADMINISTRATIVO A', 715000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat111', 'cct1', 'ADM-B', 'ADMINISTRATIVO B', 725000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat112', 'cct1', 'ADM-C', 'ADMINISTRATIVO C', 735000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat113', 'cct1', 'ADM-F', 'ADMINISTRATIVO F', 780000.00, NULL);

-- CAJEROS
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat120', 'cct1', 'CAJ-A', 'CAJERO A', 715000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat121', 'cct1', 'CAJ-B', 'CAJERO B', 725000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat122', 'cct1', 'CAJ-C', 'CAJERO C', 730000.00, NULL);

-- VENDEDORES
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat130', 'cct1', 'VEND-A', 'VENDEDOR A', 715000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat131', 'cct1', 'VEND-B', 'VENDEDOR B', 750000.00, NULL);


-- --- UOCRA (CCT 76/75) - JORNALIZADOS ---
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat200', 'cct2', 'OF-ESP', 'OFICIAL ESPECIALIZADO', NULL, 3800.00);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat201', 'cct2', 'OFIC', 'OFICIAL', NULL, 3200.00);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat202', 'cct2', 'MED-OF', 'MEDIO OFICIAL', NULL, 2900.00);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat203', 'cct2', 'AYUD', 'AYUDANTE', NULL, 2600.00);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat204', 'cct2', 'SERENO', 'SERENO', 650000.00, NULL); -- Sereno suele ser mensual


-- --- SANIDAD (CCT 122/75) ---
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat300', 'cct3', 'ENF', 'ENFERMERO/A DE PISO', 800000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat301', 'cct3', 'MUC', 'MUCAMA', 650000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat302', 'cct3', 'ADM', 'ADMINISTRATIVO 1RA', 750000.00, NULL);


-- --- GASTRONOMICOS (CCT 389/04) ---
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat400', 'cct4', 'MOZO', 'MOZO DE SALON', 680000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat401', 'cct4', 'COC', 'COCINERO', 720000.00, NULL);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat402', 'cct4', 'PEON', 'PEON DE COCINA', 600000.00, NULL);


-- --- UOM (CCT 260/75) - METALURGICOS ---
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat500', 'cct5', 'OP-CAL', 'OPERARIO CALIFICADO', NULL, 3100.00);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat501', 'cct5', 'OF-MUL', 'OFICIAL MULTIPLE', NULL, 3600.00);
INSERT INTO Categoria (id, convenio_id, codigo, nombre, basicoMensual, valorHora) VALUES ('cat502', 'cct5', 'ADM-1', 'ADMINISTRATIVO 1RA', 750000.00, NULL);
