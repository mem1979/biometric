package com.sta.biometric.dashboard;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.auxiliares.*;
import com.sta.biometric.dashboard.acciones.*;
import com.sta.biometric.dashboard.auxiliares.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

import lombok.*;

@View(members =
"fechaHoraActual, sucursalSeleccionada;" +
"observacionFeriado;" +
"Detalles {" +
"cantidadAgentesHoy, pendientesDeIngresoHoy, cantidadConLicenciaHoy, cantidadLlegadasTardeHoy, cantidadSalidasAnticipadasHoy;" +
"evaluacionJornadasHoy, distribucionAsistenciaHoy ;" +
"resumenHoyComoLista;" +
"}"
)
@Getter @Setter
public class DashboardAsistencia {

    // ================================
    // 1. FILTRO PRINCIPAL POR SUCURSALES
    // ================================

    @ManyToOne
    @DescriptionsList
    @NoCreate @NoModify
    @LabelFormat(LabelFormatType.SMALL)
    @OnChange(DashboardActualizarAction.class)
    private Sucursales sucursalSeleccionada;

    // ================================
    // 2. FECHA Y HORA FORMATEADA
    // ================================

    @ReadOnly
    @LabelFormat(LabelFormatType.NO_LABEL)
    @Depends("sucursalSeleccionada")
    @MiLabel(medida = "extra", negrita = true, recuadro = true, icon = "calendar-search")
    public String getFechaHoraActual() {
        LocalDateTime ahora = LocalDateTime.now();
        Locale locale = new Locale("es", "AR");

        String diaSemana = ahora.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
        String mes = ahora.getMonth().getDisplayName(TextStyle.FULL, locale);

        return String.format("%s, %d de %s de %d - %sHs.",
                Character.toUpperCase(diaSemana.charAt(0)) + diaSemana.substring(1),
                ahora.getDayOfMonth(),
                Character.toUpperCase(mes.charAt(0)) + mes.substring(1),
                ahora.getYear(),
                ahora.format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    // ================================
    // 3. OBSERVACIÓN DE FERIADO SI CORRESPONDE A LA FECHA
    // ================================

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

    // ================================
    // 4. CARGA DE EMPLEADOS FILTRADOS
    // ================================

    private List<Personal> getEmpleadosFiltrados() {
        EntityManager em = XPersistence.getManager();
        if (sucursalSeleccionada != null && sucursalSeleccionada.getId() != null) {
            return em.createQuery("SELECT e FROM Personal e WHERE e.activo = true AND e.sucursal.id = :id", Personal.class)
                .setParameter("id", sucursalSeleccionada.getId())
                .getResultList();
        } else {
            return em.createQuery("SELECT e FROM Personal e WHERE e.activo = true", Personal.class)
                .getResultList();
        }
    }

    private List<ResumenEmpleadoHoy> getResumenesHoy() {
        return ResumenAsistenciaHoyService.calcularResumen(getEmpleadosFiltrados(), LocalDate.now());
    }

    // ================================
    // 5. MÉTRICAS CALCULADAS
    // ================================

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "account-multiple")
    public int getCantidadAgentesHoy() {
        return (int) getResumenesHoy().stream().filter(ResumenEmpleadoHoy::isDebeTrabajar).count();
    }

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "calendar-remove")
    public int getCantidadConLicenciaHoy() {
        return (int) getResumenesHoy().stream().filter(ResumenEmpleadoHoy::isConLicencia).count();
    }

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "account-alert")
    public int getPendientesDeIngresoHoy() {
        return (int) getResumenesHoy().stream()
            .filter(r -> r.isDebeTrabajar() && !r.isConLicencia() && !r.isIngresoRealizado())
            .count();
    }

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "clock-alert")
    public int getCantidadLlegadasTardeHoy() {
        return (int) getResumenesHoy().stream().filter(ResumenEmpleadoHoy::isLlegadaTarde).count();
    }

    @Depends("sucursalSeleccionada")
    @LargeDisplay(icon = "exit-run")
    public int getCantidadSalidasAnticipadasHoy() {
        return (int) getResumenesHoy().stream().filter(ResumenEmpleadoHoy::isSalidaAnticipada).count();
    }

    // ================================
    // 6. DISTRIBUCIÓN GRÁFICA
    // ================================

    @NoCreate
    @Chart(type = ChartType.PIE)
    @ListProperties("descripcion, valor")
    public Collection<ItemGraficoPorcentageAgentesDashboard> getDistribucionAsistenciaHoy() {
        List<ResumenEmpleadoHoy> resumenes = getResumenesHoy();

        int total = (int) resumenes.stream().filter(ResumenEmpleadoHoy::isDebeTrabajar).count();
        int licencia = (int) resumenes.stream().filter(ResumenEmpleadoHoy::isConLicencia).count();
        int pendientes = (int) resumenes.stream()
            .filter(r -> r.isDebeTrabajar() && !r.isConLicencia() && !r.isIngresoRealizado())
            .count();
        int presentes = Math.max(0, total - pendientes - licencia);

        List<ItemGraficoPorcentageAgentesDashboard> resultado = new ArrayList<>();
        resultado.add(new ItemGraficoPorcentageAgentesDashboard("Presentes", presentes));
        resultado.add(new ItemGraficoPorcentageAgentesDashboard("Pendientes de ingreso", pendientes));
        resultado.add(new ItemGraficoPorcentageAgentesDashboard("Con licencia", licencia));

        return resultado;
    }
    
    // ================================
    // 6. evaluacion de registros GRÁFICA
    // ================================
    
    @NoCreate
    @Chart(type = ChartType.BAR, labelProperties = "descripcion", dataProperties = "cantidad")
    @ListProperties("descripcion, cantidad")
    public Collection<ItemGraficoEvaluacionJornadaDashboard> getEvaluacionJornadasHoy() {
        List<ResumenEmpleadoHoy> resumenes = getResumenesHoy();

        // Filtrar por sucursal si hay una seleccionada
        if (sucursalSeleccionada != null && sucursalSeleccionada.getId() != null) {
            resumenes = resumenes.stream()
                .filter(r -> r.getEmpleado() != null &&
                             r.getEmpleado().getSucursal() != null &&
                             r.getEmpleado().getSucursal().getId().equals(sucursalSeleccionada.getId()))
                .collect(Collectors.toList());
        }

        // Agrupar por evaluación
        Map<String, Long> conteoPorEvaluacion = resumenes.stream()
            .filter(r -> r.getEvaluacion() != null)
            .collect(Collectors.groupingBy(
                r -> r.getEvaluacion().toString(),
                Collectors.counting()
            ));

        // Armar resultado para el gráfico
        List<ItemGraficoEvaluacionJornadaDashboard> resultado = new ArrayList<>();
        for (Map.Entry<String, Long> entry : conteoPorEvaluacion.entrySet()) {
            resultado.add(new ItemGraficoEvaluacionJornadaDashboard(entry.getKey(), entry.getValue().intValue()));
        }

        return resultado;
    }

 // ================================
 // 7. LISTADO MEJORADO DE JORNADAS HOY
 // ================================

 @NoCreate
 @SimpleList
 @ListProperties("empleadoNombre, sucursalNombre, evaluacion, ingresoRealizadoStr, llegadaTardeStr, salidaAnticipadaStr")
 public Collection<ResumenEmpleadoHoy> getResumenHoyComoLista() {
     List<ResumenEmpleadoHoy> resumenes = getResumenesHoy();

     // Filtro por sucursal si corresponde
     if (sucursalSeleccionada != null && sucursalSeleccionada.getId() != null) {
         resumenes = resumenes.stream()
             .filter(r -> r.getEmpleado() != null &&
                          r.getEmpleado().getSucursal() != null &&
                          r.getEmpleado().getSucursal().getId().equals(sucursalSeleccionada.getId()))
             .collect(Collectors.toList());
     }

     // Orden alfabético
     resumenes.sort(Comparator.comparing(r -> r.getEmpleado().getNombreCompleto()));

     return resumenes;
 }
}
