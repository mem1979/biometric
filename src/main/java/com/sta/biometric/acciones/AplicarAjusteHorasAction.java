package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import com.sta.biometric.modelo.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AplicarAjusteHorasAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // 1. Obtener valores del diálogo
        int ajusteNormales = getView().getValueInt("ajusteNormales");
        int ajusteExtras = getView().getValueInt("ajusteExtras");
        int ajusteEspeciales = getView().getValueInt("ajusteEspeciales");
        String motivo = getView().getValueString("motivo");

        // 2. Obtener entidad original
        AuditoriaRegistros regView = (AuditoriaRegistros) getPreviousView().getEntity();
        AuditoriaRegistros reg = XPersistence.getManager().find(AuditoriaRegistros.class, regView.getId());

        if (reg == null) {
            addError("No se encontró el registro para actualizar.");
            return;
        }

        // 3. Aplicar ajustes
        reg.setAjusteMinutosNormales(ajusteNormales);
        reg.setAjusteMinutosExtras(ajusteExtras);
        reg.setAjusteMinutosEspeciales(ajusteEspeciales);

        // 4. Agregar motivo a la nota
        if (motivo != null && !motivo.isBlank()) {
            String notaActual = reg.getNota();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
            String lineaMotivo = "📝 [" + timestamp + "] Ajuste: " + motivo;

            if (notaActual == null || notaActual.isBlank()) {
                reg.setNota(lineaMotivo);
            } else {
                reg.setNota(notaActual + "\n" + lineaMotivo);
            }
        }

        // 5. Persistir
        XPersistence.getManager().merge(reg);
        XPersistence.commit();

        // 6. Cerrar y refrescar
        closeDialog();
        getPreviousView().refresh(); // Recarga los datos desde la DB

        addMessage("Ajustes aplicados correctamente.");
    }
}
