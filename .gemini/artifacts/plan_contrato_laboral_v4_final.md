# Plan de Implementación V4 FINAL: Entidad ContratoLaboral

## ✅ Decisiones Finales Confirmadas

| Aspecto | Decisión |
|---------|----------|
| **CategoriaLaboral** | Entidad en `auxiliares/` (administrable) |
| **NivelJerarquico** | Enum en `enums/` (fijo, universal) |
| **Visualización jerárquica** | Sin @Tree, usar `path` ordenado |
| **Organigrama futuro** | Preparado para reporte PDF con dependencias |
| **Campos de ubicación** | Quedan en Personal (sucursal, inicioActividades) |
| **valorHoraAjustado** | Sobrescribe el calculado si existe |
| **Campos adicionales** | tipoContrato, modalidadTrabajo, motivoFinalizacion |

---

## 📚 Especificación: NivelJerarquico (Enum Universal)

### Diseño del Enum

Este enum está diseñado para ser **universal y funcional** para la mayoría de empresas y rubros:

```java
package com.sta.biometric.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Niveles jerárquicos universales para la estructura organizacional.
 * 
 * <p>Diseñado para ser compatible con la mayoría de empresas y rubros,
 * desde pequeñas empresas hasta corporaciones multinacionales.</p>
 * 
 * <p>El campo {@code orden} permite ordenar de mayor a menor jerarquía,
 * y {@code path} facilita queries jerárquicas.</p>
 * 
 * <p><b>Uso futuro:</b> El path permite generar reportes PDF con 
 * diagramas de dependencias jerárquicas.</p>
 */
@Getter
@RequiredArgsConstructor
public enum NivelJerarquico {
    
    // =========================================================================
    // NIVEL EJECUTIVO (Alta Dirección)
    // =========================================================================
    
    PRESIDENTE("Presidente / CEO", 1, "/PRES", 
        "Máxima autoridad ejecutiva de la organización"),
    
    VICEPRESIDENTE("Vicepresidente", 2, "/PRES/VP", 
        "Segunda autoridad ejecutiva, reporta al Presidente"),
    
    DIRECTOR_GENERAL("Director General", 3, "/PRES/DG", 
        "Director máximo de operaciones o división principal"),
    
    // =========================================================================
    // NIVEL DIRECTIVO (Direcciones de área)
    // =========================================================================
    
    DIRECTOR("Director", 4, "/PRES/DG/DIR", 
        "Director de departamento o área funcional (Finanzas, RRHH, Comercial, etc.)"),
    
    SUBDIRECTOR("Subdirector", 5, "/PRES/DG/DIR/SDIR", 
        "Asiste al Director y lo reemplaza en su ausencia"),
    
    // =========================================================================
    // NIVEL GERENCIAL (Gestión de áreas)
    // =========================================================================
    
    GERENTE("Gerente", 6, "/PRES/DG/DIR/GER", 
        "Gestiona un área específica con autonomía operativa"),
    
    SUBGERENTE("Subgerente", 7, "/PRES/DG/DIR/GER/SGER", 
        "Asiste al Gerente y coordina equipos"),
    
    // =========================================================================
    // NIVEL DE JEFATURA (Supervisión de equipos)
    // =========================================================================
    
    JEFE("Jefe", 8, "/PRES/DG/DIR/GER/JEF", 
        "Jefe de sector o unidad con personal a cargo"),
    
    SUBJEFE("Subjefe", 9, "/PRES/DG/DIR/GER/JEF/SJEF", 
        "Asiste al Jefe en la coordinación del equipo"),
    
    // =========================================================================
    // NIVEL DE COORDINACIÓN (Liderazgo de equipos pequeños)
    // =========================================================================
    
    COORDINADOR("Coordinador", 10, "/PRES/DG/DIR/GER/JEF/COORD", 
        "Coordina actividades y pequeños equipos de trabajo"),
    
    SUPERVISOR("Supervisor", 11, "/PRES/DG/DIR/GER/JEF/COORD/SUP", 
        "Supervisa tareas operativas y control de calidad"),
    
    LIDER("Líder de Equipo", 12, "/PRES/DG/DIR/GER/JEF/COORD/LID", 
        "Lidera un equipo específico sin jerarquía formal"),
    
    // =========================================================================
    // NIVEL PROFESIONAL (Especialistas y expertos)
    // =========================================================================
    
    ESPECIALISTA("Especialista", 13, "/PRES/DG/DIR/GER/ESP", 
        "Experto en un área específica, sin personal a cargo"),
    
    PROFESIONAL_SENIOR("Profesional Senior", 14, "/PRES/DG/DIR/GER/ESP/PSEN", 
        "Profesional con amplia experiencia (5+ años)"),
    
    PROFESIONAL_SEMI_SENIOR("Profesional Semi-Senior", 15, "/PRES/DG/DIR/GER/ESP/PSSEN", 
        "Profesional con experiencia intermedia (2-5 años)"),
    
    PROFESIONAL_JUNIOR("Profesional Junior", 16, "/PRES/DG/DIR/GER/ESP/PJUN", 
        "Profesional en desarrollo inicial (0-2 años)"),
    
    // =========================================================================
    // NIVEL TÉCNICO (Conocimientos técnicos específicos)
    // =========================================================================
    
    TECNICO_SENIOR("Técnico Senior", 17, "/PRES/DG/DIR/GER/TEC/TSEN", 
        "Técnico con experiencia avanzada"),
    
    TECNICO("Técnico", 18, "/PRES/DG/DIR/GER/TEC", 
        "Ejecuta tareas técnicas especializadas"),
    
    TECNICO_JUNIOR("Técnico Junior", 19, "/PRES/DG/DIR/GER/TEC/TJUN", 
        "Técnico en formación o con poca experiencia"),
    
    // =========================================================================
    // NIVEL ADMINISTRATIVO (Apoyo administrativo)
    // =========================================================================
    
    ADMINISTRATIVO_SENIOR("Administrativo Senior", 20, "/PRES/DG/DIR/GER/ADM/ASEN", 
        "Personal administrativo con experiencia"),
    
    ADMINISTRATIVO("Administrativo", 21, "/PRES/DG/DIR/GER/ADM", 
        "Personal de oficina y gestión documental"),
    
    AUXILIAR_ADMINISTRATIVO("Auxiliar Administrativo", 22, "/PRES/DG/DIR/GER/ADM/AUX", 
        "Apoyo en tareas administrativas básicas"),
    
    // =========================================================================
    // NIVEL OPERATIVO (Ejecución de tareas)
    // =========================================================================
    
    OPERARIO_SENIOR("Operario Senior", 23, "/PRES/DG/DIR/GER/OPE/OSEN", 
        "Operario con experiencia y habilidades avanzadas"),
    
    OPERARIO("Operario", 24, "/PRES/DG/DIR/GER/OPE", 
        "Ejecuta tareas operativas o de producción"),
    
    AYUDANTE("Ayudante", 25, "/PRES/DG/DIR/GER/OPE/AYU", 
        "Asiste en tareas operativas"),
    
    // =========================================================================
    // NIVEL DE INGRESO (Formación inicial)
    // =========================================================================
    
    APRENDIZ("Aprendiz", 26, "/PRES/DG/DIR/GER/APR", 
        "Personal en formación o capacitación"),
    
    PASANTE("Pasante", 27, "/PRES/DG/DIR/GER/PAS", 
        "Estudiante realizando pasantía o práctica profesional"),
    
    BECARIO("Becario", 28, "/PRES/DG/DIR/GER/BEC", 
        "Persona con beca de formación laboral");

    // =========================================================================
    // CAMPOS
    // =========================================================================
    
    private final String nombre;
    private final int orden;
    private final String path;
    private final String descripcion;
    
    @Override
    public String toString() {
        return nombre;
    }
    
    /**
     * Retorna el nombre formateado con el nivel (para UI).
     * Ejemplo: "6. Gerente"
     */
    public String getNombreConOrden() {
        return orden + ". " + nombre;
    }
    
    /**
     * Retorna la profundidad jerárquica basada en el path.
     * Más barras = mayor profundidad = menor jerarquía.
     */
    public int getProfundidad() {
        return (int) path.chars().filter(c -> c == '/').count();
    }
    
    /**
     * Verifica si este nivel es superior a otro.
     */
    public boolean esSuperiorA(NivelJerarquico otro) {
        return this.orden < otro.orden;
    }
    
    /**
     * Verifica si este nivel es de categoría ejecutiva.
     */
    public boolean esEjecutivo() {
        return orden <= 3;
    }
    
    /**
     * Verifica si este nivel es de dirección o gerencia.
     */
    public boolean esDireccionOGerencia() {
        return orden >= 4 && orden <= 7;
    }
    
    /**
     * Verifica si tiene personal a cargo típicamente.
     */
    public boolean tienePersonalACargo() {
        return orden <= 12;
    }
}
```

