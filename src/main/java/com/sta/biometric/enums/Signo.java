package com.sta.biometric.enums;

/**
 * Signo para ajustes de tiempo.
 * Usa emojis para mejor visualización en combos.
 */
public enum Signo {

    MAS("➕ Sumar"),
    MENOS("➖ Restar");

    private final String descripcion;

    Signo(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Retorna el multiplicador: +1 para MAS, -1 para MENOS.
     */
    public int getMultiplicador() {
        return this == MAS ? 1 : -1;
    }

    /**
     * Retorna true si es negativo.
     */
    public boolean esNegativo() {
        return this == MENOS;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
