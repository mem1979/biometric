package com.sta.biometric.servicios;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.apache.poi.ss.usermodel.*;

import com.sta.biometric.auxiliares.*;

/**
 * Servicio para leer archivos Excel y CSV para importación de fichadas.
 * 
 * <p>
 * Soporta los siguientes formatos:
 * </p>
 * <ul>
 * <li>Excel 2007+ (.xlsx)</li>
 * <li>Excel 97-2003 (.xls)</li>
 * <li>CSV con separadores , o ;</li>
 * </ul>
 * 
 * @author Sistema STARH
 * @version 2.0
 */
public class LectorArchivoService {

    private static final int MAX_FILAS_PREVIEW = 5;
    private static final int MAX_COLUMNAS = 20;

    // ==================================================================================
    // LECTURA DE COLUMNAS
    // ==================================================================================

    /**
     * Obtiene los nombres de columnas del archivo.
     * 
     * @param modelo Modelo de importación con el archivo cargado
     * @return Lista de nombres de columnas (o índices si no hay encabezados)
     */
    public static List<String> obtenerNombresColumnas(ImportadorFichadas modelo) {
        if (!modelo.isArchivoValido()) {
            return Collections.emptyList();
        }

        try (InputStream input = modelo.getArchivoInputStream()) {
            if (modelo.isArchivoCsv()) {
                return leerColumnasCsv(input, modelo.isTieneEncabezados());
            } else {
                return leerColumnasExcel(input, modelo.isTieneEncabezados());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static List<String> leerColumnasExcel(InputStream input, boolean tieneEncabezados) throws Exception {
        List<String> columnas = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row primeraFila = sheet.getRow(0);

            if (primeraFila == null)
                return columnas;

            int numColumnas = Math.min(primeraFila.getLastCellNum(), MAX_COLUMNAS);

            for (int i = 0; i < numColumnas; i++) {
                Cell cell = primeraFila.getCell(i);
                String valor = getCellValueAsString(cell);

                if (tieneEncabezados && valor != null && !valor.isEmpty()) {
                    columnas.add(valor);
                } else {
                    columnas.add("Columna " + (i + 1));
                }
            }
        }
        return columnas;
    }

    private static List<String> leerColumnasCsv(InputStream input, boolean tieneEncabezados) throws Exception {
        List<String> columnas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String primeraLinea = reader.readLine();
            if (primeraLinea == null)
                return columnas;

            String separador = detectarSeparadorCsv(primeraLinea);
            String[] valores = primeraLinea.split(separador, -1);

            for (int i = 0; i < Math.min(valores.length, MAX_COLUMNAS); i++) {
                String valor = valores[i].trim().replace("\"", "");

                if (tieneEncabezados && !valor.isEmpty()) {
                    columnas.add(valor);
                } else {
                    columnas.add("Columna " + (i + 1));
                }
            }
        }
        return columnas;
    }

    // ==================================================================================
    // VISTA PREVIA
    // ==================================================================================

    /**
     * Genera una vista previa de las primeras filas del archivo.
     * 
     * @param modelo Modelo de importación
     * @return Texto formateado con la vista previa
     */
    public static String generarVistaPrevia(ImportadorFichadas modelo) {
        if (!modelo.isArchivoValido()) {
            return "No se ha cargado un archivo válido.";
        }

        try (InputStream input = modelo.getArchivoInputStream()) {
            List<List<String>> filas;

            if (modelo.isArchivoCsv()) {
                filas = leerFilasCsv(input, modelo.getFilaInicio(), MAX_FILAS_PREVIEW);
            } else {
                filas = leerFilasExcel(input, modelo.getFilaInicio(), MAX_FILAS_PREVIEW);
            }

            return formatearVistaPrevia(filas, modelo.getColumnasDisponibles());
        } catch (Exception e) {
            return "Error al leer el archivo: " + e.getMessage();
        }
    }

    private static List<List<String>> leerFilasExcel(InputStream input, int filaInicio, int maxFilas) throws Exception {
        List<List<String>> resultado = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            int ultimaFila = Math.min(sheet.getLastRowNum(), filaInicio + maxFilas - 1);

            for (int i = filaInicio; i <= ultimaFila; i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                List<String> fila = new ArrayList<>();
                int numCols = Math.min(row.getLastCellNum(), MAX_COLUMNAS);

                for (int j = 0; j < numCols; j++) {
                    fila.add(getCellValueAsString(row.getCell(j)));
                }
                resultado.add(fila);
            }
        }
        return resultado;
    }

