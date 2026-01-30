# Plan de Implementación V2: Entidad ContratoLaboral

## 📋 Resumen Ejecutivo

Este plan detalla la creación de la entidad `ContratoLaboral` que centraliza los parámetros laborales y económicos del empleado. La principal innovación es el **cálculo dinámico de horas mensuales** basado en las jornadas asignadas al empleado (turnos fijos, rotativos, nocturnos), lo que permite derivar automáticamente el valor hora desde un sueldo mensual acordado.

---

## 🎯 Decisiones Confirmadas

| Decisión | Respuesta |
|----------|-----------|
| Nombre de la entidad | `ContratoLaboral` |
| Usar enums | ✅ Sí: `CategoriaLaboral` y `NivelJerarquico` |
| Horas mensuales | ⚙️ Calculadas dinámicamente desde `JornadaAsignada` |
| Valor hora | ✅ Automático desde sueldo mensual + ajuste manual opcional |
| Historial | ✅ Sí: múltiples contratos con vigencia |

---

## 🔍 Análisis del Cálculo de Horas

### Lógica actual en `TurnosHorarios.java`

El sistema ya cuenta con métodos robustos para calcular horas por turno:

```java
// Minutos totales de la semana
private int obtenerTotalMinutosSemanales() {
    return getHorasParaDia(MONDAY) + getHorasParaDia(TUESDAY) + ... + getHorasParaDia(SUNDAY);
}

// Horas semanales en decimal
public BigDecimal getTotalHorasDecimal() {
    return BigDecimal.valueOf(obtenerTotalMinutosSemanales())
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
}
```

### Lógica en `JornadaAsignada.java`

```java
// Vincula un turno a un empleado por período
@ManyToOne
private TurnosHorarios turno;
private LocalDate fechaInicio;
private LocalDate fechaFin;  // null = indefinido (rotativo)
```

### Fórmula propuesta para horas mensuales

```
horasMensualesEsperadas = (minutosSemanalesDelTurno / 60) × semanasPromedioPorMes

Donde:
- minutosSemanalesDelTurno = turno.obtenerTotalMinutosSemanales()
- semanasPromedioPorMes = 4.33 (52 semanas / 12 meses)

Ejemplo:
- Turno TM.01: Lu-Vi de 06:00 a 14:00 = 40 horas/semana
- Horas mensuales = 40 × 4.33 = 173.2 horas/mes
- Si sueldo = $1,560,000 → Valor hora = $1,560,000 / 173.2 = $9,006.93
```

### Consideraciones para Turnos Rotativos

Cuando un empleado tiene **múltiples turnos rotativos**, el sistema alterna entre ellos semanalmente. Para el cálculo mensual:

```
horasMensualesPromedio = promedio(horasSemanalesTurno1, horasSemanalesTurno2, ...) × 4.33
```

---

## 🏗️ Diseño de la Entidad ContratoLaboral

### Diagrama de Estructura

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            ContratoLaboral                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ INFORMACIÓN DEL PUESTO                                                      │
│ ├── categoria (CategoriaLaboral)      → Clasificación funcional             │
│ ├── puesto (String)                   → Título del cargo                    │
│ ├── nivelJerarquico (NivelJerarquico) → Nivel organizacional               │
│ └── descripcionFunciones (String)     → Descripción del rol                 │
├─────────────────────────────────────────────────────────────────────────────┤
│ UBICACIÓN ORGANIZACIONAL                                                    │
│ ├── sucursal (Sucursales)             → Lugar de trabajo                    │
│ ├── inicioActividades (LocalDate)     → Fecha de ingreso                    │
│ └── getAntiguedadLaboral()            → Calculado automáticamente           │
├─────────────────────────────────────────────────────────────────────────────┤
│ CONFIGURACIÓN ECONÓMICA                                                     │
│ │                                                                           │
│ │  📊 CÁLCULO DINÁMICO DE HORAS                                            │
│ ├── getHorasSemanalesEsperadas()      → Desde jornadas asignadas            │
│ ├── getHorasMensualesEsperadas()      → horasSemanales × 4.33              │
│ │                                                                           │
│ │  💰 VALORES MONETARIOS                                                    │
│ ├── sueldoMensualAcordado             → Sueldo bruto mensual                │
│ ├── getValorHoraCalculado()           → sueldo / horasMensuales            │
│ ├── valorHoraAjustado                 → Sobrescritura manual (opcional)     │
│ ├── getValorHoraEfectivo()            → Retorna ajustado o calculado        │
│ ├── porcentajeHoraExtra               → % adicional (ej: 50%)               │
│ ├── porcentajeHoraEspecial            → % adicional feriados (ej: 100%)     │
│ ├── getValorHoraExtra()               → valorHora × (1 + %extra/100)        │
│ └── getValorHoraEspecial()            → valorHora × (1 + %especial/100)     │
├─────────────────────────────────────────────────────────────────────────────┤
│ VIGENCIA E HISTORIAL                                                        │
│ ├── fechaVigenciaDesde (LocalDate)    → Inicio de este contrato             │
│ ├── fechaVigenciaHasta (LocalDate)    → Fin (null = vigente)                │
│ ├── vigente (boolean calculado)       → true si no tiene fecha fin          │
│ └── fechaModificacion (LocalDateTime) → Auditoría                           │
├─────────────────────────────────────────────────────────────────────────────┤
│ RELACIONES                                                                  │
│ └── empleado (Personal)               → @ManyToOne (permite historial)      │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📚 Especificación de Enums

