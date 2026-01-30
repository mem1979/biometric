package com.sta.biometric.acciones;

import java.io.*;
import java.time.*;
import java.util.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.lang3.tuple.*;
import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;
import com.sta.biometric.servicios.InterpreteFichadasService.*;

/**
 * Acción que ejecuta la importación de fichadas desde el archivo configurado.
 * 
 * @author Sistema STARH
 * @version 2.0
 */
public class EjecutarImportacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Construir modelo desde los valores de la vista
        ImportadorFichadas modelo = construirModeloDesdeVista();

        // Validar que haya archivo
        if (!modelo.isArchivoValido()) {
            addError("Debe seleccionar un archivo Excel o CSV válido.");
            return;
        }

        // Validar mapeo completo
        if (!modelo.isMapeoCumplido()) {
            for (String msg : modelo.getMensajesMapeoPendiente()) {
                addError(msg);
            }
            return;
        }

        // Limpiar resultados anteriores
        modelo.limpiarResultados();

        try {
            // 1. Leer todos los datos del archivo
            List<Map<String, String>> filas = LectorArchivoService.leerDatosParaImportar(modelo);

            if (filas.isEmpty()) {
                addError("El archivo no contiene datos para importar.");
                return;
            }

            addMessage("Procesando " + filas.size() + " filas...");

            // 2. Mapa para agrupar registros por (empleado, fecha)
            Map<Pair<Personal, LocalDate>, List<ColeccionRegistros>> registrosPorEmpleadoFecha = new HashMap<>();

            int registrosImportados = 0;
            int registrosConError = 0;
            List<String> errores = new ArrayList<>();
            List<String> advertencias = new ArrayList<>();

            // 3. Validar y procesar cada fila
            for (Map<String, String> datosRow : filas) {
                ResultadoValidacion validacion = InterpreteFichadasService.validarFila(datosRow);

                if (!validacion.valido) {
                    for (String error : validacion.errores) {
                        errores.add(error);
                        registrosConError++;
                    }
                    continue;
                }

                // Crear registro
                ColeccionRegistros registro = InterpreteFichadasService.crearRegistro(validacion);
                if (registro == null) {
                    errores.add("Fila " + datosRow.get("_numFila") + ": Error al crear registro");
                    registrosConError++;
                    continue;
                }

                // Asignar coordenadas: si hay sucursal seleccionada, usar sus coordenadas
                // (reemplazan las del archivo)
                // Si no hay sucursal, mantener las coordenadas del archivo
                String coordenadasSucursal = modelo.getCoordenadasSucursal();
                System.out.println("[Importador] Coordenadas sucursal: " + coordenadasSucursal);
                if (coordenadasSucursal != null && !coordenadasSucursal.isBlank()) {
                    registro.setCoordenada(coordenadasSucursal);
                    System.out.println("[Importador] Asignadas coordenadas al registro: " + coordenadasSucursal);
                }

                // Agrupar por empleado y FECHA JORNADA (no fecha calendario)
                // Esto permite que fichadas de salida nocturnas (dia X+1) se asignen a la
                // jornada correcta (dia X)
                LocalDate fechaJornada = InterpreteFichadasService.determinarFechaJornada(
                        validacion.empleado, validacion.fecha, validacion.hora);

                Pair<Personal, LocalDate> clave = Pair.of(validacion.empleado, fechaJornada);
                registrosPorEmpleadoFecha
                        .computeIfAbsent(clave, k -> new ArrayList<>())
                        .add(registro);

                registrosImportados++;
            }

            // 4. Consolidar cada grupo con AsistenciaDiariaService
            int jornadasConsolidadas = 0;
            for (Map.Entry<Pair<Personal, LocalDate>, List<ColeccionRegistros>> entry : registrosPorEmpleadoFecha
                    .entrySet()) {
                try {
                    Personal empleado = entry.getKey().getLeft();
                    LocalDate fecha = entry.getKey().getRight();
                    List<ColeccionRegistros> registros = entry.getValue();

                    AsistenciaDiariaService.consolidarDia(empleado, fecha, registros);
                    jornadasConsolidadas++;

                } catch (Exception ex) {
                    Personal empleado = entry.getKey().getLeft();
                    LocalDate fecha = entry.getKey().getRight();
                    advertencias.add("Error al consolidar " + empleado.getNombreCompleto() + " (" + fecha + "): "
                            + ex.getMessage());
                }
            }

            // 5. Commit de la transacción
            XPersistence.getManager().flush();

            // 6. Mensajes finales
            if (registrosImportados > 0) {
                addMessage("✅ Importación completada: " + registrosImportados + " registros en " + jornadasConsolidadas
                        + " jornadas.");
            }

            if (registrosConError > 0) {
                addWarning("⚠️ " + registrosConError + " registros con errores.");
            }

            // 7. Cerrar el diálogo y volver a la lista
            closeDialog();
            getView().getRoot().refresh();

        } catch (Exception e) {
            addError("Error durante la importación: " + e.getMessage());
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

        // Obtener sucursal para coordenadas (OpenXava devuelve un Map con los datos de
        // la referencia)
        @SuppressWarnings("unchecked")
        Map<String, Object> sucursalMap = (Map<String, Object>) getView().getValue("sucursalUbicacion");
        System.out.println("[Importador] sucursalMap: " + sucursalMap);
        if (sucursalMap != null && sucursalMap.get("id") != null) {
            String sucursalId = sucursalMap.get("id").toString();
            System.out.println("[Importador] sucursalId: " + sucursalId);
            Sucursales sucursal = XPersistence.getManager().find(Sucursales.class, sucursalId);
            if (sucursal != null) {
                System.out.println("[Importador] Sucursal encontrada: " + sucursal.getNombre());
                if (sucursal.getDireccion() != null) {
                    String ubicacion = sucursal.getDireccion().getUbicacion();
                    System.out.println("[Importador] Ubicación de sucursal: " + ubicacion);
                    modelo.setSucursalUbicacion(sucursal);
                } else {
                    System.out.println("[Importador] Sucursal sin dirección");
                }
            } else {
                System.out.println("[Importador] Sucursal no encontrada con ID: " + sucursalId);
            }
        }

        // Obtener mapeo de columnas
        modelo.setColumnaUserId((Integer) getView().getValue("columnaUserId"));
        modelo.setColumnaFecha((Integer) getView().getValue("columnaFecha"));
        modelo.setColumnaHora((Integer) getView().getValue("columnaHora"));
        modelo.setColumnaTipoMovimiento((Integer) getView().getValue("columnaTipoMovimiento"));
        modelo.setColumnaUbicacion((Integer) getView().getValue("columnaUbicacion"));
        modelo.setColumnaObservacion((Integer) getView().getValue("columnaObservacion"));

        return modelo;
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
