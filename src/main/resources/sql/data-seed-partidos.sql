-- ============================================================================
-- DATA SEED: PARTIDOS/DEPARTAMENTOS ARGENTINOS
-- ============================================================================
-- Script universal para carga de datos de Partidos
-- Compatible con: HSQLDB, MySQL, PostgreSQL, H2, Oracle, SQL Server
--
-- IMPORTANTE: Este script requiere que las PROVINCIAS ya estén cargadas.
--             La columna provincia_numero referencia a PROVINCIAS(numero)
--
-- Uso: Ejecutar una sola vez para cargar datos iniciales.
-- Generado: 2025-12-09
-- Total: 493 partidos
-- ============================================================================

-- PROVINCIA 1: BUENOS AIRES (135 partidos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (1, '25 DE MAYO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (2, '9 DE JULIO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (3, 'ADOLFO ALSINA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (4, 'ADOLFO GONZALES CHAVES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (5, 'ALBERTI', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (6, 'ALMIRANTE BROWN', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (7, 'ARRECIFES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (8, 'AVELLANEDA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (9, 'AYACUCHO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (10, 'AZUL', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (11, 'BAHIA BLANCA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (12, 'BALCARCE', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (13, 'BARADERO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (14, 'BENITO JUAREZ', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (15, 'BERAZATEGUI', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (16, 'BERISSO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (17, 'BOLIVAR', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (18, 'BRAGADO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (19, 'BRANDSEN', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (20, 'CAMPANA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (21, 'CAÑUELAS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (22, 'CAPITAN SARMIENTO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (23, 'CARLOS CASARES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (24, 'CARLOS TEJEDOR', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (25, 'CARMEN DE ARECO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (26, 'CASTELLI', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (27, 'CHACABUCO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (28, 'CHASCOMUS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (29, 'CHIVILCOY', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (30, 'COLON', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (31, 'CORONEL DE MARINA LEONARDO ROSALES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (32, 'CORONEL DORREGO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (33, 'CORONEL PRINGLES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (34, 'CORONEL SUAREZ', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (35, 'DAIREAUX', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (36, 'DOLORES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (37, 'ENSENADA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (38, 'ESCOBAR', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (39, 'ESTEBAN ECHEVERRIA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (40, 'EXALTACION DE LA CRUZ', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (41, 'EZEIZA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (42, 'FLORENCIO VARELA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (43, 'FLORENTINO AMEGHINO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (44, 'GENERAL ALVARADO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (45, 'GENERAL ALVEAR', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (46, 'GENERAL ARENALES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (47, 'GENERAL BELGRANO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (48, 'GENERAL GUIDO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (49, 'GENERAL JUAN MADARIAGA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (50, 'GENERAL LA MADRID', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (51, 'GENERAL LAS HERAS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (52, 'GENERAL LAVALLE', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (53, 'GENERAL PAZ', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (54, 'GENERAL PINTO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (55, 'GENERAL PUEYRREDON', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (56, 'GENERAL RODRIGUEZ', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (57, 'GENERAL SAN MARTIN', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (58, 'GENERAL VIAMONTE', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (59, 'GENERAL VILLEGAS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (60, 'GUAMINI', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (61, 'HIPOLITO YRIGOYEN', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (62, 'HURLINGHAM', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (63, 'ITUZAINGO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (64, 'JOSE C. PAZ', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (65, 'JUNIN', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (66, 'LA COSTA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (67, 'LA MATANZA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (68, 'LA PLATA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (69, 'LANUS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (70, 'LAPRIDA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (71, 'LAS FLORES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (72, 'LEANDRO N. ALEM', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (73, 'LEZAMA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (74, 'LINCOLN', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (75, 'LOBERIA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (76, 'LOBOS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (77, 'LOMAS DE ZAMORA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (78, 'LUJAN', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (79, 'MAGDALENA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (80, 'MAIPU', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (81, 'MALVINAS ARGENTINAS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (82, 'MAR CHIQUITA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (83, 'MARCOS PAZ', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (84, 'MERCEDES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (85, 'MERLO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (86, 'MONTE', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (87, 'MONTE HERMOSO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (88, 'MORENO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (89, 'MORON', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (90, 'NAVARRO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (91, 'NECOCHEA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (92, 'OLAVARRIA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (93, 'PATAGONES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (94, 'PEHUAJO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (95, 'PELLEGRINI', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (96, 'PERGAMINO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (97, 'PILA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (98, 'PILAR', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (99, 'PINAMAR', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (100, 'PRESIDENTE PERON', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (101, 'PUAN', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (102, 'PUNTA INDIO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (103, 'QUILMES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (104, 'RAMALLO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (105, 'RAUCH', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (106, 'RIVADAVIA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (107, 'ROJAS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (108, 'ROQUE PEREZ', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (109, 'SAAVEDRA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (110, 'SALADILLO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (111, 'SALLIQUELO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (112, 'SALTO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (113, 'SAN ANDRES DE GILES', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (114, 'SAN ANTONIO DE ARECO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (115, 'SAN CAYETANO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (116, 'SAN FERNANDO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (117, 'SAN ISIDRO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (118, 'SAN MIGUEL', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (119, 'SAN NICOLAS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (120, 'SAN PEDRO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (121, 'SAN VICENTE', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (122, 'SUIPACHA', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (123, 'TANDIL', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (124, 'TAPALQUE', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (125, 'TIGRE', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (126, 'TORDILLO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (127, 'TORNQUIST', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (128, 'TRENQUE LAUQUEN', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (129, 'TRES ARROYOS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (130, 'TRES DE FEBRERO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (131, 'TRES LOMAS', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (132, 'VICENTE LOPEZ', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (133, 'VILLA GESELL', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (134, 'VILLARINO', 1);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (135, 'ZARATE', 1);

-- PROVINCIA 2: CATAMARCA (16 partidos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (136, 'AMBATO', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (137, 'ANCASTI', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (138, 'ANDALGALA', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (139, 'ANTOFAGASTA DE LA SIERRA', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (140, 'BELEN', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (141, 'CAPAYAN', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (142, 'CAPITAL', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (143, 'EL ALTO', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (144, 'FRAY MAMERTO ESQUIU', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (145, 'LA PAZ', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (146, 'PACLIN', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (147, 'POMAN', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (148, 'SANTA MARIA', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (149, 'SANTA ROSA', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (150, 'TINOGASTA', 2);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (151, 'VALLE VIEJO', 2);

-- PROVINCIA 3: CHACO (25 partidos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (152, '1° DE MAYO', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (153, '12 DE OCTUBRE', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (154, '2 DE ABRIL', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (155, '25 DE MAYO', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (156, '9 DE JULIO', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (157, 'ALMIRANTE BROWN', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (158, 'BERMEJO', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (159, 'CHACABUCO', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (160, 'COMANDANTE FERNANDEZ', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (161, 'FRAY JUSTO SANTA MARIA DE ORO', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (162, 'GENERAL BELGRANO', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (163, 'GENERAL DONOVAN', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (164, 'GENERAL GÜEMES', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (165, 'INDEPENDENCIA', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (166, 'LIBERTAD', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (167, 'LIBERTADOR GENERAL SAN MARTIN', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (168, 'MAIPU', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (169, 'MAYOR LUIS J. FONTANA', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (170, 'O''HIGGINS', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (171, 'PRESIDENCIA DE LA PLAZA', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (172, 'QUITILIPI', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (173, 'SAN FERNANDO', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (174, 'SAN LORENZO', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (175, 'SARGENTO CABRAL', 3);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (176, 'TAPENAGA', 3);

-- PROVINCIA 4: CHUBUT (15 partidos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (177, 'BIEDMA', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (178, 'CUSHAMEN', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (179, 'ESCALANTE', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (180, 'FLORENTINO AMEGHINO', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (181, 'FUTALEUFU', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (182, 'GAIMAN', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (183, 'GASTRE', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (184, 'LANGUIÑEO', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (185, 'MARTIRES', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (186, 'PASO DE INDIOS', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (187, 'RAWSON', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (188, 'RIO SENGUER', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (189, 'SARMIENTO', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (190, 'TEHUELCHES', 4);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (191, 'TELSEN', 4);

-- PROVINCIA 5: CABA (15 comunas)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (192, 'COMUNA 1', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (193, 'COMUNA 10', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (194, 'COMUNA 11', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (195, 'COMUNA 12', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (196, 'COMUNA 13', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (197, 'COMUNA 14', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (198, 'COMUNA 15', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (199, 'COMUNA 2', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (200, 'COMUNA 3', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (201, 'COMUNA 4', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (202, 'COMUNA 5', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (203, 'COMUNA 6', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (204, 'COMUNA 7', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (205, 'COMUNA 8', 5);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (206, 'COMUNA 9', 5);

-- PROVINCIA 6: CORDOBA (26 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (207, 'CALAMUCHITA', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (208, 'CAPITAL', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (209, 'COLON', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (210, 'CRUZ DEL EJE', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (211, 'GENERAL ROCA', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (212, 'GENERAL SAN MARTIN', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (213, 'ISCHILIN', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (214, 'JUAREZ CELMAN', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (215, 'MARCOS JUAREZ', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (216, 'MINAS', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (217, 'POCHO', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (218, 'PRESIDENTE ROQUE SAENZ PEÑA', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (219, 'PUNILLA', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (220, 'RIO CUARTO', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (221, 'RIO PRIMERO', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (222, 'RIO SECO', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (223, 'RIO SEGUNDO', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (224, 'SAN ALBERTO', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (225, 'SAN JAVIER', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (226, 'SAN JUSTO', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (227, 'SANTA MARIA', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (228, 'SOBREMONTE', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (229, 'TERCERO ARRIBA', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (230, 'TOTORAL', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (231, 'TULUMBA', 6);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (232, 'UNION', 6);

-- PROVINCIA 7: CORRIENTES (25 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (233, 'BELLA VISTA', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (234, 'BERON DE ASTRADA', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (235, 'CAPITAL', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (236, 'CONCEPCION', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (237, 'CURUZU CUATIA', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (238, 'EMPEDRADO', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (239, 'ESQUINA', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (240, 'GENERAL ALVEAR', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (241, 'GENERAL PAZ', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (242, 'GOYA', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (243, 'ITATI', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (244, 'ITUZAINGO', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (245, 'LAVALLE', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (246, 'MBURUCUYA', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (247, 'MERCEDES', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (248, 'MONTE CASEROS', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (249, 'PASO DE LOS LIBRES', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (250, 'SALADAS', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (251, 'SAN COSME', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (252, 'SAN LUIS DEL PALMAR', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (253, 'SAN MARTIN', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (254, 'SAN MIGUEL', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (255, 'SAN ROQUE', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (256, 'SANTO TOME', 7);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (257, 'SAUCE', 7);

-- PROVINCIA 8: ENTRE RIOS (17 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (258, 'COLON', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (259, 'CONCORDIA', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (260, 'DIAMANTE', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (261, 'FEDERACION', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (262, 'FEDERAL', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (263, 'FELICIANO', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (264, 'GUALEGUAY', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (265, 'GUALEGUAYCHU', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (266, 'ISLAS DEL IBICUY', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (267, 'LA PAZ', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (268, 'NOGOYA', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (269, 'PARANA', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (270, 'SAN SALVADOR', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (271, 'TALA', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (272, 'URUGUAY', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (273, 'VICTORIA', 8);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (274, 'VILLAGUAY', 8);

-- PROVINCIA 9: FORMOSA (9 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (275, 'BERMEJO', 9);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (276, 'FORMOSA', 9);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (277, 'LAISHI', 9);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (278, 'MATACOS', 9);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (279, 'PATIÑO', 9);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (280, 'PILAGAS', 9);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (281, 'PILCOMAYO', 9);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (282, 'PIRANE', 9);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (283, 'RAMON LISTA', 9);

-- PROVINCIA 10: JUJUY (16 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (284, 'COCHINOCA', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (285, 'DR. MANUEL BELGRANO', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (286, 'EL CARMEN', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (287, 'HUMAHUACA', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (288, 'LEDESMA', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (289, 'PALPALA', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (290, 'RINCONADA', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (291, 'SAN ANTONIO', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (292, 'SAN PEDRO', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (293, 'SANTA BARBARA', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (294, 'SANTA CATALINA', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (295, 'SUSQUES', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (296, 'TILCARA', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (297, 'TUMBAYA', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (298, 'VALLE GRANDE', 10);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (299, 'YAVI', 10);

-- PROVINCIA 11: LA PAMPA (22 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (300, 'ATREUCO', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (301, 'CALEU CALEU', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (302, 'CAPITAL', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (303, 'CATRILO', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (304, 'CHALILEO', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (305, 'CHAPALEUFU', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (306, 'CHICAL CO', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (307, 'CONHELO', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (308, 'CURACO', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (309, 'GUATRACHE', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (310, 'HUCAL', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (311, 'LIHUEL CALEL', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (312, 'LIMAY MAHUIDA', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (313, 'LOVENTUE', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (314, 'MARACO', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (315, 'PUELEN', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (316, 'QUEMU QUEMU', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (317, 'RANCUL', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (318, 'REALICO', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (319, 'TOAY', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (320, 'TRENEL', 11);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (321, 'UTRACAN', 11);

-- PROVINCIA 12: LA RIOJA (18 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (322, 'ANGEL VICENTE PEÑALOZA', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (323, 'ARAUCO', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (324, 'CAPITAL', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (325, 'CASTRO BARROS', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (326, 'CHAMICAL', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (327, 'CHILECITO', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (328, 'FAMATINA', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (329, 'GENERAL BELGRANO', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (330, 'GENERAL FELIPE VARELA', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (331, 'GENERAL JUAN FACUNDO QUIROGA', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (332, 'GENERAL LAMADRID', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (333, 'GENERAL ORTIZ DE OCAMPO', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (334, 'GENERAL SAN MARTIN', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (335, 'INDEPENDENCIA', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (336, 'ROSARIO VERA PEÑALOZA', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (337, 'SAN BLAS DE LOS SAUCES', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (338, 'SANAGASTA', 12);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (339, 'VINCHINA', 12);

-- PROVINCIA 13: MENDOZA (18 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (340, 'CAPITAL', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (341, 'GENERAL ALVEAR', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (342, 'GODOY CRUZ', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (343, 'GUAYMALLEN', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (344, 'JUNIN', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (345, 'LA PAZ', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (346, 'LAS HERAS', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (347, 'LAVALLE', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (348, 'LUJAN DE CUYO', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (349, 'MAIPU', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (350, 'MALARGÜE', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (351, 'RIVADAVIA', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (352, 'SAN CARLOS', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (353, 'SAN MARTIN', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (354, 'SAN RAFAEL', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (355, 'SANTA ROSA', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (356, 'TUNUYAN', 13);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (357, 'TUPUNGATO', 13);

-- PROVINCIA 14: MISIONES (17 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (358, '25 DE MAYO', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (359, 'APOSTOLES', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (360, 'CAINGUAS', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (361, 'CANDELARIA', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (362, 'CAPITAL', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (363, 'CONCEPCION', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (364, 'ELDORADO', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (365, 'GENERAL MANUEL BELGRANO', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (366, 'GUARANI', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (367, 'IGUAZU', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (368, 'LEANDRO N. ALEM', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (369, 'LIBERTADOR GENERAL SAN MARTIN', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (370, 'MONTECARLO', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (371, 'OBERA', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (372, 'SAN IGNACIO', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (373, 'SAN JAVIER', 14);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (374, 'SAN PEDRO', 14);

-- PROVINCIA 15: NEUQUEN (16 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (375, 'ALUMINE', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (376, 'AÑELO', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (377, 'CATAN LIL', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (378, 'CHOS MALAL', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (379, 'COLLON CURA', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (380, 'CONFLUENCIA', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (381, 'HUILICHES', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (382, 'LACAR', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (383, 'LONCOPUE', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (384, 'LOS LAGOS', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (385, 'MINAS', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (386, 'ÑORQUIN', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (387, 'PEHUENCHES', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (388, 'PICUN LEUFU', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (389, 'PICUNCHES', 15);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (390, 'ZAPALA', 15);

-- PROVINCIA 16: RIO NEGRO (13 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (391, '25 DE MAYO', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (392, '9 DE JULIO', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (393, 'ADOLFO ALSINA', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (394, 'AVELLANEDA', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (395, 'BARILOCHE', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (396, 'CONESA', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (397, 'EL CUY', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (398, 'GENERAL ROCA', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (399, 'ÑORQUINCO', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (400, 'PICHI MAHUIDA', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (401, 'PILCANIYEU', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (402, 'SAN ANTONIO', 16);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (403, 'VALCHETA', 16);

-- PROVINCIA 17: SALTA (23 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (404, 'ANTA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (405, 'CACHI', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (406, 'CAFAYATE', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (407, 'CAPITAL', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (408, 'CERRILLOS', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (409, 'CHICOANA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (410, 'GENERAL GÜEMES', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (411, 'GENERAL JOSE DE SAN MARTIN', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (412, 'GUACHIPAS', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (413, 'IRUYA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (414, 'LA CALDERA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (415, 'LA CANDELARIA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (416, 'LA POMA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (417, 'LA VIÑA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (418, 'LOS ANDES', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (419, 'METAN', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (420, 'MOLINOS', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (421, 'ORAN', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (422, 'RIVADAVIA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (423, 'ROSARIO DE LA FRONTERA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (424, 'ROSARIO DE LERMA', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (425, 'SAN CARLOS', 17);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (426, 'SANTA VICTORIA', 17);

-- PROVINCIA 18: SAN JUAN (19 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (427, '25 DE MAYO', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (428, '9 DE JULIO', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (429, 'ALBARDON', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (430, 'ANGACO', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (431, 'CALINGASTA', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (432, 'CAPITAL', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (433, 'CAUCETE', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (434, 'CHIMBAS', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (435, 'IGLESIA', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (436, 'JACHAL', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (437, 'POCITO', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (438, 'RAWSON', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (439, 'RIVADAVIA', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (440, 'SAN MARTIN', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (441, 'SANTA LUCIA', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (442, 'SARMIENTO', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (443, 'ULLUM', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (444, 'VALLE FERTIL', 18);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (445, 'ZONDA', 18);

-- PROVINCIA 19: SAN LUIS (9 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (446, 'AYACUCHO', 19);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (447, 'BELGRANO', 19);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (448, 'CHACABUCO', 19);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (449, 'CORONEL PRINGLES', 19);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (450, 'GENERAL PEDERNERA', 19);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (451, 'GOBERNADOR DUPUY', 19);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (452, 'JUAN MARTIN DE PUEYRREDON', 19);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (453, 'JUNIN', 19);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (454, 'LIBERTADOR GENERAL SAN MARTIN', 19);

-- PROVINCIA 20: SANTA FE (19 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (455, '9 DE JULIO', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (456, 'BELGRANO', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (457, 'CASEROS', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (458, 'CASTELLANOS', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (459, 'CONSTITUCION', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (460, 'GARAY', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (461, 'GENERAL LOPEZ', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (462, 'GENERAL OBLIGADO', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (463, 'IRIONDO', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (464, 'LA CAPITAL', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (465, 'LAS COLONIAS', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (466, 'ROSARIO', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (467, 'SAN CRISTOBAL', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (468, 'SAN JAVIER', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (469, 'SAN JERONIMO', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (470, 'SAN JUSTO', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (471, 'SAN LORENZO', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (472, 'SAN MARTIN', 20);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (473, 'VERA', 20);

-- PROVINCIA 21: SANTIAGO DEL ESTERO (1 departamento en datos originales)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (474, 'GUASAYAN', 21);

-- PROVINCIA 22: TIERRA DEL FUEGO (2 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (475, 'RIO GRANDE', 22);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (476, 'USHUAIA', 22);

-- PROVINCIA 23: TUCUMAN (17 departamentos)
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (477, 'BURRUYACU', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (478, 'CAPITAL', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (479, 'CHICLIGASTA', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (480, 'CRUZ ALTA', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (481, 'FAMAILLA', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (482, 'GRANEROS', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (483, 'JUAN BAUTISTA ALBERDI', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (484, 'LA COCHA', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (485, 'LEALES', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (486, 'LULES', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (487, 'MONTEROS', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (488, 'RIO CHICO', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (489, 'SIMOCA', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (490, 'TAFI DEL VALLE', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (491, 'TAFI VIEJO', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (492, 'TRANCAS', 23);
INSERT INTO PARTIDOS (numero, nombre, provincia_numero) VALUES (493, 'YERBA BUENA', 23);

-- ============================================================================
-- FIN DEL SCRIPT - Total: 493 partidos
-- ============================================================================
