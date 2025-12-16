package com.sta.biometric.acciones;


import java.time.format.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;

import com.sta.biometric.modelo.*;

import net.sf.jasperreports.engine.*;

public class PersonalImprimirFichaAction extends JasperReportBaseAction {

    @Override
    protected JRDataSource getDataSource() throws Exception {
        return new JREmptyDataSource();
    }

    @Override
    protected String getJRXML() throws Exception {
        return "PersonalReport.jrxml";
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map getParameters() throws Exception {
        Map params = new HashMap();

        // Obtener la entidad Personal seleccionada
        Personal personal = (Personal) MapFacade.findEntity(getModelName(), getView().getKeyValues());

        if (personal == null) {
            addError("No se ha seleccionado ningún empleado.");
            return params;
        }

        // Formateadores
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // --- Información Personal ---
        params.put("nombreCompleto", personal.getNombreCompleto() != null ? personal.getNombreCompleto() : "");
        params.put("userId", personal.getUserId() != null ? personal.getUserId() : "");

        // DNI: Solo "N°: [numero]"
        String dniStr = "";
        if (personal.getDni() != null && personal.getDni().getNumero() != null) {
            dniStr = "N°: " + personal.getDni().getNumero().trim();
        }
        params.put("dni", dniStr);

        params.put("cuil", personal.getCuil() != null ? personal.getCuil() : "");

        // Fecha Nacimiento y Edad: "dd/MM/yyyy (X Años de edad)"
        params.put("fechaNacimiento",
                personal.getFechaNacimiento() != null ? personal.getFechaNacimiento().format(dateFmt) : "");

        String edadStr = "";
        if (personal.getFechaNacimiento() != null) {
            long anios = java.time.temporal.ChronoUnit.YEARS.between(personal.getFechaNacimiento(),
                    java.time.LocalDate.now());
            edadStr = anios + " Años de edad (a la fecha de impresión)";
        }
        params.put("edad", edadStr);

        params.put("nacionalidad",
                personal.getNacionalidad() != null ? personal.getNacionalidad().getNacionalidad() : "");
        params.put("estadoCivil", personal.getEstadoCivil() != null ? personal.getEstadoCivil().toString() : "");
        params.put("activo", personal.isActivo() ? "ACTIVO" : "INACTIVO");

        // --- Foto del Empleado ---
        java.io.InputStream fotoStream = null;
        String fotoId = personal.getFoto();
        if (fotoId != null && !fotoId.isEmpty()) {
            try {
                // Usar la API de OpenXava para recuperar archivos
                org.openxava.web.editors.IFilePersistor filePersistor = org.openxava.web.editors.FilePersistorFactory
                        .getInstance();
                org.openxava.web.editors.AttachedFile file = filePersistor.find(fotoId);
                if (file != null && file.getData() != null) {
                    fotoStream = new java.io.ByteArrayInputStream(file.getData());
                }
            } catch (Exception e) {
                System.err.println("Error al recuperar foto del empleado: " + e.getMessage());
            }
        }
        params.put("fotoEmpleado", fotoStream);

        // --- Contacto y Domicilio ---

        // Dirección: Usar getDireccionFormateada() de la clase Direccion
        String direccionStr = "";
        try {
            Object dir = personal.getClass().getMethod("getDireccion").invoke(personal);
            if (dir != null) {
                // Intentamos llamar a getDireccionFormateada()
                try {
                    direccionStr = (String) dir.getClass().getMethod("getDireccionFormateada").invoke(dir);
                } catch (Exception e) {
                    // Si falla, fallback a toString o manual
                    direccionStr = dir.toString();
                }
            }
        } catch (Exception e) {
            direccionStr = "No disponible";
        }
        params.put("direccion", direccionStr);

        // Contacto: Celular, Telefono, Email
        String telefonoStr = "";
        String emailStr = "";
        try {
            Object contacto = personal.getClass().getMethod("getContacto").invoke(personal);
            if (contacto != null) {
                String cel = (String) contacto.getClass().getMethod("getCelular").invoke(contacto);
                String tel = (String) contacto.getClass().getMethod("getTelefono").invoke(contacto);
                String mail = (String) contacto.getClass().getMethod("getEmail").invoke(contacto);

                // Construir string de teléfonos
                List<String> tels = new ArrayList<>();
                if (cel != null && !cel.isBlank())
                    tels.add("Cel: " + cel);
                if (tel != null && !tel.isBlank())
                    tels.add("Tel: " + tel);

                telefonoStr = String.join(" / ", tels);
                emailStr = mail != null ? mail : "";
            }
        } catch (Exception e) {
        }
        params.put("telefono", telefonoStr);
        params.put("email", emailStr);

        // --- Información Laboral ---
        params.put("puesto", personal.getPuesto() != null ? personal.getPuesto() : "");

        String sucursalStr = "";
        try {
            Object suc = personal.getClass().getMethod("getSucursal").invoke(personal);
            if (suc != null) {
                sucursalStr = (String) suc.getClass().getMethod("getNombre").invoke(suc);
            }
        } catch (Exception e) {
        }
        params.put("sucursal", sucursalStr);

        params.put("fechaIngreso",
                personal.getInicioActividades() != null ? personal.getInicioActividades().format(dateFmt) : "");
        // Antiguedad ya se calcula al día de hoy en el modelo
        String antiguedadStr = "";
        if (personal.getAntiguedadLaboral() != null) {
            antiguedadStr = personal.getAntiguedadLaboral() + " (a la fecha de impresión)";
        }
        params.put("antiguedad", antiguedadStr);

        // --- Notas Personales ---
        params.put("notasPersonale", personal.getNotasPersonale() != null ? personal.getNotasPersonale() : "");

        // --- QR Code Content ---
        // Simplificar contenido para asegurar lectura
        StringBuilder qr = new StringBuilder();
        qr.append("Legajo:").append(personal.getUserId()).append(";");
        qr.append("Nombre:").append(personal.getNombreCompleto()).append(";");
        if (dniStr.length() > 4)
            qr.append("DNI:").append(dniStr.substring(4)).append(";"); // Quitar "N°: "
        qr.append("Puesto:").append(personal.getPuesto());

        params.put("qrContent", qr.toString());

        return params;
    }
}
