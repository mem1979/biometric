package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import com.sta.biometric.formateadores.TiempoUtils;
import com.sta.biometric.modelo.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AplicarAjusteHorasAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // 1. Obtener valores del diálogo en formato HH:MM
        String ajusteNormalesStr = getView().getValueString("ajusteNormales");
        String ajusteExtrasStr = getView().getValueString("ajusteExtras");
        String ajusteEspecialesStr = getView().getValueString("ajusteEspeciales");
        String motivo = getView().getValueString("motivo");

        // 2. Convertir HH:MM a minutos
        int ajusteNormales = TiempoUtils.parsearHHMMaMinutos(ajusteNormalesStr);
        int ajusteExtras = TiempoUtils.parsearHHMMaMinutos(ajusteExtrasStr);
        int ajusteEspeciales = TiempoUtils.parsearHHMMaMinutos(ajusteEspecialesStr);

        // 3. Obtener entidad original
        AuditoriaRegistros regView = (AuditoriaRegistros) getPreviousView().getEntity();
        AuditoriaRegistros reg = XPersistence.getManager().find(AuditoriaRegistros.class, regView.getId());

        if (reg == null) {
            addError("No se encontró el registro para actualizar.");
            return;
        }

        // 4. Aplicar ajustes
        reg.setAjusteMinutosNormales(ajusteNormales);
        reg.setAjusteMinutosExtras(ajusteExtras);
        reg.setAjusteMinutosEspeciales(ajusteEspeciales);

        // 5. Agregar motivo a la nota
        if (motivo != null && !motivo.isBlank()) {
            String notaActual = reg.getNota();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));

            // Formatear los ajustes para la nota
            StringBuilder detalleAjuste = new StringBuilder();
            if (ajusteNormales != 0) {
                detalleAjuste.append("Normales: ").append(formatearAjuste(ajusteNormales));
            }
            if (ajusteExtras != 0) {
                if (detalleAjuste.length() > 0)
                    detalleAjuste.append(" | ");
                detalleAjuste.append("Extras: ").append(formatearAjuste(ajusteExtras));
            }
            if (ajusteEspeciales != 0) {
                if (detalleAjuste.length() > 0)
                    detalleAjuste.append(" | ");
                detalleAjuste.append("Especiales: ").append(formatearAjuste(ajusteEspeciales));
            }

            String lineaMotivo = "📝 [" + timestamp + "] Ajuste (" + detalleAjuste + "): " + motivo;

            if (notaActual == null || notaActual.isBlank()) {
                reg.setNota(lineaMotivo);
            } else {
                reg.setNota(notaActual + "\n" + lineaMotivo);
            }
        }

        // 6. Persistir
        XPersistence.getManager().merge(reg);
        XPersistence.commit();

        // 7. Cerrar y refrescar
        closeDialog();
        getView().refresh(); // Después de closeDialog(), getView() retorna la vista de AuditoriaRegistros

        addMessage("✅ Ajustes aplicados correctamente.");
    }

    /**
     * Formatea minutos como HH:MM con signo
     */
    private String formatearAjuste(int minutos) {
        boolean negativo = minutos < 0;
        int absMinutos = Math.abs(minutos);
        int horas = absMinutos / 60;
        int mins = absMinutos % 60;

        String signo = negativo ? "-" : "+";
        return signo + String.format("%02d:%02d", horas, mins);
    }
}
