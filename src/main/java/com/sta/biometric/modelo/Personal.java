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
        "documentacionPersonal;" +
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
        "  notasPersonale; nota;" +
        "}")

@View(name = "VerMapa", members = "direccion")

@View(name = "VerCalendario", members = "eventos")

@View(name = "simple", members = "nombreCompleto, sucursal, puesto;")

@Tab(editors = "List", properties = "foto, nombreCompleto, userId, sucursal.nombre, puesto, activo", defaultOrder = "${activo} desc, ${nombreCompleto} asc", rowStyles = {
        @RowStyle(style = "empleadoInactivo", property = "activo", value = "false") })

public class Personal extends Identifiable {

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

    @ReadOnly // @Password
    @Column(length = 20)
    @Action(value = "Personal.borrarDeviceId", alwaysEnabled = true)
    private String deviceId;

    @Column(length = 20)
    @Mayuscula
    private String usuario;

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

    @Password
    @ReadOnly
    @Column(length = 20)
    @Action(value = "Personal.borrarContrasena", alwaysEnabled = true)
    @DefaultValueCalculator(CalculadorPassword.class)
    private String contrasena;

    @OnChange(PersonalOnChangePausaAction.class)
    @DefaultValueCalculator(TrueCalculator.class)
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean aceptaPausa;

    @Capitalizar
    @Required
    @DisplaySize(40)
    private String nombres;

    @Capitalizar
    @Required
    @DisplaySize(40)
    @Column(length = 30)
    private String apellido;

    @DisplaySize(40)
    @MiLabel(medida = "extra", negrita = true, recuadro = true, icon = "account")
    private String nombreCompleto;

    @DisplaySize(40)
    @MiLabel(medida = "extra", negrita = true, recuadro = true, icon = "account-box")
    @Depends("nombres, apellido")
    public String getApellidoNombre() {
        return apellido + ", " + nombres;
    }

    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    private LocalDate fechaNacimiento;

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

    @Enumerated(EnumType.STRING)
    private EstadoCivil estadoCivil;

    @NoCreate
    @NoModify
    @DefaultValueCalculator(NacionalidadPorDefectoCalculator.class)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @DescriptionsList(descriptionProperties = "nacionalidad") // Muestra nacionalidad como texto
    private Nacionalidades nacionalidad;

    // Relación OneToOne con Dni
    @NoFrame
    @NoSearch
    @NoCreate
    @NoModify
    @AsEmbedded
    @ReferenceView("simple")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // 'CascadeType.ALL' permite que las operaciones como
                                                                 // persist y remove se propaguen a la entidad 'Dni'
    @JoinColumn(name = "dni_id") // Crea una columna 'dni_id' que almacena la clave primaria de 'Dni'
    private Dni dni;

    @Mask("00-00000000-0")
    private String cuil; // Código Único de Identificación Laboral

    @Embedded
    @ReferenceView(forViews = "VerMapa", value = "VerMapa")
    private Direccion direccion;

    @Embedded
    private DatosContacto contacto;

    @DisplaySize(30)
    @Capitalizar
    @ReadOnly(forViews = "Simple")
    @LabelFormat(forViews = "simple", value = LabelFormatType.SMALL)
    @Column(length = 50)
    private String puesto;

    @Required
    @Stereotype("FECHA")
    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    private LocalDate inicioActividades;

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

    @Capitalizar
    @LabelFormat(forViews = "simple", value = LabelFormatType.SMALL)
    @DescriptionsList
    @ManyToOne(fetch = FetchType.LAZY)
    private Sucursales sucursal;

    @ReadOnly(forViews = "Simple")
    @LabelFormat(LabelFormatType.NO_LABEL)
    @File(acceptFileTypes = "image/*", maxFileSizeInKb = 200)
    @Column(length = 32)
    private String foto;

    @Files(maxFileSizeInKb = 200)
    @Column(length = 32)
    private String documentacionPersonal;

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

    @Money
    private BigDecimal valorHora;

    @Digits(integer = 3, fraction = 1)
    @Min(0)
    @Max(100)
    private BigDecimal porcentajeHoraExtra;

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

    @Digits(integer = 3, fraction = 1)
    @Min(0)
    @Max(100)
    private BigDecimal porcentajeHoraEspecial;

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

    @Discussion
    private String nota;

    @TextArea
    private String notasPersonale;

    // =================== NOTAS DE DESEMPEÑO ===================

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("fechaHora, calificacion, contenido, autor")
    @OrderBy("fechaHora DESC")
    private Collection<NotaDesempeno> notasDesempeno = new ArrayList<>();

    @Transient
    @Depends("notasDesempeno")
    @Stereotype("MONEY")
    public double getPromedioDesempeno() {
        if (notasDesempeno == null || notasDesempeno.isEmpty()) {
            return 0.0;
        }
        double suma = notasDesempeno.stream()
                .mapToInt(n -> n.getCalificacion().getPeso())
                .sum();
        return suma / notasDesempeno.size();
    }

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

    @ElementCollection
    @ListProperties("turno.codigo, turno.detalleJornadaHoras, fechaInicio, fechaFin")
    @OrderBy("fechaInicio")
    private List<JornadaAsignada> jornadasAsignadas = new ArrayList<>();

    @Transient
    @OnChange(ActualizarDashboardAction.class)
    public LocalDate desde;

    @Depends("inicioActividades, desde")
    public LocalDate getDesde() {
        if (desde == null) {
            return LocalDate.now().withDayOfMonth(1);
        }
        return desde;
    }

    @Transient
    @OnChange(ActualizarDashboardAction.class)
    public LocalDate hasta;

    @Depends("hasta")
    public LocalDate getHasta() {
        if (hasta == null) {
            return LocalDate.now();
        }
        return hasta;
    }

    // =============================================================================================

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

    // =============================================================================================

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

    @Chart(type = ChartType.BAR, labelProperties = "mesEtiqueta", dataProperties = "completas, incompletas, licencias, ausentes, feriadosTrabajados")
    @ListProperties("mesEtiqueta, completas, incompletas, licencias, ausentes, feriadosTrabajados")

    @Transient
    @ReadOnly
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
     * Cuenta el total de días trabajados (evaluación COMPLETA) en el rango de
     * fechas.
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
     * Fórmula: (Días trabajados / Días laborales esperados) * 100
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
     * Evolución mensual de asistencia (COMPLETA, LICENCIA, AUSENTE).
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
     * Registro de todas las llegadas tarde en el período.
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
     * Convierte formato "HH:MM" a minutos totales.
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
     * Convierte formato "HH:MM" a BigDecimal de horas.
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

    @PreRemove
    private void borrarDiscusion() {
        DiscussionComment.removeForDiscussion(nota);
    }

}