### Tabla Resumen del Enum

| Orden | Código | Nombre | Descripción |
|-------|--------|--------|-------------|
| **EJECUTIVO** ||||
| 1 | PRESIDENTE | Presidente / CEO | Máxima autoridad |
| 2 | VICEPRESIDENTE | Vicepresidente | Segunda autoridad |
| 3 | DIRECTOR_GENERAL | Director General | Director máximo de operaciones |
| **DIRECTIVO** ||||
| 4 | DIRECTOR | Director | Director de departamento |
| 5 | SUBDIRECTOR | Subdirector | Asiste al Director |
| **GERENCIAL** ||||
| 6 | GERENTE | Gerente | Gestiona área con autonomía |
| 7 | SUBGERENTE | Subgerente | Asiste al Gerente |
| **JEFATURA** ||||
| 8 | JEFE | Jefe | Jefe de sector con personal |
| 9 | SUBJEFE | Subjefe | Asiste al Jefe |
| **COORDINACIÓN** ||||
| 10 | COORDINADOR | Coordinador | Coordina equipos pequeños |
| 11 | SUPERVISOR | Supervisor | Supervisa tareas operativas |
| 12 | LIDER | Líder de Equipo | Lidera sin jerarquía formal |
| **PROFESIONAL** ||||
| 13 | ESPECIALISTA | Especialista | Experto sin personal a cargo |
| 14 | PROFESIONAL_SENIOR | Profesional Senior | 5+ años experiencia |
| 15 | PROFESIONAL_SEMI_SENIOR | Profesional Semi-Senior | 2-5 años experiencia |
| 16 | PROFESIONAL_JUNIOR | Profesional Junior | 0-2 años experiencia |
| **TÉCNICO** ||||
| 17 | TECNICO_SENIOR | Técnico Senior | Técnico experimentado |
| 18 | TECNICO | Técnico | Tareas técnicas especializadas |
| 19 | TECNICO_JUNIOR | Técnico Junior | Técnico en formación |
| **ADMINISTRATIVO** ||||
| 20 | ADMINISTRATIVO_SENIOR | Administrativo Senior | Admin con experiencia |
| 21 | ADMINISTRATIVO | Administrativo | Personal de oficina |
| 22 | AUXILIAR_ADMINISTRATIVO | Auxiliar Admin | Apoyo básico |
| **OPERATIVO** ||||
| 23 | OPERARIO_SENIOR | Operario Senior | Operario experimentado |
| 24 | OPERARIO | Operario | Tareas de producción |
| 25 | AYUDANTE | Ayudante | Asiste en operaciones |
| **INGRESO** ||||
| 26 | APRENDIZ | Aprendiz | En formación |
| 27 | PASANTE | Pasante | Práctica profesional |
| 28 | BECARIO | Becario | Beca de formación |

