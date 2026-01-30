# Plan de Implementación V3: Entidad ContratoLaboral

## 📋 Resumen de Cambios desde V2

| Aspecto | V2 | V3 (Ahora) |
|---------|----|----|
| CategoriaLaboral | Enum | **Entidad** en `auxiliares/` |
| NivelJerarquico | Enum | **Entidad** con `@Tree` jerárquico |
| Menú lateral | No mencionado | Ocultar entidades auxiliares |
| Ubicación/Sucursal | En ContratoLaboral | **Queda en Personal** |
| inicioActividades | En ContratoLaboral | **Queda en Personal** |
| antiguedadLaboral | En ContratoLaboral | **Queda en Personal** |
| valorHoraAjustado | Campo simple | **Explicado en detalle** |

---

## 🎯 Decisiones Finales Confirmadas

### 1. CategoriaLaboral y NivelJerarquico como Entidades

- ✅ Serán **entidades JPA** en el paquete `auxiliares/`
- ✅ **Ocultadas del menú** lateral (leftMenu.jsp)
- ✅ Accedidas **solo desde ContratoLaboral** como relaciones `@ManyToOne`
- ✅ NivelJerarquico con **estructura jerárquica** auto-referenciada para `@Tree`

### 2. Fórmula de cálculo

- ✅ `horasMensuales = horasSemanales × 4.33` (aceptado)

### 3. Enfoque valorHoraAjustado (Explicación detallada)

El sistema tendrá **dos valores de hora** para máxima flexibilidad:

```
┌─────────────────────────────────────────────────────────────────────┐
│  FLUJO DE CÁLCULO DEL VALOR HORA                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   sueldoMensualAcordado                                             │
│           │                                                         │
│           ▼                                                         │
│   ÷ horasMensualesEsperadas (desde turnos)                          │
│           │                                                         │
│           ▼                                                         │
│   ┌───────────────────┐                                             │
│   │ valorHoraCalculado│  ← Automático, siempre actualizado          │
│   │ (solo lectura)    │    Cambia si cambia sueldo o turno          │
│   └───────────────────┘                                             │
│           │                                                         │
│           │  ¿Existe valorHoraAjustado?                             │
│           ▼                                                         │
│   ┌───────────────────┐     ┌────────────────────┐                  │
│   │ valorHoraAjustado │     │ valorHoraCalculado │                  │
│   │ (editable)        │ OR  │ (si ajustado=null) │                  │
│   └───────────────────┘     └────────────────────┘                  │
│           │                         │                               │
│           └────────────┬────────────┘                               │
│                        ▼                                            │
│              ┌─────────────────────┐                                │
│              │ valorHoraEfectivo   │  ← Este se usa para:           │
│              │ (el que se aplica)  │    • Liquidaciones             │
│              └─────────────────────┘    • Descuentos x ausencias    │
│                        │                • Cálculo de extras         │
│                        ▼                • Snapshots de auditoría    │
│              × porcentajeHoraExtra                                  │
│              × porcentajeHoraEspecial                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

**¿Cuándo usar `valorHoraAjustado`?**

| Caso de Uso | ¿Por qué? |
|-------------|-----------|
| Empleado sin turno asignado | `valorHoraCalculado = 0`, se necesita valor manual |
| Bonificación especial | El empleado tiene un plus sobre el calculado |
| Descuento acordado | Se pactó un valor diferente al que surge del cálculo |
| Redondeo conveniente | Se prefiere usar un valor "redondo" para facilitar cálculos |
| Transición de sueldo | El sueldo cambió pero se quiere mantener temporalmente el valor anterior |

**Comportamiento:**

- Si `valorHoraAjustado = null` → Se usa `valorHoraCalculado`
- Si `valorHoraAjustado > 0` → Se usa `valorHoraAjustado` (sobrescribe)
- El usuario puede limpiar `valorHoraAjustado` para volver al cálculo automático

### 4. Campos de Ubicación: Quedan en Personal ✅

Estoy **100% de acuerdo** con tu análisis. Los campos de ubicación **no pertenecen al contrato**:

| Campo | ¿Dónde debe estar? | Justificación |
|-------|-------------------|---------------|
| `sucursal` | **Personal** | Es donde trabaja el empleado, no parte de su compensación |
| `inicioActividades` | **Personal** | Es cuando empezó a trabajar, no parte del contrato económico |
| `antiguedadLaboral` | **Personal** | Se calcula desde `inicioActividades` |

**ContratoLaboral** debe enfocarse en:

- **Puesto/Rol** → Qué hace el empleado (categoría, nivel, descripción)
- **Compensación** → Cuánto gana (sueldo, valores hora, porcentajes)
- **Vigencia** → Desde/hasta cuándo aplica este contrato

---

## 🏗️ Diseño Revisado: NivelJerarquico con @Tree

### Estructura Jerárquica para Organigrama

La entidad `NivelJerarquico` será **auto-referenciada** para crear una estructura de árbol que represente el organigrama:

```
                         C-Level (CEO)
                              │
               ┌──────────────┼──────────────┐
               ▼              ▼              ▼
           Director       Director       Director
          (Comercial)     (Operaciones)  (Finanzas)
               │              │              │
          ┌────┴────┐    ┌────┴────┐    ┌────┴────┐
          ▼         ▼    ▼         ▼    ▼         ▼
       Gerente   Gerente Gerente  Gerente Gerente Gerente
          │         │       │        │       │       │
          ▼         ▼       ▼        ▼       ▼       ▼
       Jefes     Jefes   Jefes    Jefes   Jefes   Jefes
          │         │       │        │       │       │
          ▼         ▼       ▼        ▼       ▼       ▼
       Líderes  Líderes Líderes  Líderes Líderes Líderes
          │         │       │        │       │       │
          ▼         ▼       ▼        ▼       ▼       ▼
       Staff    Staff   Staff    Staff   Staff   Staff
