package com.sta.biometric.auxiliares;

import java.io.*;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.fileupload.*;
import org.openxava.annotations.*;

import lombok.*;

/**
 * Modelo transient para el proceso de importación de fichadas desde Excel/CSV.
 * 
 * <p>
 * Este modelo gestiona todo el flujo de importación:
 * </p>
 * <ol>
 * <li>Carga del archivo Excel/CSV</li>
 * <li>Detección de columnas disponibles</li>
 * <li>Mapeo de columnas a propiedades de ColeccionRegistros</li>
 * <li>Vista previa de datos</li>
 * <li>Validación y ejecución de la importación</li>
 * </ol>
 * 
 * @author Sistema STARH
 * @version 2.0
 */
@Getter
@Setter
@View(members = "Archivo [" +
        "  archivo;" +
        "  tieneEncabezados, filaInicio;" +
        "  sucursalUbicacion" +
        "];" +
        "Mapeo [" +
        "  columnaUserId, columnaFecha, columnaHora, columnaTipoMovimiento;" +
        "  columnaUbicacion, columnaObservacion" +
        "]")
public class ImportadorFichadas {

    // ==================================================================================
    // CONSTANTES
    // ==================================================================================

    /** Propiedades que se pueden mapear desde el archivo */
    public static final String[] PROPIEDADES_MAPEABLES = {
            "userId", "fecha", "hora", "tipoMovimiento", "ubicacion", "observacion"
    };

    /** Propiedades requeridas para la importación */
    public static final String[] PROPIEDADES_REQUERIDAS = {
            "userId", "fecha", "hora", "tipoMovimiento"
    };

    // ==================================================================================
    // CONFIGURACIÓN DEL ARCHIVO
    // ==================================================================================

