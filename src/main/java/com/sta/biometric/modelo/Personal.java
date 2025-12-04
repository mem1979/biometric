package com.sta.biometric.modelo;

import java.math.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.web.editors.*;

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
 * <p>Esta clase centraliza toda la información relacionada con un empleado, incluyendo:</p>
 * <ul>
 *   <li><b>Datos personales:</b> Nombre, DNI, CUIL, fecha de nacimiento, dirección, contacto</li>
 *   <li><b>Información laboral:</b> Sucursal, puesto, fecha de inicio, antigüedad</li>
 *   <li><b>Credenciales:</b> Usuario, contraseña, deviceId para autenticación móvil</li>
 *   <li><b>Jornadas laborales:</b> Turnos asignados, horarios, pausas</li>
 *   <li><b>Honorarios:</b> Valor hora, bonificaciones por horas extras y especiales</li>
 *   <li><b>Licencias:</b> Historial de licencias y permisos</li>
 *   <li><b>Desempeño:</b> Evaluaciones y notas de desempeño</li>
 *   <li><b>Reportes:</b> Cálculos de horas trabajadas, asistencia, llegadas tarde</li>
 * </ul>
 * 
 * <p><b>Responsabilidades principales:</b></p>
 * <ul>
 *   <li>Almacenar y gestionar datos del empleado (entidad JPA)</li>
 *   <li>Calcular métricas laborales (horas trabajadas, extras, especiales)</li>
 *   <li>Gestionar turnos y jornadas asignadas</li>
 *   <li>Generar reportes e informes de asistencia</li>
 *   <li>Validar datos de entrada (DNI, CUIL, fechas)</li>
 * </ul>
 * 
 * <p><b>Relaciones con otras entidades:</b></p>
 * <ul>
 *   <li>{@link Sucursales} - Sucursal donde trabaja el empleado</li>
 *   <li>{@link TurnosHorarios} - Turnos asignados mediante {@link JornadaAsignada}</li>
 *   <li>{@link Licencia} - Licencias y permisos solicitados</li>
 *   <li>{@link AuditoriaRegistros} - Registros de entrada/salida diarios</li>
 *   <li>{@link NotaDesempeno} - Evaluaciones de desempeño</li>
 * </ul>
 * 
 * <p><b>Vistas OpenXava configuradas:</b></p>
 * <ul>
 *   <li><b>Vista principal:</b> Información completa del empleado</li>
 *   <li><b>VerMapa:</b> Visualización de dirección en mapa</li>
 *   <li><b>VerCalendario:</b> Eventos y calendario del empleado</li>
 *   <li><b>simple:</b> Vista resumida para selección rápida</li>
 * </ul>
 * 
 * <p><b>Nota importante:</b> Esta clase tiene múltiples responsabilidades y es candidata
 * para refactorización futura, dividiendo la lógica de cálculo en servicios separados.</p>
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
@Getter @Setter
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
        "inicioActividades, antiguedadLaboral;"
        + " puesto;" +
        "]; " +
        "Honorarios[" +
        "valorHora," +
        "porcentajeHoraExtra, valorHoraExtra," +
        "porcentajeHoraEspecial, valorHoraEspecial;" +
        "]; " +
        "JORNADAS[" +
        "aceptaPausa; jornadasAsignadas;" +
        "]; " +
        "}; " +

        "LICENCIAS { " +
        "licencias, licenciasResumenAnual; " +
        "}; " +

        "informes { " +
        "desde, hasta;" +
        "IndicadoresClave {" +
        "  totalDiasTrabajados,  totalHorasTrabajadasInformes, tasaAsistencia;" +
        "  cantidadLlegadasTardeInformes, diasLicenciaUtilizados;" +
        "};" +
        "Graficos {" +
        "  licenciasGraficoAnual;" +
        "  evolucionMensualAsistencia;" +
        "  distribucionTiposJornada, horasPorMes;" +
        "};" +
        "Detalles {" +
        "  topDiasHorasExtras;" +
        "  registroLlegadasTarde;" +
        "};" +
        "}; " +

        "INCIDENCIAS_Y_OBSERVACIONES { " +
        "  Desempeno[promedioDesempeno, evaluacionDesempeno]; " +
        "  notasDesempeno; " +
        "  nota;" +
        "}")

@View(name = "VerMapa", members = "direccion")

@View(name = "VerCalendario", members = "eventos")

@View(name = "simple", members = "nombreCompleto, sucursal, puesto;")

@Tab(editors = "List", properties = "foto, nombreCompleto, userId, sucursal.nombre, puesto, activo", defaultOrder = "${activo} desc, ${nombreCompleto} asc", rowStyles = {
        @RowStyle(style = "empleadoInactivo", property = "activo", value = "false") })

public class Personal extends Identifiable {

	/**
	 * Indica si el empleado está activo en el sistema.
	 * 
	 * <p>Un empleado inactivo:</p>
	 * <ul>
	 *   <li>No puede registrar asistencia</li>
	 *   <li>No aparece en listados activos</li>
	 *   <li>Mantiene su historial para consultas</li>
	 * </ul>
	 * 
	 * @see PersonalOnChangeActivoAction
	 */
    @DefaultValueCalculator(TrueCalculator.class)
    @OnChange(PersonalOnChangeActivoAction.class)
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean activo;

