package com.sta.biometric.acciones;

import java.math.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

/**
 * Acción que completa automáticamente los valores de la licencia según su tipo,
 * antigüedad del empleado, valores por defecto y normativa vigente.
 */
public class CompletarObservacionLicenciaAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {

        TipoLicenciaAR tipo = (TipoLicenciaAR) getView().getValue("tipo");
        if (tipo == null) tipo = TipoLicenciaAR.VACACIONES;

        Map<?, ?> clave = getView().getParent().getKeyValuesWithValue();
        Personal empleado = (Personal) MapFacade.findEntity(getView().getParent().getModelName(), clave);

        String keyBase = "licencia." + tipo.name();
        String descripcion = ConfiguracionesPreferencias.obtenerValor(keyBase + ".descripcion", "", String.class);
        //ModoComputoLicencia modoComputo = ConfiguracionesPreferencias.obtenerValor(keyBase + ".modoComputo", ModoComputoLicencia.DIAS_HABILES, ModoComputoLicencia.class);
        ModoComputoLicencia modoComputo =
        	    Optional.ofNullable((ModoComputoLicencia) getView().getValue("modoComputo"))
        	            .orElse(ConfiguracionesPreferencias.obtenerValor(
        	                    keyBase + ".modoComputo",
        	                    ModoComputoLicencia.DIAS_HABILES,
        	                    ModoComputoLicencia.class));

        boolean justificado = ConfiguracionesPreferencias.obtenerValor(keyBase + ".justificado", true, Boolean.class);
        int diasPorAnio = ConfiguracionesPreferencias.obtenerValor(keyBase + ".diasPorAnio", 0, Integer.class);

        String observacion = descripcion;

        // ---------------------- LÓGICA ESPECÍFICA POR TIPO DE LICENCIA ------------------------

        if (empleado != null && empleado.getInicioActividades()!= null) {

            // VACACIONES – cálculo escalonado por antigüedad
            if (tipo == TipoLicenciaAR.VACACIONES) {
                diasPorAnio = calcularDiasCorrespondientesPorAntiguedad(empleado, LocalDate.now());
                observacion += " (según antigüedad)";
            }

            // ENFERMEDAD – máximo 90 o 180 días por año
            else if (tipo == TipoLicenciaAR.ENFERMEDAD) {
                long diasTrabajados = ChronoUnit.DAYS.between(empleado.getInicioActividades(), LocalDate.now());
                diasPorAnio = diasTrabajados < 5 * 365 ? 90 : 180;
                observacion += " (límite legal según antigüedad)";
            }

            // LICENCIA_EXTRAORDINARIA – acumulación de vacaciones no usadas
            else if (tipo == TipoLicenciaAR.LICENCIA_EXTRAORDINARIA) {
                int acumulados = 0;
                int anioActual = LocalDate.now().getYear();
                int anioInicio = empleado.getInicioActividades().getYear();

                for (int anio = anioInicio; anio < anioActual; anio++) {
                    int diasCorrespondientes = calcularDiasCorrespondientesPorAntiguedad(empleado, LocalDate.of(anio, 12, 31));
                    int diasTomados = obtenerDiasTomados(empleado, TipoLicenciaAR.VACACIONES, anio);
                    int restante = diasCorrespondientes - diasTomados;
                    if (restante > 0) acumulados += restante;
                }

                diasPorAnio = acumulados;
                observacion += " (vacaciones no utilizadas en años anteriores)";
            }
        }

        // ---------------------------------------------------------------------------------------

        
        int diasTomados = obtenerDiasTomados(empleado, tipo, LocalDate.now().getYear());
        int diasRestantes = Math.max(0, diasPorAnio - diasTomados );

        getView().setValue("modoComputo", modoComputo);
        getView().setValue("justificado", justificado);
        getView().setValue("observacion", observacion);
        getView().setValue("diasRestantes", diasRestantes);

        // Cálculo automático de días solicitados entre fechas
        LocalDate inicio = (LocalDate) getView().getValue("fechaInicio");
        LocalDate fin = (LocalDate) getView().getValue("fechaFin");

        if (inicio != null && fin != null && empleado != null) {
            int total = 0;
            LocalDate actual = inicio;

            while (!actual.isAfter(fin)) {
                boolean esFeriado = Feriados.existeParaFecha(actual);
                TurnosHorarios turno = empleado.getTurnoParaFecha(actual);
                boolean esLaboral = turno != null && turno.esLaboral(actual.getDayOfWeek());

                switch (modoComputo) {
                    case DIAS_CORRIDOS:
                        total++;
                        break;
                    case DIAS_HABILES:
                        if (!esFeriado && actual.getDayOfWeek().getValue() < 6) total++;
                        break;
                    case DIAS_LABORALES:
                        if (!esFeriado && esLaboral) total++;
                        break;
                }

                actual = actual.plusDays(1);
            }

            getView().setValueNotifying("dias", total);
            getView().setValue("diasRestantes", diasRestantes);
        }
    }

    /**
     * Devuelve la cantidad de días disponibles por antigüedad del empleado, para un año dado.
     */
    private int calcularDiasCorrespondientesPorAntiguedad(Personal empleado, LocalDate fechaReferencia) {
        long diasTrabajados = ChronoUnit.DAYS.between(empleado.getInicioActividades(), fechaReferencia);
        BigDecimal antiguedadAnios = BigDecimal.valueOf(diasTrabajados)
                .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);

        if (diasTrabajados < 180) return (int) (diasTrabajados / 20);
        if (antiguedadAnios.compareTo(BigDecimal.valueOf(5)) < 0) return 14;
        if (antiguedadAnios.compareTo(BigDecimal.valueOf(10)) < 0) return 21;
        if (antiguedadAnios.compareTo(BigDecimal.valueOf(20)) < 0) return 28;
        return 35;
    }

    /**
     * Retorna la cantidad de días ya tomados por tipo de licencia en un año específico.
     */
    private int obtenerDiasTomados(Personal empleado, TipoLicenciaAR tipo, int anio) {
        if (empleado == null || tipo == null) return 0;

        String jpql = "SELECT COALESCE(SUM(l.dias), 0) FROM Licencia l " +
                      "WHERE l.empleado = :empleado AND l.tipo = :tipo " +
                      "AND FUNCTION('YEAR', l.fechaInicio) = :anio";

        Object resultado = XPersistence.getManager()
            .createQuery(jpql)
            .setParameter("empleado", empleado)
            .setParameter("tipo", tipo)
            .setParameter("anio", anio)
            .getSingleResult();

        return ((Number) resultado).intValue();
    }
}
