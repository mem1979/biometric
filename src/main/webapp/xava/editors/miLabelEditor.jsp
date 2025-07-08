<%@ include file="../imports.jsp" %>
<%
    String medida = request.getParameter("medida");
    String claseMedida = "mi-label-medida-" + medida;

    boolean recuadro = "true".equals(request.getParameter("recuadro"));
    boolean negrita = "true".equals(request.getParameter("negrita"));

    String icon = request.getParameter("icon");

    String clases = claseMedida;
    if (negrita) clases += " mi-label-negrita";
    if (!recuadro) clases += " mi-label-sin-recuadro";
    if (recuadro)  clases += " mi-label-con-recuadro";

%>

<span class="<%= clases %>" style="display: flex; align-items: center; gap: 0.3em;">
    <% if (icon != null && !icon.isEmpty()) { %>
        <i class="mdi mdi-<%= icon %>"></i>
    <% } %>
    <jsp:include page="textEditor.jsp"/>
</span>
