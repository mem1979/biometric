package com.sta.biometric.auxiliares;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.enums.*;

import lombok.*;

@Entity
@Getter @Setter
public class Nacionalidades extends Identifiable {
	
	@Required
	@Enumerated(EnumType.STRING)
	private Continentes continente;

	@Required
    @Column(length = 60)
    private String paises;

    @Required
    @Column(length = 50)
    private String nacionalidad;
}
