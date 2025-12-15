package com.sta.biometric.acciones;

import java.time.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

/**
 * Acción que genera la liquidación con el período seleccionado por el usuario.
 * 
 * <p>
 * Se ejecuta desde el diálogo de selección de período. Recupera el empleado
 * del contexto, valida que no exista superposición con otras liquidaciones,
 * y genera la liquidación para las fechas especificadas.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class ConfirmarLiquidacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener fechas del diálogo
        LocalDate periodoDesde = (LocalDate) getView().getValue("periodoDesde");
        LocalDate periodoHasta = (LocalDate) getView().getValue("periodoHasta");

        // Validar fechas
        if (periodoDesde == null || periodoHasta == null) {
            addError("Debe especificar ambas fechas del período");
            return;
        }

        if (periodoDesde.isAfter(periodoHasta)) {
            addError("La fecha de inicio debe ser anterior o igual a la fecha de fin");
            return;
        }

        // Recuperar el ID del empleado guardado en el contexto
        String empleadoId = (String) getContext().get(getRequest(), "liquidacion_empleado_id");

        if (empleadoId == null || empleadoId.isEmpty()) {
            addError("No se pudo recuperar el empleado. Por favor, inténtelo de nuevo.");
            closeDialog();
            return;
        }

        try {
            // Buscar el empleado
            Personal empleado = XPersistence.getManager().find(Personal.class, empleadoId);

            if (empleado == null) {
                addError("No se encontró el empleado");
                closeDialog();
                return;
            }

            // Verificar que el rango de fechas no se superponga con liquidaciones
            // existentes
            List<LiquidacionJornadas> liquidacionesExistentes = verificarSuperposicion(empleado, periodoDesde,
                    periodoHasta);

            if (!liquidacionesExistentes.isEmpty()) {
                StringBuilder mensaje = new StringBuilder();
                mensaje.append("Ya existen liquidaciones que se superponen con el período seleccionado:");
                for (LiquidacionJornadas liq : liquidacionesExistentes) {
                    mensaje.append("\n- ").append(liq.getPeriodoDesde())
                            .append(" a ").append(liq.getPeriodoHasta())
                            .append(" (").append(liq.getEstadoPeriodo()).append(")");
                }
                addError(mensaje.toString());
                return;
            }

            // Generar liquidación
            LiquidacionJornadas liquidacion = LiquidacionJornadaService
                    .generarLiquidacion(empleado, periodoDesde, periodoHasta);

            XPersistence.commit();

            addMessage("Liquidación generada exitosamente para el período " +
                    periodoDesde + " - " + periodoHasta);
            addMessage("Total horas normales: " + liquidacion.getHorasNormalesFormatted());
            addMessage("Total horas extras: " + liquidacion.getHorasExtrasFormatted());
            addMessage("Total horas especiales: " + liquidacion.getHorasEspecialesFormatted());
            addMessage("Gran total: $" + liquidacion.getMontoGranTotal());

            // Cerrar diálogo y refrescar vista principal
            closeDialog();
            getPreviousView().refresh();

        } catch (IllegalArgumentException e) {
            addError(e.getMessage());
        } catch (Exception e) {
            addError("Error al generar liquidación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verifica si existe superposición con liquidaciones existentes del empleado.
     * 
     * @param empleado Empleado a verificar
     * @param desde    Fecha de inicio del nuevo período
     * @param hasta    Fecha de fin del nuevo período
     * @return Lista de liquidaciones que se superponen (vacía si no hay
     *         superposición)
     */
    private List<LiquidacionJornadas> verificarSuperposicion(Personal empleado, LocalDate desde, LocalDate hasta) {
        // Buscar liquidaciones que se superpongan con el rango propuesto
        // Superposición: (L.desde <= hasta) AND (L.hasta >= desde)
        return XPersistence.getManager()
                .createQuery(
                        "SELECT l FROM LiquidacionJornadas l " +
                                "WHERE l.empleado = :emp " +
                                "AND l.periodoDesde <= :hasta " +
                                "AND l.periodoHasta >= :desde",
                        LiquidacionJornadas.class)
                .setParameter("emp", empleado)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();
    }
}
