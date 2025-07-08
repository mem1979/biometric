package com.sta.biometric.formateadores;
import java.time.*;
import java.time.format.*;

/**
 * Clase utilitaria para formateo y cálculo de tiempos,
 * contemplando casos con LocalDateTime y jornadas de más de 24 horas.
 */
public class TiempoUtils {

    // ===================== MÉTODOS DE FORMATEO DE MINUTOS =====================

    /**
     * Devuelve un texto "HH:MM" a partir de una cantidad de minutos (ej: 150 -> "02:30").
     * Si minutos <= 0, devuelve "00:00".
     * Funciona incluso si pasan de 24 horas (ej: 1500 minutos = 25:00).
     */
    public static String formatearMinutosComoHHMM(Integer minutos) {
        if (minutos == null || minutos <= 0) return "00:00";
        int horas = minutos / 60;
        int min = minutos % 60;
        return String.format("%02d:%02d", horas, min);
    }

    /**
     * Versión con segundos: "HH:MM:SS".
     * Si minutos <= 0, retorna "00:00:00".
     */
    public static String formatearMinutosComoHHMMSS(Integer minutos) {
        if (minutos == null || minutos <= 0) return "00:00:00";
        int horas = minutos / 60;
        int min = minutos % 60;
        // En este caso, asumimos que no estamos guardando segundos aparte, así que 0.
        return String.format("%02d:%02d:%02d", horas, min, 0);
    }

    /**
     * Formatea un total de minutos distinguiendo días, por ejemplo:
     * - 27 horas 30 min => "1d 03:30"
     * - 8 horas        => "08:00"
     * - 0 min          => "0 min"
     */
    public static String formatearMinutosMultiDia(int minutosTotales) {
        if (minutosTotales <= 0) return "0 min";

        int dias = minutosTotales / (24 * 60);
        int resto = minutosTotales % (24 * 60);
        int horas = resto / 60;
        int mins = resto % 60;

        if (dias > 0) {
            return String.format("%dd %02d:%02d", dias, horas, mins);
        }
        else {
            return String.format("%02d:%02d", horas, mins);
        }
    }

    // ===================== MÉTODOS DE CÁLCULO CON LocalDateTime =====================

    /**
     * Calcula la diferencia en minutos entre dos LocalDateTime (puede ser multi-día).
     * Si fin < inicio, retorna negativo (o podrías retornar 0 o forzar un plusDays, 
     * dependiendo de tu lógica).
     */
    public static int calcularMinutosEntreDateTimes(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) return 0;
        return (int) Duration.between(inicio, fin).toMinutes();
    }

    /**
     * Versión que fuerza a tratar como mínimo 0.
     * Si fin es anterior a inicio, se regresa 0 en vez de negativo.
     */
    public static int calcularMinutosEntreDateTimesNoNeg(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) return 0;
        long minutos = Duration.between(inicio, fin).toMinutes();
        return (int) Math.max(0, minutos);
    }
    
  

    // ===================== MÉTODOS DE CÁLCULO CON LocalTime =====================

    /**
     * Calcula la diferencia en minutos entre dos LocalTime asumiendo 
     * que fin podría cruzar medianoche, pero no más de 24h.
     */
    public static int calcularMinutosLocalTime(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null) return 0;
        Duration d = Duration.between(inicio, fin);
        // Si es negativo, sumamos 1 día (caso típico de cruzar medianoche)
        if (d.isNegative()) {
            d = d.plusDays(1);
        }
        return (int) d.toMinutes();
    }
    
    /**
     * Calcula la diferencia en minutos entre dos LocalTime asumiendo 
     * para turnos de hasta 24h.
     */
    
    public static int calcularMinutosEntreHoras(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null) return 0;
        Duration d = Duration.between(inicio, fin);
        return (int) Math.max(0, d.toMinutes());
    }

    // ===================== MÉTODOS CON Duration =====================

    /**
     * Convierte un entero de minutos en una Duration.
     * Si minutos < 0, retorna Duration.ZERO.
     */
    public static Duration desdeMinutos(Integer minutos) {
        if (minutos == null || minutos < 0) return Duration.ZERO;
        return Duration.ofMinutes(minutos);
    }

    /**
     * Formatea una Duration a "HH:MM" (hasta 23:59).
     * Si la Duration supera 24h, puede mostrar 25:00, 48:30, etc.
     */
    public static String formatearDurationComoHHMM(Duration duracion) {
        if (duracion == null || duracion.isZero() || duracion.isNegative()) {
            return "00:00";
        }
        long totalMinutos = duracion.toMinutes();
        long horas = totalMinutos / 60;
        long min = totalMinutos % 60;
        return String.format("%02d:%02d", horas, min);
    }

    // ===================== EJEMPLO EXTRA =====================

    /**
     * Devuelve un String con el intervalo [inicio - fin], 
     * por ejemplo "12/04/2025 08:30 - 13/04/2025 10:00 (Duración: 1d 01:30)".
     * Solo a modo ilustrativo; únete a tu formateo preferido.
     */
    public static String describirRangoFechaHora(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) return "Sin datos";
        // Formato de ejemplo "dd/MM/yyyy HH:mm"
        // Ajustar a tu preferencia (timezones, localization, etc.)
        String inicioStr = inicio.toLocalDate() + " " + inicio.toLocalTime();
        String finStr = fin.toLocalDate() + " " + fin.toLocalTime();

        int minutos = calcularMinutosEntreDateTimes(inicio, fin);
        String duracion = formatearMinutosMultiDia(minutos);

        return String.format("%s  %s (Duración: %s)", inicioStr, finStr, duracion);
    }

    public static String formatearFecha(LocalDateTime fechaHora) {
        if (fechaHora == null) return "";
        return fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
    }

    public static String formatearHora(LocalDateTime fechaHora) {
        if (fechaHora == null) return "";
        return fechaHora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
 // === Sobrecarga para formateo por tipo ===

    /**
     * Formatea una fecha LocalDate como dd/MM/yy
     */
    public static String formatearFecha(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
    }

    /**
     * Formatea una hora LocalTime como HH:mm
     */
    public static String formatearHora(LocalTime hora) {
        if (hora == null) return "";
        return hora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    
    // ===================== FIN =====================

}