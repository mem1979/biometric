package com.sta.biometric.modelo;

import java.math.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.acciones.*;
import com.sta.biometric.anotaciones.*;
import com.sta.biometric.auxiliares.*;
import com.sta.biometric.calculadores.*;
import com.sta.biometric.dashboard.auxiliares.*;
import com.sta.biometric.embebidas.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.servicios.*;

import lombok.*;

/**
 * Entidad principal que representa un empleado en el sistema biométrico.
 * 
 * <p>
 * Esta clase centraliza toda la información relacionada con un empleado,
 * incluyendo:
 * </p>
 * <ul>
 * <li><b>Datos personales:</b> Nombre, DNI, CUIL, fecha de nacimiento,
 * dirección, contacto</li>
 * <li><b>Información laboral:</b> Sucursal, puesto, fecha de inicio,
 * antigüedad</li>
 * <li><b>Credenciales:</b> Usuario, contraseña, deviceId para autenticación
 * móvil</li>
 * <li><b>Jornadas laborales:</b> Turnos asignados, horarios, pausas</li>
 * <li><b>Honorarios:</b> Valor hora, bonificaciones por horas extras y
 * especiales</li>
 * <li><b>Licencias:</b> Historial de licencias y permisos</li>
 * <li><b>Desempeño:</b> Evaluaciones y notas de desempeño</li>
 * <li><b>Reportes:</b> Cálculos de horas trabajadas, asistencia, llegadas
 * tarde</li>
 * </ul>
 * 
 * <p>
 * <b>Responsabilidades principales:</b>
 * </p>
 * <ul>
 * <li>Almacenar y gestionar datos del empleado (entidad JPA)</li>
 * <li>Calcular métricas laborales (horas trabajadas, extras, especiales)</li>
 * <li>Gestionar turnos y jornadas asignadas</li>
 * <li>Generar reportes e informes de asistencia</li>
 * <li>Validar datos de entrada (DNI, CUIL, fechas)</li>
 * </ul>
 * 
 * <p>
 * <b>Relaciones con otras entidades:</b>
 * </p>
 * <ul>
 * <li>{@link Sucursales} - Sucursal donde trabaja el empleado</li>
 * <li>{@link TurnosHorarios} - Turnos asignados mediante
 * {@link JornadaAsignada}</li>
 * <li>{@link Licencia} - Licencias y permisos solicitados</li>
 * <li>{@link AuditoriaRegistros} - Registros de entrada/salida diarios</li>
 * <li>{@link NotaDesempeno} - Evaluaciones de desempeño</li>
 * </ul>
 * 
 * <p>
 * <b>Vistas OpenXava configuradas:</b>
 * </p>
 * <ul>
 * <li><b>Vista principal:</b> Información completa del empleado</li>
 * <li><b>VerMapa:</b> Visualización de dirección en mapa</li>
 * <li><b>VerCalendario:</b> Eventos y calendario del empleado</li>
 * <li><b>simple:</b> Vista resumida para selección rápida</li>
 * </ul>
 * 
 * <p>
 * <b>Nota importante:</b> Esta clase tiene múltiples responsabilidades y es
 * candidata
 * para refactorización futura, dividiendo la lógica de cálculo en servicios
 * separados.
 * </p>
 * 
 * @author Sistema STARH - Mosquera, Marcelo
 * @version 2.0
 * @since 1.0
 * @see TurnosHorarios
 * @see AuditoriaRegistros
 * @see Licencia
 * @see JornadaAsignada
 */

@Entity
@Getter
@Setter
@View(members = "nombreCompleto, turnoActivoHoy;" +
        "InformacionPersonal { " +
        "InformacionPersonal[" +
        "apellido;" +
        "nombres;" +
        "fechaNacimiento, edad, proximoCumpleanos;" +
        "nacionalidad, estadoCivil;" +
        "dni, Personal.dni(ALWAYS);" +
        "cuil, Personal.IrANSES(ALWAYS);" +

        "], " +
        "foto[" +
        "foto;" +
        "]; " +
        "direccion;" +
        "contacto;" +
        "notasPersonale, documentacionPersonal;" +
        "}; " +

        "InformacionLaboral { " +
        "credenciales[" +
        "userId, activo;" +
        "creaUsuario;" +
        "contrasena; deviceId;" +
        "], " +

        "funcion[" +
        "sucursal;" +
        "inicioActividades, antiguedadLaboral;" +
        "puesto; tipoContrato;" +
        "]; " +
        // "CONTRATOS[" +
        "contratos;" +
        // "], " +
        "JORNADAS[" +
        "aceptaPausa; jornadasAsignadas;" +
        "]; " +
        "}; " +

        "LICENCIAS { " +
        "licencias, licenciasResumenAnual; " +
        "licenciasGraficoAnual; " +
        "}; " +

        "LIQUIDACION_JORNADAS { " +
        "liquidaciones; " +
        "}; " +

        "INCIDENCIAS_Y_OBSERVACIONES { " +
        "Personal.informeAnual(); " +
        "notasDesempeno; " +
        "}")

@View(name = "VerMapa", members = "direccion")

@View(name = "VerCalendario", members = "eventos")

@View(name = "simple", members = "nombreCompleto, sucursal")

@View(name = "Crear", members = "InformacionPersonal { " +
        "InformacionPersonal[" +
        "apellido;" +
        "nombres;" +
        "fechaNacimiento, edad, proximoCumpleanos;" +
        "nacionalidad, estadoCivil;" +
        "dni, Personal.dni(ALWAYS);" +
        "cuil, Personal.IrANSES(ALWAYS);" +

        "], " +
        "foto[" +
        "foto;" +
        "]; " +
        "direccion;" +
        "contacto;" +
        "notasPersonale, documentacionPersonal;" +
        "}; " +

        "InformacionLaboral { " +
        "credenciales[" +
        "userId, activo;" +
        "creaUsuario;" +
        "contrasena; deviceId;" +
        "], " +

        "funcion[" +
        "sucursal;" +
        "inicioActividades, antiguedadLaboral;" +
        "puesto; tipoContrato;" +
        "]; " +
        // "CONTRATOS[" +
        "contratos;" +
        // "], " +
        "JORNADAS[" +
        "aceptaPausa; jornadasAsignadas;" +
        "]; " +
        "};")

