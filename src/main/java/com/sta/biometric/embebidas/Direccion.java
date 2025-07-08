package com.sta.biometric.embebidas;

import javax.persistence.*;

import org.openxava.annotations.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.auxiliares.*;

import lombok.*;

@View(members=
"provincia, partido, localidad;" +
"calle, numero, piso ,codigoPostal" 
)

@View(name= "VerMapa", 
members="direccionFormateada;" +
		 "ubicacion")

@Embeddable
@Getter @Setter
public class Direccion {
	
			
	@Capitalizar
	@DisplaySize(50)
    private String calle;

	@DisplaySize(10)
    private String numero;

	@DisplaySize(10)
    private String piso;

	@DisplaySize(10)
	@Action("Coordenadas.OptenerCP")
	@Action("Coordenadas.verDireccion")
	private String codigoPostal;

    	
	    @ManyToOne(fetch = FetchType.LAZY)
	    @DescriptionsList(
	    		order="${nombre} asc",
	    		descriptionProperties = "nombre")
	    @NoCreate @NoModify 
	    private Provincias provincia;
    		
    	
	    @ManyToOne(fetch = FetchType.LAZY)
	    @DescriptionsList(
		    	order="${nombre} asc",
		        descriptionProperties = "nombre",
		        depends = "this.provincia",
		        condition = "${provincia.numero} = ?"
	    )
	    @NoCreate @NoModify 
	    private Partidos partido;

    	
	    @ManyToOne(fetch = FetchType.LAZY)
	    @DescriptionsList(
	    		order="${nombre} asc",
		        descriptionProperties = "nombre",
		        depends = "this.partido",
		        condition = "${partido.numero} = ?"
	    )
	    @NoModify @NoCreate
	    private Localidades localidad;
	    

	    @Coordinates
	    @Column(length=50)
	    private String ubicacion;
	    
	    /**
	     * Propiedad calculada que devuelve la dirección formateada.
	     * Ejemplo: "General Bosch 1864, Piso/Dpto 2° A, Banfield, C.P. (1864) - Lomas de Zamora - Buenos Aires, Argentina"
	     */
	    @Label
	    @LabelFormat(LabelFormatType.NO_LABEL)
	    @Depends("calle, numero, piso, departamento, localidad, codigoPostal, partido, provincia")
	    public String getDireccionFormateada() {
	        StringBuilder direccion = new StringBuilder();

	        // Agregar calle y número
	        if (calle != null && !calle.isEmpty()) {
	            direccion.append(calle);
	            if (numero != null && !numero.isEmpty()) {
	                direccion.append(" ").append(numero);
	            }
	        }

	        // Agregar piso si existe
	        if (piso != null && !piso.isEmpty()) {
	            direccion.append(", Piso/Dptp ").append(piso);
	        }

	        // Agregar localidad
	        if (localidad != null && localidad.getNombre() != null) {
	            direccion.append(", ").append(localidad.getNombre());
	        }

	        // Agregar código postal
	        if (codigoPostal != null && !codigoPostal.isEmpty()) {
	            direccion.append(", C.P. (").append(codigoPostal).append(")");
	        }

	        // Agregar partido
	        if (partido != null && partido.getNombre() != null) {
	            direccion.append(" - ").append(partido.getNombre());
	        }

	        // Agregar provincia y país
	        if (provincia != null && provincia.getNombre() != null) {
	            direccion.append(" - ").append(provincia.getNombre());
	        }
	        direccion.append(", Argentina");

	        return direccion.toString();
	    }

	
}
