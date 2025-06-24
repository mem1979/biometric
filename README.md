Biometric – Sistema de Fichaje de Empleados con OpenXava 7.5 / Java 17
Proyecto web que registra y consolida la asistencia diaria de los empleados mediante fichaje biométrico (huella digital / reconocimiento facial), reglas de turnos, licencias y feriados, métricas en tiempo real y jobs programados de apertura / cierre de jornada.

📑 Contenido del README
Características principales

Tecnologías y dependencias

Estructura del proyecto

Configuración y variables clave

Instrucciones de puesta en marcha

Endpoints REST

Jobs Quartz

Ejecutar pruebas

Prompt preciso de la aplicación

Características principales
Registro biométrico: autenticación con el Android Biometric API o dispositivos USB / lector de huella.

Consolidación inteligente de jornadas:

Reglas por turno, feriado, licencia o ausencia.

Cálculo de minutos trabajados, llegadas tarde, salidas anticipadas y horas extra.

Dashboard interactivo: métricas diarias (empleados que deberían trabajar, presentes, pendientes de ingreso, etc.) con actualizaciones dinámicas.

Tareas programadas:

Apertura de jornada (crea registros preliminares) – 00:00.

Cierre de jornada (consolida ausencias) – 23:59.

Seguridad: tokens JWT, validación de dispositivo y caducidad configurable.

API REST para fichar, consultar asistencia y autenticarse.

I18N y propiedades centralizadas en biometricConfiguracion.properties.

Tecnologías y dependencias
Tecnología	Versión	Uso
Java	17	Lenguaje principal
OpenXava	7.5.2	Framework MVC-JPA
Spring / JPA (incluido en OpenXava)	–	Persistencia
Quartz	2.3.2	Jobs programados
Jersey	2.35	Endpoints REST
JJWT	0.9.1	Tokens JWT
H2 integrado (modo dev)	–	Base de datos por defecto

Estructura del proyecto
less
Copiar
Editar
biometric-master/
├── pom.xml
├── prompt.txt
└── src/
    ├── main/
    │   ├── java/com/sta/biometric/
    │   │   ├── acciones/            ← Acciones de UI OpenXava
    │   │   ├── anotaciones/         ← Anotaciones personalizadas (@Colorido, etc.)
    │   │   ├── auxiliares/          ← DTOs y wrappers reutilizables
    │   │   ├── calculadores/        ← @DefaultValueCalculator, etc.
    │   │   ├── dashboard/           ← DashboardAsistencia + sub-paquetes
    │   │   ├── embebidas/           ← Tipos embebidos JPA (Dirección, JornadaAsignada…)
    │   │   ├── enums/               ← TipoMovimiento, EstadoJornada…
    │   │   ├── formateadores/       ← Formatters propios de OpenXava
    │   │   ├── modelo/              ← Entidades JPA (Personal, AuditoriaRegistros…)
    │   │   ├── qartzJobs/           ← AperturaJornadaJob, CierreJornadaJob…
    │   │   ├── rest/                ← AuthEndpoint, AsistenciaEndpoint
    │   │   ├── run/biometric.java   ← Main (arranca DBServer + AppServer)
    │   │   ├── servicios/           ← Lógica de negocio (AsistenciaDiariaService, etc.)
    │   │   └── util/                ← JWTUtil, TiempoUtils…
    │   ├── resources/
    │   │   ├── biometricConfiguracion.properties
    │   │   ├── xava/{aplicacion.xml, editores.xml,…}
    │   │   └── i18n/*.properties
    │   └── webapp/
    │       └── META-INF/context.xml (configuraciones BD externas)
    └── test/
        └── java/… (JUnit, pendiente de completarse)
Configuración y variables clave
Archivo	Propósito
biometricConfiguracion.properties	Parámetros globales: tolerancias de llegada tarde, tiempos de pausa, duración token JWT, reglas por tipo de licencia, etc.
xava/aplicacion.xml	Configura módulos, controladores y seguridad de OpenXava.
META-INF/context.xml	Cambiar a tu propio RDBMS (PostgreSQL, MySQL, SQL Server…).
naviox-users.properties	Usuarios iniciales para login en modo desarrollo.

Instrucciones de puesta en marcha
Pre-requisitos

JDK 17

Maven 3.9+

Clonar y compilar

bash
Copiar
Editar
git clone https://…/biometric.git
cd biometric
mvn clean verify
Arrancar en modo desarrollo (DB H2 embebida):

bash
Copiar
Editar
mvn exec:java -Dexec.mainClass="com.sta.biometric.run.biometric"
# o desde IDE → Run > Java Application
La app quedará disponible en http://localhost:8080/biometric.

BD externa

Comentar DBServer.start("biometric-db") en biometric.java.

Configurar <Resource …> en src/main/webapp/META-INF/context.xml.

Credenciales por defecto

Usuario: admin / Contraseña: admin (ver naviox-users.properties).

Endpoints REST
Método	Ruta	Descripción
POST /api/auth/login	Autenticación, devuelve JWT	
POST /api/asistencia/fichar	Recibe registro biométrico (entrada, salida, pausa…)	
GET /api/asistencia/hoy	Resumen de asistencia del día actual	

Todos los endpoints exigen cabecera Authorization: Bearer <token>.

Jobs Quartz
Job	Hora por defecto	Descripción
AperturaJornadaJob	00:00	Crea registros preliminares según turnos/licencias
CierreJornadaJob	23:59	Consolida el día, asigna ausencias e incidencias finales

Horarios configurables en biometricConfiguracion.properties.

Ejecutar pruebas
bash
Copiar
Editar
mvn -Ptest clean test
Las pruebas (JUnit 5) se ubicarán en src/test/java. Actualmente el proyecto trae plantillas básicas; se recomienda cubrir casos de feriados, licencias y fichadas fuera de turno.

Prompt preciso de la Aplicación
“Desarrollá un sistema web de control de asistencia en Java 17 con OpenXava 7.5 llamado Biometric.
Debe permitir a los empleados fichar con huella digital o reconocimiento facial desde Android y crear registros ENTRADA, SALIDA, PAUSA_INICIO y PAUSA_FIN.
La aplicación consolidará cada jornada aplicando reglas de turnos, licencias y feriados definidas en biometricConfiguracion.properties; al finalizar el día ejecutará un job Quartz que cierre la jornada y asigne incidencias (llegada tarde, ausencia, feriado trabajado).
Exponé un API REST segura con JWT para fichar y consultar el resumen diario; mostrale a RRHH un dashboard en tiempo real con métricas de presentes, pendientes y ausentes.”

Usá este prompt como punto de partida para documentar la idea, generar diagramas o pedir mejoras a un LLM.

¡Listo! Este README te servirá como guía completa para entender, montar y extender el proyecto de fichaje biométrico.