    /**
     * Archivo Excel o CSV a importar.
     */
    @FileItemUpload(acceptFileTypes = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,text/csv,application/vnd.ms-excel", maxFileSizeInKb = 5000)
    @OnChange(com.sta.biometric.acciones.CargarArchivoAction.class)
    private FileItem archivo;

    /**
     * Indica si la primera fila del archivo contiene encabezados.
     */
    @DefaultValueCalculator(value = org.openxava.calculators.TrueCalculator.class)
    private boolean tieneEncabezados = true;

    /**
     * Fila desde donde empiezan los datos (0-indexed).
     * Si tieneEncabezados=true, se ignora la fila 0.
     */
    @DefaultValueCalculator(value = org.openxava.calculators.IntegerCalculator.class, properties = @PropertyValue(name = "value", value = "1"))
    private int filaInicio = 1;

    /**
     * Sucursal opcional para asignar sus coordenadas a todos los registros
     * importados.
     * Si se deja en blanco, no se asignan coordenadas automáticamente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList(descriptionProperties = "nombre", order = "${nombre} asc")
    @NoCreate
    @NoModify
    private Sucursales sucursalUbicacion;

    // ==================================================================================
    // MAPEO DE COLUMNAS
    // ==================================================================================

    /**
     * Columnas detectadas en el archivo (nombres o índices).
     */
    @Transient
    private List<String> columnasDisponibles = new ArrayList<>();

    /**
     * Índice de columna para userId del empleado (requerido).
     */
    @LabelFormat(LabelFormatType.SMALL)
    private Integer columnaUserId;

    /**
     * Índice de columna para la fecha (requerido).
     */
    @LabelFormat(LabelFormatType.SMALL)
    private Integer columnaFecha;

    /**
     * Índice de columna para la hora (requerido).
     */
    @LabelFormat(LabelFormatType.SMALL)
    private Integer columnaHora;

    /**
     * Índice de columna para el tipo de movimiento (requerido).
     */
    @LabelFormat(LabelFormatType.SMALL)
    private Integer columnaTipoMovimiento;

    /**
     * Índice de columna para ubicación/coordenadas (opcional).
     */
    @LabelFormat(LabelFormatType.SMALL)
    private Integer columnaUbicacion;

    /**
     * Índice de columna para observaciones (opcional).
     */
    @LabelFormat(LabelFormatType.SMALL)
    private Integer columnaObservacion;

    // ==================================================================================
    // ESTADO INTERNO (solo para lógica, no en vista)
    // ==================================================================================

    /**
     * Lista de errores encontrados durante la validación/importación.
     */
    @Transient
    private List<String> errores = new ArrayList<>();

    /**
     * Lista de advertencias (filas con problemas menores).
     */
    @Transient
    private List<String> advertencias = new ArrayList<>();

    /**
     * Contador de registros importados exitosamente.
     */
    @Transient
    private int registrosImportados = 0;

    /**
     * Contador de registros con errores.
     */
    @Transient
    private int registrosConError = 0;

    // ==================================================================================
    // MÉTODOS DE VALIDACIÓN
    // ==================================================================================

    /**
     * Verifica si el mapeo de columnas requeridas está completo.
     * 
     * @return true si todas las columnas requeridas están mapeadas
     */
    public boolean isMapeoCumplido() {
        return columnaUserId != null &&
                columnaFecha != null &&
                columnaHora != null &&
                columnaTipoMovimiento != null;
    }

    /**
     * Obtiene los mensajes de error del mapeo incompleto.
     * 
     * @return lista de mensajes de campos faltantes
     */
    public List<String> getMensajesMapeoPendiente() {
        List<String> mensajes = new ArrayList<>();
        if (columnaUserId == null)
            mensajes.add("Debe seleccionar la columna para 'UserId'");
        if (columnaFecha == null)
            mensajes.add("Debe seleccionar la columna para 'Fecha'");
        if (columnaHora == null)
            mensajes.add("Debe seleccionar la columna para 'Hora'");
        if (columnaTipoMovimiento == null)
            mensajes.add("Debe seleccionar la columna para 'Tipo Movimiento'");
        return mensajes;
    }

    /**
     * Valida que el archivo esté cargado y sea válido.
     * 
     * @return true si el archivo es válido
     */
    public boolean isArchivoValido() {
        if (archivo == null)
            return false;
        String nombre = archivo.getName().toLowerCase();
        return nombre.endsWith(".xlsx") || nombre.endsWith(".xls") || nombre.endsWith(".csv");
    }

    /**
     * Determina si el archivo es CSV basándose en la extensión.
     * 
     * @return true si es archivo CSV
     */
    public boolean isArchivoCsv() {
        if (archivo == null)
            return false;
        return archivo.getName().toLowerCase().endsWith(".csv");
    }

    /**
     * Obtiene el InputStream del archivo.
     * 
     * @return InputStream del archivo
     * @throws IOException si hay error al leer el archivo
     */
    public InputStream getArchivoInputStream() throws IOException {
        if (archivo == null)
            return null;
        return archivo.getInputStream();
    }

    /**
     * Limpia los resultados y errores para una nueva importación.
     */
    public void limpiarResultados() {
        errores.clear();
        advertencias.clear();
        registrosImportados = 0;
        registrosConError = 0;
    }

    /**
     * Agrega un error a la lista.
     * 
     * @param mensaje mensaje de error
     */
    public void agregarError(String mensaje) {
        errores.add(mensaje);
        registrosConError++;
    }

    /**
     * Agrega una advertencia a la lista.
     * 
     * @param mensaje mensaje de advertencia
     */
    public void agregarAdvertencia(String mensaje) {
        advertencias.add(mensaje);
    }

    /**
     * Incrementa el contador de registros importados.
     */
    public void incrementarImportados() {
        registrosImportados++;
    }

    /**
     * Obtiene las coordenadas de la sucursal seleccionada.
     * 
     * @return Coordenadas de la sucursal o null si no hay sucursal seleccionada
     */
    public String getCoordenadasSucursal() {
        if (sucursalUbicacion == null || sucursalUbicacion.getDireccion() == null) {
            return null;
        }
        return sucursalUbicacion.getDireccion().getUbicacion();
    }
}