---

## 📚 Especificación: CategoriaLaboral (Entidad)

```java
package com.sta.biometric.auxiliares;

import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.model.*;
import lombok.*;

/**
 * Clasificación funcional de los puestos de trabajo.
 * 
 * <p>Entidad administrable que permite personalizar las categorías
 * según las necesidades de cada empresa.</p>
 * 
 * <p>Ejemplos: Operativo, Administrativo, Técnico, Profesional, etc.</p>
 */
@Entity
@Table(name = "categoria_laboral")
@Getter @Setter
@View(members = "codigo; nombre; descripcion; activo")
@Tab(properties = "codigo, nombre, descripcion, activo", 
     defaultOrder = "${codigo} asc")
public class CategoriaLaboral extends Identifiable {
    
    /** Código corto de la categoría (ej: "OPE", "ADM", "TEC") */
    @Required
    @Column(length = 5, unique = true)
    @DisplaySize(5)
    private String codigo;
    
    /** Nombre descriptivo de la categoría */
    @Required
    @Column(length = 50)
    @DisplaySize(40)
    private String nombre;
    
    /** Descripción de las actividades típicas */
    @TextArea
    @Column(length = 500)
    private String descripcion;
    
    /** Indica si la categoría está activa */
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean activo = true;
    
    @Override
    public String toString() {
        return nombre;
    }
}
```

