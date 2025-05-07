package com.sta.biometric.dashboard;
import java.time.*;
import java.time.format.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.auxiliares.*;
import com.sta.biometric.dashboard.acciones.*;
import com.sta.biometric.dashboard.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

import lombok.*;

@View(members =
"fechaHoraActual, sucursalSeleccionada;" +
"observacionFeriado;" +
"Detalles {" +
"   cantidadAgentesHoy, pendientesDeIngresoHoy, cantidadConLicenciaHoy, cantidadLlegadasTardeHoy, cantidadSalidasAnticipadasHoy;" +
"   registrosHoy; distribucionAsistenciaHoy; " +
"}"
)
@Getter @Setter
public class DashboardAsistencia {

    // ===================== FILTRO PRINCIPAL: SUCURSAL =====================

    @ManyToOne
    @DescriptionsList
    @NoCreate @NoModify
    @LabelFormat(LabelFormatType.SMALL)
    @OnChange(DashboardActualizarCantidadAgentesAction.class)
    private Sucursales sucursalSeleccionada;

    // ===================== FECHA Y HORA ACTUAL FORMATEADA =====================

    @ReadOnly
    @LabelFormat(LabelFormatType.NO_LABEL)
    @Depends("sucursalSeleccionada")
    @MiLabel(medida = "extra", negrita = true, recuadro = true, icon = "calendar-search")
    public String getFechaHoraActual() {
        LocalDateTime ahora = LocalDateTime.now();
        Locale locale = new Locale("es", "AR");

        String diaSemana = ahora.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
        diaSemana = Character.toUpperCase(diaSemana.charAt(0)) + diaSemana.substring(1);
        String mes = ahora.getMonth().getDisplayName(TextStyle.FULL, locale);
        mes = Character.toUpperCase(mes.charAt(0)) + mes.substring(1);

        return String.format("%s, %d de %s de %d - %sHs.",
                diaSemana,
                ahora.getDayOfMonth(),
                mes,
                ahora.getYear(),
                ahora.format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }

    // ===================== DETECCIÓN DE FERIADO =====================

    @Transient
    @MiLabel(medida = "chica", negrita = true, recuadro = false)
    @LabelFormat(LabelFormatType.NO_LABEL)
    @Depends("sucursalSeleccionada, fechaHoraActual")
    public String getObservacionFeriado() {
        LocalDate hoy = LocalDate.now();
        try {
            Feriados feriado = XPersistence.getManager()
                .createQuery("SELECT f FROM Feriados f WHERE f.fecha = :fecha", Feriados.class)
                .setParameter("fecha", hoy)
                .getSingleResult();
            return "FERIADO: " + feriado.getTipo().toUpperCase() + " - " + feriado.getMotivo();
        } catch (NoResultException e) {
            return "";
        }
    }

    // ===================== OBTENER EMPLEADOS ACTIVOS =====================

    private List<Personal> getEmpleadosFiltrados() {
        EntityManager em = XPersistence.getManager();
        if (sucursalSeleccionada != null && sucursalSeleccionada.getId() != null) {
            return em.createQuery("SELECT e FROM Personal e WHERE e.sucursal.id = :id AND e.activo = true", Personal.class)
                .setParameter("id", sucursalSeleccionada.getId())
                .getResultList();
        } else {
            return em.createQuery("SELECT e FROM Personal e WHERE e.activo = true", Personal.class)
                .getResultList();
        }
    }

    // ===================== CANTIDAD DE AGENTES LABORALES PARA HOY =====================

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "account-multiple")
    public int getCantidadAgentesHoy() {
        LocalDate hoy = LocalDate.now();
        DayOfWeek dia = hoy.getDayOfWeek();
        return (int) getEmpleadosFiltrados().stream()
            .filter(e -> {
                TurnosHorarios t = e.getTurnoParaFecha(hoy);
                return t != null && t.esLaboral(dia);
            })
            .count();
    }