    @Hidden
    @Transient
    private String userIdOriginal;

  
    @Required
    @SearchKey
    @Column(length = 10, unique = true)
    @DefaultValueCalculator(GeneradorCodigoUserIdCalculator.class)
    private String userId;

    /**
     * Identificador único del dispositivo móvil del empleado.
     * 
     * <p>Se genera automáticamente al instalar la app móvil y se usa para:</p>
     * <ul>
     *   <li>Autenticación del dispositivo</li>
     *   <li>Validación de registros de asistencia</li>
     *   <li>Prevención de uso no autorizado</li>
     * </ul>
     * 
     * <p>Puede ser blanqueado mediante la acción {@code Personal.borrarDeviceId}</p>
     * 
     * @see DeviceIdProvider
     */
    @ReadOnly // @Password
    @Column(length = 20)
    @Action(value = "Personal.borrarDeviceId", alwaysEnabled = true)
    private String deviceId;

    /**
     * Nombre de usuario para acceso al sistema.
     * 
     * <p>Se genera automáticamente con el formato: <code>INICIAL_NOMBRE + APELLIDO</code></p>
     * <p>Ejemplo: Juan Pérez → JPérez</p>
     * 
     * @see #getCreaUsuario()
     */
    @Column(length = 20)
    @Mayuscula
    private String usuario;

    /**
     * Genera el nombre de usuario para el empleado.
     * 
     * <p>Formato: INICIAL_NOMBRE + APELLIDO + @ + userId</p>
     * <p>Ejemplo: Juan Pérez con userId EMP001 → "JPérez@EMP001"</p>
     * 
     * @return Nombre de usuario generado, o "N/D" si faltan datos
     */
    @Mayuscula
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
     * <p>Se almacena de forma segura y puede ser blanqueada por un administrador
     * mediante la acción {@code Personal.borrarContrasena}</p>
     */
    @Password
    @ReadOnly
    @Column(length = 20)
    @Action(value = "Personal.borrarContrasena", alwaysEnabled = true)
    @DefaultValueCalculator(CalculadorPassword.class)
    private String contrasena;

    /**
     * Indica si el empleado acepta pausas durante su jornada laboral.
     * 
     * <p>Si es {@code true}, el sistema permitirá registrar pausas que no cuentan
     * como tiempo trabajado.</p>
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
     * <p>Se almacena en capitalizado automáticamente mediante {@link Capitalizar}</p>
     */
    @Capitalizar
    @Required
    @DisplaySize(40)
    private String nombres;

    /**
     * Apellido del empleado.
     * 
     * <p>Se almacena Capitalizado automáticamente mediante {@link Capitalizar}</p>
     */
    @Capitalizar
    @Required
    @DisplaySize(40)
    @Column(length = 30)
    private String apellido;

