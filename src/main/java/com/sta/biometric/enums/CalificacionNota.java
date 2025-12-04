package com.sta.biometric.enums;

/**
 * Enum para calificar las notas de desempeño de un empleado.
 * Permite categorizar cada observación y calcular un promedio de desempeño.
 */
public enum CalificacionNota {
    BUENA("Positiva", 3),
    NORMAL("Neutra", 2),
    MALA("Negativa", 1);

    private final String descripcion;
    private final int peso;

    CalificacionNota(String descripcion, int peso) {
        this.descripcion = descripcion;
        this.peso = peso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getPeso() {
        return peso;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