// Tab por defecto: muestra solo registros NO eliminados (activos en el sistema)
@Tab(editors = "List", properties = "foto, nombreCompleto, userId, sucursal.nombre, puesto, activo", defaultOrder = "${activo} desc, ${nombreCompleto} asc", baseCondition = "${eliminado} = false", rowStyles = {
        @RowStyle(style = "empleadoInactivo", property = "activo", value = "false") })

// Tab para la Papelera: muestra solo registros ELIMINADOS (soft-delete)
@Tab(name = "Eliminado", editors = "List", properties = "foto, nombreCompleto, puesto, userId, sucursal.nombre, fechaEliminacion", defaultOrder = "${fechaEliminacion} desc", baseCondition = "${eliminado} = true", rowStyles = {
        @RowStyle(style = "empleadoEliminado", property = "eliminado", value = "true") })

public class Personal extends Identifiable {

    /**
     * Indica si el empleado está activo en el sistema.
     * 
     * <p>
     * Un empleado inactivo:
     * </p>
     * <ul>
     * <li>No puede registrar asistencia</li>
     * <li>No aparece en listados activos</li>
     * <li>Mantiene su historial para consultas</li>
     * </ul>
     * 
     * @see PersonalOnChangeActivoAction
     */
    @DefaultValueCalculator(TrueCalculator.class)
    @OnChange(PersonalOnChangeActivoAction.class)
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean activo;

    /**
     * Indica si el empleado está en la papelera (eliminación lógica).
     * 
     * <p>
     * Un empleado eliminado:
     * </p>
     * <ul>
     * <li>No aparece en el listado principal de nómina</li>
     * <li>No puede registrar asistencia</li>
     * <li>Puede ser restaurado desde la papelera</li>
     * <li>Mantiene todo su historial intacto</li>
     * </ul>
     * 
     * @see EliminarPersonalParaPapeleraAction
     */
    @DefaultValueCalculator(value = org.openxava.calculators.FalseCalculator.class)
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean eliminado;

    /**
     * Fecha y hora en que el empleado fue movido a la papelera.
     * 
     * <p>
     * Se establece automáticamente al eliminar y se limpia al restaurar.
     * </p>
     */
    @ReadOnly
    private java.time.LocalDateTime fechaEliminacion;

    @Hidden
    @Transient
    private String userIdOriginal;

    /**
     * Identificador único del empleado en el sistema.
     * 
     * <p>
     * Se genera automáticamente con formato basado en nombres.
     * Es la clave de búsqueda principal y debe ser único.
     * </p>
     * 
     * <p>
     * Si el empleado se desactiva, el userId recibe prefijo "x-"
     * </p>
     * 
     * @see GeneradorCodigoUserIdCalculator
     */
    @Required
    @SearchKey
    @ReadOnly
    @Column(length = 10, unique = true)
    @DefaultValueCalculator(GeneradorCodigoUserIdCalculator.class)
    @Action(value = "Personal.cambiarLegajo", alwaysEnabled = true, notForViews = "Crear")
    private String userId;

    /**
     * Identificador único del dispositivo móvil del empleado.
     * 
     * <p>
     * Se genera automáticamente al instalar la app móvil y se usa para:
     * </p>
     * <ul>
     * <li>Autenticación del dispositivo</li>
     * <li>Validación de registros de asistencia</li>
     * <li>Prevención de uso no autorizado</li>
     * </ul>
     * 
     * <p>
     * Puede ser blanqueado mediante la acción {@code Personal.borrarDeviceId}
     * </p>
     * 
     * @see DeviceIdProvider
     */
    @ReadOnly
    @Password
    @Column(length = 20)
    @Action(value = "Personal.borrarDeviceId", alwaysEnabled = true, notForViews = "Crear")
    private String deviceId;

    /**
     * Nombre de usuario para acceso al sistema.
     * 
     * <p>
     * Se genera automáticamente con el formato:
     * <code>INICIAL_NOMBRE + APELLIDO</code>
     * </p>
     * <p>
     * Ejemplo: Juan Pérez → JPérez
     * </p>
     * 
     * @see #getCreaUsuario()
     */
    @Column(length = 20)
    private String usuario;

    /**
     * Genera el nombre de usuario para el empleado.
     * 
     * <p>
     * Formato: INICIAL_NOMBRE + APELLIDO + @ + userId
     * </p>
     * <p>
     * Ejemplo: Juan Pérez con userId EMP001 → "JPérez@EMP001"
     * </p>
     * 
     * @return Nombre de usuario generado, o "N/D" si faltan datos
     */

    @Depends("nombres, apellido, userId")
    public String getCreaUsuario() {
        if ((nombres == null || nombres.isEmpty()) || (apellido == null || apellido.isEmpty())) {
            return "N/D";
        }
        String inicialNombre = nombres.trim().substring(0, 1);
        String apellidoCompleto = apellido.trim();
        return inicialNombre + apellidoCompleto + "@" + userId;
    }

    /**
     * Contraseña encriptada del empleado.
     * 
     * <p>
     * Se almacena de forma segura y puede ser blanqueada por un administrador
     * mediante la acción {@code Personal.borrarContrasena}
     * </p>
     */
    @Password
    @ReadOnly
    @Column(length = 20)
    @Action(value = "Personal.borrarContrasena", alwaysEnabled = true, notForViews = "Crear")
    @DefaultValueCalculator(CalculadorPassword.class)
    private String contrasena;

    /**
     * Indica si el empleado acepta pausas durante su jornada laboral.
     * 
     * <p>
     * Si es {@code true}, el sistema permitirá registrar pausas que no cuentan
     * como tiempo trabajado.
     * </p>
     * 
     * @see PersonalOnChangePausaAction
     */
    @OnChange(PersonalOnChangePausaAction.class)
    @DefaultValueCalculator(TrueCalculator.class)
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean aceptaPausa;