    /**
     * Nombre completo del empleado (calculado como APELLIDO, NOMBRES).
     * 
     * <p>Se actualiza automáticamente antes de guardar.</p>
     * 
     * @see #getApellidoNombre()
     * @see #preGuardar()
     */
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
     * <p>Se usa para calcular:</p>
     * <ul>
     *   <li>Edad actual ({@link #getEdad()})</li>
     *   <li>Próximo cumpleaños ({@link #getProximoCumpleanos()})</li>
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
    @NoCreate @NoModify
    @DefaultValueCalculator(NacionalidadPorDefectoCalculator.class)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @DescriptionsList(descriptionProperties = "nacionalidad") // Muestra nacionalidad como texto
    private Nacionalidades nacionalidad;

    /**
     * Número de Documento Nacional de Identidad (DNI).
     * 
     * <p>Debe cumplir con el formato argentino (7-8 dígitos).</p>
     * <p>Es único en el sistema y se valida automáticamente.</p>
     * 
     * @see Dni
     */
    @AsEmbedded
    @NoFrame @NoSearch @NoCreate @NoModify
    @ReferenceView("simple")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) 
    @JoinColumn(name = "dni_id") 
    private Dni dni;

    /**
     * Código Único de Identificación Laboral (CUIL).
     * 
     * <p>Formato: XX-XXXXXXXX-X (11 dígitos con guiones)</p>
     * <p>Se valida automáticamente y debe ser único.</p>
     */
    @Mask("00-00000000-0")
    private String cuil; // Código Único de Identificación Laboral

    /**
     * Dirección del empleado (calle, número, localidad, provincia).
     * 
     * <p>Se usa para visualización en mapa (vista VerMapa).</p>
     * 
     * @see Direccion
     */
    @Embedded
    @ReferenceView(forViews = "VerMapa", value = "VerMapa")
    private Direccion direccion;

    @Embedded
    private DatosContacto contacto;

    
    /**
     * Puesto o cargo del empleado.
     * 
     * <p>Ejemplos: Gerente, Vendedor, Administrativo, etc.</p>
     */
    @DisplaySize(30)
    @Capitalizar
    @ReadOnly(forViews = "Simple")
    @LabelFormat(forViews = "simple", value = LabelFormatType.SMALL)
    @Column(length = 50)
    private String puesto;

    
    /**
     * Fecha de inicio de actividades laborales.
     * 
     * <p>Se usa para calcular la antigüedad laboral mediante {@link #getAntiguedadLaboral()}</p>
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
     * <p>Determina la ubicación física de trabajo y se usa para:</p>
     * <ul>
     *   <li>Validación de geolocalización en registros</li>
     *   <li>Reportes por sucursal</li>
     *   <li>Asignación de turnos específicos</li>
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
     * <p>Acepta formatos de imagen con tamaño máximo de 200KB.</p>
     */
    @ReadOnly(forViews = "Simple")
    @LabelFormat(LabelFormatType.NO_LABEL)
    @File(acceptFileTypes = "image/*", maxFileSizeInKb = 200)
    @Column(length = 32)
    private String foto;

    /**
     * Archivos de documentación personal (contratos, certificados, etc.) con tamaño máximo de 200KB.
     */
    @Files(maxFileSizeInKb = 200)
    @Column(length = 32)
    private String documentacionPersonal;

    /**
     * Obtiene todos los eventos del empleado para el calendario anual.
     * 
     * <p>Incluye feriados, licencias y auditorías diarias.</p>
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
     * <p>Solo muestra licencias cuya fecha de inicio sea del año en curso
     * (filtro automático por Hibernate @Where).</p>
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
     * <p>Para cada tipo de licencia muestra total de días utilizados
     * y días restantes disponibles.</p>
     * 
     * @return Colección de resúmenes por tipo de licencia
     * @see LicenciaResumenPorTipo
     */
    @NoCreate @SimpleList
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

                totalDias.merge(tipo, l.getDias(), Integer::sum);

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

    /**
     * Valor de la hora normal de trabajo.
     * 
     * <p>Se usa como base para calcular:</p>
     * <ul>
     *   <li>Horas extras (con {@link #porcentajeHoraExtra})</li>
     *   <li>Horas especiales (con {@link #porcentajeHoraEspecial})</li>
     * </ul>
     */
    @Money
    private BigDecimal valorHora;

    /**
     * Porcentaje de bonificación para horas extras.
     * 
     * <p>Ejemplo: 50.0 = 50% adicional sobre {@link #valorHora}</p>
     * <p>El valor total se calcula en {@link #getValorHoraExtra()}</p>
     */
    @Digits(integer = 3, fraction = 1)
    @Min(0)
    @Max(100)
    private BigDecimal porcentajeHoraExtra;
    
    /**
     * Calcula el valor de la hora extra.
     * 
     * <p>Fórmula: valorHora + (valorHora × porcentajeHoraExtra / 100)</p>
     * 
     * @return Valor hora con bonificación extra, o ZERO si faltan datos
     */
    @Label
    @Depends("valorHora, porcentajeHoraExtra")
    public BigDecimal getValorHoraExtra() {
        if (valorHora != null && porcentajeHoraExtra != null) {
            BigDecimal adicional = valorHora.multiply(porcentajeHoraExtra)
                    .divide(BigDecimal.valueOf(100));
            return valorHora.add(adicional);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Porcentaje de bonificación para horas especiales (feriados, días no laborales).
     * 
     * <p>Ejemplo: 100.0 = 100% adicional sobre {@link #valorHora}</p>
     * <p>El valor total se calcula en {@link #getValorHoraEspecial()}</p>
     */
    @Digits(integer = 3, fraction = 1)
    @Min(0)
    @Max(100)
    private BigDecimal porcentajeHoraEspecial;

    /**
     * Calcula el valor de la hora especial (feriados, días no laborales).
     * 
     * <p>Fórmula: valorHora + (valorHora × porcentajeHoraEspecial / 100)</p>
     * 
     * @return Valor hora con bonificación especial, o ZERO si faltan datos
     */
    @Label
    @Depends("valorHora, porcentajeHoraEspecial")
    public BigDecimal getValorHoraEspecial() {
        if (valorHora != null && porcentajeHoraEspecial != null) {
            BigDecimal adicional = valorHora.multiply(porcentajeHoraEspecial)
                    .divide(BigDecimal.valueOf(100));
            return valorHora.add(adicional);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calcula el valor de la hora aplicando la bonificación del turno si existe.
     * 
     * @param turno Turno para el cual calcular el valor hora
     * @return Valor hora base + bonificación del turno
     */
    @Transient
    public BigDecimal getValorHoraTurno(TurnosHorarios turno) {
        if (valorHora == null) {
            return BigDecimal.ZERO;
        }

        if (turno == null || turno.getPorcentajeBonificacion() == null ||
                turno.getPorcentajeBonificacion().compareTo(BigDecimal.ZERO) == 0) {
            return valorHora;
        }

        // Mismo formato que getValorHoraExtra: dividir por 100
        BigDecimal bonificacion = valorHora.multiply(turno.getPorcentajeBonificacion())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return valorHora.add(bonificacion);
    }

    
    /**
     * Notas/observaciones generales sobre el empleado.
     * 
     * <p>Usa el formato Discussion de OpenXava para comentarios colaborativos.</p>
     */
    @Discussion
    private String nota;

    /**
     * Notas personales sobre el empleado (texto libre).
     * 
     * <p>Campo de texto sin formato para observaciones adicionales.</p>
     */
    @TextArea
    private String notasPersonale;

    // =================== NOTAS DE DESEMPEÑO ===================

    /**
     * Colección de notas de desempeño del empleado.
     * 
     * <p>Cada nota incluye calificación, contenido y autor.
     * Se usa para calcular {@link #getPromedioDesempeno()} y 
     * {@link #getEvaluacionDesempeno()}.</p>
     * 
     * @see NotaDesempeno
     */
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("fechaHora, calificacion, contenido, autor")
    @OrderBy("fechaHora DESC")
    private Collection<NotaDesempeno> notasDesempeno = new ArrayList<>();

    /**
     * Calcula el promedio de calificaciones de desempeño.
     * 
     * @return Promedio de calificaciones (0.0 a 3.0), o 0.0 si no hay notas
     */
    @Transient
    @Depends("notasDesempeno")
    public double getPromedioDesempeno() {
        if (notasDesempeno == null || notasDesempeno.isEmpty()) {
            return 0.0;
        }
        double suma = notasDesempeno.stream()
                .mapToInt(n -> n.getCalificacion().getPeso())
                .sum();
        return suma / notasDesempeno.size();
    }

    /**
     * Obtiene la evaluación textual del desempeño.
     * 
     * <p>Criterios:</p>
     * <ul>
     *   <li>≥ 2.5: "Excelente"</li>
     *   <li>≥ 2.0: "Bueno"</li>
     *   <li>≥ 1.5: "Regular"</li>
     *   <li>< 1.5: "Requiere Mejora"</li>
     * </ul>
     * 
     * @return Evaluación textual basada en promedio
     */
    @Transient
    @Depends("notasDesempeno")
    public String getEvaluacionDesempeno() {
        double promedio = getPromedioDesempeno();
        if (promedio >= 2.5)
            return "Excelente";
        if (promedio >= 2.0)
            return "Bueno";
        if (promedio >= 1.5)
            return "Regular";
        return "Requiere Mejora";
    }

    
    /**
     * Colección de jornadas asignadas al empleado.
     * 
     * <p>Cada {@link JornadaAsignada} vincula un {@link TurnosHorarios} con un rango de fechas,
     * permitiendo:</p>
     * <ul>
     *   <li>Turnos rotativos (sin fecha fin)</li>
     *   <li>Turnos puntuales (con fecha inicio y fin)</li>
     *   <li>Múltiples turnos simultáneos</li>
     * </ul>
     * 
     * <p><b>Importante:</b> Los cambios en esta colección pueden perderse si no se persisten
     * correctamente. Ver issue relacionado en el código.</p>
     * 
     * @see JornadaAsignada
     * @see TurnosHorarios
     * @see #getTurnoParaFecha(LocalDate)
     * @see #getTurnosParaFecha(LocalDate)
     */
    @ElementCollection
    @ListProperties("turno.codigo, turno.detalleJornadaHoras, fechaInicio, fechaFin")
    @OrderBy("fechaInicio")
    private List<JornadaAsignada> jornadasAsignadas = new ArrayList<>();

    /**
     * Fecha de inicio para filtros de informes/dashboard.
     * 
     * <p>Por defecto: primer día del mes actual.</p>
     * 
     * @see ActualizarDashboardAction
     */
    @Transient
    @OnChange(ActualizarDashboardAction.class)
    public LocalDate desde;

    /**
     * Obtiene la fecha de inicio para filtros de informes.
     * 
     * @return Fecha desde configurada, o primer día del mes actual si es null
     */
    @Depends("inicioActividades, desde")
    public LocalDate getDesde() {
        if (desde == null) {
            return LocalDate.now().withDayOfMonth(1);
        }
        return desde;
    }

    /**
     * Fecha de fin para filtros de informes/dashboard.
     * 
     * <p>Por defecto: fecha actual.</p>
     * 
     * @see ActualizarDashboardAction
     */
    @Transient
    @OnChange(ActualizarDashboardAction.class)
    public LocalDate hasta;

    /**
     * Obtiene la fecha de fin para filtros de informes.
     * 
     * @return Fecha hasta configurada, o fecha actual si es null
     */
    @Depends("hasta")
    public LocalDate getHasta() {
        if (hasta == null) {
            return LocalDate.now();
        }
        return hasta;
    }

    // =============================================================================================
    /**
     * Obtiene el turno principal asignado para una fecha específica.
     * 
     * <p>Este método aplica la siguiente lógica de prioridad:</p>
     * <ol>
     *   <li><b>Jornadas puntuales:</b> Busca jornadas con fecha fin explícita que incluyan la fecha</li>
     *   <li><b>Rotaciones activas:</b> Si no hay jornadas puntuales, busca rotaciones sin fecha fin</li>
     *   <li><b>Rotación semanal:</b> Si hay múltiples rotaciones, aplica lógica de rotación por semanas</li>
     * </ol>
     * 
     * <p><b>Nota:</b> Este método retorna solo UN turno. Para obtener todos los turnos
     * aplicables (en caso de múltiples asignaciones), usar {@link #getTurnosParaFecha(LocalDate)}</p>
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

        // 1. Priorizar jornadas puntuales (con fecha fin explícita y válida)
        Optional<JornadaAsignada> jornadaFija = jornadasAsignadas.stream()
                .filter(j -> j.getFechaFin() != null &&
                        !fecha.isBefore(j.getFechaInicio()) &&
                        !fecha.isAfter(j.getFechaFin()))
                .findFirst();

        if (jornadaFija.isPresent()) {
            return jornadaFija.get().getTurno();
        }

        // 2. Buscar rotaciones activas (fechaFin == null o posterior)
        List<JornadaAsignada> rotativas = jornadasAsignadas.stream()
                .filter(j -> (j.getFechaFin() == null || !fecha.isAfter(j.getFechaFin())) &&
                        !fecha.isBefore(j.getFechaInicio()))
                .sorted(Comparator.comparing(JornadaAsignada::getFechaInicio))
                .collect(Collectors.toList());

        if (rotativas.isEmpty())
            return null;
        if (rotativas.size() == 1)
            return rotativas.get(0).getTurno();

        // 3. Aplicar rotación semanal
        LocalDate lunesBase = rotativas.get(0).getFechaInicio().with(DayOfWeek.MONDAY);
        LocalDate lunesActual = fecha.with(DayOfWeek.MONDAY);

        long semanasTranscurridas = ChronoUnit.WEEKS.between(lunesBase, lunesActual);
        int indice = (int) (semanasTranscurridas % rotativas.size());

        return rotativas.get(indice).getTurno();
    }

    /**
     * Obtiene TODOS los turnos aplicables para una fecha dada.
     * 
     * <p>A diferencia de {@link #getTurnoParaFecha(LocalDate)}, este método retorna
     * una lista con todos los turnos que aplican para la fecha, permitiendo manejar
     * casos donde un empleado tiene múltiples turnos en el mismo día.</p>
     * 
     * <p><b>Casos de uso:</b></p>
     * <ul>
     *   <li>Turno normal + guardia especial</li>
     *   <li>Múltiples turnos rotativos</li>
     *   <li>Turnos puntuales superpuestos</li>
     * </ul>
     * 
     * <p><b>Lógica aplicada:</b></p>
     * <ol>
     *   <li>Busca jornadas puntuales (con fecha fin) que incluyan la fecha</li>
     *   <li>Busca jornadas rotativas (sin fecha fin) activas en la fecha</li>
     *   <li>Para cada turno encontrado, verifica si es laboral para el día de la semana</li>
     *   <li>Retorna todos los turnos que cumplan las condiciones</li>
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
     * <p>Para cada mes del año actual cuenta: jornadas completas,
     * incompletas, licencias, ausencias y feriados trabajados.</p>
     * 
     * @return Colección de resúmenes mensuales para gráfico de barras
     * @see ResumenAnualGrafico
     */
    @Transient @ReadOnly
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
    // DASHBOARD DE INFORMES - MÉTRICAS VISUALES
    // ===============================================================================================

    // ========== MÉTRICAS @LargeDisplay ==========

    /**
     * Cuenta el total de días trabajados (evaluación COMPLETA) en el rango de fechas.
     * 
     * @return Número de días con jornada completa
     * @see EvaluacionJornada#COMPLETA
     */
    @Depends("desde, hasta")
    @LargeDisplay(icon = "calendar-check")
    public int getTotalDiasTrabajados() {
        if (getId() == null)
            return 0; // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return 0;

        Long count = (Long) XPersistence.getManager()
                .createQuery("SELECT COUNT(a) FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta " +
                        "AND a.evaluacion = :evaluacion")
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .setParameter("evaluacion", EvaluacionJornada.COMPLETA)
                .getSingleResult();

        return count != null ? count.intValue() : 0;
    }

    /**
     * Calcula la tasa de asistencia como porcentaje.
     * 
     * <p>Fórmula: (Días trabajados / Días laborales esperados) × 100</p>
     * 
     * @return Tasa de asistencia formateada (ej: "95.5%")
     */
    @Depends("desde, hasta")
    @LargeDisplay(icon = "percent")
    public String getTasaAsistencia() {
        if (getId() == null)
            return "0%"; // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return "0%";

        // Días trabajados (COMPLETA)
        int diasTrabajados = getTotalDiasTrabajados();

        // Días laborales esperados (excluyendo licencias y feriados)
        Long diasEsperados = (Long) XPersistence.getManager()
                .createQuery("SELECT COUNT(a) FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta " +
                        "AND a.evaluacion NOT IN (:feriado, :noLaboral, :sinTurno)")
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .setParameter("feriado", EvaluacionJornada.FERIADO)
                .setParameter("noLaboral", EvaluacionJornada.DIA_NO_LABORAL)
                .setParameter("sinTurno", EvaluacionJornada.SIN_TURNO_ASIGNADO)
                .getSingleResult();

        if (diasEsperados == null || diasEsperados == 0)
            return "0%";

        double tasa = (diasTrabajados * 100.0) / diasEsperados;
        return String.format("%.1f%%", tasa);
    }

    /**
     * Suma total de horas trabajadas (normales + extras + especiales) en el rango.
     * 
     * @return Total de horas en formato "HH:MM"
     */
    @Depends("desde, hasta")
    @LargeDisplay(icon = "clock-outline")
    public String getTotalHorasTrabajadasInformes() {
        if (getId() == null)
            return "0:00"; // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return "0:00";

        List<AuditoriaRegistros> registros = XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta", AuditoriaRegistros.class)
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .getResultList();

        int totalMinutos = 0;
        for (AuditoriaRegistros a : registros) {
            // Sumar minutos normales
            String horasNormales = a.getHorasTrabajadasTurno();
            if (horasNormales != null && !horasNormales.equals("00:00")) {
                totalMinutos += convertirHHMMaMinutos(horasNormales);
            }

            // Sumar minutos extras
            String horasExtras = a.getHorasExtras();
            if (horasExtras != null && !horasExtras.equals("00:00")) {
                totalMinutos += convertirHHMMaMinutos(horasExtras);
            }

            // Sumar minutos especiales
            String horasEspeciales = a.getHorasEspeciales();
            if (horasEspeciales != null && !horasEspeciales.equals("00:00")) {
                totalMinutos += convertirHHMMaMinutos(horasEspeciales);
            }
        }

        int horas = totalMinutos / 60;
        int minutos = totalMinutos % 60;
        return String.format("%d:%02d", horas, minutos);
    }

    /**
     * Cuenta la cantidad de llegadas tarde en el rango de fechas.
     * 
     * <p>Se considera llegada tarde cuando la hora de entrada supera
     * la hora esperada más la tolerancia configurada.</p>
     * 
     * @return Número de llegadas tarde
     */
    @Depends("desde, hasta")
    @LargeDisplay(icon = "clock-alert")
    public int getCantidadLlegadasTardeInformes() {
        if (getId() == null)
            return 0; // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return 0;

        List<AuditoriaRegistros> registros = XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta " +
                        "AND a.evaluacion IN (:completa, :incompleta)", AuditoriaRegistros.class)
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .setParameter("completa", EvaluacionJornada.COMPLETA)
                .setParameter("incompleta", EvaluacionJornada.INCOMPLETA)
                .getResultList();

        int llegadasTarde = 0;
        for (AuditoriaRegistros a : registros) {
            if (a.getRegistros() != null && !a.getRegistros().isEmpty() &&
                    a.getHoraEsperadaEntrada() != null) {

                // Buscar primera entrada
                Optional<LocalTime> primeraEntrada = a.getRegistros().stream()
                        .filter(r -> r.getTipoMovimiento() == TipoMovimiento.ENTRADA)
                        .map(ColeccionRegistros::getHora)
                        .min(LocalTime::compareTo);

                if (primeraEntrada.isPresent()) {
                    LocalTime horaEsperada = a.getHoraEsperadaEntrada();
                    LocalTime horaReal = primeraEntrada.get();
                    int tolerancia = a.getToleranciaMinutos();

                    // Calcular minutos de diferencia
                    int minutosRetraso = (int) ChronoUnit.MINUTES.between(horaEsperada, horaReal);

                    if (minutosRetraso > tolerancia) {
                        llegadasTarde++;
                    }
                }
            }
        }

        return llegadasTarde;
    }

    /**
     * Cuenta los días de licencia utilizados en el rango.
     * 
     * @return Número de días con evaluación LICENCIA
     */
    @Depends("desde, hasta")
    @LargeDisplay(icon = "calendar-remove")
    public int getDiasLicenciaUtilizados() {
        if (getId() == null)
            return 0; // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return 0;

        Long count = (Long) XPersistence.getManager()
                .createQuery("SELECT COUNT(a) FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta " +
                        "AND a.evaluacion = :evaluacion")
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .setParameter("evaluacion", EvaluacionJornada.LICENCIA)
                .getSingleResult();

        return count != null ? count.intValue() : 0;
    }

    // ========== GRÁFICOS @Chart ==========

    /**
     * Evolución mensual de asistencia (gráfico de barras).
     * 
     * <p>Para cada mes cuenta: días trabajados, licencias y ausencias.</p>
     * 
     * @return Colección de resúmenes mensuales
     * @see ResumenMensualAsistencia
     */
    @Chart(type = ChartType.BAR, labelProperties = "mes", dataProperties = "diasTrabajados, diasLicencia, diasAusente")
    @ListProperties("mes, diasTrabajados, diasLicencia, diasAusente")
    public Collection<ResumenMensualAsistencia> getEvolucionMensualAsistencia() {
        if (getId() == null)
            return Collections.emptyList(); // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return Collections.emptyList();

        List<AuditoriaRegistros> registros = XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta " +
                        "ORDER BY a.fecha ASC", AuditoriaRegistros.class)
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .getResultList();

        // Agrupar por mes
        Map<YearMonth, ResumenMensualAsistencia> porMes = new LinkedHashMap<>();
        Locale esAR = new Locale("es", "AR");

        for (AuditoriaRegistros a : registros) {
            if (a.getFecha() == null || a.getEvaluacion() == null)
                continue;

            YearMonth ym = YearMonth.from(a.getFecha());

            if (!porMes.containsKey(ym)) {
                String etiqueta = ym.getMonth().getDisplayName(TextStyle.SHORT, esAR);
                etiqueta = etiqueta.substring(0, 1).toUpperCase(esAR) + etiqueta.substring(1) + " " + ym.getYear();
                porMes.put(ym, new ResumenMensualAsistencia(etiqueta, 0, 0, 0));
            }

            ResumenMensualAsistencia resumen = porMes.get(ym);

            switch (a.getEvaluacion()) {
                case COMPLETA:
                    resumen.setDiasTrabajados(resumen.getDiasTrabajados() + 1);
                    break;
                case LICENCIA:
                    resumen.setDiasLicencia(resumen.getDiasLicencia() + 1);
                    break;
                case AUSENTE:
                    resumen.setDiasAusente(resumen.getDiasAusente() + 1);
                    break;
                default:
                    break;
            }
        }

        return porMes.values();
    }

    /**
     * Distribución de tipos de jornada (gráfico circular).
     * 
     * <p>Cuenta la cantidad de cada tipo de evaluación en el rango.</p>
     * 
     * @return Colección de distribución por tipo
     * @see DistribucionJornada
     */
    @Chart(type = ChartType.PIE)
    @ListProperties("tipoJornada, cantidad")
    public Collection<DistribucionJornada> getDistribucionTiposJornada() {
        if (getId() == null)
            return Collections.emptyList(); // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return Collections.emptyList();

        List<AuditoriaRegistros> registros = XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta", AuditoriaRegistros.class)
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .getResultList();

        // Contar por tipo de evaluación
        Map<String, Integer> conteo = new HashMap<>();

        for (AuditoriaRegistros a : registros) {
            if (a.getEvaluacion() == null)
                continue;

            String tipo = a.getEvaluacion().toString();
            conteo.put(tipo, conteo.getOrDefault(tipo, 0) + 1);
        }

        // Convertir a lista de DistribucionJornada
        List<DistribucionJornada> resultado = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            resultado.add(new DistribucionJornada(entry.getKey(), entry.getValue()));
        }

        // Ordenar por cantidad descendente
        resultado.sort((a, b) -> Integer.compare(b.getCantidad(), a.getCantidad()));

        return resultado;
    }

    /**
     * Horas trabajadas por mes (normales, extras, especiales).
     */
    @Chart(type = ChartType.BAR, labelProperties = "mes", dataProperties = "horasNormales, horasExtras, horasEspeciales")
    @ListProperties("mes, horasNormales, horasExtras, horasEspeciales")
    public Collection<ResumenHorasMensual> getHorasPorMes() {
        if (getId() == null)
            return Collections.emptyList(); // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return Collections.emptyList();

        List<AuditoriaRegistros> registros = XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta " +
                        "ORDER BY a.fecha ASC", AuditoriaRegistros.class)
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .getResultList();

        // Agrupar por mes
        Map<YearMonth, ResumenHorasMensual> porMes = new LinkedHashMap<>();
        Locale esAR = new Locale("es", "AR");

        for (AuditoriaRegistros a : registros) {
            if (a.getFecha() == null)
                continue;

            YearMonth ym = YearMonth.from(a.getFecha());

            if (!porMes.containsKey(ym)) {
                String etiqueta = ym.getMonth().getDisplayName(TextStyle.SHORT, esAR);
                etiqueta = etiqueta.substring(0, 1).toUpperCase(esAR) + etiqueta.substring(1) + " " + ym.getYear();
                porMes.put(ym, new ResumenHorasMensual(etiqueta, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            }

            ResumenHorasMensual resumen = porMes.get(ym);

            // Sumar horas normales
            String horasNormales = a.getHorasTrabajadasTurno();
            if (horasNormales != null && !horasNormales.equals("00:00")) {
                BigDecimal horas = convertirHHMMaBigDecimal(horasNormales);
                resumen.setHorasNormales(resumen.getHorasNormales().add(horas));
            }

            // Sumar horas extras
            String horasExtras = a.getHorasExtras();
            if (horasExtras != null && !horasExtras.equals("00:00")) {
                BigDecimal horas = convertirHHMMaBigDecimal(horasExtras);
                resumen.setHorasExtras(resumen.getHorasExtras().add(horas));
            }

            // Sumar horas especiales
            String horasEspeciales = a.getHorasEspeciales();
            if (horasEspeciales != null && !horasEspeciales.equals("00:00")) {
                BigDecimal horas = convertirHHMMaBigDecimal(horasEspeciales);
                resumen.setHorasEspeciales(resumen.getHorasEspeciales().add(horas));
            }
        }

        return porMes.values();
    }

    // ========== LISTAS @SimpleList ==========

    /**
     * Top 10 días con más horas extras.
     * 
     * @return Lista ordenada de días con más horas extras
     * @see DetalleHorasExtras
     */
    @SimpleList
    @ListProperties("fecha, diaSemana, turnoNombre, horasExtras, montoExtras")
    public Collection<DetalleHorasExtras> getTopDiasHorasExtras() {
        if (getId() == null)
            return Collections.emptyList(); // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return Collections.emptyList();

        List<AuditoriaRegistros> registros = XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta " +
                        "ORDER BY a.minutosExtras DESC", AuditoriaRegistros.class)
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .setMaxResults(10)
                .getResultList();

        List<DetalleHorasExtras> resultado = new ArrayList<>();

        for (AuditoriaRegistros a : registros) {
            if (a.getMinutosExtras() > 0) {
                String diaSemana = a.getDiaSemana();
                String turno = a.getNombreTurno() != null ? a.getNombreTurno().toString() : "N/D";
                String horasExtras = a.getHorasExtras();
                BigDecimal monto = a.getTotalHorasExtras();

                resultado.add(new DetalleHorasExtras(
                        a.getFecha(),
                        diaSemana,
                        turno,
                        horasExtras,
                        monto));
            }
        }

        return resultado;
    }

    /**
     * Registro detallado de todas las llegadas tarde en el período.
     * 
     * <p>Incluye fecha, hora esperada, hora real y minutos de retraso.</p>
     * 
     * @return Lista de llegadas tarde con detalles
     * @see DetalleLlegadaTarde
     */
    @SimpleList
    @ListProperties("fecha, horaEsperada, horaReal, minutosRetraso, justificado")
    public Collection<DetalleLlegadaTarde> getRegistroLlegadasTarde() {
        if (getId() == null)
            return Collections.emptyList(); // Entidad no persistida

        LocalDate fechaDesde = getDesde();
        LocalDate fechaHasta = getHasta();

        if (fechaDesde == null || fechaHasta == null)
            return Collections.emptyList();

        List<AuditoriaRegistros> registros = XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :desde AND :hasta " +
                        "AND a.evaluacion IN (:completa, :incompleta) " +
                        "ORDER BY a.fecha DESC", AuditoriaRegistros.class)
                .setParameter("emp", this)
                .setParameter("desde", fechaDesde)
                .setParameter("hasta", fechaHasta)
                .setParameter("completa", EvaluacionJornada.COMPLETA)
                .setParameter("incompleta", EvaluacionJornada.INCOMPLETA)
                .getResultList();

        List<DetalleLlegadaTarde> resultado = new ArrayList<>();

        for (AuditoriaRegistros a : registros) {
            if (a.getRegistros() != null && !a.getRegistros().isEmpty() &&
                    a.getHoraEsperadaEntrada() != null) {

                // Buscar primera entrada
                Optional<LocalTime> primeraEntrada = a.getRegistros().stream()
                        .filter(r -> r.getTipoMovimiento() == TipoMovimiento.ENTRADA)
                        .map(ColeccionRegistros::getHora)
                        .min(LocalTime::compareTo);

                if (primeraEntrada.isPresent()) {
                    LocalTime horaEsperada = a.getHoraEsperadaEntrada();
                    LocalTime horaReal = primeraEntrada.get();
                    int tolerancia = a.getToleranciaMinutos();

                    // Calcular minutos de retraso
                    int minutosRetraso = (int) ChronoUnit.MINUTES.between(horaEsperada, horaReal);

                    if (minutosRetraso > tolerancia) {
                        resultado.add(new DetalleLlegadaTarde(
                                a.getFecha(),
                                horaEsperada,
                                horaReal,
                                minutosRetraso,
                                a.isJustificado()));
                    }
                }
            }
        }

        return resultado;
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Convierte minutos totales a formato HH:MM.
     * 
     * <p><b>Ejemplos:</b></p>
     * <ul>
     *   <li>90 minutos → "01:30"</li>
     *   <li>480 minutos → "08:00"</li>
     *   <li>0 minutos → "00:00"</li>
     * </ul>
     * 
     * @param minutos Total de minutos a convertir
     * @return String en formato HH:MM
     */
    private int convertirHHMMaMinutos(String hhMM) {
        if (hhMM == null || hhMM.isEmpty())
            return 0;
        try {
            String[] partes = hhMM.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = Integer.parseInt(partes[1]);
            return (horas * 60) + minutos;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Convierte formato HH:MM a minutos totales.
     * 
     * <p><b>Ejemplos:</b></p>
     * <ul>
     *   <li>"01:30" → 90 minutos</li>
     *   <li>"08:00" → 480 minutos</li>
     *   <li>"00:00" → 0 minutos</li>
     * </ul>
     * 
     * @param horasMinutos String en formato HH:MM
     * @return Total de minutos
     * @throws NumberFormatException si el formato es inválido
     */
    private BigDecimal convertirHHMMaBigDecimal(String hhMM) {
        if (hhMM == null || hhMM.isEmpty())
            return BigDecimal.ZERO;
        try {
            String[] partes = hhMM.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = Integer.parseInt(partes[1]);

            BigDecimal horasDecimal = BigDecimal.valueOf(horas);
            BigDecimal minutosDecimal = BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2,
                    RoundingMode.HALF_UP);

            return horasDecimal.add(minutosDecimal);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // ===============================================================================================

    /**
     * Callback JPA antes de persistir o actualizar el empleado.
     * 
     * <p>Acciones realizadas:</p>
     * <ul>
     *   <li>Actualiza 'usuario' con getCreaUsuario()</li>
     *   <li>Actualiza 'nombreCompleto' con getApellidoNombre()</li>
     *   <li>Añade prefijo "x-" al userId si está inactivo</li>
     *   <li>Asigna coordenadas a la dirección si faltan</li>
     * </ul>
     */
    @PrePersist
    @PreUpdate
    private void preGuardar() {

        setUsuario(getCreaUsuario());
        setNombreCompleto(getApellidoNombre());
        if (Boolean.FALSE.equals(activo)) {
            if (userId != null && !userId.startsWith("x-")) {
                userId = "x-" + userId;
            }
        } else {
            // si userId empieza con "x-", y suponés que pertenecía a ese usuario,
            // podés remover el prefijo:
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

    /**
     * Callback JPA antes de eliminar el empleado.
     * 
     * <p>Elimina los comentarios de discusión asociados.</p>
     */
    @PreRemove
    private void borrarDiscusion() {
        DiscussionComment.removeForDiscussion(nota);
    }

}
