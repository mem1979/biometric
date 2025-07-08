package com.sta.biometric.servicios;
import java.util.*;
import java.util.stream.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

/**
 * Servicio encargado de normalizar textos hacia tipos de movimiento validos.
 * No realiza evaluacion de contexto horario ni estado de jornada.
 */

public class InterpreteFichadasService {

    private static final Map<TipoMovimiento, List<String>> equivalencias = new EnumMap<>(TipoMovimiento.class);

    static {
	        Properties props = ConfiguracionesPreferencias.getInstance().getProperties();
	        equivalencias.put(TipoMovimiento.ENTRADA, cargarListaDesdeProp(props, "tipos.entrada"));
	        equivalencias.put(TipoMovimiento.SALIDA, cargarListaDesdeProp(props, "tipos.salida"));
	        equivalencias.put(TipoMovimiento.PAUSA_INICIO, cargarListaDesdeProp(props, "tipos.pausa_inicio"));
	        equivalencias.put(TipoMovimiento.PAUSA_FIN, cargarListaDesdeProp(props, "tipos.pausa_fin"));
	        equivalencias.put(TipoMovimiento.UBICACION, cargarListaDesdeProp(props, "tipos.ubicacion"));
	        equivalencias.put(TipoMovimiento.MANUAL, cargarListaDesdeProp(props, "tipos.manual"));
    		}

    private static List<String> cargarListaDesdeProp(Properties props, String clave) {
        String raw = props.getProperty(clave, "");
        return Arrays.stream(raw.split(","))
                     .map(String::trim)
                     .map(String::toUpperCase)
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toList());
    }

    /**
     * Intenta deducir el tipo de movimiento a partir del texto recibido.
     * Retorna null si no encuentra coincidencias.
     */
    
    public static TipoMovimiento deducirTipoMovimiento(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String upper = texto.trim().toUpperCase();

        for (Map.Entry<TipoMovimiento, List<String>> entry : equivalencias.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (upper.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    
    /**
     * Aplica deduccion de tipo a una lista de ColeccionRegistros.
     * No modifica registros ya tipados.
     */
    public static List<ColeccionRegistros> normalizar(List<ColeccionRegistros> crudos) {
        for (ColeccionRegistros r : crudos) {
            if (r.getTipoMovimiento() == null && r.getObservacion() != null) {
                String fuente = r.getObservacion();
                TipoMovimiento tipo = deducirTipoMovimiento(fuente);

                if (tipo != null) {
                    r.setTipoMovimiento(tipo);
                } else {
                    r.setObservacion("Tipo no reconocido: " + fuente);
                }
            }
        }
        return crudos;
    }
}