    /**
     * Nombre(s) del empleado.
     * 
     * <p>
     * Se almacena en capitalizado automáticamente mediante {@link Capitalizar}
     * </p>
     */
    @Capitalizar
    @Required
    @DisplaySize(40)
    @Column(length = 30)
    private String nombres;

    /**
     * Apellido del empleado.
     * 
     * <p>
     * Se almacena Capitalizado automáticamente mediante {@link Capitalizar}
     * </p>
     */
    @Capitalizar
    @Required
    @DisplaySize(40)
    @Column(length = 30)
    private String apellido;

    /**
     * Nombre completo del empleado (calculado como APELLIDO, NOMBRES).
     * 
     * <p>
     * Se actualiza automáticamente antes de guardar.
     * </p>
     * 
     * @see #getApellidoNombre()
     * @see #preGuardar()
     */
    @ReadOnly
    @DisplaySize(40)
    @MiLabel(medida = "extra", negrita = true, recuadro = true, icon = "account")
    private String nombreCompleto;

    /**
     * Retorna el nombre completo en formato "APELLIDO, NOMBRES".
     * 
     * @return Nombre completo formateado
     */
    @DisplaySize(40)
    @MiLabel(medida = "extra", negrita = true, recuadro = true, icon = "account-box")
    @Depends("nombres, apellido")
    public String getApellidoNombre() {
        return apellido + ", " + nombres;
    }

    /**
     * Fecha de nacimiento del empleado.
     * 
     * <p>
     * Se usa para calcular:
     * </p>
     * <ul>
     * <li>Edad actual ({@link #getEdad()})</li>
     * <li>Próximo cumpleaños ({@link #getProximoCumpleanos()})</li>
     * </ul>
     */
    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    private LocalDate fechaNacimiento;

    /**
     * Calcula la edad actual del empleado.
     * 
     * @return Edad en formato " Edad: X Años " o cadena vacía si no hay fecha
     */
    @DisplaySize(15)
    @MiLabel(medida = "mediana", negrita = true, recuadro = false)
    @Depends("fechaNacimiento")
    public String getEdad() {
        if (fechaNacimiento == null)
            return "";
        return " Edad: " + ChronoUnit.YEARS.between(fechaNacimiento, LocalDate.now()) + " Años ";
    }

    @Label
    @LabelFormat(LabelFormatType.NO_LABEL)
    public String getProximoCumpleanos() { // Método para calcular la proximidad del próximo cumpleaños
        if (fechaNacimiento == null) {
            return "Fecha de nacimiento no disponible";
        }

        LocalDate hoy = LocalDate.now();
        LocalDate proximoCumpleanos = fechaNacimiento.withYear(hoy.getYear());

        // Verificar si hoy es el cumpleaños
        if (proximoCumpleanos.isEqual(hoy)) {
            return "¡HOY ES EL CUMPLEAÑOS!";
        }

        // Si el cumpleaños de este año ya pasó, tomar el del próximo año
        if (proximoCumpleanos.isBefore(hoy)) {
            proximoCumpleanos = proximoCumpleanos.plusYears(1);
        }

        Period periodo = Period.between(hoy, proximoCumpleanos);
        int meses = periodo.getMonths();
        int dias = periodo.getDays();

        return "(Cumpleaños en " + meses + " meses y " + dias + " días)";
    }

    /**
     * Estado civil del empleado.
     * 
     * @see EstadoCivil
     */
    @Enumerated(EnumType.STRING)
    private EstadoCivil estadoCivil;

    /**
     * Nacionalidad del empleado.
     * 
     * @see Nacionalidades
     */
    @NoCreate
    @NoModify
    @DefaultValueCalculator(NacionalidadPorDefectoCalculator.class)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @DescriptionsList(descriptionProperties = "nacionalidad") // Muestra nacionalidad como texto
    private Nacionalidades nacionalidad;

