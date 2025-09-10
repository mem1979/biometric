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
@Getter @Setter
@View(members =
"nombreCompleto, turnoActivoHoy;" +
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
        "userId; creaUsuario;" +
        "contrasena; deviceId," +
    "], " +

    "funcion[" +
    	"activo;"+
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
        "jornadasAsignadas;" +
        "]; " +
"}; " +

"LICENCIAS { " +
    "licencias, licenciasResumenAnual; licenciasGraficoAnual; " +
"}; " +
    

"informes { " +
    "desde, hasta;" +
"}; " +

"Notas { " +
    "nota;" +
"}")

@View(name= "VerMapa",
members = "direccion"
)

@View(name= "VerCalendario",
members = "eventos"
)

@View(name = "simple", members = "nombreCompleto, sucursal, puesto;")

@Tab(editors = "List",
	 properties = "foto, nombreCompleto, userId, sucursal.nombre, puesto, activo",
	 defaultOrder = "${activo} desc, ${nombreCompleto} asc",
	 rowStyles = {@RowStyle(style = "empleadoInactivo", property = "activo", value = "false")}
	)


public class Personal extends Identifiable {
	
	@DefaultValueCalculator(TrueCalculator.class)
	@OnChange(PersonalOnChangeActivoAction.class)
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
	private boolean activo;
	
	@Required
	@SearchKey
	@Column(length = 10, unique = true)
	@DefaultValueCalculator(GeneradorCodigoUserIdCalculator.class)
	private String userId;

    @ReadOnly  @Password
    @Column(length = 20)
    @Action(value="Personal.borrarDeviceId", alwaysEnabled=true)
    private String deviceId;

    @Column(length = 20)
    @Mayuscula
    private String usuario;

    @Mayuscula
    @Depends("nombres, apellido, userId")
    public String getCreaUsuario() {
        if ((nombres == null || nombres.isEmpty()) || (apellido == null || apellido.isEmpty()) ) {
            return "N/D";
        }
        String inicialNombre = nombres.trim().substring(0, 1);
        String apellidoCompleto = apellido.trim();
        return inicialNombre + apellidoCompleto + "@" + userId;
    }

    @Password @ReadOnly
    @Column(length = 20)
    @Action(value="Personal.borrarContrasena", alwaysEnabled=true)
    @DefaultValueCalculator(CalculadorPassword.class)
    private String contrasena;

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
        if (fechaNacimiento == null) return "";
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
    