    private static List<List<String>> leerFilasCsv(InputStream input, int filaInicio, int maxFilas) throws Exception {
        List<List<String>> resultado = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String linea;
            String separador = null;
            int filaActual = 0;
            int filasLeidas = 0;

            while ((linea = reader.readLine()) != null && filasLeidas < maxFilas) {
                if (separador == null) {
                    separador = detectarSeparadorCsv(linea);
                }

                if (filaActual >= filaInicio) {
                    String[] valores = linea.split(separador, -1);
                    List<String> fila = new ArrayList<>();

                    for (int i = 0; i < Math.min(valores.length, MAX_COLUMNAS); i++) {
                        fila.add(valores[i].trim().replace("\"", ""));
                    }
                    resultado.add(fila);
                    filasLeidas++;
                }
                filaActual++;
            }
        }
        return resultado;
    }

    private static String formatearVistaPrevia(List<List<String>> filas, List<String> columnas) {
        if (filas.isEmpty()) {
            return "El archivo no contiene datos para mostrar.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Primeras ").append(filas.size()).append(" filas de datos:\n");
        sb.append("─".repeat(60)).append("\n");

        // Encabezados
        if (!columnas.isEmpty()) {
            sb.append("│ ");
            for (int i = 0; i < Math.min(columnas.size(), 6); i++) {
                sb.append(String.format("%-12s", truncar(columnas.get(i), 11))).append(" │ ");
            }
            sb.append("\n");
            sb.append("─".repeat(60)).append("\n");
        }

        // Datos
        for (List<String> fila : filas) {
            sb.append("│ ");
            for (int i = 0; i < Math.min(fila.size(), 6); i++) {
                sb.append(String.format("%-12s", truncar(fila.get(i), 11))).append(" │ ");
            }
            sb.append("\n");
        }
        sb.append("─".repeat(60)).append("\n");

        return sb.toString();
    }

    // ==================================================================================
    // LECTURA DE DATOS PARA IMPORTACIÓN
    // ==================================================================================

    /**
     * Lee todos los datos del archivo según el mapeo configurado.
     * 
     * @param modelo Modelo con la configuración de mapeo
     * @return Lista de mapas con los datos de cada fila
     */
    public static List<Map<String, String>> leerDatosParaImportar(ImportadorFichadas modelo) {
        if (!modelo.isArchivoValido()) {
            return Collections.emptyList();
        }

        try (InputStream input = modelo.getArchivoInputStream()) {
            if (modelo.isArchivoCsv()) {
                return leerDatosCsvConMapeo(input, modelo);
            } else {
                return leerDatosExcelConMapeo(input, modelo);
            }
        } catch (Exception e) {
            modelo.agregarError("Error al leer el archivo: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private static List<Map<String, String>> leerDatosExcelConMapeo(InputStream input, ImportadorFichadas modelo)
            throws Exception {
        List<Map<String, String>> resultado = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = modelo.getFilaInicio(); i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                Map<String, String> fila = new HashMap<>();
                fila.put("_numFila", String.valueOf(i + 1));

                // Mapear cada propiedad según la configuración
                if (modelo.getColumnaUserId() != null) {
                    fila.put("userId", getCellValueAsString(row.getCell(modelo.getColumnaUserId())));
                }
                if (modelo.getColumnaFecha() != null) {
                    Cell cell = row.getCell(modelo.getColumnaFecha());
                    fila.put("fecha", getCellValueAsStringFecha(cell));
                }
                if (modelo.getColumnaHora() != null) {
                    Cell cell = row.getCell(modelo.getColumnaHora());
                    fila.put("hora", getCellValueAsStringHora(cell));
                }
                if (modelo.getColumnaTipoMovimiento() != null) {
                    fila.put("tipoMovimiento", getCellValueAsString(row.getCell(modelo.getColumnaTipoMovimiento())));
                }
                if (modelo.getColumnaUbicacion() != null) {
                    fila.put("ubicacion", getCellValueAsString(row.getCell(modelo.getColumnaUbicacion())));
                }
                if (modelo.getColumnaObservacion() != null) {
                    fila.put("observacion", getCellValueAsString(row.getCell(modelo.getColumnaObservacion())));
                }

                resultado.add(fila);
            }
        }
        return resultado;
    }

    private static List<Map<String, String>> leerDatosCsvConMapeo(InputStream input, ImportadorFichadas modelo)
            throws Exception {
        List<Map<String, String>> resultado = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String linea;
            String separador = null;
            int filaActual = 0;

            while ((linea = reader.readLine()) != null) {
                if (separador == null) {
                    separador = detectarSeparadorCsv(linea);
                }

                if (filaActual >= modelo.getFilaInicio()) {
                    String[] valores = linea.split(separador, -1);
                    Map<String, String> fila = new HashMap<>();
                    fila.put("_numFila", String.valueOf(filaActual + 1));

                    // Mapear cada propiedad según la configuración
                    if (modelo.getColumnaUserId() != null && modelo.getColumnaUserId() < valores.length) {
                        fila.put("userId", valores[modelo.getColumnaUserId()].trim().replace("\"", ""));
                    }
                    if (modelo.getColumnaFecha() != null && modelo.getColumnaFecha() < valores.length) {
                        fila.put("fecha", valores[modelo.getColumnaFecha()].trim().replace("\"", ""));
                    }
                    if (modelo.getColumnaHora() != null && modelo.getColumnaHora() < valores.length) {
                        fila.put("hora", valores[modelo.getColumnaHora()].trim().replace("\"", ""));
                    }
                    if (modelo.getColumnaTipoMovimiento() != null
                            && modelo.getColumnaTipoMovimiento() < valores.length) {
                        fila.put("tipoMovimiento", valores[modelo.getColumnaTipoMovimiento()].trim().replace("\"", ""));
                    }
                    if (modelo.getColumnaUbicacion() != null && modelo.getColumnaUbicacion() < valores.length) {
                        fila.put("ubicacion", valores[modelo.getColumnaUbicacion()].trim().replace("\"", ""));
                    }
                    if (modelo.getColumnaObservacion() != null && modelo.getColumnaObservacion() < valores.length) {
                        fila.put("observacion", valores[modelo.getColumnaObservacion()].trim().replace("\"", ""));
                    }

                    resultado.add(fila);
                }
                filaActual++;
            }
        }
        return resultado;
    }

    // ==================================================================================
    // MÉTODOS AUXILIARES
    // ==================================================================================

    private static String detectarSeparadorCsv(String linea) {
        int comas = linea.length() - linea.replace(",", "").length();
        int puntosComa = linea.length() - linea.replace(";", "").length();
        int tabs = linea.length() - linea.replace("\t", "").length();

        if (puntosComa >= comas && puntosComa >= tabs)
            return ";";
        if (tabs >= comas && tabs >= puntosComa)
            return "\t";
        return ",";
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == (long) d) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    double fd = cell.getNumericCellValue();
                    return (fd == (long) fd) ? String.valueOf((long) fd) : String.valueOf(fd);
                }
            default:
                return "";
        }
    }

    private static String getCellValueAsStringFecha(Cell cell) {
        if (cell == null)
            return "";

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                java.time.LocalDate fecha = cell.getDateCellValue().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                return fecha.toString(); // Formato ISO: yyyy-MM-dd
            }
        } catch (Exception e) {
            // Intentar como texto
        }
        return getCellValueAsString(cell);
    }

    private static String getCellValueAsStringHora(Cell cell) {
        if (cell == null)
            return "";

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double val = cell.getNumericCellValue();
                // Si es fracción de día (Excel almacena horas como fracciones)
                if (val < 1.0) {
                    long segundos = (long) (val * 86400);
                    java.time.LocalTime hora = java.time.LocalTime.ofSecondOfDay(segundos);
                    return hora.toString();
                }
            }
        } catch (Exception e) {
            // Intentar como texto
        }
        return getCellValueAsString(cell);
    }

    private static String truncar(String texto, int maxLen) {
        if (texto == null)
            return "";
        if (texto.length() <= maxLen)
            return texto;
        return texto.substring(0, maxLen - 2) + "..";
    }
}
