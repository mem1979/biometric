package com.sta.biometric.acciones;

import java.time.*;
import java.time.format.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

import net.sf.jasperreports.engine.*;

/**
 * Acción para generar el Informe Diario de Jornadas desde el diálogo.
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

        // Obtener registros para la fecha seleccionada
        List<AuditoriaRegistros> registros = obtenerRegistrosDelDia(fechaInforme);

        // Crear lista de mapas para el detalle
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

        return new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(listaDetalle);
    }

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

        // Obtener registros para la fecha seleccionada
        List<AuditoriaRegistros> registros = obtenerRegistrosDelDia(fechaInforme);

        // 1. INFORMACIÓN DEL ENCABEZADO
        agregarDatosEncabezado(params, fechaInforme);

        // 2. PARÁMETRO PARA VISIBILIDAD CONDICIONAL DE "EN CURSO"
        params.put("esFechaActual", fechaInforme.equals(LocalDate.now()));

        // 3. RESUMEN EJECUTIVO
        agregarResumenEjecutivo(params, registros);

        return params;
    }

    private List<AuditoriaRegistros> obtenerRegistrosDelDia(LocalDate fecha) {
        return XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "LEFT JOIN FETCH a.empleado e " +
                        "LEFT JOIN FETCH e.sucursal " +
                        "WHERE a.fecha = :fecha " +
                        "ORDER BY e.sucursal.nombre ASC, e.apellido ASC, e.nombres ASC",
                        AuditoriaRegistros.class)
                .setParameter("fecha", fecha)
                .getResultList();
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarResumenEjecutivo(Map params, List<AuditoriaRegistros> registros) {
        int totalEmpleados = registros.size();

        long presentes = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.COMPLETA ||
                        r.getEvaluacion() == EvaluacionJornada.INCOMPLETA ||
                        r.getEvaluacion() == EvaluacionJornada.EN_CURSO ||
                        r.getEvaluacion() == EvaluacionJornada.FERIADO_TRABAJADO ||
                        r.getEvaluacion() == EvaluacionJornada.DIA_NO_LABORAL_TRABAJADO)
                .count();

        long ausentes = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.AUSENTE)
                .count();

        long licencias = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.LICENCIA)
                .count();

        long enCurso = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.EN_CURSO)
                .count();

        long pendientes = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.PENDIENTE)
                .count();

        long diasNoLaborales = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.DIA_NO_LABORAL ||
                        r.getEvaluacion() == EvaluacionJornada.SIN_TURNO_ASIGNADO)
                .count();

        // Calcular horas totales especiales (incluye ajustes manuales)
        int minutosTotalesEspeciales = registros.stream()
                .mapToInt(r -> parsearHorasAMinutos(r.getHorasEspeciales()))
                .sum();

        // Calcular horas totales extras (incluye ajustes manuales)
        int minutosTotalesExtras = registros.stream()
                .mapToInt(r -> parsearHorasAMinutos(r.getHorasExtras()))
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
     * 
     * @param horasEnFormatoHHmm Tiempo en formato "HH:MM" (ej: "2:30")
     * @return Minutos totales (ej: 150)
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
