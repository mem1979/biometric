package com.sta.biometric.modelo;

import java.time.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.model.*;

import com.sta.biometric.calculadores.*;
import com.sta.biometric.enums.*;

import lombok.*;

/**
 * Nota de desempeño para evaluar al personal.
 * Permite registrar observaciones con una calificación (Buena, Normal, Mala)
 * para posteriormente calcular métricas de desempeño.
 */

@View(members = "autor, fechaHora, calificacion;" +
        "contenido")

@Entity
@Getter
@Setter
public class NotaDesempeno extends Identifiable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    @ReferenceView("Simple")
    @ReadOnly
    private Personal empleado;

    @Required
    @HtmlText(simple = true)
    @Column(length = 2000, nullable = false)
    private String contenido;

    @Required
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @DefaultValueCalculator(value = EnumCalculator.class, properties = {
            @PropertyValue(name = "enumType", value = "com.sta.biometric.enums.CalificacionNota"),
            @PropertyValue(name = "value", value = "NORMAL")
    })
    private CalificacionNota calificacion;

    /**
     *
     */
    @ReadOnly
    @Column(nullable = false)
    @DefaultValueCalculator(CurrentLocalDateTimeCalculator.class)
    private LocalDateTime fechaHora;

    @ReadOnly
    @Column(length = 50)
    @DefaultValueCalculator(CurrentUserCalculator.class)
    private String autor;

    // =================== MÉTODOS ESTÁTICOS DE CÁLCULO ===================

    /**
     * Calcula el promedio de calificaciones de una colección de notas.
     * 
     * @param notas Colección de notas de desempeño
     * @return Promedio de calificaciones (0.0 a 3.0), o 0.0 si no hay notas
     */
    public static double calcularPromedio(java.util.Collection<NotaDesempeno> notas) {
        if (notas == null || notas.isEmpty()) {
            return 0.0;
        }
        double suma = notas.stream()
                .mapToInt(n -> n.getCalificacion().getPeso())
                .sum();
        return suma / notas.size();
    }

    /**
     * Obtiene la evaluación textual basada en un promedio.
     * 
     * <p>
     * Criterios:
     * </p>
     * <ul>
     * <li>≥ 2.5: "Excelente"</li>
     * <li>≥ 2.0: "Bueno"</li>
     * <li>≥ 1.5: "Regular"</li>
     * <li>< 1.5: "Requiere Mejora"</li>
     * </ul>
     * 
     * @param promedio Promedio de calificaciones
     * @return Evaluación textual
     */
    public static String calcularEvaluacion(double promedio) {
        if (promedio >= 2.5)
            return "Excelente";
        if (promedio >= 2.0)
            return "Bueno";
        if (promedio >= 1.5)
            return "Regular";
        return "Requiere Mejora";
    }

    /**
     * Conveniencia: calcula la evaluación directamente de una colección de notas.
     * 
     * @param notas Colección de notas de desempeño
     * @return Evaluación textual basada en el promedio
     */
    public static String calcularEvaluacion(java.util.Collection<NotaDesempeno> notas) {
        return calcularEvaluacion(calcularPromedio(notas));
    }
}
