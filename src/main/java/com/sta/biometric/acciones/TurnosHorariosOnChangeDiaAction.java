package com.sta.biometric.acciones;

import java.time.*;
import java.util.*;

import org.openxava.actions.*;

import com.sta.biometric.formateadores.*;



/**
 * Acción que se dispara al marcar o desmarcar un día (lunes a domingo).
 * Asigna horarios automáticamente si se marca, o limpia campos si se desmarca.
 * Calcula también la duración del día y la refleja en el campo correspondiente.
 */
public class TurnosHorariosOnChangeDiaAction extends OnChangePropertyBaseAction {

    private static final List<String> DIAS = Arrays.asList("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo");

    private static final Map<String, String> HORA_ENTRADA = Map.of(
        "lunes", "horaEntradaLunes", "martes", "horaEntradaMartes", "miercoles", "horaEntradaMiercoles",
        "jueves", "horaEntradaJueves", "viernes", "horaEntradaViernes", "sabado", "horaEntradaSabado", "domingo", "horaEntradaDomingo");

    private static final Map<String, String> HORA_SALIDA = Map.of(
        "lunes", "horaSalidaLunes", "martes", "horaSalidaMartes", "miercoles", "horaSalidaMiercoles",
        "jueves", "horaSalidaJueves", "viernes", "horaSalidaViernes", "sabado", "horaSalidaSabado", "domingo", "horaSalidaDomingo");

    @Override
    public void execute() throws Exception {
        String propiedadCambiada = getChangedProperty();

        if (Boolean.TRUE.equals(getView().getValue(propiedadCambiada))) {
            habilitarCamposDeDia(propiedadCambiada);
            copiarHorarioDelPrimerDiaSeleccionado(propiedadCambiada);
        } else {
            establecerHorasANull(propiedadCambiada);
        }
    }

    private void habilitarCamposDeDia(String dia) {
        getView().setEditable(HORA_ENTRADA.get(dia), true);
        getView().setEditable(HORA_SALIDA.get(dia), true);
    }

    private void establecerHorasANull(String dia) {
        getView().setValueNotifying(HORA_ENTRADA.get(dia), null);
        getView().setValueNotifying(HORA_SALIDA.get(dia), null);
        getView().setValueNotifying("horas" + capitalize(dia), "0 Hs. 0 Min.");
        getView().setEditable(HORA_ENTRADA.get(dia), false);
        getView().setEditable(HORA_SALIDA.get(dia), false);
    }

    private void copiarHorarioDelPrimerDiaSeleccionado(String diaActual) {
        LocalTime horaEntrada = null;
        LocalTime horaSalida = null;

        for (String dia : DIAS) {
            if (esDiaSeleccionadoConHorario(dia) && !dia.equals(diaActual)) {
                horaEntrada = (LocalTime) getView().getValue(HORA_ENTRADA.get(dia));
                horaSalida = (LocalTime) getView().getValue(HORA_SALIDA.get(dia));
                break;
            }
        }

        if (horaEntrada == null || horaSalida == null) {
            horaEntrada = (LocalTime) getView().getValue(HORA_ENTRADA.get(diaActual));
            horaSalida = (LocalTime) getView().getValue(HORA_SALIDA.get(diaActual));
        }

        for (String dia : DIAS) {
            if (Boolean.TRUE.equals(getView().getValue(dia)) &&
                getView().getValue(HORA_ENTRADA.get(dia)) == null &&
                getView().getValue(HORA_SALIDA.get(dia)) == null) {
                asignarHorasADia(dia, horaEntrada, horaSalida);
            }
        }
    }

    private boolean esDiaSeleccionadoConHorario(String dia) {
        return Boolean.TRUE.equals(getView().getValue(dia)) &&
               getView().getValue(HORA_ENTRADA.get(dia)) != null &&
               getView().getValue(HORA_SALIDA.get(dia)) != null;
    }

    /**
     * Asigna entrada/salida y calcula la duración usando TiempoUtils.
     */
    private void asignarHorasADia(String dia, LocalTime entrada, LocalTime salida) {
        getView().setValueNotifying(HORA_ENTRADA.get(dia), entrada);
        getView().setValueNotifying(HORA_SALIDA.get(dia), salida);

        if (entrada != null && salida != null) {
            int minutos = TiempoUtils.calcularMinutosLocalTime(entrada, salida);
            String duracion = (minutos / 60) + " Hs. " + (minutos % 60) + " Min.";
            getView().setValueNotifying("horas" + capitalize(dia), duracion);
        }
    }

    private String capitalize(String dia) {
        return dia.substring(0, 1).toUpperCase() + dia.substring(1);
    }
}
