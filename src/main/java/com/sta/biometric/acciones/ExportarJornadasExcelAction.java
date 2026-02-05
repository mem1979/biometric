package com.sta.biometric.acciones;

import java.io.*;
import java.math.*;
import java.time.format.*;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
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

    // Clase auxiliar para totales
    private class TotalesResumen {
        BigDecimal totalMinutosNormales = BigDecimal.ZERO;
        BigDecimal totalMontoNormales = BigDecimal.ZERO;
        BigDecimal totalMinutosExtras = BigDecimal.ZERO;
        BigDecimal totalMontoExtras = BigDecimal.ZERO;
        BigDecimal totalMinutosEspeciales = BigDecimal.ZERO;
        BigDecimal totalMontoEspeciales = BigDecimal.ZERO;

        BigDecimal totalGeneral = BigDecimal.ZERO;
    }

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
                    @SuppressWarnings("unchecked")
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
    /**
     * Crea el workbook de Excel con los datos.
     */
    private XSSFWorkbook crearExcel(List<AuditoriaRegistros> jornadas, LiquidacionJornadas liquidacion) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Jornadas del Período");

        // Calcular totales
        TotalesResumen totales = calcularTotales(jornadas);

        // Estilos
        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle dateStyle = crearEstiloFecha(workbook);
        CellStyle normalStyle = crearEstiloNormal(workbook);
        CellStyle moneyStyle = crearEstiloMoneda(workbook);
        CellStyle boldStyle = crearEstiloNegrita(workbook);
        CellStyle titleStyle = crearEstiloTitulo(workbook);
        CellStyle moneyBoldStyle = crearEstiloMonedaNegrita(workbook);

        int currentRow = 0;

        // 1. Encabezado principal
        currentRow = crearEncabezadoPrincipal(sheet, liquidacion, titleStyle, currentRow);

        // 2. Tabla de Resumen
        currentRow = crearTablaResumen(sheet, totales, headerStyle, normalStyle, moneyStyle, boldStyle, moneyBoldStyle,
                currentRow);

        // Espacio antes del detalle
        currentRow++;

        // 3. Detalle de jornadas
        crearEncabezadoDetalle(sheet, headerStyle, currentRow++);

        for (AuditoriaRegistros jornada : jornadas) {
            Row row = sheet.createRow(currentRow++);
            llenarFila(row, jornada, dateStyle, normalStyle, moneyStyle);
        }

        ajustarAnchoColumnas(sheet);

        return workbook;
    }

    private TotalesResumen calcularTotales(List<AuditoriaRegistros> jornadas) {
        TotalesResumen t = new TotalesResumen();

        for (AuditoriaRegistros j : jornadas) {
            // Sumar montos
            if (j.getTotalHorasTurno() != null)
                t.totalMontoNormales = t.totalMontoNormales.add(j.getTotalHorasTurno());
            if (j.getTotalHorasExtras() != null)
                t.totalMontoExtras = t.totalMontoExtras.add(j.getTotalHorasExtras());
            if (j.getTotalHorasEspeciales() != null)
                t.totalMontoEspeciales = t.totalMontoEspeciales.add(j.getTotalHorasEspeciales());

            // Sumar minutos (parseando HH:mm)
            t.totalMinutosNormales = t.totalMinutosNormales
                    .add(new BigDecimal(parsearMinutos(j.getHorasTrabajadasTurno())));
            t.totalMinutosExtras = t.totalMinutosExtras.add(new BigDecimal(parsearMinutos(j.getHorasExtras())));
            t.totalMinutosEspeciales = t.totalMinutosEspeciales
                    .add(new BigDecimal(parsearMinutos(j.getHorasEspeciales())));
        }

        t.totalGeneral = t.totalMontoNormales.add(t.totalMontoExtras).add(t.totalMontoEspeciales);
        return t;
    }

    private int parsearMinutos(String horaHHMM) {
        if (horaHHMM == null || horaHHMM.trim().isEmpty() || !horaHHMM.contains(":")) {
            return 0;
        }
        try {
            String[] parts = horaHHMM.split(":");
            int horas = Integer.parseInt(parts[0]);
            int minutos = Integer.parseInt(parts[1]);
            return horas * 60 + minutos;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatearMinutos(BigDecimal totalMinutos) {
        int minutos = totalMinutos.intValue();
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%d:%02d", horas, mins);
    }

    private int crearEncabezadoPrincipal(XSSFSheet sheet, LiquidacionJornadas liquidacion, CellStyle titleStyle,
            int startRow) {
        Row row = sheet.createRow(startRow);
        Cell cell = row.createCell(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String desde = liquidacion.getPeriodoDesde() != null ? liquidacion.getPeriodoDesde().format(formatter) : "-";
        String hasta = liquidacion.getPeriodoHasta() != null ? liquidacion.getPeriodoHasta().format(formatter) : "-";
        String empleado = liquidacion.getEmpleado() != null ? liquidacion.getEmpleado().getNombreCompleto() : "-";

        cell.setCellValue(
                "Liquidación de Jornada para el período desde " + desde + " al " + hasta + " de: " + empleado);
        cell.setCellStyle(titleStyle);

        // Fusionar celdas A1:K3 (aprox) para el título
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 2, 0, 10));

        return startRow + 4; // Dejar espacio despues del titulo
    }

    private int crearTablaResumen(XSSFSheet sheet, TotalesResumen t, CellStyle headerStyle, CellStyle normalStyle,
            CellStyle moneyStyle, CellStyle boldStyle, CellStyle moneyBoldStyle, int startRow) {
        int currentRow = startRow;

        // Encabezados tabla resumen
        Row headerRow = sheet.createRow(currentRow++);
        crearCelda(headerRow, 0, "Concepto", headerStyle);
        crearCelda(headerRow, 1, "Horas", headerStyle);
        crearCelda(headerRow, 2, "Valor Hora", headerStyle);
        crearCelda(headerRow, 3, "Total $", headerStyle);

        // Fila Normales
        Row rowNormal = sheet.createRow(currentRow++);
        crearCelda(rowNormal, 0, "Horas Normales", boldStyle);
        crearCelda(rowNormal, 1, formatearMinutos(t.totalMinutosNormales), normalStyle);
        crearCelda(rowNormal, 2, calcularValorHora(t.totalMontoNormales, t.totalMinutosNormales), moneyStyle);
        crearCelda(rowNormal, 3, t.totalMontoNormales.doubleValue(), moneyStyle);

        // Fila Extras
        Row rowExtra = sheet.createRow(currentRow++);
        crearCelda(rowExtra, 0, "Horas Extras", boldStyle);
        crearCelda(rowExtra, 1, formatearMinutos(t.totalMinutosExtras), normalStyle);
        crearCelda(rowExtra, 2, calcularValorHora(t.totalMontoExtras, t.totalMinutosExtras), moneyStyle);
        crearCelda(rowExtra, 3, t.totalMontoExtras.doubleValue(), moneyStyle);

        // Fila Especiales
        Row rowEsp = sheet.createRow(currentRow++);
        crearCelda(rowEsp, 0, "Horas Especiales", boldStyle);
        crearCelda(rowEsp, 1, formatearMinutos(t.totalMinutosEspeciales), normalStyle);
        crearCelda(rowEsp, 2, calcularValorHora(t.totalMontoEspeciales, t.totalMinutosEspeciales), moneyStyle);
        crearCelda(rowEsp, 3, t.totalMontoEspeciales.doubleValue(), moneyStyle);

        // Fila Total General
        Row rowTotal = sheet.createRow(currentRow++);
        crearCelda(rowTotal, 0, "TOTAL GENERAL", boldStyle);
        crearCelda(rowTotal, 1, "", boldStyle); // Vacío
        crearCelda(rowTotal, 2, "", boldStyle); // Vacío
        crearCelda(rowTotal, 3, t.totalGeneral.doubleValue(), moneyBoldStyle);

        return currentRow;
    }

    private double calcularValorHora(BigDecimal montoTotal, BigDecimal minutosTotales) {
        if (minutosTotales.compareTo(BigDecimal.ZERO) == 0)
            return 0.0;
        // Convertir minutos a horas: minutos / 60
        BigDecimal horas = minutosTotales.divide(new BigDecimal(60), 4, RoundingMode.HALF_UP);
        if (horas.compareTo(BigDecimal.ZERO) == 0)
            return 0.0;
        return montoTotal.divide(horas, 2, RoundingMode.HALF_UP).doubleValue();
    }

    private void crearCelda(Row row, int col, Object valor, CellStyle style) {
        Cell cell = row.createCell(col);
        if (valor instanceof String)
            cell.setCellValue((String) valor);
        else if (valor instanceof Double)
            cell.setCellValue((Double) valor);
        else
            cell.setCellValue(valor.toString());
        cell.setCellStyle(style);
    }

    private void crearEncabezadoDetalle(XSSFSheet sheet, CellStyle headerStyle, int rowNum) {
        Row headerRow = sheet.createRow(rowNum);
        String[] columnas = {
                "Empleado", "Fecha", "Turno Planificado", "Horario", "Estado Jornada",
                "Horas Turno", "$ Hs.",
                "Horas Extras", "$ Hs. Extras",
                "Horas Especiales", "$ Hs. Especiales"
        };

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void llenarFila(Row row, AuditoriaRegistros jornada, CellStyle dateStyle, CellStyle normalStyle,
            CellStyle moneyStyle) {
        int colNum = 0;

        // Columna A: Empleado
        Cell cell = row.createCell(colNum++);
        cell.setCellValue(jornada.getEmpleado() != null ? jornada.getEmpleado().getNombreCompleto() : "");
        cell.setCellStyle(normalStyle);

        // Columna B: Fecha
        cell = row.createCell(colNum++);
        if (jornada.getFecha() != null) {
            cell.setCellValue(jornada.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        cell.setCellStyle(dateStyle);

        // Columna C: Turno Planificado
        cell = row.createCell(colNum++);
        cell.setCellValue(jornada.getTurnoPlanificado() != null ? jornada.getTurnoPlanificado() : "");
        cell.setCellStyle(normalStyle);

        // Columna D: Horario
        cell = row.createCell(colNum++);
        cell.setCellValue(jornada.getHorario() != null ? jornada.getHorario() : "");
        cell.setCellStyle(normalStyle);

        // Columna E: Estado Jornada (movida aquí)
        cell = row.createCell(colNum++);
        cell.setCellValue(jornada.getEstadoJornada() != null ? jornada.getEstadoJornada() : "");
        cell.setCellStyle(normalStyle);

        // Columna F: Horas Turno
        cell = row.createCell(colNum++);
        cell.setCellValue(jornada.getHorasTrabajadasTurno() != null ? jornada.getHorasTrabajadasTurno() : "00:00");
        cell.setCellStyle(normalStyle);

        // Columna G: $ Hs. (Monto Horas Normales)
        cell = row.createCell(colNum++);
        if (jornada.getTotalHorasTurno() != null) {
            cell.setCellValue(jornada.getTotalHorasTurno().doubleValue());
        } else {
            cell.setCellValue(0.0);
        }
        cell.setCellStyle(moneyStyle);

        // Columna H: Horas Extras
        cell = row.createCell(colNum++);
        cell.setCellValue(jornada.getHorasExtras() != null ? jornada.getHorasExtras() : "00:00");
        cell.setCellStyle(normalStyle);

        // Columna I: $ Hs. Extras (Monto Horas Extras)
        cell = row.createCell(colNum++);
        if (jornada.getTotalHorasExtras() != null) {
            cell.setCellValue(jornada.getTotalHorasExtras().doubleValue());
        } else {
            cell.setCellValue(0.0);
        }
        cell.setCellStyle(moneyStyle);

        // Columna J: Horas Especiales
        cell = row.createCell(colNum++);
        cell.setCellValue(jornada.getHorasEspeciales() != null ? jornada.getHorasEspeciales() : "00:00");
        cell.setCellStyle(normalStyle);

        // Columna K: $ Hs. Especiales (Monto Horas Especiales)
        cell = row.createCell(colNum++);
        if (jornada.getTotalHorasEspeciales() != null) {
            cell.setCellValue(jornada.getTotalHorasEspeciales().doubleValue());
        } else {
            cell.setCellValue(0.0);
        }
        cell.setCellStyle(moneyStyle);
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

    private CellStyle crearEstiloMoneda(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        // Formato de moneda con 2 decimales
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle crearEstiloNegrita(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloTitulo(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloMonedaNegrita(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$ #,##0.00"));
        return style;
    }

    private void ajustarAnchoColumnas(XSSFSheet sheet) {
        // 11 columnas: Empleado, Fecha, Turno, Horario, HorasTurno, $Hs, HorasExtras,
        // $HsExtras, HorasEsp, $HsEsp, Estado
        for (int i = 0; i < 11; i++) {
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
