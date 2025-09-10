package com.sta.biometric.acciones;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.sta.biometric.enums.*;

import net.sf.jasperreports.engine.*;

public class LicenciaImprimirConstanciaAccion extends JasperReportBaseAction {

    @Override
    protected JRDataSource getDataSource() throws Exception {
        // El reporte usará SOLO parámetros (SQL dentro del .jrxml), por eso un datasource vacío con 1 fila
        return new JREmptyDataSource(2);
    }

    @Override
    protected String getJRXML() throws Exception {
        return "InformeLicencias.jrxml";
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map getParameters() throws Exception {
        // Valida contra el modelo correcto (singular)
        Messages errors = MapFacade.validate("Licencia", getView().getValues());
        if (errors.contains()) throw new ValidationException(errors);

        Map params = new HashMap();

        // --- Datos del ítem de la colección (Licencia) ---
        // Asegúrate de que estas propiedades existen con esos nombres en tu entidad/licencia
        
        TipoLicenciaAR tipoEnum = (TipoLicenciaAR) getView().getValue("tipo");
        String tipo = tipoEnum != null ? tipoEnum.getDescripcion() : "";
        Date fechaInicio = (Date) getView().getValue("fechaInicio"); // o 'startDate' si así se llama
        Date fechaFin = (Date) getView().getValue("fechaFin");       // idem

        
        params.put("tipo", tipo);
        params.put("startDate", fechaInicio); // pasa Date; formatea en Jasper
        params.put("endDate", fechaFin);

        // --- (Opcional) Datos del padre (Empleado) desde la vista anterior ---
       
        params.put("empleadoNombre", getPreviousView().getValueString("nombreCompleto"));
        params.put("legajo", getPreviousView().getValue("legajo"));

        // (Opcional) Otros campos del diálogo
        params.put("justificada", getView().getValue("justificada"));
        params.put("dias", getView().getValue("dias"));
        params.put("observaciones", getView().getValueString("observaciones"));

        return params;
    }
}