### 1. CategoriaLaboral.java

Define la **clasificación funcional** del puesto según el tipo de trabajo realizado.

```java
package com.sta.biometric.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Clasificación funcional de los puestos de trabajo.
 * 
 * Determina el tipo de actividades y responsabilidades 
 * asociadas al cargo del empleado.
 */
@Getter
@RequiredArgsConstructor
public enum CategoriaLaboral {
    
    OPERATIVO("Operativo", 
        "Personal que ejecuta tareas manuales o de producción directa"),
    
    ADMINISTRATIVO("Administrativo", 
        "Personal de oficina con funciones de gestión documental y atención"),
    
    TECNICO("Técnico", 
        "Personal con conocimientos técnicos específicos para tareas especializadas"),
    
    PROFESIONAL("Profesional", 
        "Personal con título universitario ejerciendo su profesión"),
    
    COMERCIAL("Comercial", 
        "Personal dedicado a ventas, atención al cliente y relaciones comerciales"),
    
    SUPERVISORIO("Supervisorio", 
        "Personal que supervisa equipos operativos o técnicos"),
    
    GERENCIAL("Gerencial", 
        "Personal con responsabilidades de gestión de áreas o departamentos"),
    
    DIRECTIVO("Directivo", 
        "Alta dirección con responsabilidades estratégicas globales");

    private final String nombre;
    private final String descripcion;
    
    @Override
    public String toString() {
        return nombre;
    }
}
```

**Valores disponibles:**

| Código | Nombre | Descripción |
|--------|--------|-------------|
| `OPERATIVO` | Operativo | Tareas manuales o de producción |
| `ADMINISTRATIVO` | Administrativo | Gestión documental y atención |
| `TECNICO` | Técnico | Conocimientos técnicos específicos |
| `PROFESIONAL` | Profesional | Ejercicio de profesión con título |
| `COMERCIAL` | Comercial | Ventas y relaciones comerciales |
| `SUPERVISORIO` | Supervisorio | Supervisión de equipos |
| `GERENCIAL` | Gerencial | Gestión de áreas/departamentos |
| `DIRECTIVO` | Directivo | Alta dirección estratégica |

---

### 2. NivelJerarquico.java

Define la **posición en la estructura organizacional** y el nivel de experiencia/responsabilidad.

```java
package com.sta.biometric.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Nivel jerárquico dentro de la organización.
 * 
 * Define la posición del empleado en la estructura 
 * organizacional y su nivel de responsabilidad.
 */
@Getter
@RequiredArgsConstructor
public enum NivelJerarquico {
    
    APRENDIZ("Aprendiz", 1, 
        "Personal en formación inicial, pasante o practicante"),
    
    JUNIOR("Junior", 2, 
        "Personal con experiencia inicial (0-2 años)"),
    
    SEMI_SENIOR("Semi-Senior", 3, 
        "Personal con experiencia intermedia (2-4 años)"),
    
    SENIOR("Senior", 4, 
        "Personal con experiencia consolidada (4+ años)"),
    
    ESPECIALISTA("Especialista", 5, 
        "Experto en áreas específicas sin personal a cargo"),
    
    COORDINADOR("Coordinador", 6, 
        "Coordina tareas y pequeños equipos"),
    
    LIDER("Líder", 7, 
        "Lidera equipos de trabajo con responsabilidad directa"),
    
    JEFE("Jefe", 8, 
        "Jefe de área con personal y presupuesto a cargo"),
    
    GERENTE("Gerente", 9, 
        "Gerente de departamento con múltiples áreas"),
    
    DIRECTOR("Director", 10, 
        "Director de división o unidad de negocio"),
    
    C_LEVEL("C-Level", 11, 
        "Ejecutivo de alta dirección (CEO, CFO, COO, CTO, etc.)");

    private final String nombre;
    private final int orden;  // Para ordenamiento
    private final String descripcion;
    
    @Override
    public String toString() {
        return nombre;
    }
}
```