### Datos Iniciales para CategoriaLaboral

```sql
INSERT INTO categoria_laboral (id, codigo, nombre, descripcion, activo) VALUES
(UUID(), 'OPE', 'Operativo', 'Personal que ejecuta tareas manuales, de producción o trabajo físico directo', TRUE),
(UUID(), 'ADM', 'Administrativo', 'Personal de oficina con funciones de gestión documental, atención y trámites', TRUE),
(UUID(), 'TEC', 'Técnico', 'Personal con conocimientos técnicos específicos para tareas especializadas', TRUE),
(UUID(), 'PRO', 'Profesional', 'Personal con título universitario ejerciendo su profesión', TRUE),
(UUID(), 'COM', 'Comercial', 'Personal dedicado a ventas, atención al cliente y relaciones comerciales', TRUE),
(UUID(), 'LOG', 'Logística', 'Personal de almacén, distribución, transporte y cadena de suministro', TRUE),
(UUID(), 'MAN', 'Mantenimiento', 'Personal de mantenimiento de instalaciones, equipos y maquinaria', TRUE),
(UUID(), 'SEG', 'Seguridad', 'Personal de vigilancia, seguridad e higiene', TRUE),
(UUID(), 'SER', 'Servicios', 'Personal de servicios generales, limpieza, cafetería, etc.', TRUE);
```

---

## 📚 Especificación: Enums Adicionales

### TipoContrato.java

```java
package com.sta.biometric.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de contrato laboral según modalidad de contratación.
 */
@Getter
@RequiredArgsConstructor
public enum TipoContrato {
    
    TIEMPO_COMPLETO("Tiempo Completo", "Jornada laboral estándar según convenio"),
    MEDIO_TIEMPO("Medio Tiempo", "Jornada reducida al 50%"),
    TIEMPO_PARCIAL("Tiempo Parcial", "Jornada reducida personalizada"),
    EVENTUAL("Eventual / Temporario", "Contrato por tiempo determinado"),
    PASANTIA("Pasantía", "Contrato de formación para estudiantes"),
    POR_OBRA("Por Obra", "Contrato hasta completar una obra o proyecto"),
    FREELANCE("Freelance / Monotributista", "Facturación independiente");
    
    private final String nombre;
    private final String descripcion;
    
    @Override
    public String toString() {
        return nombre;
    }
}
```

### ModalidadTrabajo.java

```java
package com.sta.biometric.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Modalidad de trabajo según el lugar de desempeño.
 */
@Getter
@RequiredArgsConstructor
public enum ModalidadTrabajo {
    
    PRESENCIAL("Presencial", "Trabajo 100% en las instalaciones de la empresa"),
    REMOTO("Remoto / Teletrabajo", "Trabajo 100% desde ubicación remota"),
    HIBRIDO("Híbrido", "Combinación de presencial y remoto"),
    ITINERANTE("Itinerante", "Trabajo en diferentes ubicaciones o viajes frecuentes"),
    CAMPO("Trabajo en Campo", "Trabajo fuera de oficina (ventas, técnicos, etc.)");
    
    private final String nombre;
    private final String descripcion;
    
    @Override
    public String toString() {
        return nombre;
    }
}
```

