package com.sta.biometric.seed;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.openxava.jpa.XPersistence;

import com.sta.biometric.auxiliares.Partidos;
import com.sta.biometric.auxiliares.Provincias;

/**
 * Servicio de carga inicial de datos para la entidad Partidos.
 * 
 * Este servicio es UNIVERSAL y funciona con cualquier base de datos
 * soportada por Hibernate (HSQLDB, MySQL, PostgreSQL, Oracle, etc.)
 * 
 * IMPORTANTE: Requiere que las Provincias ya estén cargadas.
 * Ejecutar DataSeedProvincias.seed() primero.
 * 
 * Uso:
 * - Llamar a DataSeedPartidos.seed() para cargar todos los partidos
 * - Es idempotente: no inserta duplicados si el registro ya existe
 * 
 * @author Generado automáticamente
 */
public class DataSeedPartidos {

    private static final Logger log = Logger.getLogger(DataSeedPartidos.class.getName());

    /** Clase interna para representar un partido con su provincia */
    private static class PartidoData {
        final int numero;
        final String nombre;
        final int provinciaNumero;

        PartidoData(int numero, String nombre, int provinciaNumero) {
            this.numero = numero;
            this.nombre = nombre;
            this.provinciaNumero = provinciaNumero;
        }
    }

    /** Lista de todos los partidos argentinos */
    private static final List<PartidoData> PARTIDOS = new ArrayList<>();