**Valores disponibles:**

| Código | Nombre | Orden | Descripción |
|--------|--------|-------|-------------|
| `APRENDIZ` | Aprendiz | 1 | Pasante o practicante |
| `JUNIOR` | Junior | 2 | 0-2 años de experiencia |
| `SEMI_SENIOR` | Semi-Senior | 3 | 2-4 años de experiencia |
| `SENIOR` | Senior | 4 | 4+ años de experiencia |
| `ESPECIALISTA` | Especialista | 5 | Experto sin personal a cargo |
| `COORDINADOR` | Coordinador | 6 | Coordina pequeños equipos |
| `LIDER` | Líder | 7 | Lidera equipos de trabajo |
| `JEFE` | Jefe | 8 | Jefe de área |
| `GERENTE` | Gerente | 9 | Gerente de departamento |
| `DIRECTOR` | Director | 10 | Director de división |
| `C_LEVEL` | C-Level | 11 | CEO, CFO, COO, CTO, etc. |

---

## 📐 Algoritmo de Cálculo de Horas Mensuales

### Flujo de Cálculo

```
┌──────────────────────────────────────────────────────────────────┐
│                    Personal (empleado)                            │
│                           │                                       │
│                           ▼                                       │
│              ┌────────────────────────┐                          │
│              │   jornadasAsignadas    │  (List<JornadaAsignada>) │
│              └────────────────────────┘                          │
│                           │                                       │
│                           ▼                                       │
│    ┌──────────────────────────────────────────────────────┐      │
│    │  Filtrar jornadas vigentes (fecha actual en rango)   │      │
│    └──────────────────────────────────────────────────────┘      │
│                           │                                       │
│         ┌─────────────────┴─────────────────┐                    │
│         │                                   │                    │
│         ▼                                   ▼                    │
│  ┌─────────────┐                    ┌──────────────────┐         │
│  │ 1 turno     │                    │ Múltiples turnos │         │
│  │ (fijo/rot.) │                    │ (rotativos)      │         │
│  └─────────────┘                    └──────────────────┘         │
│         │                                   │                    │
│         ▼                                   ▼                    │
│  turno.getTotalHorasDecimal()      promedio(turno1, turno2, ...) │
│         │                                   │                    │
│         └─────────────────┬─────────────────┘                    │
│                           ▼                                       │
│              ┌────────────────────────┐                          │
│              │ horasSemanales × 4.33  │                          │
│              │ = horasMensuales       │                          │
│              └────────────────────────┘                          │
│                           │                                       │
│                           ▼                                       │
│              ┌────────────────────────┐                          │
│              │ sueldo / horasMensuales│                          │
│              │ = valorHoraCalculado   │                          │
│              └────────────────────────┘                          │
└──────────────────────────────────────────────────────────────────┘
```

### Implementación del Servicio de Cálculo

