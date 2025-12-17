package com.sta.biometric.acciones;

import java.io.*;
import java.util.*;

import org.apache.commons.fileupload.*;
import org.openxava.actions.*;
import org.openxava.util.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.servicios.*;

/**
 * Acción OnChange que se ejecuta al seleccionar un archivo.
 * 
 * @author Sistema STARH
 * @version 2.0
 */
public class CargarArchivoAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        // Construir modelo desde los valores de la vista
        ImportadorFichadas modelo = construirModeloDesdeVista();

        if (!modelo.isArchivoValido()) {
            addWarning("Por favor seleccione un archivo Excel (.xlsx) o CSV válido.");
            return;
        }

        try {
            // 1. Leer columnas del archivo
            List<String> columnas = LectorArchivoService.obtenerNombresColumnas(modelo);

            if (columnas.isEmpty()) {
                addError("El archivo está vacío o no se pudo leer.");
                return;
            }

            addMessage("Archivo cargado: " + columnas.size() + " columnas detectadas.");

            // 2. Intentar mapeo automático basado en nombres de columnas
            sugerirMapeoAutomatico(columnas);

            // Nota: NO llamar a refresh() porque resetea todos los valores

        } catch (Exception e) {
            addError("Error al procesar el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Construye el modelo ImportadorFichadas desde los valores de la vista.
     */
    private ImportadorFichadas construirModeloDesdeVista() {
        ImportadorFichadas modelo = new ImportadorFichadas();

        // Obtener archivo
        XFileItem xFileItem = (XFileItem) getView().getValue("archivo");
        if (xFileItem != null) {
            modelo.setArchivo(new FileItemWrapper(xFileItem));
        }

        // Obtener configuración
        Boolean tieneEncabezados = (Boolean) getView().getValue("tieneEncabezados");
        modelo.setTieneEncabezados(tieneEncabezados != null ? tieneEncabezados : true);

        Integer filaInicio = (Integer) getView().getValue("filaInicio");
        modelo.setFilaInicio(filaInicio != null ? filaInicio : 1);

        return modelo;
    }

    /**
     * Sugiere mapeo automático basado en nombres de columnas similares.
     */
    private void sugerirMapeoAutomatico(List<String> columnas) {
        Map<String, Integer> sugerencias = new HashMap<>();

        for (int i = 0; i < columnas.size(); i++) {
            String col = normalizarTexto(columnas.get(i));

            // UserId - buscar primero las palabras más específicas
            if (!sugerencias.containsKey("userId")) {
                if (contienePalabra(col, "userid", "user_id", "id_usuario", "codigo", "legajo",
                        "empleado_id", "empleado", "nro", "numero")) {
                    sugerencias.put("userId", i);
                } else if (col.equals("id")) {
                    // Solo "id" exacto como última opción
                    sugerencias.put("userId", i);
                }
            }
            // Fecha
            if (!sugerencias.containsKey("fecha")) {
                if (contienePalabra(col, "fecha", "date", "dia", "day", "fec")) {
                    sugerencias.put("fecha", i);
                }
            }
            // Hora
            if (!sugerencias.containsKey("hora")) {
                if (contienePalabra(col, "hora", "time", "horario", "hor")) {
                    sugerencias.put("hora", i);
                }
            }
            // Tipo de movimiento
            if (!sugerencias.containsKey("tipoMovimiento")) {
                if (contienePalabra(col, "tipo", "movimiento", "type", "accion", "evento",
                        "event", "estado", "mov", "entrada", "salida")) {
                    sugerencias.put("tipoMovimiento", i);
                }
            }
            // Ubicación
            if (!sugerencias.containsKey("ubicacion")) {
                if (contienePalabra(col, "ubicacion", "location", "lugar", "coordenada",
                        "sucursal", "local", "sede", "sitio", "ubic")) {
                    sugerencias.put("ubicacion", i);
                }
            }
            // Observación
            if (!sugerencias.containsKey("observacion")) {
                if (contienePalabra(col, "observacion", "nota", "comentario", "descripcion",
                        "obs", "detalle", "motivo")) {
                    sugerencias.put("observacion", i);
                }
            }
        }

        // Aplicar sugerencias a la vista
        if (sugerencias.containsKey("userId")) {
            getView().setValue("columnaUserId", sugerencias.get("userId"));
        }
        if (sugerencias.containsKey("fecha")) {
            getView().setValue("columnaFecha", sugerencias.get("fecha"));
        }
        if (sugerencias.containsKey("hora")) {
            getView().setValue("columnaHora", sugerencias.get("hora"));
        }
        if (sugerencias.containsKey("tipoMovimiento")) {
            getView().setValue("columnaTipoMovimiento", sugerencias.get("tipoMovimiento"));
        }
        if (sugerencias.containsKey("ubicacion")) {
            getView().setValue("columnaUbicacion", sugerencias.get("ubicacion"));
        }
        if (sugerencias.containsKey("observacion")) {
            getView().setValue("columnaObservacion", sugerencias.get("observacion"));
        }

        // Informar sobre sugerencias
        int sugeridos = sugerencias.size();
        if (sugeridos > 0) {
            addMessage("Se mapearon " + sugeridos + " columnas automáticamente. Verifique el mapeo.");
        } else {
            addWarning("No se encontraron coincidencias automáticas. Configure el mapeo manualmente.");
        }
    }

    /**
     * Normaliza texto: minúsculas, sin acentos, sin espacios extras.
     */
    private String normalizarTexto(String texto) {
        if (texto == null)
            return "";
        return texto.toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
                .replaceAll("[^a-z0-9_]", "")
                .trim();
    }

    private boolean contienePalabra(String texto, String... palabras) {
        for (String palabra : palabras) {
            if (texto.contains(palabra))
                return true;
        }
        return false;
    }

    /**
     * Wrapper para adaptar XFileItem a FileItem.
     */
    private static class FileItemWrapper implements FileItem {
        private final XFileItem xFileItem;

        public FileItemWrapper(XFileItem xFileItem) {
            this.xFileItem = xFileItem;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(xFileItem.getBytes());
        }

        @Override
        public String getName() {
            return xFileItem.getFileName();
        }

        @Override
        public String getContentType() {
            String name = getName();
            if (name == null)
                return "application/octet-stream";
            name = name.toLowerCase();
            if (name.endsWith(".xlsx"))
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            if (name.endsWith(".xls"))
                return "application/vnd.ms-excel";
            if (name.endsWith(".csv"))
                return "text/csv";
            return "application/octet-stream";
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public long getSize() {
            try {
                return xFileItem.getBytes().length;
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        public byte[] get() {
            try {
                return xFileItem.getBytes();
            } catch (Exception e) {
                return new byte[0];
            }
        }

        @Override
        public String getString(String encoding) {
            return "";
        }

        @Override
        public String getString() {
            return "";
        }

        @Override
        public void write(java.io.File file) throws Exception {
        }

        @Override
        public void delete() {
        }

        @Override
        public String getFieldName() {
            return "archivo";
        }

        @Override
        public void setFieldName(String name) {
        }

        @Override
        public boolean isFormField() {
            return false;
        }

        @Override
        public void setFormField(boolean state) {
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
        }

        @Override
        public FileItemHeaders getHeaders() {
            return null;
        }

        @Override
        public void setHeaders(FileItemHeaders headers) {
        }
    }
}