---

## 📚 Especificación: ContratoLaboral.java (Final)

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
 * Contrato laboral de un empleado.
 * 
 * <p>Define el puesto, categoría, nivel jerárquico y la configuración
 * económica (sueldo, valores hora, porcentajes).</p>
 * 
 * <p>Permite historial: múltiples contratos con fechas de vigencia.</p>
 */
@Entity
@Table(name = "contrato_laboral")
@Getter @Setter
@View(members = 
    "DatosDelPuesto { " +
    "  tipoContrato, modalidadTrabajo; " +
    "  categoria; nivelJerarquico; " +
    "  puesto; descripcionFunciones; " +
    "}; " +
    "ConfiguracionEconomica { " +
    "  sueldoMensualAcordado; " +
    "  HorasEsperadas [horasSemanalesEsperadas, horasMensualesEsperadas]; " +
    "  ValorHora [valorHoraCalculado; valorHoraAjustado; valorHoraEfectivo]; " +
    "  HorasAdicionales [porcentajeHoraExtra, valorHoraExtra; " +
    "                    porcentajeHoraEspecial, valorHoraEspecial]; " +
    "}; " +
    "Vigencia { " +
    "  fechaVigenciaDesde, fechaVigenciaHasta; vigente; " +
    "  motivoFinalizacion; observaciones; " +
    "}")
@Tab(properties = "empleado.nombreCompleto, puesto, nivelJerarquico, sueldoMensualAcordado, fechaVigenciaDesde, vigente",
     defaultOrder = "${fechaVigenciaDesde} desc")
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
    // DATOS DEL PUESTO
    // =========================================================================
    
    /** Tipo de contrato (Tiempo completo, Medio tiempo, Eventual...) */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoContrato tipoContrato;
    
    /** Modalidad de trabajo (Presencial, Remoto, Híbrido...) */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ModalidadTrabajo modalidadTrabajo;
    
    /** Clasificación funcional (Operativo, Administrativo, Técnico...) */
    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList(descriptionProperties = "nombre", 
                     condition = "${activo} = true",
                     order = "${codigo} asc")
    @NoCreate @NoModify
    private CategoriaLaboral categoria;
    
    /** Nivel en la estructura organizacional (enum universal) */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NivelJerarquico nivelJerarquico;
    
    /** Título o nombre del cargo */
    @Capitalizar
    @Column(length = 100)
    @DisplaySize(50)
    private String puesto;
    
    /** Descripción detallada de las funciones del puesto */
    @TextArea
    @Column(length = 1000)
    private String descripcionFunciones;

    // =========================================================================
    // CONFIGURACIÓN ECONÓMICA
    // =========================================================================
    
    /** Sueldo bruto mensual acordado */
    @Money
    @Column(precision = 12, scale = 2)
    private BigDecimal sueldoMensualAcordado;
    
    /** 
     * Valor hora ajustado manualmente.
     * 
     * Si tiene valor > 0, sobrescribe el valorHoraCalculado.
     * Si es null o 0, se usa el calculado automáticamente.
     * 
     * Casos de uso:
     * - Empleado sin turno asignado (calculado = 0)
     * - Bonificación o descuento especial acordado
     * - Valor pactado diferente al calculado
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
    
    /** Motivo de finalización del contrato (si aplica) */
    @Column(length = 200)
    @DisplaySize(50)
    private String motivoFinalizacion;
    
    /** Observaciones adicionales */
    @TextArea
    @Column(length = 500)
    private String observaciones;
    
    /** Fecha de última modificación */
    @ReadOnly
    private LocalDateTime fechaModificacion;

    // =========================================================================
    // GETTERS CALCULADOS - HORAS
    // =========================================================================
    
    @Label
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "clock-outline")
    public String getHorasSemanalesEsperadas() {
        BigDecimal horas = CalculadorHorasService.calcularHorasSemanales(
            empleado, LocalDate.now());
        return horas.setScale(1, RoundingMode.HALF_UP) + " Hs/sem";
    }
    
    @Label
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "calendar-clock")
    public String getHorasMensualesEsperadas() {
        BigDecimal horas = getHorasMensualesDecimal();
        return horas.setScale(1, RoundingMode.HALF_UP) + " Hs/mes";
    }
    
    @Transient
    public BigDecimal getHorasMensualesDecimal() {
        return CalculadorHorasService.calcularHorasMensuales(empleado, LocalDate.now());
    }

    // =========================================================================
    // GETTERS CALCULADOS - VALORES MONETARIOS
    // =========================================================================
    
    @Label
    @Money
    @Depends("sueldoMensualAcordado")
    public BigDecimal getValorHoraCalculado() {
        BigDecimal horasMensuales = getHorasMensualesDecimal();
        return CalculadorHorasService.calcularValorHora(
            sueldoMensualAcordado, horasMensuales);
    }
    
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
    // CALLBACKS
    // =========================================================================
    
    @PrePersist
    @PreUpdate
    private void antesDeGuardar() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
