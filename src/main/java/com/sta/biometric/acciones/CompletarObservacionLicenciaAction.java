package com.sta.biometric.acciones;

import java.time.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

public class CompletarObservacionLicenciaAction extends OnChangePropertyBaseAction {

    @SuppressWarnings("rawtypes")
	@Override
    public void execute() throws Exception {
        System.out.println("Iniciando acción CompletarObservacionLicenciaAction...");

        TipoLicenciaAR tipo = (TipoLicenciaAR) getView().getValue("tipo");
        ModoComputoLicencia modo = (ModoComputoLicencia) getView().getValue("modoComputo");
        LocalDate inicio = (LocalDate) getView().getValue("fechaInicio");
        LocalDate fin = (LocalDate) getView().getValue("fechaFin");
        
        Map clave = getView().getParent().getKeyValuesWithValue(); // getView() aquí es la de la referencia, no la principal(3)
      
            Personal empleado = (Personal) // Buscamos la factura usando la clave tecleada (4)
                MapFacade.findEntity(getView().getParent().getModelName(), clave);
        
        System.out.println("Valores recuperados:");
        System.out.println(" - Tipo: " + tipo);
        System.out.println(" - Modo: " + modo);
        System.out.println(" - Fecha inicio: " + inicio);
        System.out.println(" - Fecha fin: " + fin);
        System.out.println(" - Empleado ID: " + empleado.getUserId());

       // 1. Armar observación automática
        String observacion = "";
        if (tipo != null) {
            observacion += obtenerDescripcionPorDefecto(tipo) + " ";
        }
        if (modo != null) {
            observacion += obtenerDescripcionModoComputo(modo);
        }
        getView().setValue("observacion", observacion.trim());
        System.out.println("Observación seteada: " + observacion.trim());

        // 2. Calcular y setear días
        if (inicio != null && fin != null && modo != null && empleado != null) {
            int total = 0;
            LocalDate actual = inicio;

            while (!actual.isAfter(fin)) {
                boolean esFeriado = Feriados.existeParaFecha(actual);
                TurnosHorarios turno = empleado.getTurnoParaFecha(actual);
                boolean esLaboral = turno != null && turno.esLaboral(actual.getDayOfWeek());

                System.out.println("   Día: " + actual + " | Feriado: " + esFeriado + " | Laboral: " + esLaboral);

                switch (modo) {
                    case DIAS_CORRIDOS:
                        total++;
                        break;
                    case DIAS_HABILES:
                        if (!esFeriado && actual.getDayOfWeek().getValue() < 6) {
                            total++;
                        }
                        break;
                    case DIAS_LABORALES:
                        if (!esFeriado && esLaboral) {
                            total++;
                        }
                        break;
                    default:
                        break;
                }

                actual = actual.plusDays(1);
            }

            System.out.println("Total de días calculado: " + total);
            getView().setValueNotifying("dias", Integer.valueOf(total));
            System.out.println("Valor asignado en vista: dias = " + total);
        } else {
            System.out.println("No se pudo calcular días: falta algún valor (inicio, fin, modo o empleado)");
        }

        System.out.println("Acción finalizada.");
    }

    private String obtenerDescripcionPorDefecto(TipoLicenciaAR tipo) {
        switch (tipo) {
            case VACACIONES: return "Licencia anual ordinaria por vacaciones según LCT.";
            case ENFERMEDAD: return "Licencia médica por enfermedad con certificado.";
            case ACCIDENTE_TRABAJO: return "Accidente laboral cubierto por ART.";
            case MATERNIDAD: return "Licencia por maternidad según ley vigente.";
            case PATERNIDAD: return "Licencia por nacimiento de hijo.";
            case ESTUDIO: return "Licencia para actividades de formación educativa.";
            case EXAMENES: return "Licencia para rendir examen en institución reconocida.";
            case MATRIMONIO: return "Licencia por matrimonio del empleado.";
            case FALLECIMIENTO_FAMILIAR: return "Licencia por fallecimiento de familiar directo.";
            case MUDANZA: return "Licencia por mudanza particular.";
            case DONACION_SANGRE: return "Licencia por donación de sangre.";
            case CITACION_JUDICIAL: return "Citación judicial oficial.";
            case DEBER_CIVICO: return "Licencia por deber cívico (elecciones, jurado, etc.).";
            case ASUNTOS_PERSONALES: return "Licencia por asuntos personales.";
            case LICENCIA_SINDICAL: return "Licencia sindical autorizada.";
            case LICENCIA_SIN_GOCE: return "Licencia sin goce de haberes.";
            case LICENCIA_ADM_CON_GOCE: return "Licencia administrativa con goce de sueldo.";
            case LICENCIA_ADM_SIN_GOCE: return "Licencia administrativa sin goce de sueldo.";
            case EMBARAZO_RIESGO: return "Licencia por embarazo de riesgo.";
            case NACIMIENTO_HIJO_ADOPCION: return "Licencia por adopción o nacimiento de hijo.";
            case VIOLENCIA_GENERO: return "Licencia por situaciones de violencia de género.";
            case CUIDADO_FAMILIAR: return "Licencia para cuidado de familiar directo.";
            case TELETRABAJO_ESPECIAL: return "Licencia con modalidad remota por motivos especiales.";
            case LICENCIA_ESPECIAL_PROFESIONAL: return "Licencia profesional especial (docente, médico, etc.).";
            case LICENCIA_EXTRAORDINARIA: return "Licencia extraordinaria aprobada por resolución.";
            case OTRA:
            default: return "Otro tipo de licencia, justificar en detalle.";
        }
    }

    private String obtenerDescripcionModoComputo(ModoComputoLicencia modo) {
        switch (modo) {
            case DIAS_CORRIDOS:
                return "Cómputo: días corridos (incluye fines de semana y feriados).";
            case DIAS_HABILES:
                return "Cómputo: solo días hábiles (lunes a viernes, sin feriados).";
            case DIAS_LABORALES:
                return "Cómputo: días laborales según turno (excluye feriados).";
            default:
                return "";
        }
    } 
}
