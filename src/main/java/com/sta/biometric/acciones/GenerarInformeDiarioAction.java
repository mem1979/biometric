package com.sta.biometric.acciones;

import java.time.*;
import java.time.format.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.dashboard.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

import net.sf.jasperreports.engine.*;

/**
 * Acción para generar el Informe Diario de Jornadas desde el diálogo.
 * 
 * LÓGICA HÍBRIDA:
 * - Para la FECHA ACTUAL: Usa la lógica del Dashboard (todos los empleados
 * activos)
 * para asegurar que el informe esté completo aunque el job de apertura no se
 * haya ejecutado.
 * - Para FECHAS ANTERIORES: Usa los registros históricos de AuditoriaRegistros.
 */
public class GenerarInformeDiarioAction extends JasperReportBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener la fecha del modelo del View
        LocalDate fechaInforme = (LocalDate) getView().getValue("fecha");

        if (fechaInforme == null) {
            addError("Debe seleccionar una fecha para generar el informe.");
            return;
        }

        // Guardar en request para usar en getParameters
        getRequest().setAttribute("fechaInforme", fechaInforme);

        // Cerrar diálogo
        closeDialog();

        // Generar reporte
        super.execute();
    }

    @Override
    protected JRDataSource getDataSource() throws Exception {
        LocalDate fechaInforme = (LocalDate) getRequest().getAttribute("fechaInforme");
        if (fechaInforme == null) {
            fechaInforme = LocalDate.now();
        }

        // Usar lógica híbrida: Dashboard para hoy, AuditoriaRegistros para fechas
        // pasadas
        List<Map<String, Object>> listaDetalle = obtenerDatosHibridos(fechaInforme);

        return new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(listaDetalle);
    }

    /**
     * Obtiene los datos del informe usando lógica híbrida:
     * - Fecha actual: todos los empleados activos con cálculo en tiempo real
     * - Fechas pasadas: registros históricos de AuditoriaRegistros
     */
    private List<Map<String, Object>> obtenerDatosHibridos(LocalDate fecha) {
        boolean esFechaActual = fecha.equals(LocalDate.now());

        if (esFechaActual) {
            return obtenerDatosDesdeEmpleadosActivos(fecha);
        } else {
            return obtenerDatosDesdeAuditoria(fecha);
        }
    }

    /**
     * Para la fecha actual: Obtiene TODOS los empleados activos y calcula su estado
     * en tiempo real usando la misma lógica que el Dashboard.
     */
    private List<Map<String, Object>> obtenerDatosDesdeEmpleadosActivos(LocalDate fecha) {
        EntityManager em = XPersistence.getManager();
        List<Map<String, Object>> listaDetalle = new ArrayList<>();

        // Obtener todos los empleados activos (excluyendo eliminados)
        List<Personal> empleados = em.createQuery(
                "SELECT e FROM Personal e WHERE e.activo = true AND e.eliminado = false ORDER BY e.sucursal.nombre ASC, e.apellido ASC, e.nombres ASC",
                Personal.class)
                .getResultList();

        // Calcular resumen para cada empleado usando el servicio del Dashboard
        List<ResumenEmpleadoHoy> resumenes = ResumenAsistenciaHoyService.calcularResumen(empleados, fecha);

        // Ordenar por sucursal y nombre
        resumenes.sort(Comparator
                .comparing((ResumenEmpleadoHoy r) -> r.getEmpleado().getSucursal() != null
                        ? r.getEmpleado().getSucursal().getNombre()
                        : "")
                .thenComparing(r -> r.getEmpleado().getNombreCompleto()));

        for (ResumenEmpleadoHoy resumen : resumenes) {
            Map<String, Object> fila = new HashMap<>();
            Personal emp = resumen.getEmpleado();

            // Datos del empleado
            fila.put("empleadoNombre", emp.getNombreCompleto());
            fila.put("sucursal", emp.getSucursal() != null ? emp.getSucursal().getNombre() : "");

            // Datos del turno
            fila.put("turnoPlanificado",
                    resumen.getNombreTurno() != null ? resumen.getNombreTurno() : "Sin turno");
            fila.put("horarioEsperado", formatearHorarioEsperadoResumen(resumen));
            fila.put("horarioReal", formatearHorarioRealResumen(resumen));

            // Estado
            fila.put("evaluacion", resumen.getEvaluacion() != null ? resumen.getEvaluacion().toString() : "");
            fila.put("estadoIcono", obtenerIconoEstado(resumen.getEvaluacion()));
            fila.put("estadoJornada", obtenerEstadoJornadaResumen(resumen));

            // Horas - Para fecha actual, obtener de AuditoriaRegistros si existe, o
            // calcular
            AuditoriaRegistros auditoria = buscarAuditoriaExistente(emp, fecha);
            if (auditoria != null) {
                fila.put("horasTurno", auditoria.getHorasTrabajadasTurno());
                fila.put("horasExtras", auditoria.getHorasExtras());
                fila.put("horasEspeciales", auditoria.getHorasEspeciales());
                fila.put("nota", auditoria.getNota() != null ? auditoria.getNota() : generarNotaDesdeResumen(resumen));
            } else {
                fila.put("horasTurno", "00:00");
                fila.put("horasExtras", "00:00");
                fila.put("horasEspeciales", "00:00");
                fila.put("nota", generarNotaDesdeResumen(resumen));
            }

            listaDetalle.add(fila);
        }

        return listaDetalle;
    }

    /**
     * Para fechas pasadas: Obtiene los registros históricos de AuditoriaRegistros.
     */
    private List<Map<String, Object>> obtenerDatosDesdeAuditoria(LocalDate fecha) {
        List<AuditoriaRegistros> registros = XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "LEFT JOIN FETCH a.empleado e " +
                        "LEFT JOIN FETCH e.sucursal " +
                        "WHERE a.fecha = :fecha " +
                        "ORDER BY e.sucursal.nombre ASC, e.apellido ASC, e.nombres ASC",
                        AuditoriaRegistros.class)
                .setParameter("fecha", fecha)
                .getResultList();

        List<Map<String, Object>> listaDetalle = new ArrayList<>();

        for (AuditoriaRegistros reg : registros) {
            Map<String, Object> fila = new HashMap<>();

            // Datos del empleado
            Personal emp = reg.getEmpleado();
            fila.put("empleadoNombre", emp != null ? emp.getNombreCompleto() : "");
            fila.put("sucursal", emp != null && emp.getSucursal() != null ? emp.getSucursal().getNombre() : "");

            // Datos del turno
            fila.put("turnoPlanificado", reg.getTurnoPlanificado() != null ? reg.getTurnoPlanificado() : "Sin turno");
            fila.put("horarioEsperado", formatearHorarioEsperado(reg));
            fila.put("horarioReal", reg.getHorario() != null ? reg.getHorario() : "-");

            // Estado
            fila.put("evaluacion", reg.getEvaluacion() != null ? reg.getEvaluacion().toString() : "");
            fila.put("estadoIcono", obtenerIconoEstado(reg.getEvaluacion()));
            fila.put("estadoJornada", reg.getEstadoJornada() != null ? reg.getEstadoJornada() : "");

            // Horas
            fila.put("horasTurno", reg.getHorasTrabajadasTurno());
            fila.put("horasExtras", reg.getHorasExtras());
            fila.put("horasEspeciales", reg.getHorasEspeciales());

            // Observaciones
            fila.put("nota", reg.getNota() != null ? reg.getNota() : "");

            listaDetalle.add(fila);
        }

        return listaDetalle;
    }

    /**
     * Busca si existe un registro de AuditoriaRegistros para el empleado en la
     * fecha.
     */
    private AuditoriaRegistros buscarAuditoriaExistente(Personal empleado, LocalDate fecha) {
        try {
            return XPersistence.getManager()
                    .createQuery("SELECT a FROM AuditoriaRegistros a WHERE a.empleado = :emp AND a.fecha = :fecha",
                            AuditoriaRegistros.class)
                    .setParameter("emp", empleado)
                    .setParameter("fecha", fecha)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // ==================================================================================
    // MÉTODOS DE FORMATEO PARA RESUMEN (FECHA ACTUAL)
    // ==================================================================================

    private String formatearHorarioEsperadoResumen(ResumenEmpleadoHoy resumen) {
        if (resumen.getEntradaEsperada() == null || resumen.getSalidaEsperada() == null) {
            return "-";
        }
        return String.format("%02d:%02d - %02d:%02d",
                resumen.getEntradaEsperada().getHour(),
                resumen.getEntradaEsperada().getMinute(),
                resumen.getSalidaEsperada().getHour(),
                resumen.getSalidaEsperada().getMinute());
    }

    private String formatearHorarioRealResumen(ResumenEmpleadoHoy resumen) {
        StringBuilder sb = new StringBuilder();
        if (resumen.getHoraEntrada() != null) {
            sb.append(String.format("Entrada: %02d:%02d",
                    resumen.getHoraEntrada().getHour(),
                    resumen.getHoraEntrada().getMinute()));
        }
        if (resumen.getHoraSalida() != null) {
            if (sb.length() > 0)
                sb.append(" / ");
            sb.append(String.format("Salida: %02d:%02d",
                    resumen.getHoraSalida().getHour(),
                    resumen.getHoraSalida().getMinute()));
        }
        return sb.length() > 0 ? sb.toString() : "-";
    }

    private String obtenerEstadoJornadaResumen(ResumenEmpleadoHoy resumen) {
        if (resumen.getEvaluacion() == null)
            return "";
        return resumen.getEvaluacion().getDescripcion();
    }

    private String generarNotaDesdeResumen(ResumenEmpleadoHoy resumen) {
        if (resumen.getEvaluacion() == null)
            return "";

        switch (resumen.getEvaluacion()) {
            case PENDIENTE:
                return "Pendiente de ingreso.";
            case EN_CURSO:
                return "Jornada en curso. Ingreso: " +
                        (resumen.getHoraEntrada() != null
                                ? String.format("%02d:%02d", resumen.getHoraEntrada().getHour(),
                                        resumen.getHoraEntrada().getMinute())
                                : "-")
                        +
                        (resumen.isLlegadaTarde() ? ". Llegada tardía: " + calcularDiferencia(resumen) + " min antes."
                                : ".");
            case AUSENTE:
                return "Ausente sin justificación.";
            case LICENCIA:
                return "Con licencia activa.";
            case FERIADO:
                return "Feriado nacional.";
            case DIA_NO_LABORAL:
                return "Día no laboral según turno asignado.";
            case SIN_TURNO_ASIGNADO:
                return "Sin turno asignado para esta fecha.";
            default:
                return resumen.getEvaluacion().getDescripcion();
        }
    }

    private String calcularDiferencia(ResumenEmpleadoHoy resumen) {
        if (resumen.getEntradaEsperada() != null && resumen.getHoraEntrada() != null) {
            long minutos = Duration.between(resumen.getEntradaEsperada(), resumen.getHoraEntrada()).toMinutes();
            return String.valueOf(Math.abs(minutos));
        }
        return "0";
    }

    // ==================================================================================
    // MÉTODOS EXISTENTES (SIN CAMBIOS)
    // ==================================================================================

    @Override
    protected String getJRXML() throws Exception {
        return "InformeDiarioJornadas.jrxml";
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map getParameters() throws Exception {
        Map params = new HashMap();

        LocalDate fechaInforme = (LocalDate) getRequest().getAttribute("fechaInforme");
        if (fechaInforme == null) {
            fechaInforme = LocalDate.now();
        }

        // Obtener datos usando lógica híbrida
        List<Map<String, Object>> registros = obtenerDatosHibridos(fechaInforme);

        // 1. INFORMACIÓN DEL ENCABEZADO
        agregarDatosEncabezado(params, fechaInforme);

        // 2. PARÁMETRO PARA VISIBILIDAD CONDICIONAL DE "EN CURSO"
        params.put("esFechaActual", fechaInforme.equals(LocalDate.now()));

        // 3. RESUMEN EJECUTIVO
        agregarResumenEjecutivoHibrido(params, registros);

        return params;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarDatosEncabezado(Map params, LocalDate fechaInforme) {
        // Formatear fecha
        DateTimeFormatter formatterLargo = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy",
                new Locale("es", "ES"));
        DateTimeFormatter formatterCorto = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        params.put("fechaInforme", fechaInforme.format(formatterLargo));
        params.put("fechaInformeCorta", fechaInforme.format(formatterCorto));
        params.put("diaSemana", fechaInforme.getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.FULL, new Locale("es", "ES")).toUpperCase());
        params.put("fechaGeneracion", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        params.put("anioInforme", fechaInforme.getYear());
    }

    /**
     * Calcula el resumen ejecutivo a partir de los datos híbridos (mapas).
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarResumenEjecutivoHibrido(Map params, List<Map<String, Object>> registros) {
        int totalEmpleados = registros.size();

        long presentes = registros.stream()
                .filter(r -> {
                    String eval = (String) r.get("evaluacion");
                    return "COMPLETA".equals(eval) || "INCOMPLETA".equals(eval) || "EN_CURSO".equals(eval) ||
                            "FERIADO_TRABAJADO".equals(eval) || "DIA_NO_LABORAL_TRABAJADO".equals(eval);
                })
                .count();

        long ausentes = registros.stream()
                .filter(r -> "AUSENTE".equals(r.get("evaluacion")))
                .count();

        long licencias = registros.stream()
                .filter(r -> "LICENCIA".equals(r.get("evaluacion")))
                .count();

        long enCurso = registros.stream()
                .filter(r -> "EN_CURSO".equals(r.get("evaluacion")))
                .count();

        long pendientes = registros.stream()
                .filter(r -> "PENDIENTE".equals(r.get("evaluacion")))
                .count();

        long diasNoLaborales = registros.stream()
                .filter(r -> {
                    String eval = (String) r.get("evaluacion");
                    return "DIA_NO_LABORAL".equals(eval) || "SIN_TURNO_ASIGNADO".equals(eval);
                })
                .count();

        // Calcular horas totales especiales
        int minutosTotalesEspeciales = registros.stream()
                .mapToInt(r -> parsearHorasAMinutos((String) r.get("horasEspeciales")))
                .sum();

        // Calcular horas totales extras
        int minutosTotalesExtras = registros.stream()
                .mapToInt(r -> parsearHorasAMinutos((String) r.get("horasExtras")))
                .sum();

        params.put("totalEmpleados", totalEmpleados);
        params.put("totalPresentes", presentes);
        params.put("totalAusentes", ausentes);
        params.put("totalLicencias", licencias);
        params.put("totalEnCurso", enCurso);
        params.put("totalPendientes", pendientes);
        params.put("totalSinTurno", diasNoLaborales);
        params.put("horasTotalesEspeciales", formatearMinutos(minutosTotalesEspeciales));
        params.put("horasTotalesExtras", formatearMinutos(minutosTotalesExtras));
        params.put("cantidadRegistros", registros.size());
    }

    private String formatearHorarioEsperado(AuditoriaRegistros reg) {
        if (reg.getHoraEsperadaEntrada() == null || reg.getHoraEsperadaSalida() == null) {
            return "-";
        }
        return String.format("%02d:%02d - %02d:%02d",
                reg.getHoraEsperadaEntrada().getHour(),
                reg.getHoraEsperadaEntrada().getMinute(),
                reg.getHoraEsperadaSalida().getHour(),
                reg.getHoraEsperadaSalida().getMinute());
    }

    private String obtenerIconoEstado(EvaluacionJornada evaluacion) {
        if (evaluacion == null)
            return "?";

        switch (evaluacion) {
            case COMPLETA:
                return "OK";
            case EN_CURSO:
                return ">>>";
            case INCOMPLETA:
                return "!";
            case PENDIENTE:
                return "...";
            case AUSENTE:
                return "X";
            case LICENCIA:
                return "L";
            case FERIADO:
                return "F";
            case FERIADO_TRABAJADO:
                return "F+";
            case DIA_NO_LABORAL:
                return "-";
            case DIA_NO_LABORAL_TRABAJADO:
                return "-+";
            case SIN_TURNO_ASIGNADO:
                return "ST";
            default:
                return "?";
        }
    }

    private String formatearMinutos(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%d:%02d", horas, mins);
    }

    /**
     * Parsea un string en formato "HH:MM" a minutos totales.
     */
    private int parsearHorasAMinutos(String horasEnFormatoHHmm) {
        if (horasEnFormatoHHmm == null || horasEnFormatoHHmm.isEmpty()) {
            return 0;
        }
        try {
            String[] partes = horasEnFormatoHHmm.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = partes.length > 1 ? Integer.parseInt(partes[1]) : 0;
            return horas * 60 + minutos;
        } catch (Exception e) {
            return 0;
        }
    }
}