```

### Implementación con @Tree de OpenXava

```java
@Entity
@Getter @Setter
@Tab(properties = "nombre, path, orden")
public class NivelJerarquico extends Identifiable {
    
    @Required
    @Column(length = 50)
    private String nombre;  // Ej: "Gerente", "Jefe", "Líder"
    
    private int orden;  // Para ordenamiento (1=más alto, 11=más bajo)
    
    @TextArea
    @Column(length = 200)
    private String descripcion;
    
    /** Nivel superior en la jerarquía (null = nivel raíz) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_padre_id")
    @DescriptionsList(descriptionProperties = "nombre")
    private NivelJerarquico nivelPadre;
    
    /** Path materializado para consultas eficientes */
    @Column(length = 500)
    @ReadOnly
    private String path;  // Ej: "/C-Level/Director/Gerente"
    
    /** Subordinados directos (para visualización @Tree) */
    @OneToMany(mappedBy = "nivelPadre")
    @OrderBy("orden")
    @Tree  // ← Visualización jerárquica de OpenXava
    private Collection<NivelJerarquico> subordinados;
    
    @PrePersist
    @PreUpdate
    private void calcularPath() {
        if (nivelPadre == null) {
            this.path = "/" + nombre;
        } else {
            this.path = nivelPadre.getPath() + "/" + nombre;
        }
    }
}
```

**Beneficios del @Tree:**

- Visualización tipo organigrama expandible
- Navegación intuitiva de la estructura
- Path materializado para queries eficientes
- Fácil determinar "quién reporta a quién"

---

## 📁 Estructura de Archivos Final

### Nuevos Archivos a Crear

```
src/main/java/com/sta/biometric/
├── auxiliares/
│   ├── CategoriaLaboral.java         # Entidad (clasificación funcional)
│   └── NivelJerarquico.java          # Entidad con @Tree (jerarquía)
├── modelo/
│   └── ContratoLaboral.java          # Entidad principal
└── servicios/
    └── CalculadorHorasService.java   # Servicio de cálculo

src/main/resources/i18n/
└── biometric-labels_es.properties    # Etiquetas nuevas
```

### Archivos a Modificar

```
src/main/webapp/naviox/leftMenu.jsp
  - Agregar a modulosOcultos:
    • "biometric/CategoriaLaboral"
    • "biometric/NivelJerarquico"
    • "biometric/ContratoLaboral"

src/main/java/com/sta/biometric/modelo/Personal.java
  - Agregar relación @OneToMany a ContratoLaboral
  - Método getContratoVigente()
  - Modificar getters de valorHora para delegación
  - Mantener: sucursal, inicioActividades, antiguedadLaboral
```

---

## 📝 Especificación: CategoriaLaboral.java

```java
package com.sta.biometric.auxiliares;

import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.model.*;
import lombok.*;

/**
 * Clasificación funcional de los puestos de trabajo.
 * 
 * <p>Define el tipo de actividades y responsabilidades 
 * asociadas a un cargo.</p>
 * 
 * <p>Ejemplos: Operativo, Administrativo, Técnico, Profesional, etc.</p>
 */