    // ===================== CANTIDAD CON LICENCIA HOY =====================

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "calendar-remove")
    public int getCantidadConLicenciaHoy() {
        LocalDate hoy = LocalDate.now();
        return (int) getEmpleadosFiltrados().stream()
            .filter(e -> Licencia.tieneLicenciaEnFecha(e, hoy))
            .count();
    }

    // ===================== AGENTES QUE AÚN NO INGRESARON =====================

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "account-alert")
    public int getPendientesDeIngresoHoy() {
        EntityManager em = XPersistence.getManager();
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        DayOfWeek dia = hoy.getDayOfWeek();

        int pendientes = 0;
        for (Personal e : getEmpleadosFiltrados()) {
            TurnosHorarios turno = e.getTurnoParaFecha(hoy);
            if (turno == null || !turno.esLaboral(dia)) continue;
            if (Licencia.tieneLicenciaEnFecha(e, hoy)) continue;

            LocalTime entrada = turno.getEntradaParaDia(dia);
            if (entrada == null) continue;

            int tolerancia = Optional.ofNullable(turno.getTolerancia()).orElse(5);
            LocalTime limite = entrada.plusMinutes(tolerancia);

            if (ahora.isAfter(limite)) {
                Long cantidad = em.createQuery(
                    "SELECT COUNT(r) FROM ColeccionRegistros r WHERE r.asistenciaDiaria.empleado = :emp AND r.fecha = :fecha AND r.tipoMovimiento = :tipo",
                    Long.class)
                    .setParameter("emp", e)
                    .setParameter("fecha", hoy)
                    .setParameter("tipo", TipoMovimiento.ENTRADA)
                    .getSingleResult();

                if (cantidad == 0) pendientes++;
            }
        }
        return pendientes;
    }

    // ===================== CANTIDAD DE LLEGADAS TARDE =====================

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "clock-alert")
    public int getCantidadLlegadasTardeHoy() {
        EntityManager em = XPersistence.getManager();
        LocalDate hoy = LocalDate.now();
        DayOfWeek dia = hoy.getDayOfWeek();
        int tarde = 0;

        for (Personal e : getEmpleadosFiltrados()) {
            TurnosHorarios turno = e.getTurnoParaFecha(hoy);
            if (turno == null || !turno.esLaboral(dia)) continue;
            if (Licencia.tieneLicenciaEnFecha(e, hoy)) continue;

            LocalTime entrada = turno.getEntradaParaDia(dia);
            if (entrada == null) continue;
            int tolerancia = Optional.ofNullable(turno.getTolerancia()).orElse(5);

            List<LocalTime> entradas = em.createQuery(
                "SELECT r.hora FROM ColeccionRegistros r WHERE r.asistenciaDiaria.empleado = :emp AND r.fecha = :fecha AND r.tipoMovimiento = :tipo ORDER BY r.hora ASC",
                LocalTime.class)
                .setParameter("emp", e)
                .setParameter("fecha", hoy)
                .setParameter("tipo", TipoMovimiento.ENTRADA)
                .getResultList();

            if (!entradas.isEmpty() && entradas.get(0).isAfter(entrada.plusMinutes(tolerancia))) {
                tarde++;
            }
        }
        return tarde;
    }

    // ===================== CANTIDAD DE SALIDAS ANTICIPADAS =====================

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "exit-run")
    public int getCantidadSalidasAnticipadasHoy() {
        EntityManager em = XPersistence.getManager();
        LocalDate hoy = LocalDate.now();
        DayOfWeek dia = hoy.getDayOfWeek();
        int anticipadas = 0;

        for (Personal e : getEmpleadosFiltrados()) {
            TurnosHorarios turno = e.getTurnoParaFecha(hoy);
            if (turno == null || !turno.esLaboral(dia)) continue;
            if (Licencia.tieneLicenciaEnFecha(e, hoy)) continue;

            LocalTime salida = turno.getSalidaParaDia(dia);
            if (salida == null) continue;
            int tolerancia = Optional.ofNullable(turno.getTolerancia()).orElse(5);

            List<LocalTime> salidas = em.createQuery(
                "SELECT r.hora FROM ColeccionRegistros r WHERE r.asistenciaDiaria.empleado = :emp AND r.fecha = :fecha AND r.tipoMovimiento = :tipo ORDER BY r.hora DESC",
                LocalTime.class)
                .setParameter("emp", e)
                .setParameter("fecha", hoy)
                .setParameter("tipo", TipoMovimiento.SALIDA)
                .getResultList();

            if (!salidas.isEmpty() && salidas.get(0).isBefore(salida.minusMinutes(tolerancia))) {
                anticipadas++;
            }
        }
        return anticipadas;
    }

    // ===================== DISTRIBUCIÓN GRÁFICA =====================

    @Chart(type = ChartType.PIE)
    @ListProperties("descripcion, valor")
    public Collection<ItemGraficoPorcentageAgentesDashboard> getDistribucionAsistenciaHoy() {
        int total = getCantidadAgentesHoy();
        int licencia = getCantidadConLicenciaHoy();
        int pendientes = getPendientesDeIngresoHoy();
        int presentes = Math.max(0, total - pendientes - licencia);

        List<ItemGraficoPorcentageAgentesDashboard> resultado = new ArrayList<>();
        resultado.add(new ItemGraficoPorcentageAgentesDashboard("Presentes", presentes));
        resultado.add(new ItemGraficoPorcentageAgentesDashboard("Pendientes de ingreso", pendientes));
        resultado.add(new ItemGraficoPorcentageAgentesDashboard("Con licencia", licencia));

        return resultado;
    }

    // ===================== DETALLE TABULAR DE REGISTROS =====================

    @ReadOnly
    @ListProperties("sucursal, empleado, turnoHoy, tipoMovimiento, evaluacion")
    @RowStyle(style = "asistenciaCorrecta", property = "evaluacion", value = "ENTRADA EN HORARIO")
    @RowStyle(style = "asistenciaCorrecta", property = "evaluacion", value = "SALIDA EN HORARIO")
    @RowStyle(style = "llegadaTarde", property = "evaluacion", value = "ENTRADA TARDE")
    @RowStyle(style = "llegadaTarde", property = "evaluacion", value = "SALIDA ANTICIPADA")
    @RowStyle(style = "salidaAnticipada", property = "evaluacion", value = "ENTRADA ANTICIPADA")
    @RowStyle(style = "salidaAnticipada", property = "evaluacion", value = "SALIDA TARDIA")
    public Collection<RegistroResumenDashboard> getRegistrosHoy() {
        EntityManager em = XPersistence.getManager();
        LocalDate hoy = LocalDate.now();

        List<ColeccionRegistros> registros = (sucursalSeleccionada != null && sucursalSeleccionada.getId() != null)
            ? em.createQuery("SELECT r FROM ColeccionRegistros r WHERE r.fecha = :fecha AND r.tipoMovimiento IN (:entr, :sal) AND r.asistenciaDiaria.empleado.sucursal.id = :sucId AND r.asistenciaDiaria.empleado.activo = true", ColeccionRegistros.class)
                .setParameter("fecha", hoy)
                .setParameter("entr", TipoMovimiento.ENTRADA)
                .setParameter("sal", TipoMovimiento.SALIDA)
                .setParameter("sucId", sucursalSeleccionada.getId())
                .getResultList()
            : em.createQuery("SELECT r FROM ColeccionRegistros r WHERE r.fecha = :fecha AND r.tipoMovimiento IN (:entr, :sal) AND r.asistenciaDiaria.empleado.activo = true", ColeccionRegistros.class)
                .setParameter("fecha", hoy)
                .setParameter("entr", TipoMovimiento.ENTRADA)
                .setParameter("sal", TipoMovimiento.SALIDA)
                .getResultList();

        List<RegistroResumenDashboard> resultado = new ArrayList<>();
        for (ColeccionRegistros r : registros) {
            Personal emp = r.getAsistenciaDiaria().getEmpleado();
            String turno = emp.getTurnoParaFecha(hoy) != null ? emp.getTurnoParaFecha(hoy).getCodigo() : "Sin turno";
            resultado.add(new RegistroResumenDashboard(
                emp.getSucursal() != null ? emp.getSucursal().getNombre() : "N/D",
                emp.getNombreCompleto(),
                turno,
                r.getTipoMovimiento(),
                r.getEvaluacion()
            ));
        }
        return resultado;
    }
}
