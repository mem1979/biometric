package com.sta.biometric.formateadores;
import javax.servlet.http.*;

import org.apache.commons.lang.*;
import org.openxava.formatters.*;

public class Capitalizar implements IFormatter {

	   @Override
	    public String format(HttpServletRequest request, Object string) {
	        if (string == null) return "";
	        return WordUtils.capitalizeFully(string.toString());
	    }

	    @Override
	    public Object parse(HttpServletRequest request, String string) {
	        return string == null ? "" : WordUtils.capitalizeFully(string);
	    }
	}
