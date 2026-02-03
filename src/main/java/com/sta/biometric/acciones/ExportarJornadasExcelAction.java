package com.sta.biometric.acciones;

import java.io.*;
import java.time.format.*;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.view.*;

import com.sta.biometric.modelo.*;

/**
 * Acción para exportar la colección calculada jornadasDelPeriodo a Excel.
 * 
 * Esta acción se ejecuta como @ListAction de la colección calculada
 * jornadasDelPeriodo
 * que está dentro de la vista SoloJornadas de LiquidacionJornadas.
 * 
 * Implementa IJavaScriptPostAction para ejecutar JavaScript que abre la URL
 * de descarga en una nueva ventana después de la ejecución de la acción.
 * 
 * @author Sistema STARH
 */
public class ExportarJornadasExcelAction extends CollectionBaseAction implements IJavaScriptPostAction {

    private String javaScript = null;

    @Override
    public String getPostJavaScript() {
        return javaScript;
    }

    @Override
    public void execute() throws Exception {
        try {
            // Obtener el ID de la liquidación desde la jerarquía de vistas
            Object liquidacionId = obtenerLiquidacionId();

            if (liquidacionId == null) {
                addError("No se pudo obtener la liquidación para exportar");
                return;
            }

            // Siempre refrescar desde la BD para obtener los datos completos
            // La entidad desde la vista puede tener campos lazy no cargados
            LiquidacionJornadas liquidacion = XPersistence.getManager().find(LiquidacionJornadas.class, liquidacionId);

            if (liquidacion == null) {
                addError("No se encontró la liquidación en la base de datos");
                return;
            }

            // Obtener la colección de jornadas
            List<AuditoriaRegistros> jornadas = liquidacion.getJornadasDelPeriodo();

            if (jornadas == null || jornadas.isEmpty()) {
                addError("No hay jornadas para exportar en este período");
                return;
            }

            // Crear el archivo Excel
            XSSFWorkbook workbook = crearExcel(jornadas, liquidacion);

            // Generar nombre de archivo
            String nombreArchivo = generarNombreArchivo(liquidacion);

            // Guardar en sesión
            guardarEnSesion(workbook, nombreArchivo);

            // JavaScript para abrir la URL de descarga en nueva ventana
            String contextPath = getRequest().getContextPath();
            javaScript = "window.open('" + contextPath + "/downloadExcel', '_blank');";

            addMessage("Exportando " + jornadas.size() + " jornadas a Excel...");

        } catch (Exception e) {
            addError("Error al generar el archivo Excel: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Obtiene el ID de la LiquidacionJornadas navegando la jerarquía de vistas.
     * 
     * @return El ID de la liquidación o null si no se puede obtener
     */
    private Object obtenerLiquidacionId() {
        // Método 1: Desde la vista padre de la colección (obtener entity y luego ID)
        try {
            View collectionView = getCollectionElementView();
            if (collectionView != null) {
                View parentView = collectionView.getParent();
                if (parentView != null) {
                    Object entity = parentView.getEntity();
                    if (entity instanceof LiquidacionJornadas) {
                        return ((LiquidacionJornadas) entity).getId();
                    }
                }
            }
        } catch (Exception e) {
            // Continuar con siguiente método
        }

        // Método 2: Desde keyValues de la vista padre
        try {
            View collectionView = getCollectionElementView();
            if (collectionView != null) {
                View parentView = collectionView.getParent();
                if (parentView != null) {
                    Map<String, Object> keyValues = parentView.getKeyValues();
                    if (keyValues != null && keyValues.containsKey("id")) {
                        return keyValues.get("id");
                    }
                }
            }
        } catch (Exception e) {
            // Continuar
        }

        // Método 3: Desde getView() directamente
        try {
            Object entity = getView().getEntity();
            if (entity instanceof LiquidacionJornadas) {
                return ((LiquidacionJornadas) entity).getId();
            }
        } catch (Exception e) {
            // Continuar
        }

        // Método 4: Desde getPreviousView()
        try {
            Object entity = getPreviousView().getEntity();
            if (entity instanceof LiquidacionJornadas) {
                return ((LiquidacionJornadas) entity).getId();
            }
        } catch (Exception e) {
            // Continuar
        }

        return null;
    }

    /**
     * Guarda el archivo Excel en la sesión HTTP para que el servlet lo descargue.
     */
    private void guardarEnSesion(XSSFWorkbook workbook, String nombreArchivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        getRequest().getSession().setAttribute("EXCEL_FILE_NAME", nombreArchivo);
        getRequest().getSession().setAttribute("EXCEL_FILE_CONTENT", baos.toByteArray());
        getRequest().getSession().setAttribute("EXCEL_FILE_TYPE",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    /**
     * Crea el workbook de Excel con los datos.
     */
    private XSSFWorkbook crearExcel(List<AuditoriaRegistros> jornadas, LiquidacionJornadas liquidacion) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Jornadas del Período");

        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle dateStyle = crearEstiloFecha(workbook);
        CellStyle normalStyle = crearEstiloNormal(workbook);

        crearEncabezado(sheet, headerStyle);

        int rowNum = 1;
        for (AuditoriaRegistros jornada : jornadas) {
            Row row = sheet.createRow(rowNum++);
            llenarFila(row, jornada, dateStyle, normalStyle);
        }

        ajustarAnchoColumnas(sheet);

        return workbook;
    }

    private void crearEncabezado(XSSFSheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] columnas = {
                "Empleado", "Fecha", "Turno Planificado", "Horario", "Evaluación",
                "Horas Turno", "Horas Extras", "Horas Especiales", "Estado Jornada"
        };

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void llenarFila(Row row, AuditoriaRegistros jornada, CellStyle dateStyle, CellStyle normalStyle) {
        int colNum = 0;

        Cell cell0 = row.createCell(colNum++);
        cell0.setCellValue(jornada.getEmpleado() != null ? jornada.getEmpleado().getNombreCompleto() : "");
        cell0.setCellStyle(normalStyle);

        Cell cell1 = row.createCell(colNum++);
        if (jornada.getFecha() != null) {
            cell1.setCellValue(jornada.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        cell1.setCellStyle(dateStyle);

        Cell cell2 = row.createCell(colNum++);
        cell2.setCellValue(jornada.getTurnoPlanificado() != null ? jornada.getTurnoPlanificado() : "");
        cell2.setCellStyle(normalStyle);

        Cell cell3 = row.createCell(colNum++);
        cell3.setCellValue(jornada.getHorario() != null ? jornada.getHorario() : "");
        cell3.setCellStyle(normalStyle);

        Cell cell4 = row.createCell(colNum++);
        cell4.setCellValue(jornada.getEvaluacion() != null ? jornada.getEvaluacion().toString() : "");
        cell4.setCellStyle(normalStyle);

        Cell cell5 = row.createCell(colNum++);
        cell5.setCellValue(jornada.getHorasTrabajadasTurno() != null ? jornada.getHorasTrabajadasTurno() : "00:00");
        cell5.setCellStyle(normalStyle);

        Cell cell6 = row.createCell(colNum++);
        cell6.setCellValue(jornada.getHorasExtras() != null ? jornada.getHorasExtras() : "00:00");
        cell6.setCellStyle(normalStyle);

        Cell cell7 = row.createCell(colNum++);
        cell7.setCellValue(jornada.getHorasEspeciales() != null ? jornada.getHorasEspeciales() : "00:00");
        cell7.setCellStyle(normalStyle);

        Cell cell8 = row.createCell(colNum++);
        cell8.setCellValue(jornada.getEstadoJornada() != null ? jornada.getEstadoJornada() : "");
        cell8.setCellStyle(normalStyle);
    }

    private CellStyle crearEstiloEncabezado(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloFecha(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloNormal(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void ajustarAnchoColumnas(XSSFSheet sheet) {
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    private String generarNombreArchivo(LiquidacionJornadas liquidacion) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String empleado = liquidacion.getEmpleado() != null
                ? liquidacion.getEmpleado().getNombreCompleto().replaceAll("[^a-zA-Z0-9]", "_")
                : "SinEmpleado";
        String desde = liquidacion.getPeriodoDesde() != null
                ? liquidacion.getPeriodoDesde().format(formatter)
                : "SinFecha";
        String hasta = liquidacion.getPeriodoHasta() != null
                ? liquidacion.getPeriodoHasta().format(formatter)
                : "SinFecha";

        return String.format("Jornadas_%s_%s_al_%s.xlsx", empleado, desde, hasta);
    }
}
