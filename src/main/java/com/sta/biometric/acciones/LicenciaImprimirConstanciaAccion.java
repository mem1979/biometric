package com.sta.biometric.acciones;


import java.sql.Date;
import java.time.*;
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
        // El reporte usará SOLO parámetros 
        return new JREmptyDataSource();
    }

    @Override
    protected String getJRXML() throws Exception {
        return "Licencia.jrxml";
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
        LocalDate fechaInicio = (LocalDate) getView().getValue("fechaInicio");
        LocalDate fechaFin    = (LocalDate) getView().getValue("fechaFin");

        // Conversión segura a java.sql.Date (no tiene huso horario)
        Date fechaInicioSql = (fechaInicio != null) ? Date.valueOf(fechaInicio) : null;
        Date fechaFinSql    = (fechaFin != null) ? Date.valueOf(fechaFin) : null;

        params.put("tipo", tipo);
        params.put("startDate", fechaInicioSql);
        params.put("endDate",   fechaFinSql);

        // --- (Opcional) Datos del padre (Empleado) desde la vista anterior ---
       
        params.put("empleadoNombre", getPreviousView().getValueString("nombreCompleto"));
        

        // (Opcional) Otros campos del diálogo
        Boolean justificado = (Boolean) getView().getValue("justificado"); 
        String justificadaStr = (justificado != null && justificado) ? "SI" : "NO";
        params.put("justificada", justificadaStr);
        
        params.put("dias", getView().getValueInt("dias"));
        params.put("observaciones", getView().getValueString("observacion"));

        return params;
    }
}