    /**
     * Número de Documento Nacional de Identidad (DNI).
     * 
     * <p>
     * Debe cumplir con el formato argentino (7-8 dígitos).
     * </p>
     * <p>
     * Es único en el sistema y se valida automáticamente.
     * </p>
     * 
     * @see Dni
     */
    @AsEmbedded
    @NoFrame
    @NoSearch
    @NoCreate
    @NoModify
    @ReferenceView("simple")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "dni_id")
    private Dni dni;

    /**
     * Código Único de Identificación Laboral (CUIL).
     * 
     * <p>
     * Formato: XX-XXXXXXXX-X (11 dígitos con guiones)
     * </p>
     * <p>
     * Se valida automáticamente y debe ser único.
     * </p>
     */
    @Mask("00-00000000-0")
    private String cuil; // Código Único de Identificación Laboral

    /**
     * Dirección del empleado (calle, número, localidad, provincia).
     * 
     * <p>
     * Se usa para visualización en mapa (vista VerMapa).
     * </p>
     * 
     * @see Direccion
     */
    @Embedded
    @ReferenceView(forViews = "VerMapa", value = "VerMapa")
    private Direccion direccion;

    /**
     * Datos de contacto del empleado (teléfono, email, etc.).
     * 
     * @see DatosContacto
     */
    @Embedded
    private DatosContacto contacto;

    /**
     * Obtiene el puesto del empleado desde el contrato vigente.
     * 
     * @return Puesto del contrato vigente, o null si no hay contrato
     */
    @Transient
    @DisplaySize(20)
    @Depends("contratos.id")
    public String getPuesto() {
        ContratoLaboral contrato = getContratoVigente();
        return contrato != null ? contrato.getPuesto() : null;
    }

    /**
     * Obtiene el tipo de contrato desde el contrato vigente.
     * 
     * @return Tipo de contrato vigente, o null si no hay contrato
     */
    @Transient
    @DisplaySize(20)
    @Depends("contratos.id")
    public TipoContrato getTipoContrato() {
        ContratoLaboral contrato = getContratoVigente();
        return contrato != null ? contrato.getTipoContrato() : null;
    }

    /**
     * Fecha de inicio de actividades laborales.
     * 
     * <p>
     * Se usa para calcular la antigüedad laboral mediante
     * {@link #getAntiguedadLaboral()}
     * </p>
     */
    @Required
    @Stereotype("FECHA")
    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    private LocalDate inicioActividades;

    /**
     * Calcula la antigüedad laboral desde la fecha de inicio.
     * 
     * @return Antigüedad en formato "X años, Y meses y Z días"
     */
    @Label
    @Depends("inicioActividades")
    public String getAntiguedadLaboral() {
        if (inicioActividades == null) {
            return "Sin fecha de ingreso";
        }

        LocalDate hoy = LocalDate.now();
        Period periodo = Period.between(inicioActividades, hoy);

        int anios = periodo.getYears();
        int meses = periodo.getMonths();
        int dias = periodo.getDays();

        StringBuilder sb = new StringBuilder();
        if (anios > 0)
            sb.append(anios).append(anios == 1 ? " año" : " años");
        if (meses > 0) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(meses).append(meses == 1 ? " mes" : " meses");
        }
        if (dias > 0) {
            if (sb.length() > 0)
                sb.append(" y ");
            sb.append(dias).append(dias == 1 ? " dia" : " dias");
        }

        return sb.length() > 0 ? sb.toString() : "Menos de un dia";
    }

    /**
     * Sucursal donde trabaja el empleado.
     * 
     * <p>
     * Determina la ubicación física de trabajo y se usa para:
     * </p>
     * <ul>
     * <li>Validación de geolocalización en registros</li>
     * <li>Reportes por sucursal</li>
     * <li>Asignación de turnos específicos</li>
     * </ul>
     * 
     * @see Sucursales
     */
    @Capitalizar
    @LabelFormat(forViews = "simple", value = LabelFormatType.SMALL)
    @DescriptionsList
    @ManyToOne(fetch = FetchType.LAZY)
    private Sucursales sucursal;

    /**
     * Foto del empleado (archivo de imagen).
     * 
     * <p>
     * Acepta formatos de imagen con tamaño máximo de 200KB.
     * </p>
     */
    @ReadOnly(forViews = "Simple")
    @LabelFormat(LabelFormatType.NO_LABEL)
    @File(acceptFileTypes = "image/*", maxFileSizeInKb = 200)
    @Column(length = 32)
    private String foto;

    /**
     * Archivos de documentación personal (contratos, certificados, etc.) con tamaño
     * máximo de 200KB.
     */
    @Files(maxFileSizeInKb = 200)
    @Column(length = 32)
    private String documentacionPersonal;

    /**
     * Obtiene todos los eventos del empleado para el calendario anual.
     * 
     * <p>
     * Incluye feriados, licencias y auditorías diarias.
     * </p>
     * 
     * @return Colección de eventos para el editor de calendario
     * @see DtoLicenciasFeriados
     */
    @Editor("yearCalendarEditor")
    public Collection<DtoLicenciasFeriados> getEventos() {

        EntityManager em = org.openxava.jpa.XPersistence.getManager();
        List<DtoLicenciasFeriados> out = new ArrayList<>();

        /* 1) (Opcional) Feriados “comunes” como contexto visual */
        em.createQuery("select f from Feriados f", Feriados.class)
                .getResultList()
                .forEach(f -> out.add(DtoLicenciasFeriados.of(f)));

        /* 2) Licencias del empleado (rango real) */
        em.createQuery("select l from Licencia l where l.empleado = :yo", Licencia.class)
                .setParameter("yo", this)
                // Si querés limitar al año actual, descomentá:
                // .setParameter("d", LocalDate.of(LocalDate.now().getYear(),1,1))
                // .setParameter("h", LocalDate.of(LocalDate.now().getYear(),12,31))
                .getResultList()
                .forEach(l -> out.add(DtoLicenciasFeriados.of(l)));

        /*
         * 3) Auditoría diaria: COM/INC/AUS + FERIADO_TRABAJADO (NO LICENCIA para evitar
         * duplicados)
         */
        int anio = java.time.LocalDate.now().getYear();
        java.time.LocalDate desde = java.time.LocalDate.of(anio, 1, 1);
        java.time.LocalDate hasta = java.time.LocalDate.of(anio, 12, 31);

        List<EvaluacionJornada> evs = java.util.Arrays.asList(
                EvaluacionJornada.COMPLETA,
                EvaluacionJornada.INCOMPLETA,
                EvaluacionJornada.AUSENTE,
                EvaluacionJornada.FERIADO_TRABAJADO);

        List<AuditoriaRegistros> regs = em.createQuery(
                "select a from AuditoriaRegistros a " +
                        "where a.empleado = :yo and a.evaluacion in :evs " +
                        "and a.fecha between :d and :h " +
                        "order by a.fecha asc",
                AuditoriaRegistros.class)
                .setParameter("yo", this)
                .setParameter("evs", evs)
                // Si 'a.fecha' es java.util.Date, usa java.sql.Date.valueOf(...)
                .setParameter("d", desde)
                .setParameter("h", hasta)
                .getResultList();

        // Mapear cada día a su evento por tipo
        for (AuditoriaRegistros a : regs) {
            if (a.getFecha() == null || a.getEvaluacion() == null)
                continue;
            switch (a.getEvaluacion()) {
                case COMPLETA:
                    out.add(DtoLicenciasFeriados.ofCompleta(a));
                    break;
                case INCOMPLETA:
                    out.add(DtoLicenciasFeriados.ofIncompleta(a));
                    break;
                case AUSENTE:
                    out.add(DtoLicenciasFeriados.ofAusente(a));
                    break;
                case FERIADO_TRABAJADO:
                    out.add(DtoLicenciasFeriados.ofFeriadoTrabajado(a));
                    break;
                default:
                    break; // LICENCIA/FERIADO “común” no se generan aquí
            }
        }
        return out;
    }

    /**
     * Colección de licencias del empleado para el año actual.
     * 
     * <p>
     * Solo muestra licencias cuya fecha de inicio sea del año en curso
     * (filtro automático por Hibernate @Where).
     * </p>
     * 
     * @see Licencia
     * @see TipoLicenciaAR
     */
    @ListAction("Licencia.VerCalendario")
    @ListAction("Licencia.crearLista")
    @DeleteSelectedAction("")
    @NewAction("Licencia.AsignarLicencia")
    @EditAction("Licencia.EditarLicencia")
    @SaveAction("Licencia.Guardar")
    @NoDefaultActions
    @DetailAction("Licencia.ImprimirConstancia")
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)
    @ListProperties("tipo, fechaInicio, fechaFin, dias, justificado")
    @OrderBy("fechaInicio desc")
    @org.hibernate.annotations.Where(clause = "YEAR(fechaInicio) = YEAR(CURDATE())")
    private Collection<Licencia> licencias;

    /**
     * Obtiene un resumen de licencias por tipo para el año actual.
     * 
     * <p>
     * Para cada tipo de licencia muestra total de días utilizados
     * y días restantes disponibles.
     * </p>
     * 
     * @return Colección de resúmenes por tipo de licencia
     * @see LicenciaResumenPorTipo
     */
    @NoCreate
    @SimpleList
    public Collection<LicenciaResumenPorTipo> getLicenciasResumenAnual() {
        Map<TipoLicenciaAR, Integer> totalDias = new TreeMap<>();
        Map<TipoLicenciaAR, Licencia> ultimaLicenciaPorTipo = new TreeMap<>();
        int anioActual = LocalDate.now().getYear();

        if (getLicencias() == null || getLicencias().isEmpty())
            return Collections.emptyList();

        // 1. Recorrer licencias del año actual
        for (Licencia l : getLicencias()) {
            if (l.getFechaInicio() != null && l.getFechaInicio().getYear() == anioActual) {
                TipoLicenciaAR tipo = l.getTipo();

                totalDias.merge(tipo, l.getDias(), (a, b) -> a + b);

                // Mantener la última licencia (por fecha)
                ultimaLicenciaPorTipo.compute(tipo, (k, licenciaAnterior) -> {
                    if (licenciaAnterior == null)
                        return l;
                    return l.getFechaInicio().isAfter(licenciaAnterior.getFechaInicio()) ? l : licenciaAnterior;
                });
            }
        }

        // 2. Armar colección de resumen con el último "diasRestantes" por tipo
        List<LicenciaResumenPorTipo> resultado = new ArrayList<>();
        for (TipoLicenciaAR tipo : totalDias.keySet()) {
            int dias = totalDias.getOrDefault(tipo, 0);
            int restantes = ultimaLicenciaPorTipo.get(tipo) != null
                    ? ultimaLicenciaPorTipo.get(tipo).getDiasRestantes()
                    : 0;

            resultado.add(new LicenciaResumenPorTipo(tipo, dias, restantes));
        }

        return resultado;
    }

    // =========================================================================
    // GETTERS DELEGADOS A CONTRATO VIGENTE - VALORES MONETARIOS
    // =========================================================================

    /**
     * Obtiene el valor hora desde el contrato vigente.
     * 
     * @return Valor hora efectivo del contrato, o ZERO si no hay contrato
     */
    @Transient
    public BigDecimal getValorHora() {
        ContratoLaboral contrato = getContratoVigente();
        return contrato != null ? contrato.getValorHoraEfectivo() : BigDecimal.ZERO;
    }

    /**
     * Obtiene el porcentaje de hora extra desde el contrato vigente.
     * 
     * @return Porcentaje hora extra del contrato, o null si no hay contrato
     */
    @Transient
    public BigDecimal getPorcentajeHoraExtra() {
        ContratoLaboral contrato = getContratoVigente();
        return contrato != null ? contrato.getPorcentajeHoraExtra() : null;
    }

    /**
     * Obtiene el valor de la hora extra desde el contrato vigente.
     * 
     * @return Valor hora extra del contrato, o ZERO si no hay contrato
     */
    @Transient
    public BigDecimal getValorHoraExtra() {
        ContratoLaboral contrato = getContratoVigente();
        return contrato != null ? contrato.getValorHoraExtra() : BigDecimal.ZERO;
    }

    /**
     * Obtiene el porcentaje de hora especial desde el contrato vigente.
     * 
     * @return Porcentaje hora especial del contrato, o null si no hay contrato
     */
    @Transient
    public BigDecimal getPorcentajeHoraEspecial() {
        ContratoLaboral contrato = getContratoVigente();
        return contrato != null ? contrato.getPorcentajeHoraEspecial() : null;
    }

    /**
     * Obtiene el valor de la hora especial desde el contrato vigente.
     * 
     * @return Valor hora especial del contrato, o ZERO si no hay contrato
     */
    @Transient
    public BigDecimal getValorHoraEspecial() {
        ContratoLaboral contrato = getContratoVigente();
        return contrato != null ? contrato.getValorHoraEspecial() : BigDecimal.ZERO;
    }

    /**
     * Calcula el valor de la hora aplicando la bonificación del turno si existe.
     * 
     * @param turno Turno para el cual calcular el valor hora
     * @return Valor hora base + bonificación del turno
     */
    @Transient
    @Money
    public BigDecimal getValorHoraTurno(TurnosHorarios turno) {
        BigDecimal vHora = getValorHora(); // Usar getter delegado
        if (vHora == null) {
            return BigDecimal.ZERO;
        }

        if (turno == null || turno.getPorcentajeBonificacion() == null ||
                turno.getPorcentajeBonificacion().compareTo(BigDecimal.ZERO) == 0) {
            return vHora;
        }

        // Mismo formato que getValorHoraExtra: dividir por 100
        BigDecimal bonificacion = vHora.multiply(turno.getPorcentajeBonificacion())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return vHora.add(bonificacion);
    }

    /**
     * Notas personales sobre el empleado (texto libre).
     * 
     * <p>
     * Campo de texto sin formato para observaciones adicionales.
     * </p>
     */
    @TextArea
    private String notasPersonale;

    // =================== NOTAS DE DESEMPEÑO ===================

    /**
     * Colección de notas de desempeño del empleado.
     * 
     * <p>
     * Cada nota incluye calificación, contenido y autor.
     * Se usa para calcular {@link #getPromedioDesempeno()} y
     * {@link #getEvaluacionDesempeno()}.
     * </p>
     * 
     * @see NotaDesempeno
     */

    @NoDefaultActions
    @ListAction("Print.generatePdf")
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("autor, fechaHora, calificacion, contenido")
    @OrderBy("fechaHora DESC")
    private Collection<NotaDesempeno> notasDesempeno = new ArrayList<>();

    /**
     * Colección de jornadas asignadas al empleado.
     * 
     * <p>
     * Cada {@link JornadaAsignada} vincula un {@link TurnosHorarios} con un rango
     * de fechas,
     * permitiendo:
     * </p>
     * <ul>
     * <li>Turnos rotativos (sin fecha fin)</li>
     * <li>Turnos puntuales (con fecha inicio y fin)</li>
     * <li>Múltiples turnos simultáneos</li>
     * </ul>
     * 
     * <p>
     * <b>Importante:</b> Los cambios en esta colección pueden perderse si no se
     * persisten
     * correctamente. Ver issue relacionado en el código.
     * </p>
     * 
     * @see JornadaAsignada
     * @see TurnosHorarios
     * @see #getTurnoParaFecha(LocalDate)
     * @see #getTurnosParaFecha(LocalDate)
     */
    @NoDefaultActions
    @OneToMany(mappedBy = "personal", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("turno.codigo, turno.detalleJornadaHoras, fechaInicio, fechaFin")
    @OrderBy("fechaInicio")
    private List<JornadaAsignada> jornadasAsignadas = new ArrayList<>();

    // ==================================================================================
    // LIQUIDACIONES DE JORNADAS
    // ==================================================================================

    /**
     * Colección de liquidaciones de jornadas del empleado.
     * 
     * <p>
     * Cada liquidación consolida las horas trabajadas (normales, extras,
     * especiales)
     * para un período determinado, junto con los valores monetarios
     * correspondientes.
     * </p>
     * 
     * @see LiquidacionJornadas
     */
    @NoDefaultActions
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)
    @ListProperties("periodoDesde, periodoHasta, estadoPeriodo, horasNormalesFormatted, horasExtrasFormatted, horasEspecialesFormatted, montoGranTotal")
    @OrderBy("periodoDesde desc")
    @CollectionView("DetalleCompleto")
    @NewAction("LiquidacionJornadas.nuevaLiquidacion")
    @RemoveSelectedAction("")
    @DeleteSelectedAction("")
    @RemoveAction("LiquidacionJornadas.eliminarLiquidacion")
    @DetailAction("LiquidacionJornadas.CerrarLiquidacion")
    @DetailAction("LiquidacionJornadas.Recalcular")
    @DetailAction("LiquidacionJornadas.aplicarRedondeo")
    @DetailAction("LiquidacionJornadas.revertirRedondeo")
    private Collection<LiquidacionJornadas> liquidaciones;

    // ==================================================================================
    // CONTRATOS LABORALES
    // ==================================================================================

    /**
     * Colección de contratos laborales del empleado (historial).
     * 
     * <p>
     * Permite mantener múltiples contratos con diferentes vigencias
     * para registrar cambios de puesto, sueldo, categoría, etc.
     * </p>
     * 
     * @see ContratoLaboral
     */

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("puesto, nivelJerarquico, sueldoMensualAcordado, fechaVigenciaDesde, fechaVigenciaHasta, vigente")
    @OrderBy("fechaVigenciaDesde desc")
    private Collection<ContratoLaboral> contratos = new ArrayList<>();

    /**
     * Obtiene el contrato laboral vigente del empleado.
     * 
     * <p>
     * Un contrato está vigente si:
     * </p>
     * <ul>
     * <li>La fecha actual es >= fechaVigenciaDesde</li>
     * <li>La fecha actual es <= fechaVigenciaHasta (o fechaVigenciaHasta es
     * null)</li>
     * </ul>
     * 
     * @return Contrato vigente o null si no hay ninguno
     */
    @Transient
    public ContratoLaboral getContratoVigente() {
        if (contratos == null || contratos.isEmpty()) {
            return null;
        }
        return contratos.stream()
                .filter(ContratoLaboral::isVigente)
                .findFirst()
                .orElse(null);
    }

    // =============================================================================================
    /**
     * Obtiene el turno principal asignado para una fecha específica.
     * 
     * <p>
     * Este método aplica la siguiente lógica de prioridad:
     * </p>
     * <ol>
     * <li><b>Jornadas puntuales:</b> Busca jornadas con fecha fin explícita que
     * incluyan la fecha</li>
     * <li><b>Rotaciones activas:</b> Si no hay jornadas puntuales, busca rotaciones
     * sin fecha fin</li>
     * <li><b>Rotación semanal:</b> Si hay múltiples rotaciones, aplica lógica de
     * rotación por semanas</li>
     * </ol>
     * 
     * <p>
     * <b>Nota:</b> Este método retorna solo UN turno. Para obtener todos los turnos
     * aplicables (en caso de múltiples asignaciones), usar
     * {@link #getTurnosParaFecha(LocalDate)}
     * </p>
     * 
     * @param fecha Fecha para la cual buscar el turno (no puede ser null)
     * @return El turno asignado para la fecha, o {@code null} si no hay turno
     * @throws NullPointerException si fecha es null
     * @see #getTurnosParaFecha(LocalDate)
     * @see JornadaAsignada
     * @see TurnosHorarios
     */
    public TurnosHorarios getTurnoParaFecha(LocalDate fecha) {
        if (jornadasAsignadas == null || jornadasAsignadas.isEmpty())
            return null;

        // 1. Obtener TODAS las jornadas vigentes para la fecha
        List<JornadaAsignada> vigentes = jornadasAsignadas.stream()
                .filter(j -> !fecha.isBefore(j.getFechaInicio()) &&
                        (j.getFechaFin() == null || !fecha.isAfter(j.getFechaFin())))
                .collect(Collectors.toList());

        if (vigentes.isEmpty()) {
            return null;
        }

        // 2. PRIORIDAD: Turnos programados (con fecha fin) tienen prioridad
        // sobre turnos indefinidos. Actúan como "override temporal".
        List<JornadaAsignada> programadas = vigentes.stream()
                .filter(j -> j.getFechaFin() != null)
                .sorted(Comparator.comparing(JornadaAsignada::getFechaInicio).reversed())
                .collect(Collectors.toList());

        if (!programadas.isEmpty()) {
            // Retornar la más reciente (por fecha inicio)
            return programadas.get(0).getTurno();
        }

        // 3. Solo quedan jornadas indefinidas (rotativas)
        List<JornadaAsignada> rotativas = vigentes.stream()
                .filter(j -> j.getFechaFin() == null)
                .sorted(Comparator.comparing(JornadaAsignada::getFechaInicio))
                .collect(Collectors.toList());

        if (rotativas.size() == 1) {
            return rotativas.get(0).getTurno();
        }

        // 4. Múltiples rotativas: aplicar rotación semanal
        LocalDate lunesBase = rotativas.get(0).getFechaInicio().with(DayOfWeek.MONDAY);
        LocalDate lunesActual = fecha.with(DayOfWeek.MONDAY);

        long semanasTranscurridas = ChronoUnit.WEEKS.between(lunesBase, lunesActual);
        if (semanasTranscurridas < 0)
            semanasTranscurridas = 0;

        int indice = (int) (semanasTranscurridas % rotativas.size());

        return rotativas.get(indice).getTurno();
    }

    /**
     * Obtiene TODOS los turnos aplicables para una fecha dada.
     * 
     * <p>
     * A diferencia de {@link #getTurnoParaFecha(LocalDate)}, este método retorna
     * una lista con todos los turnos que aplican para la fecha, permitiendo manejar
     * casos donde un empleado tiene múltiples turnos en el mismo día.
     * </p>
     * 
     * <p>
     * <b>Casos de uso:</b>
     * </p>
     * <ul>
     * <li>Turno normal + guardia especial</li>
     * <li>Múltiples turnos rotativos</li>
     * <li>Turnos puntuales superpuestos</li>
     * </ul>
     * 
     * <p>
     * <b>Lógica aplicada:</b>
     * </p>
     * <ol>
     * <li>Busca jornadas puntuales (con fecha fin) que incluyan la fecha</li>
     * <li>Busca jornadas rotativas (sin fecha fin) activas en la fecha</li>
     * <li>Para cada turno encontrado, verifica si es laboral para el día de la
     * semana</li>
     * <li>Retorna todos los turnos que cumplan las condiciones</li>
     * </ol>
     * 
     * @param fecha Fecha para la cual buscar turnos (no puede ser null)
     * @return Lista de turnos aplicables (nunca null, puede estar vacía)
     * @throws NullPointerException si fecha es null
     * @see #getTurnoParaFecha(LocalDate)
     * @see TurnosHorarios#esLaboral(DayOfWeek)
     * @since 2.0
     */
    public List<TurnosHorarios> getTurnosParaFecha(LocalDate fecha) {
        if (jornadasAsignadas == null || jornadasAsignadas.isEmpty())
            return Collections.emptyList();
        List<TurnosHorarios> turnosAplicables = new ArrayList<>();
        // 1. Buscar jornadas puntuales (con fecha fin explicita y valida)
        List<JornadaAsignada> jornadasFijas = jornadasAsignadas.stream()
                .filter(j -> j.getFechaFin() != null &&
                        !fecha.isBefore(j.getFechaInicio()) &&
                        !fecha.isAfter(j.getFechaFin()))
                .collect(Collectors.toList());
        for (JornadaAsignada jf : jornadasFijas) {
            if (jf.getTurno() != null && !turnosAplicables.contains(jf.getTurno())) {
                turnosAplicables.add(jf.getTurno());
            }
        }
        // 2. Buscar rotaciones activas (fechaFin == null o posterior)
        // CAMBIO IMPORTANTE: Evaluar CADA rotación individualmente
        List<JornadaAsignada> rotativas = jornadasAsignadas.stream()
                .filter(j -> (j.getFechaFin() == null || !fecha.isAfter(j.getFechaFin())) &&
                        !fecha.isBefore(j.getFechaInicio()))
                .collect(Collectors.toList());
        for (JornadaAsignada rotacion : rotativas) {
            TurnosHorarios turno = rotacion.getTurno();
            if (turno != null && !turnosAplicables.contains(turno)) {
                // Verificar si este turno es laboral para el día de la semana de la fecha
                DayOfWeek diaSemana = fecha.getDayOfWeek();
                if (turno.esLaboral(diaSemana)) {
                    turnosAplicables.add(turno);
                }
            }
        }
        return turnosAplicables;
    }

    // =============================================================================================
    /**
     * Obtiene la descripción del turno activo para hoy.
     * 
     * @return Descripción del turno actual o mensaje de estado
     * @see #getTurnoDescripcionParaFecha(LocalDate)
     */
    @DisplaySize(40)
    @MiLabel(medida = "grande", negrita = true, recuadro = true, icon = "clock")
    public String getTurnoActivoHoy() {
        return getTurnoDescripcionParaFecha(LocalDate.now());
    };

    /**
     * Devuelve la descripción del turno asignado para una fecha dada.
     */
    @Transient
    public String getTurnoDescripcionParaFecha(LocalDate fecha) {
        if (fecha == null)
            return "Fecha no especificada";

        TurnosHorarios turno = getTurnoParaFecha(fecha);
        if (turno == null)
            return "Sin turno asignado";

        DayOfWeek dia = fecha.getDayOfWeek();

        if (!turno.esLaboral(dia)) {
            return turno.getCodigo() + " - Dia no laboral";
        }

        LocalTime entrada = turno.getEntradaParaDia(dia);
        LocalTime salida = turno.getSalidaParaDia(dia);

        if (entrada == null || salida == null) {
            return turno.getCodigo() + " - Sin horario definido";
        }

        String horario = String.format("%02d:%02d a %02d:%02d",
                entrada.getHour(), entrada.getMinute(), salida.getHour(), salida.getMinute());

        String diaNombre = dia.getDisplayName(java.time.format.TextStyle.SHORT, new Locale("es", "ES")).toUpperCase();
        int minutos = turno.getHorasParaDia(dia);
        String horasTurno = (minutos / 60) + " Hs. " + (minutos % 60) + " Min.";

        return turno.getCodigo() + " / " + diaNombre + " de " + horario + " / " + horasTurno;
    }

    // =============================================================================================
    /**
     * Genera datos para el gráfico anual de asistencia por mes.
     * 
     * <p>
     * Para cada mes del año actual cuenta: jornadas completas,
     * incompletas, licencias, ausencias y feriados trabajados.
     * </p>
     * 
     * @return Colección de resúmenes mensuales para gráfico de barras
     * @see ResumenAnualGrafico
     */
    @Transient
    @ReadOnly
    @Chart(type = ChartType.BAR, labelProperties = "mesEtiqueta", dataProperties = "completas, incompletas, licencias, ausentes, feriadosTrabajados")
    @ListProperties("mesEtiqueta, completas, incompletas, licencias, ausentes, feriadosTrabajados")
    public Collection<ResumenAnualGrafico> getLicenciasGraficoAnual() {
        if (getId() == null)
            return Collections.emptyList(); // Entidad no persistida

        final int anio = LocalDate.now().getYear();
        final LocalDate desde = LocalDate.of(anio, 1, 1);
        final LocalDate hasta = LocalDate.of(anio, 12, 31);

        EntityManager em = XPersistence.getManager();
        List<AuditoriaRegistros> registros = em.createQuery(
                "select a from AuditoriaRegistros a " +
                        "where a.empleado = :emp and a.fecha between :d and :h",
                AuditoriaRegistros.class)
                .setParameter("emp", this)
                .setParameter("d", desde) // usa java.sql.Date.valueOf(...) si tu campo es Date
                .setParameter("h", hasta)
                .getResultList();

        // Inicializar meses
        Locale esAR = new Locale("es", "AR");
        Map<YearMonth, ResumenAnualGrafico> porMes = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(anio, m);
            String et = ym.getMonth().getDisplayName(TextStyle.SHORT, esAR);
            et = et.substring(0, 1).toUpperCase(esAR) + et.substring(1);
            porMes.put(ym, new ResumenAnualGrafico(et, 0, 0, 0, 0, 0));
        }

        for (AuditoriaRegistros a : registros) {
            if (a.getFecha() == null || a.getEvaluacion() == null)
                continue;
            YearMonth ym = YearMonth.from(a.getFecha()); // adapta si usás java.util.Date
            ResumenAnualGrafico r = porMes.get(ym);
            if (r == null)
                continue;

            switch (a.getEvaluacion()) {
                case COMPLETA:
                    r.setCompletas(r.getCompletas() + 1);
                    break;
                case INCOMPLETA:
                    r.setIncompletas(r.getIncompletas() + 1);
                    break;
                case LICENCIA:
                    r.setLicencias(r.getLicencias() + 1);
                    break;
                case AUSENTE:
                    r.setAusentes(r.getAusentes() + 1);
                    break;
                case FERIADO_TRABAJADO:
                    r.setFeriadosTrabajados(r.getFeriadosTrabajados() + 1);
                    break;
                case FERIADO:
                    /* NO contar */ break;
                default:
                    break;
            }
        }
        return porMes.values();
    }

    // ===============================================================================================

    /**
     * Callback JPA antes de persistir o actualizar el empleado.
     * 
     * <p>
     * Acciones realizadas:
     * </p>
     * <ul>
     * <li>Actualiza 'usuario' con getCreaUsuario()</li>
     * <li>Actualiza 'nombreCompleto' con getApellidoNombre()</li>
     * <li>Añade prefijo "x-" al userId si está inactivo</li>
     * <li>Asigna coordenadas a la dirección si faltan</li>
     * </ul>
     */
    @PrePersist
    @PreUpdate
    private void preGuardar() {

        // === GESTIÓN DE BORRADO LÓGICO Y CONTRATOS ===
        if (eliminado) {
            // Forzar inactividad si está eliminado
            this.activo = false;

            // Asegurar fecha de eliminación
            if (this.fechaEliminacion == null) {
                this.fechaEliminacion = LocalDateTime.now();
            }

            // CERRAR CONTRATO VIGENTE
            ContratoLaboral contrato = getContratoVigente();
            if (contrato != null) {
                // Solo cerrar si no tiene ya fecha de fin o si esta es futura
                if (contrato.getFechaVigenciaHasta() == null ||
                        contrato.getFechaVigenciaHasta().isAfter(LocalDate.now())) {
                    contrato.setFechaVigenciaHasta(LocalDate.now());
                    if (contrato.getMotivoFinalizacion() == null || contrato.getMotivoFinalizacion().isBlank()) {
                        contrato.setMotivoFinalizacion("Baja automática por eliminación de empleado");
                    }
                    System.out.println("[Personal] Contrato cerrado automáticamente para: " + getNombreCompleto());
                }
            }
        } else {
            // Si no está eliminado (restauración o activo normal), limpiar fecha
            // eliminación
            this.fechaEliminacion = null;
        }

        // === VERIFICAR CONTRATO VIGENTE (Para activos no eliminados) ===
        // Si el empleado está activo pero no tiene contrato vigente, desactivarlo
        if (activo && !eliminado && getContratoVigente() == null) {
            activo = false;
            System.out.println("[Personal] " + getNombreCompleto() + " desactivado: sin contrato vigente");
        }

        setUsuario(getCreaUsuario());
        setNombreCompleto(getApellidoNombre());

        // Gestión de prefijo "x-" para userId
        if (Boolean.FALSE.equals(activo)) {
            if (userId != null && !userId.startsWith("x-")) {
                userId = "x-" + userId;
            }
        } else {
            if (userId != null && userId.startsWith("x-")) {
                userId = userId.substring(2);
            }
        }

        try {
            AsignarCoordenadasService.asignarCoordenadasSiFaltan(this.direccion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
