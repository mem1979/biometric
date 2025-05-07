package com.sta.biometric.dashboard.acciones;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.dashboard.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

/**
 * Acción que se ejecuta automáticamente cuando el usuario cambia la sucursal
 * seleccionada en el dashboard.
 * 
 * Calcula y actualiza:
 * - cantidad de agentes esperados para hoy
 * - cantidad de empleados con licencia
 * - cantidad de pendientes de ingreso
 * - cantidad de llegadas tarde
 * - cantidad de salidas anticipadas
 * 
 * Basado en registros ya consolidados en ColeccionRegistros + Turno actual.
 */
public class DashboardActualizarCantidadAgentesAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        DashboardAsistencia dashboard = (DashboardAsistencia) getView().getEntity();
        Sucursales sucursal = dashboard.getSucursalSeleccionada();

        EntityManager em = XPersistence.getManager();
        LocalDate hoy = LocalDate.now();
        DayOfWeek dia = hoy.getDayOfWeek();
        LocalTime ahora = LocalTime.now();

        // ===================== FILTRAR EMPLEADOS ACTIVOS =====================
        List<Personal> empleados = (sucursal != null && sucursal.getId() != null)
            ? em.createQuery("SELECT e FROM Personal e WHERE e.sucursal.id = :id AND e.activo = true", Personal.class)
                 .setParameter("id", sucursal.getId())
                 .getResultList()
            : em.createQuery("SELECT e FROM Personal e WHERE e.activo = true", Personal.class)
                 .getResultList();

        // ===================== INICIALIZAR CONTADORES =====================
        int cantidadAgentesHoy = 0;
        int conLicenciaHoy = 0;
        int pendientesIngreso = 0;
        int cantidadLlegadasTarde = 0;
        int cantidadSalidasAnticipadas = 0;

        // ===================== ANALIZAR CADA EMPLEADO =====================
        for (Personal e : empleados) {
            TurnosHorarios turno = e.getTurnoParaFecha(hoy);
            if (turno == null || !turno.esLaboral(dia)) continue;

            cantidadAgentesHoy++;

            if (Licencia.tieneLicenciaEnFecha(e, hoy)) {
                conLicenciaHoy++;
                continue; // No evaluar si está con licencia
            }

            LocalTime entrada = turno.getEntradaParaDia(dia);
            LocalTime salida = turno.getSalidaParaDia(dia);
            if (entrada == null || salida == null) continue;

            int tolerancia = Optional.ofNullable(turno.getTolerancia()).orElse(5);
            LocalTime limiteEntrada = entrada.plusMinutes(tolerancia);
            LocalTime umbralSalida = salida.minusMinutes(tolerancia);

            // ===================== ENTRADAS =====================
            List<LocalTime> entradas = em.createQuery(
                "SELECT r.hora FROM ColeccionRegistros r " +
                "WHERE r.asistenciaDiaria.empleado = :emp " +
                "AND r.fecha = :fecha " +
                "AND r.tipoMovimiento = :tipo " +
                "ORDER BY r.hora ASC", LocalTime.class)
                .setParameter("emp", e)
                .setParameter("fecha", hoy)
                .setParameter("tipo", TipoMovimiento.ENTRADA)
                .getResultList();

            // ===================== SALIDAS =====================
            List<LocalTime> salidas = em.createQuery(
                "SELECT r.hora FROM ColeccionRegistros r " +
                "WHERE r.asistenciaDiaria.empleado = :emp " +
                "AND r.fecha = :fecha " +
                "AND r.tipoMovimiento = :tipo " +
                "ORDER BY r.hora DESC", LocalTime.class)
                .setParameter("emp", e)
                .setParameter("fecha", hoy)
                .setParameter("tipo", TipoMovimiento.SALIDA)
                .getResultList();

            // ===================== EVALUACIÓN DE INGRESO PENDIENTE =====================
            if (ahora.isAfter(limiteEntrada) && entradas.isEmpty()) {
                pendientesIngreso++;
            }

            // ===================== EVALUACIÓN DE LLEGADA TARDE =====================
            if (!entradas.isEmpty()) {
                if (entradas.get(0).isAfter(limiteEntrada)) {
                    cantidadLlegadasTarde++;
                }
            }

            // ===================== EVALUACIÓN DE SALIDA ANTICIPADA =====================
            if (!salidas.isEmpty()) {
                if (salidas.get(0).isBefore(umbralSalida)) {
                    cantidadSalidasAnticipadas++;
                }
            }
        }

        // ===================== REFRESCAR CAMPOS DEL DASHBOARD =====================
        getView().setValue("cantidadAgentesHoy", cantidadAgentesHoy);
        getView().setValue("cantidadConLicenciaHoy", conLicenciaHoy);
        getView().setValue("pendientesDeIngresoHoy", pendientesIngreso);
        getView().setValue("cantidadLlegadasTardeHoy", cantidadLlegadasTarde);
        getView().setValue("cantidadSalidasAnticipadasHoy", cantidadSalidasAnticipadas);

        // Forzar actualización visual
        getView().setValue("fechaHoraActual", dashboard.getFechaHoraActual());
        getView().refreshCollections(); // Actualiza la grilla `registrosHoy` y gráfico
    }
}