   	@NoCreate @NoModify 
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
    @ReferenceView ("simple")
    @OneToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL) // 'CascadeType.ALL' permite que las operaciones como persist y remove se propaguen a la entidad 'Dni'
    @JoinColumn(name = "dni_id") // Crea una columna 'dni_id' que almacena la clave primaria de 'Dni'
    private Dni dni;
    
    @Mask("00-00000000-0")
	private String cuil; // Código Único de Identificación Laboral

    @Embedded
    @ReferenceView(forViews="VerMapa", value="VerMapa")
    private Direccion direccion;


    @Embedded
    private DatosContacto contacto;

    @DisplaySize(30)
    @Capitalizar
    @ReadOnly(forViews="Simple")
    @LabelFormat(forViews="simple" , value = LabelFormatType.SMALL)
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
        if (anios > 0) sb.append(anios).append(anios == 1 ? " año" : " años");
        if (meses > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(meses).append(meses == 1 ? " mes" : " meses");
        }
        if (dias > 0) {
            if (sb.length() > 0) sb.append(" y ");
            sb.append(dias).append(dias == 1 ? " dia" : " dias");
        }

        return sb.length() > 0 ? sb.toString() : "Menos de un dia";
    }

    
    @Capitalizar
    @LabelFormat(forViews="simple" , value =  LabelFormatType.SMALL)
    @DescriptionsList 
    @ManyToOne(fetch=FetchType.LAZY)
    private Sucursales sucursal;

    @ReadOnly(forViews="Simple")
    @LabelFormat(LabelFormatType.NO_LABEL)
    @File(acceptFileTypes="image/*", maxFileSizeInKb=200)
    @Column(length=32)
    private String foto;
    
    @Files( maxFileSizeInKb=200)
    @Column(length=32)
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

        /* 3) Auditoría diaria: COM/INC/AUS + FERIADO_TRABAJADO (NO LICENCIA para evitar duplicados) */
        int anio = java.time.LocalDate.now().getYear();
        java.time.LocalDate desde = java.time.LocalDate.of(anio, 1, 1);
        java.time.LocalDate hasta = java.time.LocalDate.of(anio, 12, 31);

        List<EvaluacionJornada> evs = java.util.Arrays.asList(
            EvaluacionJornada.COMPLETA,
            EvaluacionJornada.INCOMPLETA,
            EvaluacionJornada.AUSENTE,
            EvaluacionJornada.FERIADO_TRABAJADO
        );

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
            if (a.getFecha() == null || a.getEvaluacion() == null) continue;
            switch (a.getEvaluacion()) {
                case COMPLETA:            out.add(DtoLicenciasFeriados.ofCompleta(a));          break;
                case INCOMPLETA:          out.add(DtoLicenciasFeriados.ofIncompleta(a));        break;
                case AUSENTE:             out.add(DtoLicenciasFeriados.ofAusente(a));           break;
                case FERIADO_TRABAJADO:   out.add(DtoLicenciasFeriados.ofFeriadoTrabajado(a));  break;
                default: break; // LICENCIA/FERIADO “común” no se generan aquí
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
    @ListProperties("tipo, fechaInicio, fechaFin, dias")
    @OrderBy("fechaInicio desc")
    @org.hibernate.annotations.Where(clause = "YEAR(fechaInicio) = YEAR(CURDATE())")
    private Collection<Licencia> licencias;
  
    
    @NoCreate
    @SimpleList
    public Collection<LicenciaResumenPorTipo> getLicenciasResumenAnual() {
        Map<TipoLicenciaAR, Integer> totalDias = new TreeMap<>();
        Map<TipoLicenciaAR, Licencia> ultimaLicenciaPorTipo = new TreeMap<>();
        int anioActual = LocalDate.now().getYear();

        if (getLicencias() == null || getLicencias().isEmpty()) return Collections.emptyList();

        // 1. Recorrer licencias del año actual
        for (Licencia l : getLicencias()) {
            if (l.getFechaInicio() != null && l.getFechaInicio().getYear() == anioActual) {
                TipoLicenciaAR tipo = l.getTipo();

                totalDias.merge(tipo, l.getDias(), Integer::sum);

                // Mantener la última licencia (por fecha)
                ultimaLicenciaPorTipo.compute(tipo, (k, licenciaAnterior) -> {
                    if (licenciaAnterior == null) return l;
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

    @Digits(integer=3, fraction=1)
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

    @Digits(integer=3, fraction=1)
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

    
    @Discussion
    @Column(length=32)
    private String nota;
    
    
    @ElementCollection
    @ListProperties("turno.codigo, turno.detalleJornadaHoras, fechaInicio, fechaFin")
    @OrderBy("fechaInicio")
    private List<JornadaAsignada> jornadasAsignadas = new ArrayList<>();
    


    @Transient
    public LocalDate desde;
    
    @Depends("inicioActividades, desde")
    public LocalDate getDesde() {
        return LocalDate.now().withDayOfMonth(1);
    }
    
    @Transient
    public LocalDate hasta;
    
    @Depends("hasta")
    public LocalDate getHasta() {
        return LocalDate.now();
    }
    
    
    //=============================================================================================  
 
    public TurnosHorarios getTurnoParaFecha(LocalDate fecha) {
        if (jornadasAsignadas == null || jornadasAsignadas.isEmpty()) return null;

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

        if (rotativas.isEmpty()) return null;
        if (rotativas.size() == 1) return rotativas.get(0).getTurno();

        // 3. Aplicar rotación semanal
        LocalDate lunesBase = rotativas.get(0).getFechaInicio().with(DayOfWeek.MONDAY);
        LocalDate lunesActual = fecha.with(DayOfWeek.MONDAY);

        long semanasTranscurridas = ChronoUnit.WEEKS.between(lunesBase, lunesActual);
        int indice = (int) (semanasTranscurridas % rotativas.size());

        return rotativas.get(indice).getTurno();
    }
    
    //=============================================================================================
    
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
        if (fecha == null) return "Fecha no especificada";
        
        TurnosHorarios turno = getTurnoParaFecha(fecha);
        if (turno == null) return "Sin turno asignado";

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
    
    
	//=============================================================================================

    @Chart(
    		  type = ChartType.BAR,
    		  labelProperties = "mesEtiqueta",
    		  dataProperties  = "completas, incompletas, licencias, ausentes, feriadosTrabajados"
    		)
    		@ListProperties("mesEtiqueta, completas, incompletas, licencias, ausentes, feriadosTrabajados")
    	
    		@Transient @ReadOnly
    		public Collection<ResumenAnualGrafico> getLicenciasGraficoAnual() {
    		    final int anio = LocalDate.now().getYear();
    		    final LocalDate desde = LocalDate.of(anio, 1, 1);
    		    final LocalDate hasta = LocalDate.of(anio, 12, 31);

    		    EntityManager em = XPersistence.getManager();
    		    List<AuditoriaRegistros> registros = em.createQuery(
    		        "select a from AuditoriaRegistros a " +
    		        "where a.empleado = :emp and a.fecha between :d and :h",
    		        AuditoriaRegistros.class)
    		        .setParameter("emp", this)
    		        .setParameter("d", desde)   // usa java.sql.Date.valueOf(...) si tu campo es Date
    		        .setParameter("h", hasta)
    		        .getResultList();

    		    // Inicializar meses
    		    Locale esAR = new Locale("es","AR");
    		    Map<YearMonth, ResumenAnualGrafico> porMes = new LinkedHashMap<>();
    		    for (int m = 1; m <= 12; m++) {
    		        YearMonth ym = YearMonth.of(anio, m);
    		        String et = ym.getMonth().getDisplayName(TextStyle.SHORT, esAR);
    		        et = et.substring(0,1).toUpperCase(esAR) + et.substring(1);
    		        porMes.put(ym, new ResumenAnualGrafico(et, 0,0,0,0,0));
    		    }

    		    for (AuditoriaRegistros a : registros) {
    		        if (a.getFecha() == null || a.getEvaluacion() == null) continue;
    		        YearMonth ym = YearMonth.from(a.getFecha()); // adapta si usás java.util.Date
    		        ResumenAnualGrafico r = porMes.get(ym);
    		        if (r == null) continue;

    		        switch (a.getEvaluacion()) {
    		            case COMPLETA:            r.setCompletas(r.getCompletas()+1);                 break;
    		            case INCOMPLETA:          r.setIncompletas(r.getIncompletas()+1);             break;
    		            case LICENCIA:            r.setLicencias(r.getLicencias()+1);                 break;
    		            case AUSENTE:             r.setAusentes(r.getAusentes()+1);                   break;
    		            case FERIADO_TRABAJADO:   r.setFeriadosTrabajados(r.getFeriadosTrabajados()+1); break;
    		            case FERIADO:             /* NO contar */                                      break;
    		            default: break;
    		        }
    		    }
    		    return porMes.values();
    		}

    //===============================================================================================
    
   

    @PrePersist @PreUpdate
    private void preGuardar() {
    	
        setUsuario(getCreaUsuario());
        setNombreCompleto(getApellidoNombre());
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
