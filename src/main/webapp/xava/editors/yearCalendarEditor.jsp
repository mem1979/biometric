<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="/WEB-INF/openxava.tld"         prefix="xava" %>

<%@ page import="java.util.*" %>
<%@ page import="org.openxava.view.View" %>

<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>

<jsp:useBean id="context" class="org.openxava.controller.ModuleContext" scope="session"/>

<%
    /* 1. Parámetros que OpenXava pasa al editor */
    String collectionName = request.getParameter("collectionName");
    String viewObject     = request.getParameter("viewObject");

    /* 2. Acceso al subview y los datos */
    View  view    = (View) context.get(request, viewObject);
    View  subview = view.getSubview(collectionName);
    List<Map<String,Object>> rows = subview.getCollectionValues();

    /* 3. Normalizar LocalDate -> "yyyy-MM-dd"  */
    DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE;
    List<Map<String,Object>> normalized = new ArrayList<>();

    for (Map<String,Object> row : rows) {
        Map<String,Object> copy = new LinkedHashMap<>(row.size());
        for (Map.Entry<String,Object> e : row.entrySet()) {
            Object v = e.getValue();
            copy.put(e.getKey(),
                     v instanceof LocalDate ? ((LocalDate) v).format(iso) : v);
        }
        normalized.add(copy);
    }

    /* 4. Serializar  */
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(normalized);
%>

<div id="<xava:id name='yearCal'/>"
     class="ox-year-cal"
     data-items='<%=json%>'>
</div>
