<%@include file="../xava/imports.jsp" %>
    <% String app=request.getParameter("application"); %>

        <div id="starh_branding">
            <div id="starh_logo"></div>
            <div id="starh_title">STA.RH</div>
            <div id="starh_subtitle">SOLUCIONES TECNOLOGICAS DE AVANZADA</div>
        </div>

        <div id="sign_in_box">
            <jsp:include page='<%="../xava/module.jsp?application=" + app + "&module=SignIn"%>' />
        </div>

        <div id="starh_footer">
            <p>Sistema de Gestion de Recursos Humanos</p>
            <p>Desarrollado por <strong>S.T.A.</strong></p>
        </div>