@Entity
@Table(name = "categoria_laboral")
@Getter @Setter
@Tab(properties = "codigo, nombre, descripcion")
public class CategoriaLaboral extends Identifiable {
    
    /** Código corto de la categoría (ej: "OPE", "ADM", "TEC") */
    @Required
    @Column(length = 5, unique = true)
    private String codigo;
    
    /** Nombre descriptivo de la categoría */
    @Required
    @Column(length = 50)
    private String nombre;
    
    /** Descripción detallada de las actividades típicas */
    @TextArea
    @Column(length = 500)
    private String descripcion;
    
    /** Indica si la categoría está activa para nuevas asignaciones */
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean activo = true;
    
    @Override
    public String toString() {
        return nombre;
    }
}
```

**Datos iniciales sugeridos:**

| Código | Nombre | Descripción |
|--------|--------|-------------|
| `OPE` | Operativo | Tareas manuales o de producción directa |
| `ADM` | Administrativo | Gestión documental y atención |
| `TEC` | Técnico | Conocimientos técnicos especializados |
| `PRO` | Profesional | Ejercicio de profesión con título |
| `COM` | Comercial | Ventas y relaciones comerciales |
| `SUP` | Supervisorio | Supervisión de equipos |
| `GER` | Gerencial | Gestión de áreas/departamentos |
| `DIR` | Directivo | Alta dirección estratégica |

---

## 📝 Especificación: NivelJerarquico.java (con @Tree)

```java
package com.sta.biometric.auxiliares;

import java.util.*;
import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.model.*;
import lombok.*;

/**
 * Nivel jerárquico dentro de la estructura organizacional.
 * 
 * <p>Define la posición del empleado en el organigrama y permite
 * visualizar la estructura con @Tree de OpenXava.</p>
 * 
 * <p>Implementa auto-referencia para crear jerarquía padre-hijo.</p>
 * 
 * @see ContratoLaboral
 */
@Entity
@Table(name = "nivel_jerarquico")
@Getter @Setter
@View(members = "codigo, nombre; orden; descripcion; nivelPadre; subordinados")
@Tab(properties = "codigo, nombre, path, orden", defaultOrder = "${orden} asc")
public class NivelJerarquico extends Identifiable {
    
    /** Código corto del nivel (ej: "GER", "JEF", "LID") */
    @Required
    @Column(length = 5, unique = true)
    private String codigo;
    
    /** Nombre del nivel jerárquico */
    @Required
    @Column(length = 50)
    private String nombre;
    
    /** Orden numérico (1 = más alto como CEO, 10+ = más bajo como aprendiz) */
    @Required
    @Min(1) @Max(20)
    private int orden;
    
    /** Descripción del nivel y responsabilidades típicas */
    @TextArea
    @Column(length = 500)
    private String descripcion;
    
    // =========================================================================
    // JERARQUÍA (para @Tree)
    // =========================================================================
    
    /** Nivel superior en la jerarquía (null = nivel raíz como CEO) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_padre_id")
    @DescriptionsList(descriptionProperties = "nombre")
    private NivelJerarquico nivelPadre;
    
    /** 
     * Path materializado para consultas eficientes.
     * Formato: "/CEO/Director/Gerente/Jefe"
     */
    @Column(length = 500)
    @ReadOnly
    private String path;
    
    /** Niveles subordinados directos (visualización @Tree) */
    @OneToMany(mappedBy = "nivelPadre", cascade = CascadeType.ALL)
    @OrderBy("orden")
    @Tree(pathProperty = "path", pathSeparator = "/")
    @ListProperties("nombre, descripcion")
    private Collection<NivelJerarquico> subordinados = new ArrayList<>();
    
    /** Indica si el nivel está activo para nuevas asignaciones */
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean activo = true;
    
    // =========================================================================
    // CALLBACKS
    // =========================================================================
    
    @PrePersist
    @PreUpdate
    private void calcularPath() {
        if (nivelPadre == null) {
            this.path = "/" + codigo;
        } else {
            this.path = nivelPadre.getPath() + "/" + codigo;
        }
    }
    
    // =========================================================================
    // MÉTODOS DE UTILIDAD
    // =========================================================================
    
    /** Retorna el nivel de profundidad en la jerarquía (0 = raíz) */
    @Transient
    public int getProfundidad() {
        if (path == null) return 0;
        return (int) path.chars().filter(c -> c == '/').count() - 1;
    }
    
