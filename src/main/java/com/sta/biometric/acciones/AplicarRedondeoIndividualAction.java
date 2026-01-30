package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openxava.util.Users;

import com.sta.biometric.enums.TipoRedondeo;
import com.sta.biometric.modelo.*;

/**
 * Acción que aplica redondeo MANUAL a un registro individual.
 * 
 * <p>
 * Este redondeo afecta los campos de ajuste MANUAL (ajusteMinutosXXX),
 * NO los campos de redondeo automático.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class AplicarRedondeoIndividualAction extends ViewBaseAction {

    private static final int AJUSTE_MAXIMO = 30;

    @Override
    public void execute() throws Exception {
        // Obtener valores del diálogo
        Integer intervalo = (Integer) getView().getValue("intervaloMinutos");
        TipoRedondeo estrategiaNormales = (TipoRedondeo) getView().getValue("estrategiaNormales");
        TipoRedondeo estrategiaExtras = (TipoRedondeo) getView().getValue("estrategiaExtras");
        TipoRedondeo estrategiaEspeciales = (TipoRedondeo) getView().getValue("estrategiaEspeciales");

        // Validaciones
        if (intervalo == null || intervalo <= 0) {
            addError("El intervalo de redondeo debe ser mayor a 0.");
            return;
        }

        if (estrategiaNormales == null || estrategiaExtras == null || estrategiaEspeciales == null) {
            addError("Debe seleccionar una estrategia para cada tipo de hora.");
            return;
        }

        // Obtener registro del contexto
        String registroId = (String) getContext().get(getRequest(), "redondeo_registro_id");
        if (registroId == null) {
            addError("No se pudo recuperar el registro.");
            closeDialog();
            return;
        }

        AuditoriaRegistros registro = XPersistence.getManager()
                .find(AuditoriaRegistros.class, registroId);
        if (registro == null) {
            addError("No se encontró el registro especificado.");
            closeDialog();
            return;
        }

        // Aplicar redondeo manual
        String resultado = aplicarRedondeoManual(registro, intervalo,
                estrategiaNormales, estrategiaExtras, estrategiaEspeciales);

        if (resultado != null) {
            XPersistence.getManager().merge(registro);
            XPersistence.commit();
            addMessage("✅ Ajuste manual aplicado: " + resultado);
        } else {
            addMessage("ℹ️ No hubo cambios que aplicar con el intervalo seleccionado.");
        }

        // Limpiar contexto y cerrar
        getContext().remove(getRequest(), "redondeo_registro_id");
        closeDialog();
        getView().refresh();
    }

    /**
     * Aplica redondeo a los campos de ajuste MANUAL de un registro.
     * 
     * @return Descripción de los cambios, o null si no hubo cambios
     */
    private String aplicarRedondeoManual(AuditoriaRegistros registro, int intervalo,
            TipoRedondeo estrategiaNormales, TipoRedondeo estrategiaExtras,
            TipoRedondeo estrategiaEspeciales) {

        StringBuilder cambios = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String usuario = Users.getCurrent();

        // === HORAS NORMALES ===
        int minutosNormales = parsearHHMM(registro.getHorasBaseNormales()) + registro.getAjusteMinutosNormales();
        int ajusteN = calcularAjuste(minutosNormales, intervalo, estrategiaNormales);
        if (ajusteN != 0) {
            registro.setAjusteMinutosNormales(registro.getAjusteMinutosNormales() + ajusteN);
            cambios.append("Normales[").append(abreviarEstrategia(estrategiaNormales)).append("]: ")
                    .append(formatearConSigno(ajusteN)).append("; ");
        }

        // === HORAS EXTRAS ===
        int minutosExtras = parsearHHMM(registro.getHorasBaseExtras()) + registro.getAjusteMinutosExtras();
        int ajusteE = calcularAjuste(minutosExtras, intervalo, estrategiaExtras);
        if (ajusteE != 0) {
            registro.setAjusteMinutosExtras(registro.getAjusteMinutosExtras() + ajusteE);
            cambios.append("Extras[").append(abreviarEstrategia(estrategiaExtras)).append("]: ")
                    .append(formatearConSigno(ajusteE)).append("; ");
        }

        // === HORAS ESPECIALES ===
        int minutosEspeciales = parsearHHMM(registro.getHorasBaseEspeciales()) + registro.getAjusteMinutosEspeciales();
        int ajusteS = calcularAjuste(minutosEspeciales, intervalo, estrategiaEspeciales);
        if (ajusteS != 0) {
            registro.setAjusteMinutosEspeciales(registro.getAjusteMinutosEspeciales() + ajusteS);
            cambios.append("Especiales[").append(abreviarEstrategia(estrategiaEspeciales)).append("]: ")
                    .append(formatearConSigno(ajusteS)).append("; ");
        }

        if (cambios.length() == 0) {
            return null;
        }

        // Registrar trazabilidad
        String lineaNota = String.format("✏️ [%s] %s | Ajuste manual (%dmin): %s",
                timestamp, usuario, intervalo, cambios.toString().trim());
        String notaActual = registro.getNota();
        if (notaActual == null || notaActual.isBlank()) {
            registro.setNota(lineaNota);
        } else {
            registro.setNota(notaActual + "\n" + lineaNota);
        }

        return cambios.toString().trim();
    }

    private int calcularAjuste(int minutos, int intervalo, TipoRedondeo tipo) {
        if (intervalo <= 0 || minutos <= 0)
            return 0;

        int residuo = minutos % intervalo;
        if (residuo == 0)
            return 0;

        int ajusteArriba = intervalo - residuo;
        int ajusteAbajo = -residuo;

        int ajuste;
        switch (tipo) {
            case A_FAVOR_EMPLEADO:
                ajuste = ajusteArriba;
                break;
            case A_FAVOR_EMPRESA:
                ajuste = ajusteAbajo;
                break;
            case MATEMATICO:
            default:
                ajuste = (ajusteArriba <= Math.abs(ajusteAbajo)) ? ajusteArriba : ajusteAbajo;
                break;
        }

        // Limitar al ajuste máximo
        if (ajuste > AJUSTE_MAXIMO) {
            ajuste = AJUSTE_MAXIMO;
        } else if (ajuste < -AJUSTE_MAXIMO) {
            ajuste = -AJUSTE_MAXIMO;
        }

        return ajuste;
    }

    private String abreviarEstrategia(TipoRedondeo tipo) {
        if (tipo == null)
            return "?";
        switch (tipo) {
            case A_FAVOR_EMPLEADO:
                return "Empl";
            case A_FAVOR_EMPRESA:
                return "Emp";
            case MATEMATICO:
                return "Mat";
            default:
                return tipo.name();
        }
    }

    private int parsearHHMM(String hhmm) {
        if (hhmm == null || hhmm.isBlank())
            return 0;
        try {
            String[] partes = hhmm.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = partes.length > 1 ? Integer.parseInt(partes[1]) : 0;
            return horas * 60 + minutos;
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatearConSigno(int minutos) {
        String signo = minutos >= 0 ? "+" : "";
        int abs = Math.abs(minutos);
        int h = abs / 60;
        int m = abs % 60;
        if (h > 0) {
            return signo + String.format("%d:%02d", h, m);
        }
        return signo + m + "min";
    }
}
