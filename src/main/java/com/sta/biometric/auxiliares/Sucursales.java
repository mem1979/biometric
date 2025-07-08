package com.sta.biometric.auxiliares;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.embebidas.*;
import com.sta.biometric.servicios.*;

import lombok.*;

@View(name= "VerMapa",
members = "direccion"
)


@Entity
@Getter @Setter
public class Sucursales extends Identifiable {
	
	 	@Mayuscula
	    @Required
	    @DisplaySize(50)
	 	@Size(max = 50, message = "El Nombre de la Sucursal no puede exceder los 50 caracteres.")
	    @Column(length = 50)
	    private String nombre; // nombre sucursal
	 	
	 // Direccion Sucursal
	 	@NoFrame
	 	@Embedded
	    @ReferenceView(forViews="VerMapa", value="VerMapa")
	    Direccion direccion;
	    
	   

@PrePersist
private void antesDeGuardar() {
    try {
	    	AsignarCoordenadasService.asignarCoordenadasSiFaltan(this.direccion);
	    	} catch (Exception e) {
	        e.printStackTrace(); 
    	}
   	}
}