    /** Verifica si es un nivel raíz (sin padre) */
    @Transient
    public boolean isNivelRaiz() {
        return nivelPadre == null;
    }
    
    /** Verifica si tiene subordinados */
    @Transient
    public boolean tieneSubordinados() {
        return subordinados != null && !subordinados.isEmpty();
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
```

**Datos iniciales sugeridos (estructura jerárquica):**

```
/ C_LEVEL (orden: 1)
    └── DIRECTOR (orden: 2)
        └── GERENTE (orden: 3)
            └── JEFE (orden: 4)
                └── COORDINADOR (orden: 5)
                    └── LIDER (orden: 6)
                        └── ESPECIALISTA (orden: 7)
                        └── SENIOR (orden: 8)
                            └── SEMI_SENIOR (orden: 9)
                                └── JUNIOR (orden: 10)
                                    └── APRENDIZ (orden: 11)
```

---

## 📝 Especificación: ContratoLaboral.java (Simplificado)

```java
package com.sta.biometric.modelo;

import java.math.*;
import java.time.*;
import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.model.*;
import com.sta.biometric.anotaciones.*;
import com.sta.biometric.auxiliares.*;
import com.sta.biometric.servicios.*;
import lombok.*;

/**
 * Contrato laboral de un empleado.
 * 
 * <p>Define el puesto, categoría, nivel jerárquico y la configuración
 * económica (sueldo, valores hora, porcentajes).</p>
 * 
 * <p>NO incluye ubicación (sucursal, inicio actividades) ya que esos
 * datos pertenecen a Personal.</p>
 * 
 * <p>Permite historial: múltiples contratos con fechas de vigencia.</p>
 */
@Entity
@Table(name = "contrato_laboral")
@Getter @Setter
@View(members = 
    "DatosDelPuesto { " +
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
    "  observaciones; " +
    "}")
@Tab(properties = "empleado.nombreCompleto, puesto, sueldoMensualAcordado, fechaVigenciaDesde, vigente",
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
    
    /** Clasificación funcional (Operativo, Administrativo, Técnico, etc.) */
    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList(descriptionProperties = "nombre")
    @NoCreate @NoModify
    private CategoriaLaboral categoria;
    
    /** Nivel en la estructura organizacional (con jerarquía @Tree) */
    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList(descriptionProperties = "nombre", order = "${orden} asc")
    @NoCreate @NoModify
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
     * Uso:
     * - null → Se usa valorHoraCalculado (automático)
     * - > 0  → Sobrescribe el calculado (se usa este valor)
     * 
     * Casos de uso:
     * - Empleado sin turno asignado
     * - Bonificación/descuento especial
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
    
    /** Observaciones sobre el contrato o cambios realizados */
    @TextArea
    @Column(length = 500)
    private String observaciones;
    
    /** Fecha de última modificación (auditoría) */
    @ReadOnly
    private LocalDateTime fechaModificacion;

    // =========================================================================
    // GETTERS CALCULADOS - HORAS
    // =========================================================================
    
    /**
     * Horas semanales esperadas según jornadas del empleado.
     */
    @Label
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "clock-outline")
    public String getHorasSemanalesEsperadas() {
        BigDecimal horas = CalculadorHorasService.calcularHorasSemanales(
            empleado, LocalDate.now());
        return horas.setScale(1, RoundingMode.HALF_UP) + " Hs/sem";
    }
    
    /**
     * Horas mensuales esperadas (horasSemanales × 4.33).
     */
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
    
    /**
     * Valor hora calculado: sueldoMensual / horasMensuales
     * Siempre se muestra como referencia (solo lectura).
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
     * Valor hora efectivo: usa ajustado si existe, sino el calculado.
     * ESTE ES EL VALOR QUE SE USA EN LIQUIDACIONES Y DESCUENTOS.
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
     * Valor hora especial: valorHoraEfectivo × (1 + porcentajeEspecial/100)
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

## 🔄 Modificación en leftMenu.jsp

Agregar las nuevas entidades a la lista de módulos ocultos:

```jsp
List<String> modulosOcultos = Arrays.asList(
    // ... existentes ...
    "biometric/CategoriaLaboral",
    "biometric/NivelJerarquico",
    "biometric/ContratoLaboral"
);
```

---

## 📊 Estructura Final de ContratoLaboral

```
┌─────────────────────────────────────────────────────────────────┐
│                        ContratoLaboral                          │
├─────────────────────────────────────────────────────────────────┤
│ 📋 DATOS DEL PUESTO                                             │
│ ├── categoria → CategoriaLaboral (entidad)                      │
│ ├── nivelJerarquico → NivelJerarquico (entidad con @Tree)       │
│ ├── puesto (String) - Título del cargo                          │
│ └── descripcionFunciones (TextArea)                             │
├─────────────────────────────────────────────────────────────────┤
│ 💰 CONFIGURACIÓN ECONÓMICA                                      │
│ ├── sueldoMensualAcordado                                       │
│ ├── getHorasSemanalesEsperadas() ← desde jornadas asignadas     │
│ ├── getHorasMensualesEsperadas() ← × 4.33                       │
│ ├── getValorHoraCalculado() ← sueldo / horas (solo lectura)     │
│ ├── valorHoraAjustado ← sobrescritura opcional                  │
│ ├── getValorHoraEfectivo() ← ajustado o calculado               │
│ ├── porcentajeHoraExtra, getValorHoraExtra()                    │
│ └── porcentajeHoraEspecial, getValorHoraEspecial()              │
├─────────────────────────────────────────────────────────────────┤
│ 📅 VIGENCIA (historial)                                         │
│ ├── fechaVigenciaDesde                                          │
│ ├── fechaVigenciaHasta (null = vigente)                         │
│ ├── isVigente() (calculado)                                     │
│ └── observaciones                                               │
├─────────────────────────────────────────────────────────────────┤
│ Campos que QUEDAN EN PERSONAL                                   │
│ ✓ sucursal                                                      │
│ ✓ inicioActividades                                             │
│ ✓ antiguedadLaboral                                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## ❓ Campos Adicionales a Considerar

Después de analizar el sistema, aquí hay campos que podrían ser útiles:

### Opcionales (sugerencia)

| Campo | Propósito | ¿Incluir? |
|-------|-----------|-----------|
| `tipoContrato` | Enum: TIEMPO_COMPLETO, MEDIO_TIEMPO, EVENTUAL | Sugerido |
| `modalidadTrabajo` | Enum: PRESENCIAL, REMOTO, HIBRIDO | Opcional |
| `fechaFinPeriodoPrueba` | Para contratos con período de prueba | Opcional |
| `motivoFinalizacion` | Texto si `fechaVigenciaHasta` no es null | Opcional |
| `documentoContrato` | Archivo adjunto del contrato firmado | Opcional |

**¿Querés que incluya alguno de estos campos?**

---

## 📊 Fases de Implementación Actualizadas

### Fase 1: Entidades Base (Día 1-2)

- [ ] Crear `CategoriaLaboral.java` en `auxiliares/`
- [ ] Crear `NivelJerarquico.java` con `@Tree` en `auxiliares/`
- [ ] Crear datos iniciales (script SQL o clase de carga)
- [ ] Actualizar `leftMenu.jsp` para ocultarlas

### Fase 2: Servicio de Cálculo (Día 2)

- [ ] Crear `CalculadorHorasService.java`
- [ ] Tests unitarios del servicio

### Fase 3: Entidad Principal (Día 2-3)

- [ ] Crear `ContratoLaboral.java`
- [ ] Agregar relación `@OneToMany` en `Personal.java`
- [ ] Método `getContratoVigente()`
- [ ] Agregar etiquetas i18n

### Fase 4: Integración (Día 3-4)

- [ ] Modificar getters en `Personal` para delegación
- [ ] Verificar `AuditoriaRegistros` y `LiquidacionJornadas`
- [ ] Ocultar `ContratoLaboral` del menú

### Fase 5: Migración y Pruebas (Día 4-5)

- [ ] Script de migración de datos existentes
- [ ] Pruebas integrales
- [ ] Verificar reportes

---

## ✅ Checklist de Validación V3

Por favor confirma antes de implementar:

- [ ] ¿CategoriaLaboral y NivelJerarquico como entidades está OK?
- [ ] ¿La estructura jerárquica @Tree para NivelJerarquico está OK?
- [ ] ¿El enfoque de valorHoraAjustado queda claro?
- [ ] ¿Sucursal/inicioActividades quedan en Personal - correcto?
- [ ] ¿Alguno de los campos adicionales sugeridos te interesa?
- [ ] ¿Algo más que agregar o modificar?

---

*Plan V3 - Generado el 2026-01-15 - STA.RH Sistema Biométrico*
