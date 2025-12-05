-- =========================================================================================
-- SCRIPT DE CONCEPTOS DE LIQUIDACION - ARGENTINA (COMPLETO)
-- Motor: HSQLDB / Standard SQL
-- Entidad: ConceptoRecibo
-- Dependencias: TipoConcepto (Enum)
-- =========================================================================================

-- TIPOS POSIBLES (Enum TipoConcepto):
-- REMUNERATIVO, NO_REMUNERATIVO, DEDUCCION, RETENCION_IMPOSITIVA, PAGO_EN_ESPECIE, AUXILIAR_CALCULO

-- =========================================================================================
-- 1. HABERES REMUNERATIVOS (Sujetos a Descuentos) - Orden 10-100
-- =========================================================================================

-- 100: Sueldo Básico Mensual
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c100', '100', 'SUELDO BASICO', 'REMUNERATIVO', 10, FALSE, FALSE);

-- 101: Jornal Hora (Para UOCRA / Maestranza Hora)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c101', '101', 'HS NORMALES', 'REMUNERATIVO', 10, FALSE, TRUE);

-- 110: Antigüedad (Generalmente % por año)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad, porcentaje)
VALUES ('c110', '110', 'ANTIGUEDAD', 'REMUNERATIVO', 20, FALSE, FALSE, 1.00);

-- 111: Presentismo (Comercio 8.33% sobre Basico+Ant)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad, porcentaje)
VALUES ('c111', '111', 'PRESENTISMO', 'REMUNERATIVO', 30, TRUE, FALSE, 8.33);

-- 112: Título (Secundario / Terciario / Universitario)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c112', '112', 'ADICIONAL TITULO', 'REMUNERATIVO', 35, FALSE, FALSE);

-- 120: Comisiones (Ventas)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c120', '120', 'COMISIONES VENTAS', 'REMUNERATIVO', 40, FALSE, TRUE);

-- 130: Vacaciones Gozadas
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c130', '130', 'VACACIONES GOZADAS', 'REMUNERATIVO', 50, FALSE, TRUE);

-- 140: S.A.C. (Aguinaldo)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c140', '140', 'S.A.C. (AGUINALDO)', 'REMUNERATIVO', 60, FALSE, FALSE);

-- 150: Feriado Trabajado (Al 100%)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c150', '150', 'FERIADO TRABAJADO', 'REMUNERATIVO', 70, FALSE, TRUE);

-- 151: Feriado No Trabajado (Pago del día plus feriado)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c151', '151', 'DIA FERIADO', 'REMUNERATIVO', 70, FALSE, FALSE);

-- 201: Horas Extras 50% (Habituales)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c201', '201', 'HS EXTRAS 50%', 'REMUNERATIVO', 80, FALSE, TRUE);

-- 202: Horas Extras 100% (Sábados/Domingos)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c202', '202', 'HS EXTRAS 100%', 'REMUNERATIVO', 81, FALSE, TRUE);

-- 205: Zona Desfavorable
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c205', '205', 'ZONA DESFAVORABLE', 'REMUNERATIVO', 85, FALSE, FALSE);

-- =========================================================================================
-- 2. HABERES NO REMUNERATIVOS (De Bolsillo) - Orden 200-300
-- =========================================================================================

-- 301: Viáticos (Con Comprobantes)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c301', '301', 'VIATICOS C/COMPR.', 'NO_REMUNERATIVO', 200, FALSE, TRUE);

-- 302: Reintegro Gastos
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c302', '302', 'REINTEGRO GASTOS', 'NO_REMUNERATIVO', 210, FALSE, TRUE);

-- 303: Asignación No Remunerativa (Acuerdos Gremiales)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c303', '303', 'ACUERDO NO REMUN.', 'NO_REMUNERATIVO', 220, TRUE, FALSE);

-- 350: Asignaciones Familiares (Si se liquidan por recibo - SUAF suele pagar directo, pero a veces se incluye informativo)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c350', '350', 'ASIG. FAMILIAR HIJO', 'NO_REMUNERATIVO', 230, FALSE, FALSE);

-- =========================================================================================
-- 3. DEDUCCIONES (Descuentos de Ley y Otros) - Orden 500+
-- =========================================================================================

-- 501: Jubilación (SIPA) - 11%
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad, porcentaje)
VALUES ('c501', '501', 'JUBILACION', 'DEDUCCION', 500, FALSE, FALSE, 11.00);

-- 502: Ley 19.032 (PAMI) - 3%
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad, porcentaje)
VALUES ('c502', '502', 'LEY 19.032 (INSSJP)', 'DEDUCCION', 501, FALSE, FALSE, 3.00);

-- 503: Obra Social - 3%
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad, porcentaje)
VALUES ('c503', '503', 'OBRA SOCIAL', 'DEDUCCION', 502, FALSE, FALSE, 3.00);

-- 504: Sindicato (Cuota Sindical General) - % Variable
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad, porcentaje)
VALUES ('c504', '504', 'CUOTA SINDICAL', 'DEDUCCION', 505, FALSE, FALSE, 2.00); <!-- Default 2% -->

-- 505: FAECYS (Comercio Específico)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad, porcentaje)
VALUES ('c505', '505', 'FAECYS', 'DEDUCCION', 506, FALSE, FALSE, 0.50);

-- 510: Seguro de Vida Obligatorio
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad, montoFijo)
VALUES ('c510', '510', 'SEG. VIDA OBLIG.', 'DEDUCCION', 510, FALSE, FALSE, 100.00); <!-- Monto Fijo Ejemplo -->

-- 520: Adelanto de Haberes
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c520', '520', 'ADELANTO SUELDO', 'DEDUCCION', 520, FALSE, TRUE);

-- 530: Descuento por Faltas Injustificadas
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c530', '530', 'FALTAS INJUSTIFIC.', 'DEDUCCION', 530, FALSE, TRUE);

-- 550: Impuesto a las Ganancias (4ta Categoría)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c550', '550', 'IMP. GANANCIAS', 'DEDUCCION', 550, FALSE, FALSE);

-- 560: Embargo Judicial
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c560', '560', 'EMBARGO SUELDO', 'DEDUCCION', 560, FALSE, FALSE);

-- =========================================================================================
-- 4. RETENCIONES IMPOSITIVAS (Para Honorarios / Facturas)
-- =========================================================================================

-- 601: Retención Ingresos Brutos
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c601', '601', 'RETENCION IIBB', 'RETENCION_IMPOSITIVA', 600, FALSE, FALSE);

-- 602: Retención Ganancias (Locación de Servicios)
INSERT INTO ConceptoRecibo (id, codigo, descripcion, tipo, ordenCalculo, esProporcionalAInasistencias, esVariableNovedad)
VALUES ('c602', '602', 'RET. GANANCIAS', 'RETENCION_IMPOSITIVA', 601, FALSE, FALSE);

-- FIN SCRIPT
