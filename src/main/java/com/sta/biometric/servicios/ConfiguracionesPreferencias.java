package com.sta.biometric.servicios;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.*;
import org.openxava.util.*;

public class ConfiguracionesPreferencias {

    private static final String FILE_PROPERTIES = "biometricConfiguracion.properties";
    private static Log log = LogFactory.getLog(ConfiguracionesPreferencias.class);
    private static ConfiguracionesPreferencias instance;
    private Properties properties;

    private ConfiguracionesPreferencias() { }

    public static ConfiguracionesPreferencias getInstance() {
        if (instance == null) {
            instance = new ConfiguracionesPreferencias();
        }
        return instance;
    }

    public Properties getProperties() {
        if (properties == null) {
            PropertiesReader reader = new PropertiesReader(ConfiguracionesPreferencias.class, FILE_PROPERTIES);
            try {
                properties = reader.get();
            } catch (IOException ex) {
                log.error(XavaResources.getString("properties_file_error", FILE_PROPERTIES), ex);
                properties = new Properties();
            }
        }
        return properties;
    }

    // ========= Métodos utilitarios para acceso seguro ==========

    public int getInt(String clave, int porDefecto) {
        try {
            return Integer.parseInt(getProperties().getProperty(clave, String.valueOf(porDefecto)));
        } catch (Exception e) {
            return porDefecto;
        }
    }

    public boolean getBoolean(String clave, boolean porDefecto) {
        return Boolean.parseBoolean(getProperties().getProperty(clave, String.valueOf(porDefecto)));
    }
}

