<%@include file="../xava/imports.jsp" %>
    <!DOCTYPE html>
    <html lang="es">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Bienvenido a STA.RH</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/xava/style/custom.css">
    </head>

    <body class="welcome-body">

        <!-- Panel Izquierdo - Branding -->
        <div class="welcome-left">

            <!-- Logo -->
            <div class="welcome-logo-box">
                <img src="<%=request.getContextPath()%>/naviox/images/starh-logo.jpg" width="110" height="110"
                    alt="STA.RH Logo">
            </div>

            <!-- Titulo -->
            <h1 class="welcome-title">STA.RH</h1>

            <!-- Subtitulo -->
            <p class="welcome-subtitle">Soluciones Tecnologicas de Avanzada</p>

            <!-- Slogan -->
            <p class="welcome-slogan">
                <strong>Tome el control hoy.</strong><br>
                Audite el pasado, gestione el presente<br>
                y planifique el futuro.
            </p>
        </div>

        <!-- Panel Derecho - Login -->
        <div class="welcome-right">

            <h2 class="welcome-heading">Bienvenido</h2>
            <p class="welcome-desc">Sistema de Gestion de Recursos Humanos</p>

            <button id="welcome_go_signin" class="welcome-btn">
                Iniciar Sesion
            </button>

            <p class="welcome-footer">
                Desarrollado por <strong>S.T.A.</strong>
            </p>
        </div>

        <script type="text/javascript" <xava:nonce />>
        document.getElementById('welcome_go_signin').onclick = function() {
        window.location = 'm/SignIn';
        };
        </script>
    </body>

    </html>