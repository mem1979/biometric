package com.sta.biometric.servicios;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

import javax.persistence.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

import org.openxava.jpa.*; // Necesario para XPersistence

/**
 * Servicio unificado para interpretar y validar fichadas.
 * 
 * <p>
 * Proporciona:
 * <ul>
 * <li>Parseo de fechas y horas en múltiples formatos</li>
 * <li>Interpretación de tipos de movimiento (configurable desde
 * preferencias)</li>
 * <li>Búsqueda de empleados por userId</li>
 * <li>Validación de filas para importación</li>
 * <li>Normalización de registros</li>
 * </ul>
 * 
 * @author Sistema STARH
 * @version 2.0
 */
public class InterpreteFichadasService {

    // ==================================================================================
    // FORMATOS DE FECHA Y HORA
    // ==================================================================================

    private static final List<DateTimeFormatter> FORMATOS_FECHA = Arrays.asList(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("d/M/yy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yy"));

    private static final List<DateTimeFormatter> FORMATOS_HORA = Arrays.asList(
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH.mm.ss"),
            DateTimeFormatter.ofPattern("HH.mm"));

    // ==================================================================================
    // EQUIVALENCIAS DE TIPOS DE MOVIMIENTO (desde properties)
    // ==================================================================================

    private static final Map<TipoMovimiento, List<String>> equivalencias = new EnumMap<>(TipoMovimiento.class);

    static {
        Properties props = ConfiguracionesPreferencias.getInstance().getProperties();
        equivalencias.put(TipoMovimiento.ENTRADA, cargarListaDesdeProp(props, "tipos.entrada"));
        equivalencias.put(TipoMovimiento.SALIDA, cargarListaDesdeProp(props, "tipos.salida"));
        equivalencias.put(TipoMovimiento.PAUSA_INICIO, cargarListaDesdeProp(props, "tipos.pausa_inicio"));
        equivalencias.put(TipoMovimiento.PAUSA_FIN, cargarListaDesdeProp(props, "tipos.pausa_fin"));
        equivalencias.put(TipoMovimiento.UBICACION, cargarListaDesdeProp(props, "tipos.ubicacion"));
        equivalencias.put(TipoMovimiento.MANUAL, cargarListaDesdeProp(props, "tipos.manual"));

        // Valores por defecto si no están configurados en properties
        agregarValoresPorDefecto();
    }

