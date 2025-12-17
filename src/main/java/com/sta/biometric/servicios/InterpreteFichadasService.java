package com.sta.biometric.servicios;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

import javax.persistence.*;

import org.openxava.jpa.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

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