```

---

## 📁 Estructura de Archivos a Crear

```
src/main/java/com/sta/biometric/
├── auxiliares/
│   └── CategoriaLaboral.java          # Entidad administrable
├── enums/
│   ├── NivelJerarquico.java           # Enum universal (28 niveles)
│   ├── TipoContrato.java              # Enum tipos de contrato
│   └── ModalidadTrabajo.java          # Enum modalidades
├── modelo/
│   └── ContratoLaboral.java           # Entidad principal
└── servicios/
    └── CalculadorHorasService.java    # Servicio de cálculo
```

---

## 📊 Fases de Implementación

### Fase 1: Enums y Servicio (Día 1)

- [ ] Crear `NivelJerarquico.java` (enum con 28 niveles)
- [ ] Crear `TipoContrato.java`
- [ ] Crear `ModalidadTrabajo.java`
- [ ] Crear `CalculadorHorasService.java`

### Fase 2: CategoriaLaboral (Día 1-2)

- [ ] Crear `CategoriaLaboral.java` (entidad)
- [ ] Agregar a leftMenu.jsp como oculta
- [ ] Script de datos iniciales
- [ ] Etiquetas i18n

### Fase 3: ContratoLaboral (Día 2-3)

- [ ] Crear `ContratoLaboral.java`
- [ ] Agregar relación `@OneToMany` en `Personal.java`
- [ ] Método `getContratoVigente()`
- [ ] Modificar getters de valorHora en Personal
- [ ] Actualizar leftMenu.jsp
- [ ] Etiquetas i18n completas

### Fase 4: Integración y Pruebas (Día 3-4)

- [ ] Verificar cálculos de horas
- [ ] Verificar delegación de valores en Personal
- [ ] Probar AuditoriaRegistros y LiquidacionJornadas
- [ ] Verificar reportes existentes

### Fase 5: Migración de Datos (Día 4-5)

- [ ] Script de migración
- [ ] Crear contratos para empleados existentes
- [ ] Validar integridad

---

## 📋 Etiquetas i18n a Agregar

```properties
# ContratoLaboral
ContratoLaboral=Contrato Laboral
contratos=Contratos

ContratoLaboral.DatosDelPuesto=Datos del Puesto
ContratoLaboral.ConfiguracionEconomica=Configuración Económica
ContratoLaboral.HorasEsperadas=Horas Esperadas
ContratoLaboral.ValorHora=Valor Hora
ContratoLaboral.HorasAdicionales=Horas Adicionales
ContratoLaboral.Vigencia=Vigencia