    static {
        // BUENOS AIRES (135 partidos)
        PARTIDOS.add(new PartidoData(1, "25 DE MAYO", 1));
        PARTIDOS.add(new PartidoData(2, "9 DE JULIO", 1));
        PARTIDOS.add(new PartidoData(3, "ADOLFO ALSINA", 1));
        PARTIDOS.add(new PartidoData(4, "ADOLFO GONZALES CHAVES", 1));
        PARTIDOS.add(new PartidoData(5, "ALBERTI", 1));
        PARTIDOS.add(new PartidoData(6, "ALMIRANTE BROWN", 1));
        PARTIDOS.add(new PartidoData(7, "ARRECIFES", 1));
        PARTIDOS.add(new PartidoData(8, "AVELLANEDA", 1));
        PARTIDOS.add(new PartidoData(9, "AYACUCHO", 1));
        PARTIDOS.add(new PartidoData(10, "AZUL", 1));
        PARTIDOS.add(new PartidoData(11, "BAHIA BLANCA", 1));
        PARTIDOS.add(new PartidoData(12, "BALCARCE", 1));
        PARTIDOS.add(new PartidoData(13, "BARADERO", 1));
        PARTIDOS.add(new PartidoData(14, "BENITO JUAREZ", 1));
        PARTIDOS.add(new PartidoData(15, "BERAZATEGUI", 1));
        PARTIDOS.add(new PartidoData(16, "BERISSO", 1));
        PARTIDOS.add(new PartidoData(17, "BOLIVAR", 1));
        PARTIDOS.add(new PartidoData(18, "BRAGADO", 1));
        PARTIDOS.add(new PartidoData(19, "BRANDSEN", 1));
        PARTIDOS.add(new PartidoData(20, "CAMPANA", 1));
        PARTIDOS.add(new PartidoData(21, "CAÑUELAS", 1));
        PARTIDOS.add(new PartidoData(22, "CAPITAN SARMIENTO", 1));
        PARTIDOS.add(new PartidoData(23, "CARLOS CASARES", 1));
        PARTIDOS.add(new PartidoData(24, "CARLOS TEJEDOR", 1));
        PARTIDOS.add(new PartidoData(25, "CARMEN DE ARECO", 1));
        PARTIDOS.add(new PartidoData(26, "CASTELLI", 1));
        PARTIDOS.add(new PartidoData(27, "CHACABUCO", 1));
        PARTIDOS.add(new PartidoData(28, "CHASCOMUS", 1));
        PARTIDOS.add(new PartidoData(29, "CHIVILCOY", 1));
        PARTIDOS.add(new PartidoData(30, "COLON", 1));
        PARTIDOS.add(new PartidoData(31, "CORONEL DE MARINA LEONARDO ROSALES", 1));
        PARTIDOS.add(new PartidoData(32, "CORONEL DORREGO", 1));
        PARTIDOS.add(new PartidoData(33, "CORONEL PRINGLES", 1));
        PARTIDOS.add(new PartidoData(34, "CORONEL SUAREZ", 1));
        PARTIDOS.add(new PartidoData(35, "DAIREAUX", 1));
        PARTIDOS.add(new PartidoData(36, "DOLORES", 1));
        PARTIDOS.add(new PartidoData(37, "ENSENADA", 1));
        PARTIDOS.add(new PartidoData(38, "ESCOBAR", 1));
        PARTIDOS.add(new PartidoData(39, "ESTEBAN ECHEVERRIA", 1));
        PARTIDOS.add(new PartidoData(40, "EXALTACION DE LA CRUZ", 1));
        PARTIDOS.add(new PartidoData(41, "EZEIZA", 1));
        PARTIDOS.add(new PartidoData(42, "FLORENCIO VARELA", 1));
        PARTIDOS.add(new PartidoData(43, "FLORENTINO AMEGHINO", 1));
        PARTIDOS.add(new PartidoData(44, "GENERAL ALVARADO", 1));
        PARTIDOS.add(new PartidoData(45, "GENERAL ALVEAR", 1));
        PARTIDOS.add(new PartidoData(46, "GENERAL ARENALES", 1));
        PARTIDOS.add(new PartidoData(47, "GENERAL BELGRANO", 1));
        PARTIDOS.add(new PartidoData(48, "GENERAL GUIDO", 1));
        PARTIDOS.add(new PartidoData(49, "GENERAL JUAN MADARIAGA", 1));
        PARTIDOS.add(new PartidoData(50, "GENERAL LA MADRID", 1));
        PARTIDOS.add(new PartidoData(51, "GENERAL LAS HERAS", 1));
        PARTIDOS.add(new PartidoData(52, "GENERAL LAVALLE", 1));
        PARTIDOS.add(new PartidoData(53, "GENERAL PAZ", 1));
        PARTIDOS.add(new PartidoData(54, "GENERAL PINTO", 1));
        PARTIDOS.add(new PartidoData(55, "GENERAL PUEYRREDON", 1));
        PARTIDOS.add(new PartidoData(56, "GENERAL RODRIGUEZ", 1));
        PARTIDOS.add(new PartidoData(57, "GENERAL SAN MARTIN", 1));
        PARTIDOS.add(new PartidoData(58, "GENERAL VIAMONTE", 1));
        PARTIDOS.add(new PartidoData(59, "GENERAL VILLEGAS", 1));
        PARTIDOS.add(new PartidoData(60, "GUAMINI", 1));
        PARTIDOS.add(new PartidoData(61, "HIPOLITO YRIGOYEN", 1));
        PARTIDOS.add(new PartidoData(62, "HURLINGHAM", 1));
        PARTIDOS.add(new PartidoData(63, "ITUZAINGO", 1));
        PARTIDOS.add(new PartidoData(64, "JOSE C. PAZ", 1));
        PARTIDOS.add(new PartidoData(65, "JUNIN", 1));
        PARTIDOS.add(new PartidoData(66, "LA COSTA", 1));
        PARTIDOS.add(new PartidoData(67, "LA MATANZA", 1));
        PARTIDOS.add(new PartidoData(68, "LA PLATA", 1));
        PARTIDOS.add(new PartidoData(69, "LANUS", 1));
        PARTIDOS.add(new PartidoData(70, "LAPRIDA", 1));
        PARTIDOS.add(new PartidoData(71, "LAS FLORES", 1));
        PARTIDOS.add(new PartidoData(72, "LEANDRO N. ALEM", 1));
        PARTIDOS.add(new PartidoData(73, "LEZAMA", 1));
        PARTIDOS.add(new PartidoData(74, "LINCOLN", 1));
        PARTIDOS.add(new PartidoData(75, "LOBERIA", 1));
        PARTIDOS.add(new PartidoData(76, "LOBOS", 1));
        PARTIDOS.add(new PartidoData(77, "LOMAS DE ZAMORA", 1));
        PARTIDOS.add(new PartidoData(78, "LUJAN", 1));
        PARTIDOS.add(new PartidoData(79, "MAGDALENA", 1));
        PARTIDOS.add(new PartidoData(80, "MAIPU", 1));
        PARTIDOS.add(new PartidoData(81, "MALVINAS ARGENTINAS", 1));
        PARTIDOS.add(new PartidoData(82, "MAR CHIQUITA", 1));
        PARTIDOS.add(new PartidoData(83, "MARCOS PAZ", 1));
        PARTIDOS.add(new PartidoData(84, "MERCEDES", 1));
        PARTIDOS.add(new PartidoData(85, "MERLO", 1));
        PARTIDOS.add(new PartidoData(86, "MONTE", 1));
        PARTIDOS.add(new PartidoData(87, "MONTE HERMOSO", 1));
        PARTIDOS.add(new PartidoData(88, "MORENO", 1));
        PARTIDOS.add(new PartidoData(89, "MORON", 1));
        PARTIDOS.add(new PartidoData(90, "NAVARRO", 1));
        PARTIDOS.add(new PartidoData(91, "NECOCHEA", 1));
        PARTIDOS.add(new PartidoData(92, "OLAVARRIA", 1));
        PARTIDOS.add(new PartidoData(93, "PATAGONES", 1));
        PARTIDOS.add(new PartidoData(94, "PEHUAJO", 1));
        PARTIDOS.add(new PartidoData(95, "PELLEGRINI", 1));
        PARTIDOS.add(new PartidoData(96, "PERGAMINO", 1));
        PARTIDOS.add(new PartidoData(97, "PILA", 1));
        PARTIDOS.add(new PartidoData(98, "PILAR", 1));
        PARTIDOS.add(new PartidoData(99, "PINAMAR", 1));
        PARTIDOS.add(new PartidoData(100, "PRESIDENTE PERON", 1));
        PARTIDOS.add(new PartidoData(101, "PUAN", 1));
        PARTIDOS.add(new PartidoData(102, "PUNTA INDIO", 1));
        PARTIDOS.add(new PartidoData(103, "QUILMES", 1));
        PARTIDOS.add(new PartidoData(104, "RAMALLO", 1));
        PARTIDOS.add(new PartidoData(105, "RAUCH", 1));
        PARTIDOS.add(new PartidoData(106, "RIVADAVIA", 1));
        PARTIDOS.add(new PartidoData(107, "ROJAS", 1));
        PARTIDOS.add(new PartidoData(108, "ROQUE PEREZ", 1));
        PARTIDOS.add(new PartidoData(109, "SAAVEDRA", 1));
        PARTIDOS.add(new PartidoData(110, "SALADILLO", 1));
        PARTIDOS.add(new PartidoData(111, "SALLIQUELO", 1));
        PARTIDOS.add(new PartidoData(112, "SALTO", 1));
        PARTIDOS.add(new PartidoData(113, "SAN ANDRES DE GILES", 1));
        PARTIDOS.add(new PartidoData(114, "SAN ANTONIO DE ARECO", 1));
        PARTIDOS.add(new PartidoData(115, "SAN CAYETANO", 1));
        PARTIDOS.add(new PartidoData(116, "SAN FERNANDO", 1));
        PARTIDOS.add(new PartidoData(117, "SAN ISIDRO", 1));
        PARTIDOS.add(new PartidoData(118, "SAN MIGUEL", 1));
        PARTIDOS.add(new PartidoData(119, "SAN NICOLAS", 1));
        PARTIDOS.add(new PartidoData(120, "SAN PEDRO", 1));
        PARTIDOS.add(new PartidoData(121, "SAN VICENTE", 1));
        PARTIDOS.add(new PartidoData(122, "SUIPACHA", 1));
        PARTIDOS.add(new PartidoData(123, "TANDIL", 1));
        PARTIDOS.add(new PartidoData(124, "TAPALQUE", 1));
        PARTIDOS.add(new PartidoData(125, "TIGRE", 1));
        PARTIDOS.add(new PartidoData(126, "TORDILLO", 1));
        PARTIDOS.add(new PartidoData(127, "TORNQUIST", 1));
        PARTIDOS.add(new PartidoData(128, "TRENQUE LAUQUEN", 1));
        PARTIDOS.add(new PartidoData(129, "TRES ARROYOS", 1));
        PARTIDOS.add(new PartidoData(130, "TRES DE FEBRERO", 1));
        PARTIDOS.add(new PartidoData(131, "TRES LOMAS", 1));
        PARTIDOS.add(new PartidoData(132, "VICENTE LOPEZ", 1));
        PARTIDOS.add(new PartidoData(133, "VILLA GESELL", 1));
        PARTIDOS.add(new PartidoData(134, "VILLARINO", 1));
        PARTIDOS.add(new PartidoData(135, "ZARATE", 1));

        // CATAMARCA (16 partidos)
        PARTIDOS.add(new PartidoData(136, "AMBATO", 2));
        PARTIDOS.add(new PartidoData(137, "ANCASTI", 2));
        PARTIDOS.add(new PartidoData(138, "ANDALGALA", 2));
        PARTIDOS.add(new PartidoData(139, "ANTOFAGASTA DE LA SIERRA", 2));
        PARTIDOS.add(new PartidoData(140, "BELEN", 2));
        PARTIDOS.add(new PartidoData(141, "CAPAYAN", 2));
        PARTIDOS.add(new PartidoData(142, "CAPITAL", 2));
        PARTIDOS.add(new PartidoData(143, "EL ALTO", 2));
        PARTIDOS.add(new PartidoData(144, "FRAY MAMERTO ESQUIU", 2));
        PARTIDOS.add(new PartidoData(145, "LA PAZ", 2));
        PARTIDOS.add(new PartidoData(146, "PACLIN", 2));
        PARTIDOS.add(new PartidoData(147, "POMAN", 2));
        PARTIDOS.add(new PartidoData(148, "SANTA MARIA", 2));
        PARTIDOS.add(new PartidoData(149, "SANTA ROSA", 2));
        PARTIDOS.add(new PartidoData(150, "TINOGASTA", 2));
        PARTIDOS.add(new PartidoData(151, "VALLE VIEJO", 2));

        // CHACO (25 partidos)
        PARTIDOS.add(new PartidoData(152, "1° DE MAYO", 3));
        PARTIDOS.add(new PartidoData(153, "12 DE OCTUBRE", 3));
        PARTIDOS.add(new PartidoData(154, "2 DE ABRIL", 3));
        PARTIDOS.add(new PartidoData(155, "25 DE MAYO", 3));
        PARTIDOS.add(new PartidoData(156, "9 DE JULIO", 3));
        PARTIDOS.add(new PartidoData(157, "ALMIRANTE BROWN", 3));
        PARTIDOS.add(new PartidoData(158, "BERMEJO", 3));
        PARTIDOS.add(new PartidoData(159, "CHACABUCO", 3));
        PARTIDOS.add(new PartidoData(160, "COMANDANTE FERNANDEZ", 3));
        PARTIDOS.add(new PartidoData(161, "FRAY JUSTO SANTA MARIA DE ORO", 3));
        PARTIDOS.add(new PartidoData(162, "GENERAL BELGRANO", 3));
        PARTIDOS.add(new PartidoData(163, "GENERAL DONOVAN", 3));
        PARTIDOS.add(new PartidoData(164, "GENERAL GÜEMES", 3));
        PARTIDOS.add(new PartidoData(165, "INDEPENDENCIA", 3));
        PARTIDOS.add(new PartidoData(166, "LIBERTAD", 3));
        PARTIDOS.add(new PartidoData(167, "LIBERTADOR GENERAL SAN MARTIN", 3));
        PARTIDOS.add(new PartidoData(168, "MAIPU", 3));
        PARTIDOS.add(new PartidoData(169, "MAYOR LUIS J. FONTANA", 3));
        PARTIDOS.add(new PartidoData(170, "O'HIGGINS", 3));
        PARTIDOS.add(new PartidoData(171, "PRESIDENCIA DE LA PLAZA", 3));
        PARTIDOS.add(new PartidoData(172, "QUITILIPI", 3));
        PARTIDOS.add(new PartidoData(173, "SAN FERNANDO", 3));
        PARTIDOS.add(new PartidoData(174, "SAN LORENZO", 3));
        PARTIDOS.add(new PartidoData(175, "SARGENTO CABRAL", 3));
        PARTIDOS.add(new PartidoData(176, "TAPENAGA", 3));

        // CHUBUT (15 partidos)
        PARTIDOS.add(new PartidoData(177, "BIEDMA", 4));
        PARTIDOS.add(new PartidoData(178, "CUSHAMEN", 4));
        PARTIDOS.add(new PartidoData(179, "ESCALANTE", 4));
        PARTIDOS.add(new PartidoData(180, "FLORENTINO AMEGHINO", 4));
        PARTIDOS.add(new PartidoData(181, "FUTALEUFU", 4));
        PARTIDOS.add(new PartidoData(182, "GAIMAN", 4));
        PARTIDOS.add(new PartidoData(183, "GASTRE", 4));
        PARTIDOS.add(new PartidoData(184, "LANGUIÑEO", 4));
        PARTIDOS.add(new PartidoData(185, "MARTIRES", 4));
        PARTIDOS.add(new PartidoData(186, "PASO DE INDIOS", 4));
        PARTIDOS.add(new PartidoData(187, "RAWSON", 4));
        PARTIDOS.add(new PartidoData(188, "RIO SENGUER", 4));
        PARTIDOS.add(new PartidoData(189, "SARMIENTO", 4));
        PARTIDOS.add(new PartidoData(190, "TEHUELCHES", 4));
        PARTIDOS.add(new PartidoData(191, "TELSEN", 4));

        // CABA (15 comunas)
        PARTIDOS.add(new PartidoData(192, "COMUNA 1", 5));
        PARTIDOS.add(new PartidoData(193, "COMUNA 10", 5));
        PARTIDOS.add(new PartidoData(194, "COMUNA 11", 5));
        PARTIDOS.add(new PartidoData(195, "COMUNA 12", 5));
        PARTIDOS.add(new PartidoData(196, "COMUNA 13", 5));
        PARTIDOS.add(new PartidoData(197, "COMUNA 14", 5));
        PARTIDOS.add(new PartidoData(198, "COMUNA 15", 5));
        PARTIDOS.add(new PartidoData(199, "COMUNA 2", 5));
        PARTIDOS.add(new PartidoData(200, "COMUNA 3", 5));
        PARTIDOS.add(new PartidoData(201, "COMUNA 4", 5));
        PARTIDOS.add(new PartidoData(202, "COMUNA 5", 5));
        PARTIDOS.add(new PartidoData(203, "COMUNA 6", 5));
        PARTIDOS.add(new PartidoData(204, "COMUNA 7", 5));
        PARTIDOS.add(new PartidoData(205, "COMUNA 8", 5));
        PARTIDOS.add(new PartidoData(206, "COMUNA 9", 5));

        // CORDOBA (26 departamentos)
        PARTIDOS.add(new PartidoData(207, "CALAMUCHITA", 6));
        PARTIDOS.add(new PartidoData(208, "CAPITAL", 6));
        PARTIDOS.add(new PartidoData(209, "COLON", 6));
        PARTIDOS.add(new PartidoData(210, "CRUZ DEL EJE", 6));
        PARTIDOS.add(new PartidoData(211, "GENERAL ROCA", 6));
        PARTIDOS.add(new PartidoData(212, "GENERAL SAN MARTIN", 6));
        PARTIDOS.add(new PartidoData(213, "ISCHILIN", 6));
        PARTIDOS.add(new PartidoData(214, "JUAREZ CELMAN", 6));
        PARTIDOS.add(new PartidoData(215, "MARCOS JUAREZ", 6));
        PARTIDOS.add(new PartidoData(216, "MINAS", 6));
        PARTIDOS.add(new PartidoData(217, "POCHO", 6));
        PARTIDOS.add(new PartidoData(218, "PRESIDENTE ROQUE SAENZ PEÑA", 6));
        PARTIDOS.add(new PartidoData(219, "PUNILLA", 6));
        PARTIDOS.add(new PartidoData(220, "RIO CUARTO", 6));
        PARTIDOS.add(new PartidoData(221, "RIO PRIMERO", 6));
        PARTIDOS.add(new PartidoData(222, "RIO SECO", 6));
        PARTIDOS.add(new PartidoData(223, "RIO SEGUNDO", 6));
        PARTIDOS.add(new PartidoData(224, "SAN ALBERTO", 6));
        PARTIDOS.add(new PartidoData(225, "SAN JAVIER", 6));
        PARTIDOS.add(new PartidoData(226, "SAN JUSTO", 6));
        PARTIDOS.add(new PartidoData(227, "SANTA MARIA", 6));
        PARTIDOS.add(new PartidoData(228, "SOBREMONTE", 6));
        PARTIDOS.add(new PartidoData(229, "TERCERO ARRIBA", 6));
        PARTIDOS.add(new PartidoData(230, "TOTORAL", 6));
        PARTIDOS.add(new PartidoData(231, "TULUMBA", 6));
        PARTIDOS.add(new PartidoData(232, "UNION", 6));

        // CORRIENTES (25 departamentos)
        PARTIDOS.add(new PartidoData(233, "BELLA VISTA", 7));
        PARTIDOS.add(new PartidoData(234, "BERON DE ASTRADA", 7));
        PARTIDOS.add(new PartidoData(235, "CAPITAL", 7));
        PARTIDOS.add(new PartidoData(236, "CONCEPCION", 7));
        PARTIDOS.add(new PartidoData(237, "CURUZU CUATIA", 7));
        PARTIDOS.add(new PartidoData(238, "EMPEDRADO", 7));
        PARTIDOS.add(new PartidoData(239, "ESQUINA", 7));
        PARTIDOS.add(new PartidoData(240, "GENERAL ALVEAR", 7));
        PARTIDOS.add(new PartidoData(241, "GENERAL PAZ", 7));
        PARTIDOS.add(new PartidoData(242, "GOYA", 7));
        PARTIDOS.add(new PartidoData(243, "ITATI", 7));
        PARTIDOS.add(new PartidoData(244, "ITUZAINGO", 7));
        PARTIDOS.add(new PartidoData(245, "LAVALLE", 7));
        PARTIDOS.add(new PartidoData(246, "MBURUCUYA", 7));
        PARTIDOS.add(new PartidoData(247, "MERCEDES", 7));
        PARTIDOS.add(new PartidoData(248, "MONTE CASEROS", 7));
        PARTIDOS.add(new PartidoData(249, "PASO DE LOS LIBRES", 7));
        PARTIDOS.add(new PartidoData(250, "SALADAS", 7));
        PARTIDOS.add(new PartidoData(251, "SAN COSME", 7));
        PARTIDOS.add(new PartidoData(252, "SAN LUIS DEL PALMAR", 7));
        PARTIDOS.add(new PartidoData(253, "SAN MARTIN", 7));
        PARTIDOS.add(new PartidoData(254, "SAN MIGUEL", 7));
        PARTIDOS.add(new PartidoData(255, "SAN ROQUE", 7));
        PARTIDOS.add(new PartidoData(256, "SANTO TOME", 7));
        PARTIDOS.add(new PartidoData(257, "SAUCE", 7));

        // ENTRE RIOS (17 departamentos)
        PARTIDOS.add(new PartidoData(258, "COLON", 8));
        PARTIDOS.add(new PartidoData(259, "CONCORDIA", 8));
        PARTIDOS.add(new PartidoData(260, "DIAMANTE", 8));
        PARTIDOS.add(new PartidoData(261, "FEDERACION", 8));
        PARTIDOS.add(new PartidoData(262, "FEDERAL", 8));
        PARTIDOS.add(new PartidoData(263, "FELICIANO", 8));
        PARTIDOS.add(new PartidoData(264, "GUALEGUAY", 8));
        PARTIDOS.add(new PartidoData(265, "GUALEGUAYCHU", 8));
        PARTIDOS.add(new PartidoData(266, "ISLAS DEL IBICUY", 8));
        PARTIDOS.add(new PartidoData(267, "LA PAZ", 8));
        PARTIDOS.add(new PartidoData(268, "NOGOYA", 8));
        PARTIDOS.add(new PartidoData(269, "PARANA", 8));
        PARTIDOS.add(new PartidoData(270, "SAN SALVADOR", 8));
        PARTIDOS.add(new PartidoData(271, "TALA", 8));
        PARTIDOS.add(new PartidoData(272, "URUGUAY", 8));
        PARTIDOS.add(new PartidoData(273, "VICTORIA", 8));
        PARTIDOS.add(new PartidoData(274, "VILLAGUAY", 8));

        // FORMOSA (9 departamentos)
        PARTIDOS.add(new PartidoData(275, "BERMEJO", 9));
        PARTIDOS.add(new PartidoData(276, "FORMOSA", 9));
        PARTIDOS.add(new PartidoData(277, "LAISHI", 9));
        PARTIDOS.add(new PartidoData(278, "MATACOS", 9));
        PARTIDOS.add(new PartidoData(279, "PATIÑO", 9));
        PARTIDOS.add(new PartidoData(280, "PILAGAS", 9));
        PARTIDOS.add(new PartidoData(281, "PILCOMAYO", 9));
        PARTIDOS.add(new PartidoData(282, "PIRANE", 9));
        PARTIDOS.add(new PartidoData(283, "RAMON LISTA", 9));

        // JUJUY (16 departamentos)
        PARTIDOS.add(new PartidoData(284, "COCHINOCA", 10));
        PARTIDOS.add(new PartidoData(285, "DR. MANUEL BELGRANO", 10));
        PARTIDOS.add(new PartidoData(286, "EL CARMEN", 10));
        PARTIDOS.add(new PartidoData(287, "HUMAHUACA", 10));
        PARTIDOS.add(new PartidoData(288, "LEDESMA", 10));
        PARTIDOS.add(new PartidoData(289, "PALPALA", 10));
        PARTIDOS.add(new PartidoData(290, "RINCONADA", 10));
        PARTIDOS.add(new PartidoData(291, "SAN ANTONIO", 10));
        PARTIDOS.add(new PartidoData(292, "SAN PEDRO", 10));
        PARTIDOS.add(new PartidoData(293, "SANTA BARBARA", 10));
        PARTIDOS.add(new PartidoData(294, "SANTA CATALINA", 10));
        PARTIDOS.add(new PartidoData(295, "SUSQUES", 10));
        PARTIDOS.add(new PartidoData(296, "TILCARA", 10));
        PARTIDOS.add(new PartidoData(297, "TUMBAYA", 10));
        PARTIDOS.add(new PartidoData(298, "VALLE GRANDE", 10));
        PARTIDOS.add(new PartidoData(299, "YAVI", 10));

        // LA PAMPA (22 departamentos)
        PARTIDOS.add(new PartidoData(300, "ATREUCO", 11));
        PARTIDOS.add(new PartidoData(301, "CALEU CALEU", 11));
        PARTIDOS.add(new PartidoData(302, "CAPITAL", 11));
        PARTIDOS.add(new PartidoData(303, "CATRILO", 11));
        PARTIDOS.add(new PartidoData(304, "CHALILEO", 11));
        PARTIDOS.add(new PartidoData(305, "CHAPALEUFU", 11));
        PARTIDOS.add(new PartidoData(306, "CHICAL CO", 11));
        PARTIDOS.add(new PartidoData(307, "CONHELO", 11));
        PARTIDOS.add(new PartidoData(308, "CURACO", 11));
        PARTIDOS.add(new PartidoData(309, "GUATRACHE", 11));
        PARTIDOS.add(new PartidoData(310, "HUCAL", 11));
        PARTIDOS.add(new PartidoData(311, "LIHUEL CALEL", 11));
        PARTIDOS.add(new PartidoData(312, "LIMAY MAHUIDA", 11));
        PARTIDOS.add(new PartidoData(313, "LOVENTUE", 11));
        PARTIDOS.add(new PartidoData(314, "MARACO", 11));
        PARTIDOS.add(new PartidoData(315, "PUELEN", 11));
        PARTIDOS.add(new PartidoData(316, "QUEMU QUEMU", 11));
        PARTIDOS.add(new PartidoData(317, "RANCUL", 11));
        PARTIDOS.add(new PartidoData(318, "REALICO", 11));
        PARTIDOS.add(new PartidoData(319, "TOAY", 11));
        PARTIDOS.add(new PartidoData(320, "TRENEL", 11));
        PARTIDOS.add(new PartidoData(321, "UTRACAN", 11));

        // LA RIOJA (18 departamentos)
        PARTIDOS.add(new PartidoData(322, "ANGEL VICENTE PEÑALOZA", 12));
        PARTIDOS.add(new PartidoData(323, "ARAUCO", 12));
        PARTIDOS.add(new PartidoData(324, "CAPITAL", 12));
        PARTIDOS.add(new PartidoData(325, "CASTRO BARROS", 12));
        PARTIDOS.add(new PartidoData(326, "CHAMICAL", 12));
        PARTIDOS.add(new PartidoData(327, "CHILECITO", 12));
        PARTIDOS.add(new PartidoData(328, "FAMATINA", 12));
        PARTIDOS.add(new PartidoData(329, "GENERAL BELGRANO", 12));
        PARTIDOS.add(new PartidoData(330, "GENERAL FELIPE VARELA", 12));
        PARTIDOS.add(new PartidoData(331, "GENERAL JUAN FACUNDO QUIROGA", 12));
        PARTIDOS.add(new PartidoData(332, "GENERAL LAMADRID", 12));
        PARTIDOS.add(new PartidoData(333, "GENERAL ORTIZ DE OCAMPO", 12));
        PARTIDOS.add(new PartidoData(334, "GENERAL SAN MARTIN", 12));
        PARTIDOS.add(new PartidoData(335, "INDEPENDENCIA", 12));
        PARTIDOS.add(new PartidoData(336, "ROSARIO VERA PEÑALOZA", 12));
        PARTIDOS.add(new PartidoData(337, "SAN BLAS DE LOS SAUCES", 12));
        PARTIDOS.add(new PartidoData(338, "SANAGASTA", 12));
        PARTIDOS.add(new PartidoData(339, "VINCHINA", 12));

        // MENDOZA (18 departamentos)
        PARTIDOS.add(new PartidoData(340, "CAPITAL", 13));
        PARTIDOS.add(new PartidoData(341, "GENERAL ALVEAR", 13));
        PARTIDOS.add(new PartidoData(342, "GODOY CRUZ", 13));
        PARTIDOS.add(new PartidoData(343, "GUAYMALLEN", 13));
        PARTIDOS.add(new PartidoData(344, "JUNIN", 13));
        PARTIDOS.add(new PartidoData(345, "LA PAZ", 13));
        PARTIDOS.add(new PartidoData(346, "LAS HERAS", 13));
        PARTIDOS.add(new PartidoData(347, "LAVALLE", 13));
        PARTIDOS.add(new PartidoData(348, "LUJAN DE CUYO", 13));
        PARTIDOS.add(new PartidoData(349, "MAIPU", 13));
        PARTIDOS.add(new PartidoData(350, "MALARGÜE", 13));
        PARTIDOS.add(new PartidoData(351, "RIVADAVIA", 13));
        PARTIDOS.add(new PartidoData(352, "SAN CARLOS", 13));
        PARTIDOS.add(new PartidoData(353, "SAN MARTIN", 13));
        PARTIDOS.add(new PartidoData(354, "SAN RAFAEL", 13));
        PARTIDOS.add(new PartidoData(355, "SANTA ROSA", 13));
        PARTIDOS.add(new PartidoData(356, "TUNUYAN", 13));
        PARTIDOS.add(new PartidoData(357, "TUPUNGATO", 13));

        // MISIONES (17 departamentos)
        PARTIDOS.add(new PartidoData(358, "25 DE MAYO", 14));
        PARTIDOS.add(new PartidoData(359, "APOSTOLES", 14));
        PARTIDOS.add(new PartidoData(360, "CAINGUAS", 14));
        PARTIDOS.add(new PartidoData(361, "CANDELARIA", 14));
        PARTIDOS.add(new PartidoData(362, "CAPITAL", 14));
        PARTIDOS.add(new PartidoData(363, "CONCEPCION", 14));
        PARTIDOS.add(new PartidoData(364, "ELDORADO", 14));
        PARTIDOS.add(new PartidoData(365, "GENERAL MANUEL BELGRANO", 14));
        PARTIDOS.add(new PartidoData(366, "GUARANI", 14));
        PARTIDOS.add(new PartidoData(367, "IGUAZU", 14));
        PARTIDOS.add(new PartidoData(368, "LEANDRO N. ALEM", 14));
        PARTIDOS.add(new PartidoData(369, "LIBERTADOR GENERAL SAN MARTIN", 14));
        PARTIDOS.add(new PartidoData(370, "MONTECARLO", 14));
        PARTIDOS.add(new PartidoData(371, "OBERA", 14));
        PARTIDOS.add(new PartidoData(372, "SAN IGNACIO", 14));
        PARTIDOS.add(new PartidoData(373, "SAN JAVIER", 14));
        PARTIDOS.add(new PartidoData(374, "SAN PEDRO", 14));

        // NEUQUEN (16 departamentos)
        PARTIDOS.add(new PartidoData(375, "ALUMINE", 15));
        PARTIDOS.add(new PartidoData(376, "AÑELO", 15));
        PARTIDOS.add(new PartidoData(377, "CATAN LIL", 15));
        PARTIDOS.add(new PartidoData(378, "CHOS MALAL", 15));
        PARTIDOS.add(new PartidoData(379, "COLLON CURA", 15));
        PARTIDOS.add(new PartidoData(380, "CONFLUENCIA", 15));
        PARTIDOS.add(new PartidoData(381, "HUILICHES", 15));
        PARTIDOS.add(new PartidoData(382, "LACAR", 15));
        PARTIDOS.add(new PartidoData(383, "LONCOPUE", 15));
        PARTIDOS.add(new PartidoData(384, "LOS LAGOS", 15));
        PARTIDOS.add(new PartidoData(385, "MINAS", 15));
        PARTIDOS.add(new PartidoData(386, "ÑORQUIN", 15));
        PARTIDOS.add(new PartidoData(387, "PEHUENCHES", 15));
        PARTIDOS.add(new PartidoData(388, "PICUN LEUFU", 15));
        PARTIDOS.add(new PartidoData(389, "PICUNCHES", 15));
        PARTIDOS.add(new PartidoData(390, "ZAPALA", 15));

        // RIO NEGRO (13 departamentos)
        PARTIDOS.add(new PartidoData(391, "25 DE MAYO", 16));
        PARTIDOS.add(new PartidoData(392, "9 DE JULIO", 16));
        PARTIDOS.add(new PartidoData(393, "ADOLFO ALSINA", 16));
        PARTIDOS.add(new PartidoData(394, "AVELLANEDA", 16));
        PARTIDOS.add(new PartidoData(395, "BARILOCHE", 16));
        PARTIDOS.add(new PartidoData(396, "CONESA", 16));
        PARTIDOS.add(new PartidoData(397, "EL CUY", 16));
        PARTIDOS.add(new PartidoData(398, "GENERAL ROCA", 16));
        PARTIDOS.add(new PartidoData(399, "ÑORQUINCO", 16));
        PARTIDOS.add(new PartidoData(400, "PICHI MAHUIDA", 16));
        PARTIDOS.add(new PartidoData(401, "PILCANIYEU", 16));
        PARTIDOS.add(new PartidoData(402, "SAN ANTONIO", 16));
        PARTIDOS.add(new PartidoData(403, "VALCHETA", 16));

        // SALTA (23 departamentos)
        PARTIDOS.add(new PartidoData(404, "ANTA", 17));
        PARTIDOS.add(new PartidoData(405, "CACHI", 17));
        PARTIDOS.add(new PartidoData(406, "CAFAYATE", 17));
        PARTIDOS.add(new PartidoData(407, "CAPITAL", 17));
        PARTIDOS.add(new PartidoData(408, "CERRILLOS", 17));
        PARTIDOS.add(new PartidoData(409, "CHICOANA", 17));
        PARTIDOS.add(new PartidoData(410, "GENERAL GÜEMES", 17));
        PARTIDOS.add(new PartidoData(411, "GENERAL JOSE DE SAN MARTIN", 17));
        PARTIDOS.add(new PartidoData(412, "GUACHIPAS", 17));
        PARTIDOS.add(new PartidoData(413, "IRUYA", 17));
        PARTIDOS.add(new PartidoData(414, "LA CALDERA", 17));
        PARTIDOS.add(new PartidoData(415, "LA CANDELARIA", 17));
        PARTIDOS.add(new PartidoData(416, "LA POMA", 17));
        PARTIDOS.add(new PartidoData(417, "LA VIÑA", 17));
        PARTIDOS.add(new PartidoData(418, "LOS ANDES", 17));
        PARTIDOS.add(new PartidoData(419, "METAN", 17));
        PARTIDOS.add(new PartidoData(420, "MOLINOS", 17));
        PARTIDOS.add(new PartidoData(421, "ORAN", 17));
        PARTIDOS.add(new PartidoData(422, "RIVADAVIA", 17));
        PARTIDOS.add(new PartidoData(423, "ROSARIO DE LA FRONTERA", 17));
        PARTIDOS.add(new PartidoData(424, "ROSARIO DE LERMA", 17));
        PARTIDOS.add(new PartidoData(425, "SAN CARLOS", 17));
        PARTIDOS.add(new PartidoData(426, "SANTA VICTORIA", 17));

        // SAN JUAN (19 departamentos)
        PARTIDOS.add(new PartidoData(427, "25 DE MAYO", 18));
        PARTIDOS.add(new PartidoData(428, "9 DE JULIO", 18));
        PARTIDOS.add(new PartidoData(429, "ALBARDON", 18));
        PARTIDOS.add(new PartidoData(430, "ANGACO", 18));
        PARTIDOS.add(new PartidoData(431, "CALINGASTA", 18));
        PARTIDOS.add(new PartidoData(432, "CAPITAL", 18));
        PARTIDOS.add(new PartidoData(433, "CAUCETE", 18));
        PARTIDOS.add(new PartidoData(434, "CHIMBAS", 18));
        PARTIDOS.add(new PartidoData(435, "IGLESIA", 18));
        PARTIDOS.add(new PartidoData(436, "JACHAL", 18));
        PARTIDOS.add(new PartidoData(437, "POCITO", 18));
        PARTIDOS.add(new PartidoData(438, "RAWSON", 18));
        PARTIDOS.add(new PartidoData(439, "RIVADAVIA", 18));
        PARTIDOS.add(new PartidoData(440, "SAN MARTIN", 18));
        PARTIDOS.add(new PartidoData(441, "SANTA LUCIA", 18));
        PARTIDOS.add(new PartidoData(442, "SARMIENTO", 18));
        PARTIDOS.add(new PartidoData(443, "ULLUM", 18));
        PARTIDOS.add(new PartidoData(444, "VALLE FERTIL", 18));
        PARTIDOS.add(new PartidoData(445, "ZONDA", 18));

        // SAN LUIS (9 departamentos)
        PARTIDOS.add(new PartidoData(446, "AYACUCHO", 19));
        PARTIDOS.add(new PartidoData(447, "BELGRANO", 19));
        PARTIDOS.add(new PartidoData(448, "CHACABUCO", 19));
        PARTIDOS.add(new PartidoData(449, "CORONEL PRINGLES", 19));
        PARTIDOS.add(new PartidoData(450, "GENERAL PEDERNERA", 19));
        PARTIDOS.add(new PartidoData(451, "GOBERNADOR DUPUY", 19));
        PARTIDOS.add(new PartidoData(452, "JUAN MARTIN DE PUEYRREDON", 19));
        PARTIDOS.add(new PartidoData(453, "JUNIN", 19));
        PARTIDOS.add(new PartidoData(454, "LIBERTADOR GENERAL SAN MARTIN", 19));

        // SANTA FE (19 departamentos)
        PARTIDOS.add(new PartidoData(455, "9 DE JULIO", 20));
        PARTIDOS.add(new PartidoData(456, "BELGRANO", 20));
        PARTIDOS.add(new PartidoData(457, "CASEROS", 20));
        PARTIDOS.add(new PartidoData(458, "CASTELLANOS", 20));
        PARTIDOS.add(new PartidoData(459, "CONSTITUCION", 20));
        PARTIDOS.add(new PartidoData(460, "GARAY", 20));
        PARTIDOS.add(new PartidoData(461, "GENERAL LOPEZ", 20));
        PARTIDOS.add(new PartidoData(462, "GENERAL OBLIGADO", 20));
        PARTIDOS.add(new PartidoData(463, "IRIONDO", 20));
        PARTIDOS.add(new PartidoData(464, "LA CAPITAL", 20));
        PARTIDOS.add(new PartidoData(465, "LAS COLONIAS", 20));
        PARTIDOS.add(new PartidoData(466, "ROSARIO", 20));
        PARTIDOS.add(new PartidoData(467, "SAN CRISTOBAL", 20));
        PARTIDOS.add(new PartidoData(468, "SAN JAVIER", 20));
        PARTIDOS.add(new PartidoData(469, "SAN JERONIMO", 20));
        PARTIDOS.add(new PartidoData(470, "SAN JUSTO", 20));
        PARTIDOS.add(new PartidoData(471, "SAN LORENZO", 20));
        PARTIDOS.add(new PartidoData(472, "SAN MARTIN", 20));
        PARTIDOS.add(new PartidoData(473, "VERA", 20));

        // SANTIAGO DEL ESTERO (1 en datos originales)
        PARTIDOS.add(new PartidoData(474, "GUASAYAN", 21));

        // TIERRA DEL FUEGO (2 departamentos)
        PARTIDOS.add(new PartidoData(475, "RIO GRANDE", 22));
        PARTIDOS.add(new PartidoData(476, "USHUAIA", 22));

        // TUCUMAN (17 departamentos)
        PARTIDOS.add(new PartidoData(477, "BURRUYACU", 23));
        PARTIDOS.add(new PartidoData(478, "CAPITAL", 23));
        PARTIDOS.add(new PartidoData(479, "CHICLIGASTA", 23));
        PARTIDOS.add(new PartidoData(480, "CRUZ ALTA", 23));
        PARTIDOS.add(new PartidoData(481, "FAMAILLA", 23));
        PARTIDOS.add(new PartidoData(482, "GRANEROS", 23));
        PARTIDOS.add(new PartidoData(483, "JUAN BAUTISTA ALBERDI", 23));
        PARTIDOS.add(new PartidoData(484, "LA COCHA", 23));
        PARTIDOS.add(new PartidoData(485, "LEALES", 23));
        PARTIDOS.add(new PartidoData(486, "LULES", 23));
        PARTIDOS.add(new PartidoData(487, "MONTEROS", 23));
        PARTIDOS.add(new PartidoData(488, "RIO CHICO", 23));
        PARTIDOS.add(new PartidoData(489, "SIMOCA", 23));
        PARTIDOS.add(new PartidoData(490, "TAFI DEL VALLE", 23));
        PARTIDOS.add(new PartidoData(491, "TAFI VIEJO", 23));
        PARTIDOS.add(new PartidoData(492, "TRANCAS", 23));
        PARTIDOS.add(new PartidoData(493, "YERBA BUENA", 23));
    }

