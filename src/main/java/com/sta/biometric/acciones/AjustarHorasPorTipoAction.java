package com.sta.biometric.acciones;

import java.math.BigDecimal;
import java.util.List;

import org.openxava.actions.*;

import com.sta.biometric.enums.Signo;
import com.sta.biometric.modelo.*;

/**
 * Acción que sobrescribe el comportamiento de RemoveSelected para abrir
 * el diálogo de ajuste de horas según el tipo seleccionado.
 * 
 * Extiende ViewBaseAction porque @ElementCollection no soporta las clases
 * de acciones de colección estándar.
 */
public class AjustarHorasPorTipoAction extends ViewBaseAction {

    private int row;
    private String viewObject;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getViewObject() {
        return viewObject;
    }

    public void setViewObject(String viewObject) {
        this.viewObject = viewObject;
    }

    @Override
    public void execute() throws Exception {
        // Obtener el registro padre desde la vista raíz
        Object entity = getView().getRoot().getEntity();

        // Verificar que estamos en el contexto correcto (AuditoriaRegistros)
        if (!(entity instanceof AuditoriaRegistros)) {
            addError("Esta acción solo puede ejecutarse desde el módulo de Auditoría de Registros.");
            return;
        }

        AuditoriaRegistros reg = (AuditoriaRegistros) entity;

        // Obtener las filas de cálculo usando el índice de fila
        List<FilaCalculo> filas = reg.getFilasCalculo();

        if (filas == null || filas.isEmpty() || row < 0 || row >= filas.size()) {
            addError("No se pudo obtener la fila seleccionada (row=" + row + ").");
            return;
        }

        // Tomar la fila correspondiente al índice
        FilaCalculo filaSeleccionada = filas.get(row);
        String tipo = filaSeleccionada.getTipo();
        BigDecimal valorHora = filaSeleccionada.getValorHora();
        String horasRegistradas = filaSeleccionada.getHorasRegistradas();

        if (tipo == null) {
            addError("No se pudo determinar el tipo de hora seleccionado.");
            return;
        }

        // Determinar el tipo de hora y obtener el ajuste actual
        String tipoNormalizado = normalizarTipo(tipo);
        int ajusteActual = obtenerAjusteActual(reg, tipoNormalizado);

        // Guardar el tipo en contexto para la acción de aplicar
        getContext().put(getRequest(), "ajuste_tipo_hora", tipoNormalizado);
        getContext().put(getRequest(), "ajuste_registro_id", reg.getId());

        // Mostrar diálogo
        showDialog();
        getView().setTitle("⚙️ Ajustar " + tipo);
        getView().setModelName("AjusteHorasPorTipo");

        // Establecer valores readonly
        getView().setValue("tipoHora", tipo);
        getView().setValue("valorHora", valorHora);
        getView().setValue("horasRegistradas", horasRegistradas);

        // Establecer ajuste actual
        if (ajusteActual < 0) {
            getView().setValue("signo", Signo.MENOS);
            getView().setValue("ajuste", formatearMinutos(Math.abs(ajusteActual)));
        } else {
            getView().setValue("signo", Signo.MAS);
            getView().setValue("ajuste", formatearMinutos(ajusteActual));
        }

        // Motivo vacío
        getView().setValue("motivo", "");

        // Asignar controlador del diálogo
        setControllers("AjusteHorasPorTipo");
    }

    /**
     * Normaliza el tipo para comparación.
     */
    private String normalizarTipo(String tipo) {
        if (tipo == null)
            return "";
        String lower = tipo.toLowerCase();
        if (lower.contains("normal"))
            return "normales";
        if (lower.contains("extra"))
            return "extras";
        if (lower.contains("especial"))
            return "especiales";
        return tipo;
    }

    /**
     * Obtiene el ajuste actual según el tipo.
     */
    private int obtenerAjusteActual(AuditoriaRegistros reg, String tipo) {
        switch (tipo) {
            case "normales":
                return reg.getAjusteMinutosNormales();
            case "extras":
                return reg.getAjusteMinutosExtras();
            case "especiales":
                return reg.getAjusteMinutosEspeciales();
            default:
                return 0;
        }
    }

    private String formatearMinutos(int minutos) {
        int h = minutos / 60;
        int m = minutos % 60;
        return String.format("%02d:%02d", h, m);
    }
}