```java
/**
 * Servicio para calcular horas esperadas según jornadas asignadas.
 */
public class CalculadorHorasService {
    
    private static final BigDecimal SEMANAS_POR_MES = new BigDecimal("4.33");
    
    /**
     * Calcula las horas semanales esperadas para un empleado.
     * 
     * Lógica:
     * 1. Si tiene UN turno vigente → usa sus horas semanales
     * 2. Si tiene MÚLTIPLES turnos rotativos → promedia las horas
     * 3. Si tiene turno puntual (con fecha fin) → tiene prioridad sobre rotativos
     * 
     * @param empleado Personal para calcular
     * @param fecha Fecha de referencia (generalmente hoy)
     * @return Horas semanales esperadas como BigDecimal
     */
    public static BigDecimal calcularHorasSemanales(Personal empleado, LocalDate fecha) {
        if (empleado == null || empleado.getJornadasAsignadas() == null) {
            return BigDecimal.ZERO;
        }
        
        List<JornadaAsignada> vigentes = empleado.getJornadasAsignadas().stream()
            .filter(j -> j.isVigenteParaFecha(fecha))
            .collect(Collectors.toList());
        
        if (vigentes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Si hay turnos puntuales (con fecha fin), tienen prioridad
        List<JornadaAsignada> puntuales = vigentes.stream()
            .filter(j -> j.getFechaFin() != null)
            .collect(Collectors.toList());
        
        if (!puntuales.isEmpty()) {
            // Usar el turno puntual más reciente
            return puntuales.get(0).getTurno().getTotalHorasDecimal();
        }
        
        // Turnos rotativos: promediar
        BigDecimal sumaHoras = vigentes.stream()
            .map(j -> j.getTurno().getTotalHorasDecimal())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return sumaHoras.divide(
            BigDecimal.valueOf(vigentes.size()), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula las horas mensuales esperadas.
     * 
     * @return horasSemanales × 4.33
     */
    public static BigDecimal calcularHorasMensuales(Personal empleado, LocalDate fecha) {
        return calcularHorasSemanales(empleado, fecha)
            .multiply(SEMANAS_POR_MES)
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula el valor hora desde el sueldo mensual.
     * 
     * @return sueldoMensual / horasMensuales
     */
    public static BigDecimal calcularValorHora(BigDecimal sueldoMensual, 
                                                BigDecimal horasMensuales) {
        if (sueldoMensual == null || horasMensuales == null 
            || horasMensuales.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return sueldoMensual.divide(horasMensuales, 2, RoundingMode.HALF_UP);
    }
}
```

---

## 📁 Estructura de Archivos

### Nuevos Archivos a Crear

```
src/main/java/com/sta/biometric/
├── modelo/
│   └── ContratoLaboral.java              # Entidad principal
├── enums/
│   ├── CategoriaLaboral.java             # Enum categorías
│   └── NivelJerarquico.java              # Enum niveles
└── servicios/
    └── CalculadorHorasService.java       # Servicio de cálculo

src/main/resources/i18n/
└── biometric-labels_es.properties        # Etiquetas nuevas
```

### Archivos a Modificar

```
src/main/java/com/sta/biometric/modelo/Personal.java
  - Agregar relación @OneToMany a ContratoLaboral
  - Método getContratoVigente()
  - Modificar getters de valorHora para delegación
  - Actualizar @View

src/main/resources/xava/controladores.xml
  - (Opcional) Módulo independiente para contratos

src/main/resources/xava/aplicacion.xml
  - Agregar al menú si se crea módulo
```

---

## 📝 Especificación Completa: ContratoLaboral.java