ContratoLaboral.tipoContrato=Tipo de Contrato
ContratoLaboral.modalidadTrabajo=Modalidad
ContratoLaboral.categoria=Categoría
ContratoLaboral.nivelJerarquico=Nivel Jerárquico
ContratoLaboral.puesto=Puesto / Cargo
ContratoLaboral.descripcionFunciones=Descripción de Funciones
ContratoLaboral.sueldoMensualAcordado=Sueldo Mensual
ContratoLaboral.valorHoraCalculado=Valor Hora (Calculado)
ContratoLaboral.valorHoraAjustado=Valor Hora (Ajustado)
ContratoLaboral.valorHoraEfectivo=Valor Hora Efectivo
ContratoLaboral.porcentajeHoraExtra=% Hora Extra
ContratoLaboral.valorHoraExtra=Valor Hora Extra
ContratoLaboral.porcentajeHoraEspecial=% Hora Especial
ContratoLaboral.valorHoraEspecial=Valor Hora Especial
ContratoLaboral.horasSemanalesEsperadas=Horas Semanales
ContratoLaboral.horasMensualesEsperadas=Horas Mensuales
ContratoLaboral.fechaVigenciaDesde=Vigente Desde
ContratoLaboral.fechaVigenciaHasta=Vigente Hasta
ContratoLaboral.vigente=Vigente
ContratoLaboral.motivoFinalizacion=Motivo de Finalización
ContratoLaboral.observaciones=Observaciones

# CategoriaLaboral
CategoriaLaboral=Categoría Laboral
CategoriaLaboral.codigo=Código
CategoriaLaboral.nombre=Nombre
CategoriaLaboral.descripcion=Descripción
CategoriaLaboral.activo=Activo

# TipoContrato
TipoContrato.TIEMPO_COMPLETO=Tiempo Completo
TipoContrato.MEDIO_TIEMPO=Medio Tiempo
TipoContrato.TIEMPO_PARCIAL=Tiempo Parcial
TipoContrato.EVENTUAL=Eventual
TipoContrato.PASANTIA=Pasantía
TipoContrato.POR_OBRA=Por Obra
TipoContrato.FREELANCE=Freelance

# ModalidadTrabajo
ModalidadTrabajo.PRESENCIAL=Presencial
ModalidadTrabajo.REMOTO=Remoto
ModalidadTrabajo.HIBRIDO=Híbrido
ModalidadTrabajo.ITINERANTE=Itinerante
ModalidadTrabajo.CAMPO=Trabajo en Campo

# NivelJerarquico (28 niveles)
NivelJerarquico.PRESIDENTE=Presidente / CEO
NivelJerarquico.VICEPRESIDENTE=Vicepresidente
NivelJerarquico.DIRECTOR_GENERAL=Director General
NivelJerarquico.DIRECTOR=Director
NivelJerarquico.SUBDIRECTOR=Subdirector
NivelJerarquico.GERENTE=Gerente
NivelJerarquico.SUBGERENTE=Subgerente
NivelJerarquico.JEFE=Jefe
NivelJerarquico.SUBJEFE=Subjefe
NivelJerarquico.COORDINADOR=Coordinador
NivelJerarquico.SUPERVISOR=Supervisor
NivelJerarquico.LIDER=Líder de Equipo
NivelJerarquico.ESPECIALISTA=Especialista
NivelJerarquico.PROFESIONAL_SENIOR=Profesional Senior
NivelJerarquico.PROFESIONAL_SEMI_SENIOR=Profesional Semi-Senior
NivelJerarquico.PROFESIONAL_JUNIOR=Profesional Junior
NivelJerarquico.TECNICO_SENIOR=Técnico Senior
NivelJerarquico.TECNICO=Técnico
NivelJerarquico.TECNICO_JUNIOR=Técnico Junior
NivelJerarquico.ADMINISTRATIVO_SENIOR=Administrativo Senior
NivelJerarquico.ADMINISTRATIVO=Administrativo
NivelJerarquico.AUXILIAR_ADMINISTRATIVO=Auxiliar Administrativo
NivelJerarquico.OPERARIO_SENIOR=Operario Senior
NivelJerarquico.OPERARIO=Operario
NivelJerarquico.AYUDANTE=Ayudante
NivelJerarquico.APRENDIZ=Aprendiz
NivelJerarquico.PASANTE=Pasante
NivelJerarquico.BECARIO=Becario
```

---

## ✅ Plan Listo para Implementar

Este es el **Plan V4 Final**. ¿Aprobás comenzar la implementación?

Si es así, empiezo con la **Fase 1** (enums y servicio de cálculo).

---

*Plan V4 Final - 2026-01-16 00:12 - STA.RH Sistema Biométrico*
