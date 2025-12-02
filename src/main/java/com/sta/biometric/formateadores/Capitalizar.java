package com.sta.biometric.formateadores;

import javax.servlet.http.*;

import org.openxava.formatters.*;

public class Capitalizar implements IFormatter {

    @Override
    public String format(HttpServletRequest request, Object string) {
        if (string == null) return "";
        return capitalizar(string.toString());
    }

    @Override
    public Object parse(HttpServletRequest request, String string) {
        return string == null ? "" : capitalizar(string);
    }

    /**
     * Convierte a capitalizado (tipo "Hola Mundo").
     */
    private String capitalizar(String texto) {
        String[] palabras = texto.toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (palabra.length() > 0) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                         .append(palabra.substring(1))
                         .append(" ");
            }
        }
        return resultado.toString().trim();
    }
}
