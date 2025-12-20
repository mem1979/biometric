package com.sta.biometric.acciones;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.enums.Signo;
import com.sta.biometric.modelo.*;

/**
 * Acción que aplica el ajuste de horas para un tipo específico.
 * 
 * Lee el tipo guardado en contexto y aplica solo ese ajuste al registro.
 */
public class AplicarAjusteHorasPorTipoAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener valores del diálogo
        Signo signo = (Signo) getView().getValue("signo");
        String ajusteStr = getView().getValueString("ajuste");
        String motivo = getView().getValueString("motivo");
        String tipoHora = getView().getValueString("tipoHora");

        // Validar motivo
        if (motivo == null || motivo.isBlank()) {
            addError("El motivo del ajuste es obligatorio.");
            return;
        }

        // Obtener contexto
        String tipo = (String) getContext().get(getRequest(), "ajuste_tipo_hora");
        String registroId = (String) getContext().get(getRequest(), "ajuste_registro_id");

        if (tipo == null || registroId == null) {
            addError("No se pudo recuperar la información del ajuste.");
            closeDialog();
            return;
        }

        // Calcular minutos con signo
        int minutos = parsearHHMM(ajusteStr);
        if (signo == Signo.MENOS) {
            minutos = -minutos;
        }

        // Buscar y actualizar el registro
        AuditoriaRegistros reg = XPersistence.getManager().find(AuditoriaRegistros.class, registroId);
        if (reg == null) {
            addError("No se encontró el registro de auditoría.");
            closeDialog();
            return;
        }

        // Aplicar ajuste según tipo
        String detalleAjuste = "";
        switch (tipo) {
            case "normales":
                reg.setAjusteMinutosNormales(minutos);
                detalleAjuste = "Normales: " + formatearConSigno(minutos);
                break;
            case "extras":
                reg.setAjusteMinutosExtras(minutos);
                detalleAjuste = "Extras: " + formatearConSigno(minutos);
                break;
            case "especiales":
                reg.setAjusteMinutosEspeciales(minutos);
                detalleAjuste = "Especiales: " + formatearConSigno(minutos);
                break;
            default:
                addError("Tipo de hora no reconocido: " + tipo);
                closeDialog();
                return;
        }

        // Generar línea de motivo con timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String usuario = org.openxava.util.Users.getCurrent();
        String lineaMotivo = String.format("📝 [%s] Usuario: %s | Ajuste (%s): %s",
                timestamp, usuario, detalleAjuste, motivo);

        // Agregar a la nota existente
        String notaActual = reg.getNota();
        if (notaActual == null || notaActual.isBlank()) {
            reg.setNota(lineaMotivo);
        } else {
            reg.setNota(notaActual + "\n" + lineaMotivo);
        }

        // Persistir
        XPersistence.getManager().merge(reg);
        XPersistence.commit();

        // Limpiar contexto
        getContext().remove(getRequest(), "ajuste_tipo_hora");
        getContext().remove(getRequest(), "ajuste_registro_id");

        // Cerrar diálogo y refrescar
        closeDialog();
        getView().getRoot().refresh();

        addMessage("✅ Ajuste de " + tipoHora + " aplicado correctamente.");
    }

    private int parsearHHMM(String valor) {
        if (valor == null || valor.isBlank())
            return 0;
        String limpio = valor.replace("_", "0").trim();
        String[] partes = limpio.split(":");
        try {
            int horas = partes.length >= 1 ? Integer.parseInt(partes[0].trim()) : 0;
            int minutos = partes.length >= 2 ? Integer.parseInt(partes[1].trim()) : 0;
            return horas * 60 + minutos;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatearConSigno(int minutos) {
        String signo = minutos >= 0 ? "+" : "";
        int abs = Math.abs(minutos);
        int h = abs / 60;
        int m = abs % 60;
        return signo + String.format("%02d:%02d", h, m);
    }
}