    /**
     * Ejecuta la carga de todos los partidos.
     * IMPORTANTE: Las provincias deben estar cargadas previamente.
     * Es idempotente: verifica si cada partido existe antes de insertarlo.
     * 
     * @return número de partidos insertados (no cuenta los existentes)
     */
    public static int seed() {
        log.info("Iniciando carga de datos de Partidos...");

        // Verificar que las provincias estén cargadas
        if (!DataSeedProvincias.isComplete()) {
            log.warning(
                    "Las provincias no están completamente cargadas. Ejecutando DataSeedProvincias.seed() primero...");
            DataSeedProvincias.seed();
        }

        EntityManager em = XPersistence.getManager();
        int insertados = 0;
        int existentes = 0;

        for (PartidoData data : PARTIDOS) {
            // Verificar si ya existe
            Partidos existente = em.find(Partidos.class, data.numero);

            if (existente == null) {
                // Obtener la provincia
                Provincias provincia = em.find(Provincias.class, data.provinciaNumero);
                if (provincia == null) {
                    log.warning("Provincia no encontrada: " + data.provinciaNumero + " para partido " + data.nombre);
                    continue;
                }

                // Crear nuevo partido
                Partidos nuevo = new Partidos();
                nuevo.setNumero(data.numero);
                nuevo.setNombre(data.nombre);
                nuevo.setProvincia(provincia);
                em.persist(nuevo);
                insertados++;
            } else {
                existentes++;
            }
        }

        log.info("Carga de Partidos completada. Insertados: " + insertados + ", Existentes: " + existentes);
        return insertados;
    }

    /**
     * Verifica si todos los partidos están cargados.
     * 
     * @return true si los 493 partidos existen
     */
    public static boolean isComplete() {
        EntityManager em = XPersistence.getManager();
        Long count = em.createQuery("SELECT COUNT(p) FROM Partidos p", Long.class)
                .getSingleResult();
        return count >= PARTIDOS.size();
    }

    /**
     * Obtiene la cantidad de partidos definidos.
     * 
     * @return número total de partidos (493)
     */
    public static int getPartidosCount() {
        return PARTIDOS.size();
    }
}
