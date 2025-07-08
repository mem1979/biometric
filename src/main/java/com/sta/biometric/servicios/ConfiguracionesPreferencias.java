package com.sta.biometric.servicios;

import java.io.*;
import java.util.*;

public class ConfiguracionesPreferencias {

    private static final String ARCHIVO = "biometricConfiguracion.properties";

    private static final ConfiguracionesPreferencias instancia = new ConfiguracionesPreferencias();
    private final Properties props;

    private ConfiguracionesPreferencias() {
        props = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(ARCHIVO)) {
            if (is != null) {
                props.load(is);
            } else {
                System.err.println("No se encontró el archivo de configuración: " + ARCHIVO);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error cargando configuración desde: " + ARCHIVO, ex);
        }
    }

    public static ConfiguracionesPreferencias getInstance() {
        return instancia;
    }

    public Properties getProperties() {
        return props;
    }

    /**
     * Obtiene el valor de una propiedad con conversión segura y tipo.
     */
    
    @SuppressWarnings("unchecked")
    public static <T> T obtenerValor(String clave, T valorPorDefecto, Class<T> tipo) {
        String valor = getInstance().getProperties().getProperty(clave);

        if (valor == null || valor.trim().isEmpty()) return valorPorDefecto;

        try {
            if (tipo == String.class) return (T) valor;
            if (tipo == Integer.class) return (T) Integer.valueOf(valor);
            if (tipo == Boolean.class) return (T) Boolean.valueOf(valor);
            if (tipo.isEnum()) {
                for (Object constant : tipo.getEnumConstants()) {
                    if (((Enum<?>) constant).name().equalsIgnoreCase(valor)) {
                        return (T) constant;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error convirtiendo '" + valor + "' a " + tipo.getSimpleName() + ": " + ex.getMessage());
        }

        return valorPorDefecto;
    }

    /**
     * Versión clásica solo para String.
     */
    public static String obtenerValor(String clave) {
        return getInstance().getProperties().getProperty(clave);
    }
}
