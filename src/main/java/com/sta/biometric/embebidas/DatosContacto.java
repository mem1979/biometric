package com.sta.biometric.embebidas;
import javax.persistence.*;
import javax.validation.constraints.*;

import org.hibernate.validator.constraints.*;
import org.openxava.annotations.*;

import com.sta.biometric.calculadores.*;

import lombok.*;

@View(members=
"celular, telefono;" +
"email;" +
"web"		
)

@Embeddable
@Getter @Setter
public class DatosContacto {
	
	@Mask("(####)##############")
    @Column(length = 22)
    @DefaultValueCalculator(
            value = CalculadorCodigoAreaDefault.class,
            properties = @PropertyValue(name = "tipoTelefono", value = "CELULAR")
        )
    private String celular;

    @Mask("(###)########")
    @Column(length = 22)
    @DefaultValueCalculator(
            value = CalculadorCodigoAreaDefault.class,
            properties = @PropertyValue(name = "tipoTelefono", value = "TELEFONO")
        )
    private String telefono;

    @Stereotype("EMAIL")
    @Column(length = 50)
    @Size(max = 50, message = "El valor no puede exceder los 50 caracteres")
    private String email;
    
    @URL
    @Column(length = 100)
    @Size(max = 100, message = "El valor no puede exceder los 100 caracteres")
    private String web;

}
