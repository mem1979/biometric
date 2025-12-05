package com.sta.biometric.anotaciones;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Calendarizado {
	
	String forViews() default "";
	
	String notForViews() default "";
		
}