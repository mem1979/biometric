/*
 * Generated by the Jasper component of Apache Tomcat
 * Version: Apache Tomcat/9.0.106
 * Generated at: 2025-07-06 15:42:29 UTC
 * Note: The last modified time of this file was set to
 *       the last modified time of the source file after
 *       generation to assist with modification tracking.
 */
package org.apache.jsp.xava.editors;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.openxava.model.meta.MetaProperty;
import org.openxava.model.meta.MetaProperty;

public final class validValuesEditor_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent,
                 org.apache.jasper.runtime.JspSourceImports {

  private static final javax.servlet.jsp.JspFactory _jspxFactory =
          javax.servlet.jsp.JspFactory.getDefaultFactory();

  private static java.util.Map<java.lang.String,java.lang.Long> _jspx_dependants;

  static {
    _jspx_dependants = new java.util.HashMap<java.lang.String,java.lang.Long>(2);
    _jspx_dependants.put("/xava/editors/validValueEditorCommon.jsp", Long.valueOf(1750456748000L));
    _jspx_dependants.put("/xava/editors/descriptionValidValuesEditor.jsp", Long.valueOf(1750456748000L));
  }

  private static final java.util.Set<java.lang.String> _jspx_imports_packages;

  private static final java.util.Set<java.lang.String> _jspx_imports_classes;

  static {
    _jspx_imports_packages = new java.util.LinkedHashSet<>(4);
    _jspx_imports_packages.add("javax.servlet");
    _jspx_imports_packages.add("javax.servlet.http");
    _jspx_imports_packages.add("javax.servlet.jsp");
    _jspx_imports_classes = new java.util.LinkedHashSet<>(2);
    _jspx_imports_classes.add("org.openxava.model.meta.MetaProperty");
  }

  private volatile javax.el.ExpressionFactory _el_expressionfactory;
  private volatile org.apache.tomcat.InstanceManager _jsp_instancemanager;

  public java.util.Map<java.lang.String,java.lang.Long> getDependants() {
    return _jspx_dependants;
  }

  public java.util.Set<java.lang.String> getPackageImports() {
    return _jspx_imports_packages;
  }

  public java.util.Set<java.lang.String> getClassImports() {
    return _jspx_imports_classes;
  }

  public javax.el.ExpressionFactory _jsp_getExpressionFactory() {
    if (_el_expressionfactory == null) {
      synchronized (this) {
        if (_el_expressionfactory == null) {
          _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
        }
      }
    }
    return _el_expressionfactory;
  }

  public org.apache.tomcat.InstanceManager _jsp_getInstanceManager() {
    if (_jsp_instancemanager == null) {
      synchronized (this) {
        if (_jsp_instancemanager == null) {
          _jsp_instancemanager = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(getServletConfig());
        }
      }
    }
    return _jsp_instancemanager;
  }

  public void _jspInit() {
  }

  public void _jspDestroy() {
  }

  public void _jspService(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)
      throws java.io.IOException, javax.servlet.ServletException {

    if (!javax.servlet.DispatcherType.ERROR.equals(request.getDispatcherType())) {
      final java.lang.String _jspx_method = request.getMethod();
      if ("OPTIONS".equals(_jspx_method)) {
        response.setHeader("Allow","GET, HEAD, POST, OPTIONS");
        return;
      }
      if (!"GET".equals(_jspx_method) && !"POST".equals(_jspx_method) && !"HEAD".equals(_jspx_method)) {
        response.setHeader("Allow","GET, HEAD, POST, OPTIONS");
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "JSPs only permit GET, POST or HEAD. Jasper also permits OPTIONS");
        return;
      }
    }

    final javax.servlet.jsp.PageContext pageContext;
    javax.servlet.http.HttpSession session = null;
    final javax.servlet.ServletContext application;
    final javax.servlet.ServletConfig config;
    javax.servlet.jsp.JspWriter out = null;
    final java.lang.Object page = this;
    javax.servlet.jsp.JspWriter _jspx_out = null;
    javax.servlet.jsp.PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write('\r');
      out.write('\n');

String propertyKey = request.getParameter("propertyKey");
MetaProperty p = (MetaProperty) request.getAttribute(propertyKey);
Object description = null; 
int baseIndex = 1; 
int value = 0; 
Object ovalue = request.getAttribute(propertyKey + ".value");
MetaProperty validValuesProperty = (MetaProperty) request.getAttribute(propertyKey + ".validValuesProperty"); 
if (validValuesProperty == null) validValuesProperty = p;
if (validValuesProperty.hasValidValues()) {  	
	if (p.isNumber()) {
		value = ovalue==null?0:((Integer) ovalue).intValue();	
	}
	else {
		// We assume that if it isn't Number then it's an Enum of Java 5, we use instropection
		// to allow this code run in a Java 1.4 servlet container.
		baseIndex = 0;
		if (ovalue == null) {
			value = -1;	
		}
		else if (ovalue instanceof Number) { // Directly the ordinal
			value = ((Number) ovalue).intValue();
		}
		else { // An object of enum type
			value = ((Integer) org.openxava.util.XObjects.execute(ovalue, "ordinal")).intValue();
		}
	}
	int labelIndex = p != validValuesProperty && validValuesProperty.getMetaModel().isAnnotatedEJB3()?value - 1:value; 
	description = labelIndex == -1?"":validValuesProperty.getValidValueLabel(labelIndex);
}

      out.write(" \r\n");
      out.write("\r\n");
 
boolean editable = "true".equals(request.getParameter("editable")); 
boolean label = org.openxava.util.XavaPreferences.getInstance().isReadOnlyAsLabel();
boolean required = p.isRequired();

      out.write('\r');
      out.write('\n');
      out.write("\r\n");
      out.write("\r\n");
      org.openxava.web.style.Style style = null;
      style = (org.openxava.web.style.Style) _jspx_page_context.getAttribute("style", javax.servlet.jsp.PageContext.REQUEST_SCOPE);
      if (style == null){
        style = new org.openxava.web.style.Style();
        _jspx_page_context.setAttribute("style", style, javax.servlet.jsp.PageContext.REQUEST_SCOPE);
      }
      out.write('\r');
      out.write('\n');

if (editable) { 

      out.write("\r\n");
      out.write("<select id=\"");
      out.print(propertyKey);
      out.write("\" name=\"");
      out.print(propertyKey);
      out.write("\" tabindex=\"1\" class=");
      out.print(style.getEditor());
      out.write(" title=\"");
      out.print(p.getDescription(request));
      out.write("\">\r\n");
 
	boolean isInElementCollection = request.getParameter("collectionName") != null;
	if (isInElementCollection || !required) {

      out.write("\r\n");
      out.write("	    <option value=\"");
      out.print(baseIndex==0?"":"0");
      out.write("\"></option>\r\n");

    }
    java.util.Iterator it = validValuesProperty.validValuesLabels();
	for (int i = baseIndex; it.hasNext(); i++) {
		String selected = value == i ?"selected":"";

      out.write("\r\n");
      out.write("	<option value=\"");
      out.print(i);
      out.write('"');
      out.write(' ');
      out.print(selected);
      out.write('>');
      out.print(it.next());
      out.write("</option>\r\n");

	} // while

      out.write("\r\n");
      out.write("</select>	\r\n");
      out.write("<input type=\"hidden\" name=\"");
      out.print(propertyKey);
      out.write("__DESCRIPTION__\" value=\"");
      out.print(description);
      out.write("\"/>\r\n");
 
} else { 
	if (label) {

      out.write('\r');
      out.write('\n');
      out.write('	');
      out.print(description);
      out.write('\r');
      out.write('\n');

	}
	else {

      out.write("\r\n");
      out.write("	<input name = \"");
      out.print(propertyKey);
      out.write("_DESCRIPTION_\" class=");
      out.print(style.getEditor());
      out.write("\r\n");
      out.write("	type=\"text\" \r\n");
      out.write("	title=\"");
      out.print(p.getDescription(request));
      out.write("\"	\r\n");
      out.write("	maxlength=\"");
      out.print(p.getSize());
      out.write("\" 	\r\n");
      out.write("	size=\"");
      out.print(description.toString().length() + 1);
      out.write("\" \r\n");
      out.write("	value=\"");
      out.print(description);
      out.write("\"\r\n");
      out.write("	disabled\r\n");
      out.write("	/>\r\n");
  } 
      out.write("\r\n");
      out.write("	<input type=\"hidden\" name=\"");
      out.print(propertyKey);
      out.write("\" value=\"");
      out.print(value==-1?"":String.valueOf(value));
      out.write("\">	\r\n");
 } 
      out.write("			\r\n");
    } catch (java.lang.Throwable t) {
      if (!(t instanceof javax.servlet.jsp.SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try {
            if (response.isCommitted()) {
              out.flush();
            } else {
              out.clearBuffer();
            }
          } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
