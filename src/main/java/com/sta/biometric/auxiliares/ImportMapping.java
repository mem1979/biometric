package com.sta.biometric.auxiliares;

import com.sta.biometric.servicios.*;

/**
 * Clase utilitaria para cargar la configuración de mapeo de columnas
 * desde las propiedades definidas (por ejemplo, en xava.properties).
 */
public class ImportMapping {

    public int colFecha = 1;
    public int colHora = 2;
    public int colUbicacion = 3;
    public int colTipoMovimiento = 4;
    public int colUserId = 6;

    /**
     * Carga el mapeo de columnas desde las propiedades, usando un prefijo como "import.default" o "import.clienteA".
     * Si alguna propiedad no está definida, se usan los valores por defecto.
     * 
     * @param prefijo Prefijo usado para buscar en las propiedades. Ej: "import.default"
     * @return Un objeto ImportMapping con las columnas configuradas.
     */
    public static ImportMapping cargarDesdePrefijo(String prefijo) {
        ImportMapping m = new ImportMapping();
        try {
            m.colFecha = Integer.parseInt(getProp(prefijo + ".columna.fecha", String.valueOf(m.colFecha)));
            m.colHora = Integer.parseInt(getProp(prefijo + ".columna.hora", String.valueOf(m.colHora)));
            m.colUbicacion = Integer.parseInt(getProp(prefijo + ".columna.ubicacion", String.valueOf(m.colUbicacion)));
            m.colTipoMovimiento = Integer.parseInt(getProp(prefijo + ".columna.tipoMovimiento", String.valueOf(m.colTipoMovimiento)));
            m.colUserId = Integer.parseInt(getProp(prefijo + ".columna.userId", String.valueOf(m.colUserId)));
        } catch (Exception e) {
            // En caso de error se usan los valores por defecto
        }
        return m;
    }

    /**
     * Metodo auxiliar para obtener una propiedad con valor por defecto.
     */
    private static String getProp(String clave, String porDefecto) {
        return ConfiguracionesPreferencias.getInstance().getProperties().getProperty(clave, porDefecto);
    }
}
