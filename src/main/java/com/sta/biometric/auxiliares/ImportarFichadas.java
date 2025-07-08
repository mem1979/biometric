package com.sta.biometric.auxiliares;
import org.apache.commons.fileupload.*;
import org.openxava.annotations.*;

import lombok.*;



@Getter @Setter
public class ImportarFichadas {

//	@Transient // Indica a JPA que esta propiedad no se persiste en la base de datos
	@FileItemUpload(acceptFileTypes = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", maxFileSizeInKb = 500)
	private FileItem fichero;
	
}