```java
package com.sta.biometric.modelo;

import java.math.*;
import java.time.*;
import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.model.*;
import com.sta.biometric.anotaciones.*;
import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.servicios.*;
import lombok.*;

/**
 * Representa un contrato laboral de un empleado con información del puesto,
 * ubicación organizacional y configuración económica.
 * 
 * <p>Permite mantener historial de configuraciones laborales con fechas de vigencia.</p>
 * 
 * <p><b>Cálculos automáticos:</b></p>
 * <ul>
 *   <li>Horas semanales/mensuales desde jornadas asignadas</li>
 *   <li>Valor hora desde sueldo mensual acordado</li>
 *   <li>Valores hora extra y especial con porcentajes</li>
 * </ul>
 * 
 * @see Personal
 * @see JornadaAsignada
 * @see TurnosHorarios
 */
@Entity
@Table(name = "contrato_laboral")
@Getter @Setter
@View(members = 
    "DatosDelPuesto { " +
    "  categoria, nivelJerarquico; " +
    "  puesto; " +
    "  descripcionFunciones; " +
    "}; " +
    "Ubicacion { " +
    "  sucursal; " +
    "  inicioActividades, antiguedadLaboral; " +
    "}; " +
    "ConfiguracionEconomica { " +
    "  sueldoMensualAcordado; " +
    "  HorasCalculadas [horasSemanalesEsperadas, horasMensualesEsperadas]; " +
    "  ValorHora [valorHoraCalculado, valorHoraAjustado, valorHoraEfectivo]; " +
    "  HorasAdicionales [porcentajeHoraExtra, valorHoraExtra; " +
    "                    porcentajeHoraEspecial, valorHoraEspecial]; " +
    "}; " +
    "Vigencia { " +
    "  fechaVigenciaDesde, fechaVigenciaHasta; vigente; " +
    "  fechaModificacion; " +
    "}")
public class ContratoLaboral extends Identifiable {

    // =========================================================================
    // RELACIÓN CON EMPLEADO
    // =========================================================================
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    @ReferenceView("simple")
    @NoFrame
    @ReadOnly
    private Personal empleado;

    // =========================================================================
    // INFORMACIÓN DEL PUESTO
    // =========================================================================
    
    /** Clasificación funcional del puesto */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CategoriaLaboral categoria;
    
    /** Título o nombre del cargo */
    @Capitalizar
    @Column(length = 100)
    @DisplaySize(50)
    private String puesto;
    
    /** Nivel dentro de la estructura organizacional */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NivelJerarquico nivelJerarquico;
    
    /** Descripción detallada de las funciones del puesto */
    @TextArea
    @Column(length = 1000)
    private String descripcionFunciones;

    // =========================================================================
    // UBICACIÓN ORGANIZACIONAL
    // =========================================================================
    
    /** Sucursal o sector donde trabaja */
    @DescriptionsList
    @ManyToOne(fetch = FetchType.LAZY)
    private Sucursales sucursal;
    
    /** Fecha de inicio de actividades laborales */
    @Required
    @Stereotype("FECHA")
    private LocalDate inicioActividades;
    
    /** Antigüedad calculada automáticamente */
    @Label
    @Depends("inicioActividades")
    public String getAntiguedadLaboral() {
        if (inicioActividades == null) {
            return "Sin fecha de ingreso";
        }
        Period periodo = Period.between(inicioActividades, LocalDate.now());
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
            sb.append(dias).append(dias == 1 ? " día" : " días");
        }
        return sb.length() > 0 ? sb.toString() : "Menos de un día";
    }

    // =========================================================================
    // CONFIGURACIÓN ECONÓMICA
    // =========================================================================
    
    /** Sueldo bruto mensual acordado */
    @Money
    @Column(precision = 12, scale = 2)
    private BigDecimal sueldoMensualAcordado;
    
    /** 
     * Valor hora ajustado manualmente (sobrescribe el calculado).
     * Útil para descuentos por ausencias u otros ajustes específicos.
     */
    @Money
    @Column(precision = 10, scale = 2)
    private BigDecimal valorHoraAjustado;
    
    /** Porcentaje adicional para horas extras (ej: 50 = 50%) */
    @Digits(integer = 3, fraction = 1)
    @Min(0) @Max(200)
    @Column(precision = 4, scale = 1)
    private BigDecimal porcentajeHoraExtra;
    
    /** Porcentaje adicional para horas especiales/feriados (ej: 100 = 100%) */
    @Digits(integer = 3, fraction = 1)
    @Min(0) @Max(200)
    @Column(precision = 4, scale = 1)
    private BigDecimal porcentajeHoraEspecial;

    // =========================================================================
    // VIGENCIA E HISTORIAL
    // =========================================================================
    
    /** Fecha desde la cual el contrato está vigente */
    @Required
    @Stereotype("FECHA")
    private LocalDate fechaVigenciaDesde;
    
    /** Fecha hasta la cual el contrato está vigente (null = activo) */
    @Stereotype("FECHA")
    private LocalDate fechaVigenciaHasta;
    
    /** Fecha de última modificación (auditoría) */
    @ReadOnly
    private LocalDateTime fechaModificacion;

    // =========================================================================
    // GETTERS CALCULADOS - HORAS
    // =========================================================================
    
    /**
     * Horas semanales esperadas según jornadas asignadas del empleado.
     * Considera turnos fijos, rotativos y nocturnos.
     */
    @Label
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "clock-outline")
    public String getHorasSemanalesEsperadas() {
        BigDecimal horas = CalculadorHorasService.calcularHorasSemanales(
            empleado, LocalDate.now());
        return horas.setScale(1, RoundingMode.HALF_UP) + " Hs/semana";
    }
    
    /**
     * Horas mensuales esperadas (horasSemanales × 4.33).
     */
    @Label
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "calendar-clock")
    public String getHorasMensualesEsperadas() {
        BigDecimal horas = CalculadorHorasService.calcularHorasMensuales(
            empleado, LocalDate.now());
        return horas.setScale(1, RoundingMode.HALF_UP) + " Hs/mes";
    }
    
    /**
     * Obtiene las horas mensuales como BigDecimal para cálculos.
     */
    @Transient
    public BigDecimal getHorasMensualesDecimal() {
        return CalculadorHorasService.calcularHorasMensuales(
            empleado, LocalDate.now());
    }

    // =========================================================================
    // GETTERS CALCULADOS - VALORES MONETARIOS
    // =========================================================================
    
    /**
     * Valor hora calculado automáticamente desde sueldo mensual.
     * Fórmula: sueldoMensual / horasMensualesEsperadas
     */
    @Label
    @Money
    @Depends("sueldoMensualAcordado")
    public BigDecimal getValorHoraCalculado() {
        BigDecimal horasMensuales = getHorasMensualesDecimal();
        return CalculadorHorasService.calcularValorHora(
            sueldoMensualAcordado, horasMensuales);
    }
    
    /**
     * Valor hora efectivo: usa el ajustado si existe, sino el calculado.
     * Este es el valor que se usa en liquidaciones y descuentos.
     */
    @Transient
    @Money
    @MiLabel(medida = "grande", negrita = true, recuadro = true, icon = "currency-usd")
    public BigDecimal getValorHoraEfectivo() {
        if (valorHoraAjustado != null && 
            valorHoraAjustado.compareTo(BigDecimal.ZERO) > 0) {
            return valorHoraAjustado;
        }
        return getValorHoraCalculado();
    }
    
    /**
     * Valor hora extra: valorHoraEfectivo × (1 + porcentajeExtra/100)
     */
    @Label
    @Money
    @Depends("sueldoMensualAcordado, valorHoraAjustado, porcentajeHoraExtra")
    public BigDecimal getValorHoraExtra() {
        BigDecimal base = getValorHoraEfectivo();
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (porcentajeHoraExtra == null) {
            return base;
        }
        BigDecimal adicional = base.multiply(porcentajeHoraExtra)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return base.add(adicional);
    }
    
    /**
     * Valor hora especial (feriados): valorHoraEfectivo × (1 + porcentajeEspecial/100)
     */
    @Label
    @Money
    @Depends("sueldoMensualAcordado, valorHoraAjustado, porcentajeHoraEspecial")
    public BigDecimal getValorHoraEspecial() {
        BigDecimal base = getValorHoraEfectivo();
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (porcentajeHoraEspecial == null) {
            return base;
        }
        BigDecimal adicional = base.multiply(porcentajeHoraEspecial)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return base.add(adicional);
    }
    
    /**
     * Indica si el contrato está vigente actualmente.
     */
    @Label
    @Depends("fechaVigenciaDesde, fechaVigenciaHasta")
    public boolean isVigente() {
        LocalDate hoy = LocalDate.now();
        if (fechaVigenciaDesde == null || hoy.isBefore(fechaVigenciaDesde)) {
            return false;
        }
        return fechaVigenciaHasta == null || !hoy.isAfter(fechaVigenciaHasta);
    }

    // =========================================================================
    // CALLBACKS JPA
    // =========================================================================
    
    @PrePersist
    @PreUpdate
    private void antesDeGuardar() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
```