    private static List<String> cargarListaDesdeProp(Properties props, String clave) {
        String raw = props.getProperty(clave, "");
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static void agregarValoresPorDefecto() {
        // Si las listas están vacías, agregar valores por defecto
        if (equivalencias.get(TipoMovimiento.ENTRADA).isEmpty()) {
            equivalencias.put(TipoMovimiento.ENTRADA,
                    Arrays.asList("ENTRADA", "INGRESO", "LLEGADA", "IN", "CHECK-IN", "CHECKIN", "E", "0"));
        }
        if (equivalencias.get(TipoMovimiento.SALIDA).isEmpty()) {
            equivalencias.put(TipoMovimiento.SALIDA,
                    Arrays.asList("SALIDA", "EGRESO", "OUT", "CHECK-OUT", "CHECKOUT", "S", "1"));
        }
        if (equivalencias.get(TipoMovimiento.PAUSA_INICIO).isEmpty()) {
            equivalencias.put(TipoMovimiento.PAUSA_INICIO,
                    Arrays.asList("PAUSA INICIO", "INICIO PAUSA", "BREAK START", "PAUSA_INICIO", "PI"));
        }
        if (equivalencias.get(TipoMovimiento.PAUSA_FIN).isEmpty()) {
            equivalencias.put(TipoMovimiento.PAUSA_FIN,
                    Arrays.asList("PAUSA FIN", "FIN PAUSA", "BREAK END", "PAUSA_FIN", "PF"));
        }
        if (equivalencias.get(TipoMovimiento.MANUAL).isEmpty()) {
            equivalencias.put(TipoMovimiento.MANUAL, Arrays.asList("MANUAL", "M"));
        }
    }

    // ==================================================================================
    // PARSEO DE FECHA
    // ==================================================================================

    /**
     * Parsea un texto a LocalDate probando múltiples formatos.
     */
    public static LocalDate parsearFecha(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        String limpio = texto.trim().replaceAll("[\"']", "").replaceAll("\\s+", "");

        // Intentar formato ISO primero
        try {
            return LocalDate.parse(limpio);
        } catch (DateTimeParseException e) {
            // Continuar
        }

        for (DateTimeFormatter fmt : FORMATOS_FECHA) {
            try {
                return LocalDate.parse(limpio, fmt);
            } catch (DateTimeParseException e) {
                // Continuar
            }
        }

        return null;
    }

    // ==================================================================================
    // PARSEO DE HORA
    // ==================================================================================

    /**
     * Parsea un texto a LocalTime probando múltiples formatos.
     */
    public static LocalTime parsearHora(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        String limpio = texto.trim().replaceAll("[\"']", "");

        try {
            return LocalTime.parse(limpio);
        } catch (DateTimeParseException e) {
            // Continuar
        }

        for (DateTimeFormatter fmt : FORMATOS_HORA) {
            try {
                return LocalTime.parse(limpio, fmt);
            } catch (DateTimeParseException e) {
                // Continuar
            }
        }

        return null;
    }

    // ==================================================================================
    // INTERPRETACIÓN DE TIPO DE MOVIMIENTO
    // ==================================================================================

    /**
     * Deduce el tipo de movimiento desde un texto flexible.
     * Usa las equivalencias configuradas en properties o los valores por defecto.
     */
    public static TipoMovimiento deducirTipoMovimiento(String texto) {
        if (texto == null || texto.isBlank())
            return null;
        String upper = texto.trim().toUpperCase();

        for (Map.Entry<TipoMovimiento, List<String>> entry : equivalencias.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (upper.contains(keyword) || keyword.contains(upper)) {
                    return entry.getKey();
                }
            }
        }

        // Intentar como enum directo
        try {
            return TipoMovimiento.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ==================================================================================
    // BÚSQUEDA DE EMPLEADO
    // ==================================================================================

    /**
     * Busca un empleado por su userId.
     */
    public static Personal buscarEmpleado(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }

        try {
            return XPersistence.getManager()
                    .createQuery("SELECT e FROM Personal e WHERE e.userId = :userId", Personal.class)
                    .setParameter("userId", userId.trim())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // ==================================================================================
    // NORMALIZACIÓN DE REGISTROS (LOTE)
    // ==================================================================================

    /**
     * Aplica deducción de tipo a una lista de ColeccionRegistros.
     * Solo modifica registros sin tipo asignado.
     */
    public static List<ColeccionRegistros> normalizar(List<ColeccionRegistros> crudos) {
        for (ColeccionRegistros r : crudos) {
            if (r.getTipoMovimiento() == null && r.getObservacion() != null) {
                TipoMovimiento tipo = deducirTipoMovimiento(r.getObservacion());
                if (tipo != null) {
                    r.setTipoMovimiento(tipo);
                } else {
                    r.setObservacion("Tipo no reconocido: " + r.getObservacion());
                }
            }
        }
        return crudos;
    }

    /**
     * Normaliza una secuencia de fichadas aplicando lógica contextual.
     * 
     * <p>
     * La regla es: ENTRADA → (PAUSA_INICIO → PAUSA_FIN)* → SALIDA
     * </p>
     * 
     * <ul>
     * <li>Preserva tipos específicos (PAUSA_INICIO, PAUSA_FIN) sin modificar</li>
     * <li>Solo procesa tipos genéricos (ENTRADA, SALIDA)</li>
     * <li>Para secuencias SALIDA -> ENTRADA:
     * <ul>
     * <li>Si diferencia < 4 horas: Es PAUSA (Almuerzo/Descanso)</li>
     * <li>Si diferencia >= 4 horas: Es CAMBIO DE TURNO (Fin jornada -> Inicio
     * siguiente)</li>
     * </ul>
     * </li>
     * </ul>
     * 
     * @param registros Lista de registros a normalizar (se modifica in-place)
     * @return La misma lista con tipos normalizados
     */
    public static List<ColeccionRegistros> normalizarSecuencia(List<ColeccionRegistros> registros) {
        if (registros == null || registros.size() < 2) {
            return registros;
        }

        // Ordenar por fecha y hora
        registros.sort(Comparator
                .comparing(ColeccionRegistros::getFecha, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(ColeccionRegistros::getHora, Comparator.nullsFirst(Comparator.naturalOrder())));

        int total = registros.size();

        for (int i = 0; i < total; i++) {
            ColeccionRegistros reg = registros.get(i);
            TipoMovimiento tipo = reg.getTipoMovimiento();

            // Si ya tiene tipo específico de pausa, NO tocar
            if (tipo == TipoMovimiento.PAUSA_INICIO || tipo == TipoMovimiento.PAUSA_FIN) {
                continue;
            }

            // Procesar ENTRADA genéricas
            if (tipo == TipoMovimiento.ENTRADA && i > 0) {
                ColeccionRegistros anterior = registros.get(i - 1);
                TipoMovimiento tipoAnterior = anterior.getTipoMovimiento();

                // Caso: ... -> PAUSA_INICIO -> ENTRADA => ... -> PAUSA_INICIO -> PAUSA_FIN
                if (tipoAnterior == TipoMovimiento.PAUSA_INICIO) {
                    reg.setTipoMovimiento(TipoMovimiento.PAUSA_FIN);
                }
                // Caso: ... -> SALIDA -> ENTRADA
                else if (tipoAnterior == TipoMovimiento.SALIDA) {
                    // Verificar si fue convertida a PAUSA_INICIO en la iteración anterior
                    // O si debemos evaluar el tiempo aquí (aunque mejor evaluar en el paso de
                    // SALIDA)
                    // Si el anterior quedó como SALIDA, significa que la diferencia es grande
                    // (cambio de turno).
                    // Entonces esta ENTRADA es correcta (inicio del siguiente turno).
                    // No hacemos nada.
                }
            }

            // Procesar SALIDA genéricas
            if (tipo == TipoMovimiento.SALIDA && i < total - 1) {
                ColeccionRegistros siguiente = registros.get(i + 1);
                TipoMovimiento tipoSiguiente = siguiente.getTipoMovimiento();

                if (tipoSiguiente == TipoMovimiento.ENTRADA || tipoSiguiente == TipoMovimiento.PAUSA_FIN) {
                    // Calcular tiempo entre esta SALIDA y la siguiente ENTRADA/PAUSA_FIN
                    // Si es corto (< 4h) -> Es PAUSA_INICIO
                    // Si es largo (>= 4h) -> Es fin de turno real (SALIDA)

                    if (esPausaYNoCambioTurno(reg, siguiente)) {
                        reg.setTipoMovimiento(TipoMovimiento.PAUSA_INICIO);
                    }
                }
            }
        }

        return registros;
    }

    /**
     * Determina si el lapso entre dos registros corresponde a una pausa o a un
     * cambio de turno.
     * Criterio: Diferencia menor a 4 horas = Pausa.
     */
    private static boolean esPausaYNoCambioTurno(ColeccionRegistros salida, ColeccionRegistros siguiente) {
        try {
            LocalDateTime fechaHoraSalida = LocalDateTime.of(salida.getFecha(), salida.getHora());
            LocalDateTime fechaHoraSiguiente = LocalDateTime.of(siguiente.getFecha(), siguiente.getHora());

            Duration duracion = Duration.between(fechaHoraSalida, fechaHoraSiguiente);
            long horas = Math.abs(duracion.toHours());

            // Umbral de 4 horas para distinguir pausa de cambio de turno
            // Ejemplo: 13:00 a 14:00 (1h) -> Pausa
            // Ejemplo: 06:00 a 22:00 (16h) -> Cambio de turno
            return horas < 4;

        } catch (Exception e) {
            // Ante error en cálculo de fechas (ej. nulos), asumimos comportamiento default
            // (Pausa)
            // para mantener compatibilidad, o false (Salida) para ser conservadores.
            // Asumimos false para no romper cierres de jornada.
            return false;
        }
    }

    // ==================================================================================
    // VALIDACIÓN DE FILAS PARA IMPORTACIÓN
    // ==================================================================================

    /**
     * Resultado de validación de una fila de importación.
     */
    public static class ResultadoValidacion {
        public boolean valido = true;
        public List<String> errores = new ArrayList<>();
        public Personal empleado;
        public LocalDate fecha;
        public LocalTime hora;
        public TipoMovimiento tipoMovimiento;
        public String ubicacion;
        public String observacion;

        public void agregarError(String error) {
            valido = false;
            errores.add(error);
        }
    }

    /**
     * Determina la fecha operativa de la jornada a la que pertenece una fichada.
     * 
     * <p>
     * Resuelve el problema de turnos nocturnos donde la salida ocurre al día
     * siguiente.
     * </p>
     * 
     * @param empleado     Empleado que ficha
     * @param fechaFichada Fecha calendario de la fichada
     * @param horaFichada  Hora exacta de la fichada
     * @return La fecha de la jornada a la que debe asignarse (puede ser
     *         fechaFichada o fechaFichada - 1)
     */
    public static LocalDate determinarFechaJornada(Personal empleado, LocalDate fechaFichada, LocalTime horaFichada) {
        if (empleado == null || fechaFichada == null || horaFichada == null) {
            return fechaFichada;
        }

        // Revisar si pertenece al turno de AYER ("Jornada Nocturna")
        LocalDate fechaAyer = fechaFichada.minusDays(1);
        TurnosHorarios turnoAyer = empleado.getTurnoParaFecha(fechaAyer);

        if (turnoAyer != null && turnoAyer.esLaboral(fechaAyer.getDayOfWeek())
                && turnoAyer.esNocturnoParaDia(fechaAyer.getDayOfWeek())) {
            // El turno de ayer es nocturno (cruza medianoche).
            // Verificar si la hora de fichada es coherente con la salida de ese turno.
            // Generalmente, si es antes de las 12:00 PM (mediodía), asumimos que es cierre
            // del nocturno.
            // O podemos ser más precisos comparando con la hora de salida teórica + margen.

            // Margen generoso: hasta 4 horas después de la salida teórica
            // O si es temprano en la mañana (antes de las 12:00)
            if (horaFichada.isBefore(LocalTime.of(14, 0))) { // Asumimos corte a las 14:00 para nocturnos
                return fechaAyer;
            }
        }

        return fechaFichada;
    }

    /**
     * Valida una fila de datos para importación.
     */
    public static ResultadoValidacion validarFila(Map<String, String> datos) {
        ResultadoValidacion resultado = new ResultadoValidacion();
        String numFila = datos.getOrDefault("_numFila", "?");

        // Validar userId y buscar empleado
        String userId = datos.get("userId");
        if (userId == null || userId.trim().isEmpty()) {
            resultado.agregarError("Fila " + numFila + ": UserId está vacío");
        } else {
            resultado.empleado = buscarEmpleado(userId);
            if (resultado.empleado == null) {
                resultado.agregarError("Fila " + numFila + ": No se encontró empleado con UserId '" + userId + "'");
            }
        }

        // Validar fecha
        String fechaStr = datos.get("fecha");
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            resultado.agregarError("Fila " + numFila + ": Fecha está vacía");
        } else {
            resultado.fecha = parsearFecha(fechaStr);
            if (resultado.fecha == null) {
                resultado.agregarError("Fila " + numFila + ": Formato de fecha inválido '" + fechaStr + "'");
            }
        }

        // Validar hora
        String horaStr = datos.get("hora");
        if (horaStr == null || horaStr.trim().isEmpty()) {
            resultado.agregarError("Fila " + numFila + ": Hora está vacía");
        } else {
            resultado.hora = parsearHora(horaStr);
            if (resultado.hora == null) {
                resultado.agregarError("Fila " + numFila + ": Formato de hora inválido '" + horaStr + "'");
            }
        }

        // Validar tipo de movimiento
        String tipoStr = datos.get("tipoMovimiento");
        if (tipoStr == null || tipoStr.trim().isEmpty()) {
            resultado.agregarError("Fila " + numFila + ": Tipo de movimiento está vacío");
        } else {
            resultado.tipoMovimiento = deducirTipoMovimiento(tipoStr);
            if (resultado.tipoMovimiento == null) {
                resultado.agregarError("Fila " + numFila + ": Tipo de movimiento no reconocido '" + tipoStr + "'");
            }
        }

        // Campos opcionales (con truncamiento para evitar errores de BD)
        resultado.ubicacion = datos.getOrDefault("ubicacion", "");
        if (resultado.ubicacion.length() > 250) {
            resultado.ubicacion = resultado.ubicacion.substring(0, 250);
        }

        resultado.observacion = datos.getOrDefault("observacion", "Importado desde archivo");
        if (resultado.observacion.length() > 495) {
            resultado.observacion = resultado.observacion.substring(0, 495);
        }

        return resultado;
    }

    /**
     * Crea un ColeccionRegistros a partir del resultado de validación.
     */
    public static ColeccionRegistros crearRegistro(ResultadoValidacion resultado) {
        if (!resultado.valido)
            return null;

        ColeccionRegistros registro = new ColeccionRegistros();
        registro.setFecha(resultado.fecha);
        registro.setHora(resultado.hora);
        registro.setTipoMovimiento(resultado.tipoMovimiento);
        registro.setCoordenada(resultado.ubicacion.isEmpty() ? null : resultado.ubicacion);
        registro.setObservacion(resultado.observacion);

        return registro;
    }
}
