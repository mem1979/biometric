package com.sta.biometric.acciones;

import java.time.*;
import java.time.format.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

import com.sta.biometric.formateadores.*;
import com.sta.biometric.modelo.*;

/**
 * Acción que aplica los ajustes de tiempo al registro de AuditoriaRegistros.
 * 
 * Validaciones:
 * - Si hay cambios en los ajustes, el motivo es obligatorio
 * - Registra automáticamente fecha, hora y usuario en la nota
 */
public class AplicarAjusteHorasAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // 1. Obtener valores del diálogo
        String ajusteNormalesStr = getView().getValueString("ajusteNormales");
        String ajusteExtrasStr = getView().getValueString("ajusteExtras");
        String ajusteEspecialesStr = getView().getValueString("ajusteEspeciales");
        String motivo = getView().getValueString("motivo");

        // 2. Convertir HH:MM a minutos
        int ajusteNormales = TiempoUtils.parsearHHMMaMinutos(ajusteNormalesStr);
        int ajusteExtras = TiempoUtils.parsearHHMMaMinutos(ajusteExtrasStr);
        int ajusteEspeciales = TiempoUtils.parsearHHMMaMinutos(ajusteEspecialesStr);

        // 3. Obtener entidad original para comparar
        AuditoriaRegistros regView = (AuditoriaRegistros) getPreviousView().getEntity();
        AuditoriaRegistros reg = XPersistence.getManager().find(AuditoriaRegistros.class, regView.getId());

        if (reg == null) {
            addError("No se encontró el registro para actualizar.");
            return;
        }

        // 4. VALIDACIÓN: Verificar si hay cambios
        boolean hayCambios = ajusteNormales != reg.getAjusteMinutosNormales() ||
                ajusteExtras != reg.getAjusteMinutosExtras() ||
                ajusteEspeciales != reg.getAjusteMinutosEspeciales();

        // 5. VALIDACIÓN: Si hay cambios, el motivo es obligatorio
        if (hayCambios && (motivo == null || motivo.isBlank())) {
            addError("Debe ingresar un motivo para justificar los cambios en los ajustes.");
            return; // NO cerrar el diálogo
        }

        // Si no hay cambios, simplemente cerrar
        if (!hayCambios) {
            closeDialog();
            addMessage("No se realizaron cambios.");
            return;
        }

        // 6. Aplicar ajustes de tiempo
        reg.setAjusteMinutosNormales(ajusteNormales);
        reg.setAjusteMinutosExtras(ajusteExtras);
        reg.setAjusteMinutosEspeciales(ajusteEspeciales);

        // 7. Construir detalle del ajuste con FECHA, HORA y USUARIO
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String usuario = Users.getCurrent(); // Usuario logueado

        StringBuilder detalleAjuste = new StringBuilder();

        // Detallar cambios de tiempo
        if (ajusteNormales != 0) {
            detalleAjuste.append("Normales: ").append(TiempoUtils.formatearMinutosConSigno(ajusteNormales));
        }
        if (ajusteExtras != 0) {
            if (detalleAjuste.length() > 0)
                detalleAjuste.append(" | ");
            detalleAjuste.append("Extras: ").append(TiempoUtils.formatearMinutosConSigno(ajusteExtras));
        }
        if (ajusteEspeciales != 0) {
            if (detalleAjuste.length() > 0)
                detalleAjuste.append(" | ");
            detalleAjuste.append("Especiales: ").append(TiempoUtils.formatearMinutosConSigno(ajusteEspeciales));
        }

        // Formato: 📝 [17/12/2025 23:15] Usuario: admin | Ajuste (Normales: +01:00):
        // Motivo ingresado
        String lineaMotivo = String.format("📝 [%s] Usuario: %s | Ajuste", timestamp, usuario);
        if (detalleAjuste.length() > 0) {
            lineaMotivo += " (" + detalleAjuste + ")";
        }
        lineaMotivo += ": " + motivo;

        // 8. Agregar a la nota existente
        String notaActual = reg.getNota();
        if (notaActual == null || notaActual.isBlank()) {
            reg.setNota(lineaMotivo);
        } else {
            reg.setNota(notaActual + "\n" + lineaMotivo);
        }

        // 9. Persistir
        XPersistence.getManager().merge(reg);
        XPersistence.commit();

        // 10. Cerrar y refrescar
        closeDialog();
        getView().refresh();

        addMessage("✅ Ajustes aplicados correctamente.");
    }
}