---

## 🔄 Modificaciones en Personal.java

### Agregar relación con contratos

```java
// En Personal.java

/**
 * Colección de contratos laborales del empleado (historial).
 * Permite múltiples contratos con diferentes vigencias.
 */
@OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
@ListProperties("puesto, categoria, sueldoMensualAcordado, fechaVigenciaDesde, fechaVigenciaHasta, vigente")
@OrderBy("fechaVigenciaDesde desc")
private Collection<ContratoLaboral> contratos;

/**
 * Obtiene el contrato vigente del empleado.
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

// Modificar getters para delegación (mantener compatibilidad)
@Transient
public BigDecimal getValorHora() {
    ContratoLaboral contrato = getContratoVigente();
    return contrato != null ? contrato.getValorHoraEfectivo() : this.valorHora;
}

@Transient  
public BigDecimal getValorHoraExtra() {
    ContratoLaboral contrato = getContratoVigente();
    return contrato != null ? contrato.getValorHoraExtra() : calcularValorHoraExtraLegacy();
}

@Transient
public BigDecimal getValorHoraEspecial() {
    ContratoLaboral contrato = getContratoVigente();
    return contrato != null ? contrato.getValorHoraEspecial() : calcularValorHoraEspecialLegacy();
}
```

### Actualizar Vista

