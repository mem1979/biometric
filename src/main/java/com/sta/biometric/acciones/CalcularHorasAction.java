package com.sta.biometric.acciones;



import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.view.*;

import com.sta.biometric.formateadores.*;
import com.sta.biometric.modelo.*;

/**
 * Acción que solicita un rango de fechas y calcula el total de horas trabajadas
 * para el empleado seleccionado, mostrando un resumen.
 */
public class CalcularHorasAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Si estamos en el diálogo de rango de fechas, procedemos al cálculo
        if (getView().getModelName().equals("RangoFechas")) {
            calcularYMostrarResumen();
        } else {
            // Si no, mostramos el diálogo para pedir fechas
            mostrarDialogoFechas();
        }
    }

    private void mostrarDialogoFechas() throws Exception {
        showDialog();
        getView().setModelName("RangoFechas");
        getView().setTitle("Seleccionar Periodo");
        setControllers("DialogoCalcularHoras"); // Controlador con acciones Aceptar/Cancelar
    }

    private void calcularYMostrarResumen() throws Exception {
        LocalDate desde = (LocalDate) getView().getValue("fechaDesde");
        LocalDate hasta = (LocalDate) getView().getValue("fechaHasta");

        // Recuperamos el empleado desde la vista padre (la vista principal de Personal)
        // Nota: getPreviousView() nos da la vista que invocó el diálogo
        View vistaPersonal = getPreviousView();
        String empleadoId = (String) vistaPersonal.getValue("id"); // Asumiendo que 'id' es la clave

        if (empleadoId == null) {
            addError("No se ha seleccionado un empleado.");
            closeDialog();
            return;
        }

        Personal empleado = XPersistence.getManager().find(Personal.class, empleadoId);

        ResumenHoras resumen = calcularHoras(empleado, desde, hasta);

        // Cerramos el diálogo de fechas y mostramos el resumen
        closeDialog();
        showDialog();
        getView().setModelName("ResumenHoras");
        getView().setTitle("Resumen de Horas: " + empleado.getNombreCompleto());
        getView().setEditable(false); // Solo lectura

        // Llenamos la vista con los datos calculados
        getView().setValue("diasTotales", resumen.getDiasTotales());
        getView().setValue("totalHorasNormales", resumen.getTotalHorasNormales());
        getView().setValue("totalHorasExtras", resumen.getTotalHorasExtras());
        getView().setValue("totalHorasEspeciales", resumen.getTotalHorasEspeciales());

        setControllers("DialogoCerrar"); // Controlador con solo botón Cerrar
    }

    private ResumenHoras calcularHoras(Personal empleado, LocalDate desde, LocalDate hasta) {
        TypedQuery<AuditoriaRegistros> query = XPersistence.getManager().createQuery(
                "SELECT a FROM AuditoriaRegistros a WHERE a.empleado = :empleado AND a.fecha BETWEEN :desde AND :hasta",
                AuditoriaRegistros.class);
        query.setParameter("empleado", empleado);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);

        List<AuditoriaRegistros> registros = query.getResultList();

        int totalMinutosNormales = 0;
        int totalMinutosExtras = 0;
        int totalMinutosEspeciales = 0;

        for (AuditoriaRegistros reg : registros) {
            // Usamos los getters que ya incluyen los ajustes manuales
            totalMinutosNormales += TiempoUtils.parsearHHMMaMinutos(reg.getHorasTrabajadasTurno());
            totalMinutosExtras += TiempoUtils.parsearHHMMaMinutos(reg.getHorasExtras());
            totalMinutosEspeciales += TiempoUtils.parsearHHMMaMinutos(reg.getHorasEspeciales());
        }

        ResumenHoras resumen = new ResumenHoras();
        resumen.setDiasTotales(registros.size());
        resumen.setTotalHorasNormales(TiempoUtils.formatearMinutosComoHHMM(totalMinutosNormales));
        resumen.setTotalHorasExtras(TiempoUtils.formatearMinutosComoHHMM(totalMinutosExtras));
        resumen.setTotalHorasEspeciales(TiempoUtils.formatearMinutosComoHHMM(totalMinutosEspeciales));

        return resumen;
    }
}
