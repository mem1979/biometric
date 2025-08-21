package com.sta.biometric.auxiliares;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.enums.*;

import lombok.*;

@View(members=
"documento;" +
"FOTOS [" +
"dniFrente, dniDorso" +
 "]" 
)

@View(name = "simple",
members="tipo, numero")

@Entity
@Getter @Setter
public class Dni extends Identifiable {
	

@Required
@ReadOnly(onCreate=false)
@Enumerated(EnumType.STRING)
private DocumentoIdentidadTipo tipo;


@Required
@Mask(" 00.000.000  ")
@ReadOnly(onCreate=false)
private String numero; // Documento Nacional de Identidad

@Label
@ReadOnly
@Depends("tipo, numero") // Se recalcula cuando cambia tipo o número
@MiLabel(medida = "grande", negrita = true, recuadro = false, icon = "account-box")
public String getDocumento() {
    return  tipo + "  N°: " + numero;
}


@Label
@LabelFormat(LabelFormatType.SMALL)
@File(acceptFileTypes="image/*", maxFileSizeInKb=200)
@Column(length=32)
private String dniFrente; // Foto del Fente DNI

@Label
@LabelFormat(LabelFormatType.SMALL)
@File(acceptFileTypes="image/*", maxFileSizeInKb=200)
@Column(length=32)
private String dniDorso; // Foto del Dorso DNI

}