```java
@View(members = "nombreCompleto, turnoActivoHoy;" +
    "InformacionPersonal { ... }; " +
    "InformacionLaboral { " +
    "   credenciales[...], " +
    "   contratos; " +  // ← Colección de contratos con historial
    "   JORNADAS[aceptaPausa; jornadasAsignadas;]; " +
    "}; " +
    // resto...
)
```

---

## 📊 Fases de Implementación

### Fase 1: Fundamentos (Día 1-2)

- [ ] Crear `CategoriaLaboral.java`
- [ ] Crear `NivelJerarquico.java`
- [ ] Crear `CalculadorHorasService.java`
- [ ] Agregar etiquetas i18n

### Fase 2: Entidad Principal (Día 2-3)

- [ ] Crear `ContratoLaboral.java`
- [ ] Agregar relación en `Personal.java`
- [ ] Actualizar vistas

### Fase 3: Integración (Día 3-4)

- [ ] Modificar getters en `Personal` para delegación
- [ ] Verificar `AuditoriaRegistros` usa getters delegados
- [ ] Verificar `LiquidacionJornadas` captura snapshots correctos

### Fase 4: Migración (Día 4-5)

- [ ] Crear script SQL de migración
- [ ] Migrar datos existentes a contratos
- [ ] Validar integridad

### Fase 5: Pruebas (Día 5-6)

- [ ] Pruebas unitarias del cálculo de horas
- [ ] Pruebas de integración con liquidaciones
- [ ] Validar reportes y dashboards

---

## 🗃️ Script de Migración SQL

```sql
-- 1. Crear tabla contrato_laboral
CREATE TABLE contrato_laboral (
    id VARCHAR(32) PRIMARY KEY,
    empleado_id VARCHAR(32) NOT NULL,
    categoria VARCHAR(30),
    puesto VARCHAR(100),
    nivel_jerarquico VARCHAR(30),
    descripcion_funciones VARCHAR(1000),
    sucursal_id VARCHAR(32),
    inicio_actividades DATE,
    sueldo_mensual_acordado DECIMAL(12,2),
    valor_hora_ajustado DECIMAL(10,2),
    porcentaje_hora_extra DECIMAL(4,1),
    porcentaje_hora_especial DECIMAL(4,1),
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    fecha_modificacion DATETIME,
    FOREIGN KEY (empleado_id) REFERENCES personal(id),
    FOREIGN KEY (sucursal_id) REFERENCES sucursales(id)
);

-- 2. Migrar datos desde Personal (un contrato por empleado)
INSERT INTO contrato_laboral (
    id, empleado_id, puesto, sucursal_id, inicio_actividades,
    valor_hora_ajustado, porcentaje_hora_extra, porcentaje_hora_especial,
    fecha_vigencia_desde, fecha_modificacion
)
SELECT 
    UUID(), id, puesto, sucursal_id, inicio_actividades,
    valor_hora, porcentaje_hora_extra, porcentaje_hora_especial,
    COALESCE(inicio_actividades, CURDATE()), NOW()
FROM personal
WHERE eliminado = false;

-- 3. (Opcional) Marcar campos legacy como deprecated
-- ALTER TABLE personal ADD COLUMN contrato_migrado BOOLEAN DEFAULT FALSE;
-- UPDATE personal SET contrato_migrado = TRUE WHERE eliminado = false;
```

---

## 📋 Etiquetas i18n a Agregar

