<%@ include file="../imports.jsp" %>
    <% String medida=request.getParameter("medida"); String claseMedida="mi-label-medida-" + medida; boolean
        recuadro="true" .equals(request.getParameter("recuadro")); boolean negrita="true"
        .equals(request.getParameter("negrita")); String icon=request.getParameter("icon"); boolean multiline="true"
        .equals(request.getParameter("multiline")); boolean mayuscula="true" .equals(request.getParameter("mayuscula"));
        String clases=claseMedida; if (negrita) clases +=" mi-label-negrita" ; if (!recuadro) clases
        +=" mi-label-sin-recuadro" ; if (recuadro) clases +=" mi-label-con-recuadro" ; if (mayuscula) clases
        +=" mi-label-mayuscula" ; String extraStyle=!mayuscula ? " text-transform: none !important;" : "" ; %>

        <span class="<%= clases %>" style="display: flex; align-items: center; gap: 0.3em;<%= extraStyle %>">
            <% if (icon !=null && !icon.isEmpty()) { %>
                <i class="mdi mdi-<%= icon %>"></i>
                <% } %>
                    <% if (multiline) { %>
                        <jsp:include page="textAreaEditor.jsp" />
                        <% } else { %>
                            <jsp:include page="textEditor.jsp" />
                            <% } %>
        </span>