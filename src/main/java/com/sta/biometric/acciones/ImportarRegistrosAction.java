package com.sta.biometric.acciones;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.tuple.*;
import org.apache.poi.ss.usermodel.*;
import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

/**
 * AcciÃ³n personalizada para importar registros de fichadas desde un archivo Excel.
 * Refactorizada para trabajar con las nuevas clases `AuditoriaRegistros` y `ColeccionRegistros`
 * utilizando `LocalDate` y `LocalTime` en lugar de `LocalDateTime`.
 */


public class ImportarRegistrosAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {

        // 1) Obtener archivo cargado desde la vista
        XFileItem fichero = (XFileItem) getView().getValue("fichero");
        if (fichero == null) {
            addError("Debe seleccionar un archivo vÃ¡lido.");
            return;
        }

        // 2) Cargar configuracion de columnas desde propiedades
        ImportMapping columnas = ImportMapping.cargarDesdePrefijo("import.default");

        // 3) Mapa para agrupar fichadas por (empleado, fecha)
        Map<Pair<Personal, LocalDate>, List<ColeccionRegistros>> porEmpleadoYFecha = new HashMap<>();

        try (InputStream input = new ByteArrayInputStream(fichero.getBytes())) {
            Workbook workbook = WorkbookFactory.create(input);
            Sheet sheet = workbook.getSheetAt(0);

            // Formatos de fecha posibles
            DateTimeFormatter formatoCorto = DateTimeFormatter.ofPattern("dd/MM/yy");
            DateTimeFormatter formatoLargo = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            EntityManager em = XPersistence.getManager();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // === PARSEO DE DATOS ===
                    LocalDate fecha = parsearFecha(row.getCell(columnas.colFecha), formatoCorto, formatoLargo);
                    LocalTime hora = parsearHora(row.getCell(columnas.colHora));
                    if (fecha == null || hora == null) throw new IllegalArgumentException("Fecha u hora invÃ¡lida");

                    String userId = getCellValueAsString(row.getCell(columnas.colUserId)).trim();
                    if (userId.isEmpty()) throw new IllegalArgumentException("UserId vacÃ­o");

                    Personal empleado = em.createQuery(
                        "SELECT e FROM Personal e WHERE e.userId = :userId", Personal.class)
                        .setParameter("userId", userId)
                        .getSingleResult();

                    String ubicacion = getCellValueAsString(row.getCell(columnas.colUbicacion)).trim();
                    if (ubicacion.equalsIgnoreCase("local") || ubicacion.isEmpty()) {
                        ubicacion = Optional.ofNullable(empleado.getSucursal())
                            .map(s -> s.getDireccion().getUbicacion())
                            .orElse("Local");
                    }

                    String descripcion = getCellValueAsString(row.getCell(columnas.colTipoMovimiento)).trim();
                    TipoMovimiento tipo = InterpreteFichadasService.deducirTipoMovimiento(descripcion);
                    if (tipo == null) throw new IllegalArgumentException("No se pudo deducir tipo de movimiento");

                    // === CREACIO“N DEL REGISTRO ===
                    ColeccionRegistros cr = new ColeccionRegistros();
                    cr.setFecha(fecha);
                    cr.setHora(hora);
                    cr.setCoordenada(ubicacion);
                    cr.setTipoMovimiento(tipo);
                    cr.setObservacion("Origen del registro: Importado");

                    Pair<Personal, LocalDate> clave = Pair.of(empleado, fecha);
                    porEmpleadoYFecha.computeIfAbsent(clave, k -> new ArrayList<>()).add(cr);

                } catch (Exception e) {
                    addWarning("Error en fila " + (i + 1) + ": " + e.getMessage());
                }
            }

            workbook.close();

                        auditoria.agregarRegistro(r); // sin duplicados fecha/hora
                    Personal empleado = entry.getKey().getLeft();
                    LocalDate fecha = entry.getKey().getRight();
                    List<ColeccionRegistros> registros = entry.getValue();

                    AuditoriaRegistros auditoria = em.createQuery(
                        "SELECT a FROM AuditoriaRegistros a WHERE a.empleado = :emp AND a.fecha = :fecha", AuditoriaRegistros.class)
                        .setParameter("emp", empleado)
                        .setParameter("fecha", fecha)
                        .getResultStream()
                        .findFirst()
                        .orElse(new AuditoriaRegistros());

                    auditoria.setEmpleado(empleado);
                    auditoria.setFecha(fecha);
                    auditoria.getRegistros().clear();

                    for (ColeccionRegistros r : registros) {
                        r.setAsistenciaDiaria(auditoria);
                        auditoria.getRegistros().add(r);
                    }

                    auditoria.consolidarDesdeRegistros();
                    em.persist(auditoria);
                    em.flush();

                } catch (Exception ex) {
                    addWarning("No se pudo consolidar asistencia para " +
                        entry.getKey().getLeft().getNombreCompleto() + " el " + entry.getKey().getRight() +
                        ": " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            closeDialog();
            getView().refresh();
            addMessage("Importación finalizada correctamente.");
        }
    }

    // ===================== MÃ‰TODOS AUXILIARES =====================

    private LocalDate parsearFecha(Cell cell, DateTimeFormatter corto, DateTimeFormatter largo) {
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            String texto = getCellValueAsString(cell).trim()
                .replaceAll("[\"']", "")
                .replace("-", "/")
                .replace(".", "/")
                .replaceAll("\\s+", "");

            List<DateTimeFormatter> formatos = Arrays.asList(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ofPattern("d/M/yy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
            );

            for (DateTimeFormatter fmt : formatos) {
                try {
                    return LocalDate.parse(texto, fmt);
                } catch (DateTimeParseException ignored) {}
            }

            return LocalDate.parse(texto); // fallback

        } catch (Exception e) {
            addWarning("Fecha invÃ¡lida: '" + getCellValueAsString(cell) + "'");
            return null;
        }
    }

    private LocalTime parsearHora(Cell cell) {
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double val = cell.getNumericCellValue();
                return LocalTime.ofSecondOfDay((long) (val * 86400));
            } else {
                return LocalTime.parse(getCellValueAsString(cell).trim());
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == (long) d) return String.valueOf((long) d);
                return String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    double fd = cell.getNumericCellValue();
                    return (fd == (long) fd) ? String.valueOf((long) fd) : String.valueOf(fd);
                }
            default: return "";
        }
    }
}