```properties
# biometric-labels_es.properties

# Entidad ContratoLaboral
ContratoLaboral=Contrato Laboral
contratos=Contratos Laborales

# Secciones
ContratoLaboral.DatosDelPuesto=Datos del Puesto
ContratoLaboral.Ubicacion=Ubicación Organizacional
ContratoLaboral.ConfiguracionEconomica=Configuración Económica
ContratoLaboral.HorasCalculadas=Horas Calculadas
ContratoLaboral.ValorHora=Valor Hora
ContratoLaboral.HorasAdicionales=Horas Adicionales
ContratoLaboral.Vigencia=Vigencia

# Campos
ContratoLaboral.categoria=Categoría
ContratoLaboral.puesto=Puesto / Cargo
ContratoLaboral.nivelJerarquico=Nivel Jerárquico
ContratoLaboral.descripcionFunciones=Descripción de Funciones
ContratoLaboral.sucursal=Sucursal / Sector
ContratoLaboral.inicioActividades=Inicio de Actividades
ContratoLaboral.antiguedadLaboral=Antigüedad
ContratoLaboral.sueldoMensualAcordado=Sueldo Mensual Acordado
ContratoLaboral.horasSemanalesEsperadas=Horas Semanales
ContratoLaboral.horasMensualesEsperadas=Horas Mensuales
ContratoLaboral.valorHoraCalculado=Valor Hora (Calculado)
ContratoLaboral.valorHoraAjustado=Valor Hora (Ajustado)
ContratoLaboral.valorHoraEfectivo=Valor Hora Efectivo
ContratoLaboral.porcentajeHoraExtra=% Hora Extra
ContratoLaboral.valorHoraExtra=Valor Hora Extra
ContratoLaboral.porcentajeHoraEspecial=% Hora Especial
ContratoLaboral.valorHoraEspecial=Valor Hora Especial
ContratoLaboral.fechaVigenciaDesde=Vigente Desde
ContratoLaboral.fechaVigenciaHasta=Vigente Hasta
ContratoLaboral.vigente=Vigente
ContratoLaboral.fechaModificacion=Última Modificación

# Enums
CategoriaLaboral.OPERATIVO=Operativo
CategoriaLaboral.ADMINISTRATIVO=Administrativo
CategoriaLaboral.TECNICO=Técnico
CategoriaLaboral.PROFESIONAL=Profesional
CategoriaLaboral.COMERCIAL=Comercial
CategoriaLaboral.SUPERVISORIO=Supervisorio
CategoriaLaboral.GERENCIAL=Gerencial
CategoriaLaboral.DIRECTIVO=Directivo

NivelJerarquico.APRENDIZ=Aprendiz
NivelJerarquico.JUNIOR=Junior
NivelJerarquico.SEMI_SENIOR=Semi-Senior
NivelJerarquico.SENIOR=Senior
NivelJerarquico.ESPECIALISTA=Especialista
NivelJerarquico.COORDINADOR=Coordinador
NivelJerarquico.LIDER=Líder
NivelJerarquico.JEFE=Jefe
NivelJerarquico.GERENTE=Gerente
NivelJerarquico.DIRECTOR=Director
NivelJerarquico.C_LEVEL=C-Level
```

---

## ⚠️ Consideraciones Importantes

### 1. Compatibilidad con Sistemas Existentes

- ✅ `AuditoriaRegistros` usa snapshots → No requiere cambios
- ✅ `LiquidacionJornadas` captura valores → Funcionará con nuevos getters
- ✅ Reportes JasperReports → Acceden via `empleado.valorHora`

### 2. Turnos Sin Asignar

- Si un empleado no tiene jornadas asignadas → `horasMensuales = 0`
- Esto causará que `valorHoraCalculado = 0`
- Solución: Usar `valorHoraAjustado` como obligatorio si no hay jornadas

### 3. Valores por Defecto Sugeridos

- `porcentajeHoraExtra = 50` (50%)
- `porcentajeHoraEspecial = 100` (100%)
- `fechaVigenciaDesde = inicioActividades`

### 4. Historial de Contratos

- Un empleado puede tener múltiples contratos
- Solo uno puede estar vigente a la vez (sin fechaVigenciaHasta o fecha futura)
- Los contratos pasados quedan como historial

---

## ✅ Checklist de Validación

Antes de aprobar para implementación:

- [ ] ¿El nombre `ContratoLaboral` es correcto?
- [ ] ¿Los valores de `CategoriaLaboral` son apropiados?
- [ ] ¿Los valores de `NivelJerarquico` son apropiados?
- [ ] ¿La fórmula `horasSemanales × 4.33` es aceptable?
- [ ] ¿El campo `valorHoraAjustado` para sobrescribir está bien?
- [ ] ¿Se requiere algún campo adicional?
- [ ] ¿La estructura de vistas es adecuada?

---

*Plan V2 - Generado el 2026-01-15 - STA.RH Sistema Biométrico*
