package com.sta.biometric.anotaciones;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface MiLabel {
	
	String medida() default "mediana";     // chica | mediana | grande | extra
	
    boolean recuadro() default true;        // borde alrededor o no
    
    boolean negrita() default false;        // texto en bold
    
    /**
     * Icono a mostrar junto al campo. Debe ser el nombre del ícono sin el prefijo 'mdi-'.
     * Por ejemplo: "account", "map-marker", "calendar"
     */
    String icon() default "";              // Ej: "account"
    
    /**
     * Lista de nombres de vistas separados por comas donde se aplica esta anotación. <p>
     * 
     * Exclusivo con notForViews.<br>
     * Si se omiten tanto forViews como notForViews, entonces esta anotación se aplica 
     * a todas las vistas.<br>
     * Puedes usar la cadena "DEFAULT" para hacer referencia a la vista 
     * predeterminada (la vista sin nombre).
     */	
    String forViews() default "";
	
    /**
     * Lista de nombres de vistas separados por comas donde esta anotación no se aplica. <p>
     * 
     * Exclusivo con forViews.<br>
     * Si se omiten tanto forViews como notForViews, entonces esta anotación se aplica 
     * a todas las vistas.<br>
     * Puedes usar la cadena "DEFAULT" para hacer referencia a la vista 
     * predeterminada (la vista sin nombre).
     */ 	
    String notForViews() default "";
}