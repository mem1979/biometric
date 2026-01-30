# Plan de Implementación: Entidad ConfiguracionLaboral

## 📋 Resumen Ejecutivo

Este plan detalla la creación de una nueva entidad `ConfiguracionLaboral` que centraliza y organiza los parámetros laborales del empleado, actualmente distribuidos en la entidad `Personal.java`. El objetivo es mejorar la arquitectura, permitir el cálculo automático de valores por hora basándose en un sueldo mensual acordado, y proporcionar una mejor estructura para la información del puesto y función del empleado.

---

## 🔍 Análisis del Estado Actual

### Campos actuales en `Personal.java` relacionados con la configuración laboral

| Campo | Tipo | Ubicación Actual | Propósito |
|-------|------|------------------|-----------|
| `puesto` | String | Personal.java:580 | Cargo/puesto del empleado |
| `sucursal` | Sucursales | Personal.java:648 | Ubicación de trabajo |
| `inicioActividades` | LocalDate | Personal.java:593 | Fecha de ingreso |
| `valorHora` | BigDecimal | Personal.java:841 | Valor hora normal |
| `porcentajeHoraExtra` | BigDecimal | Personal.java:856 | % adicional horas extras |
| `porcentajeHoraEspecial` | BigDecimal | Personal.java:892 | % adicional horas especiales |

### Vista actual del grupo "Honorarios" (imagen proporcionada)

- **Valor Hora**: $7,800.00
- **% Hora Extra**: 50.0 → Valor hora extra: 11,700.00
- **% Hora Especial**: 100.0 → Valor hora especial: 15,600.00

### Vista actual del grupo "Función"

- **Sucursal/Sector**: CENTRAL
- **Inicio de Actividades**: 01/01/2025 → Antigüedad calculada
- **Puesto**: Gerente

---

## 🎯 Objetivos de la Implementación

### 1. **Crear entidad `ConfiguracionLaboral`**

- Centralizar toda la información laboral-económica del empleado
- Separar responsabilidades de la entidad `Personal`
- Facilitar el mantenimiento y la extensibilidad

### 2. **Nuevo campo: Sueldo Mensual Acordado**

- Permitir configurar el sueldo bruto mensual
- **Cálculo automático del valor hora**: `sueldoMensual / horasMensualesLegales`
- Mantener la opción de sobrescribir manualmente el valor hora

### 3. **Ampliar información del Puesto/Función**

- Agregar campos para mayor detalle del cargo
- Posibilidad de vincular a un catálogo de puestos

### 4. **Mantener compatibilidad hacia atrás**

- Los registros históricos (AuditoriaRegistros, LiquidacionJornadas) ya usan snapshots
- Los valores calculados deben seguir funcionando correctamente

---

## 🏗️ Diseño Propuesto

### Opción A: Entidad Separada `ConfiguracionLaboral` (RECOMENDADA)

```
┌─────────────────────────────────────────────────────────────────┐
│                      ConfiguracionLaboral                        │
├─────────────────────────────────────────────────────────────────┤
│ PUESTO Y FUNCIÓN                                                │
│ ├── categoria (enum CategoriaLaboral)                           │
│ ├── puesto (String) - Título del cargo                          │
│ ├── descripcionFunciones (String) - Descripción del rol         │
│ ├── nivelJerarquico (enum NivelJerarquico)                      │
│ └── departamento/area (String o relación)                       │
├─────────────────────────────────────────────────────────────────┤
│ ASIGNACIÓN ORGANIZACIONAL                                       │
│ ├── sucursal (Sucursales) - Ubicación de trabajo                │
│ ├── inicioActividades (LocalDate)                               │
│ └── antiguedadLaboral (calculado)                               │
├─────────────────────────────────────────────────────────────────┤
│ CONFIGURACIÓN ECONÓMICA                                         │
│ ├── sueldoMensualAcordado (BigDecimal) - NUEVO                  │
│ ├── horasMensualesLegales (int = 200 por defecto)              │
│ ├── valorHoraCalculado (getter calculado)                       │
│ ├── valorHoraManual (BigDecimal) - Sobrescritura opcional       │
│ ├── porcentajeHoraExtra (BigDecimal)                            │
│ ├── porcentajeHoraEspecial (BigDecimal)                         │
│ ├── getValorHoraEfectivo() - Retorna manual si existe, o calc.  │
│ ├── getValorHoraExtra() - calculado                             │
│ └── getValorHoraEspecial() - calculado                          │
├─────────────────────────────────────────────────────────────────┤
│ METADATOS                                                       │
│ ├── fechaVigenciaDesde (LocalDate)                              │
│ ├── fechaVigenciaHasta (LocalDate) - null = vigente             │
│ ├── activo (boolean)                                            │
│ └── fechaModificacion (LocalDateTime)                           │
└─────────────────────────────────────────────────────────────────┘
```

### Opción B: Entidad Embebida (Clase @Embeddable)

- Se embebe directamente en `Personal`
- Menos flexibilidad pero más simple
- No permite historial de configuraciones

### **Decisión Recomendada: Opción A con relación OneToOne**

La relación OneToOne permite:

- Historial de configuraciones (si se cambia a OneToMany en el futuro)
- Mejor separación de responsabilidades
- Facilita la reutilización del esquema

---

## 📁 Estructura de Archivos a Crear/Modificar

### Nuevos Archivos

```
src/main/java/com/sta/biometric/
├── modelo/
│   └── ConfiguracionLaboral.java          # Nueva entidad principal
├── enums/
│   ├── CategoriaLaboral.java              # Enum para categorías
│   └── NivelJerarquico.java               # Enum para niveles
└── calculadores/
    └── CalculadorValorHoraCalculator.java # Calculador automático
```

### Archivos a Modificar

```
src/main/java/com/sta/biometric/modelo/Personal.java
  - Agregar relación @OneToOne a ConfiguracionLaboral
  - Modificar getters de valorHora, valorHoraExtra, valorHoraEspecial
  - Actualizar @View para incluir la nueva entidad
  - Mantener compatibilidad hacia atrás

src/main/resources/i18n/biometric-labels_es.properties
  - Agregar etiquetas para los nuevos campos

src/main/resources/xava/controladores.xml
  - Agregar módulo ConfiguracionLaboral si se desea gestión independiente
```

---

## 📝 Especificación Detallada de la Entidad

### ConfiguracionLaboral.java

```java
@Entity
@Getter @Setter
@View(members = "DatosDelPuesto { categoria, puesto; nivelJerarquico; descripcionFunciones }; " +
                "Ubicacion { sucursal; inicioActividades, antiguedadLaboral }; " +
                "Economia { sueldoMensualAcordado, horasMensualesLegales; " +
                "          valorHoraCalculado; valorHoraManual; " +
                "          porcentajeHoraExtra, valorHoraExtra; " +
                "          porcentajeHoraEspecial, valorHoraEspecial }")
public class ConfiguracionLaboral extends Identifiable {

    // PUESTO Y FUNCIÓN
    @Enumerated(EnumType.STRING)
    private CategoriaLaboral categoria;
    
    @Capitalizar
    @Column(length = 100)
    private String puesto;
    
    @Enumerated(EnumType.STRING)
    private NivelJerarquico nivelJerarquico;
    
    @TextArea
    @Column(length = 500)
    private String descripcionFunciones;
    
    // ASIGNACIÓN ORGANIZACIONAL
    @DescriptionsList
    @ManyToOne(fetch = FetchType.LAZY)
    private Sucursales sucursal;
    
    @Required
    @Stereotype("FECHA")
    private LocalDate inicioActividades;
    
    // CONFIGURACIÓN ECONÓMICA
    @Money
    @Column(precision = 12, scale = 2)
    private BigDecimal sueldoMensualAcordado;
    
    @DefaultValueCalculator(value = org.openxava.calculators.IntegerCalculator.class, 
                           properties = @PropertyValue(name="value", value="200"))
    private int horasMensualesLegales = 200;
    
    @Money
    @Column(precision = 10, scale = 2)
    private BigDecimal valorHoraManual;  // Sobrescribe el calculado si existe
    
    @Digits(integer = 3, fraction = 1)
    @Min(0) @Max(200)
    private BigDecimal porcentajeHoraExtra;
    
    @Digits(integer = 3, fraction = 1)
    @Min(0) @Max(200)
    private BigDecimal porcentajeHoraEspecial;
    
    // METADATOS
    @ReadOnly
    private LocalDate fechaVigenciaDesde;
    
    private LocalDate fechaVigenciaHasta;  // null = vigente
    
    // GETTERS CALCULADOS
    @Label
    @Money
    @Depends("sueldoMensualAcordado, horasMensualesLegales")
    public BigDecimal getValorHoraCalculado() {
        if (sueldoMensualAcordado == null || horasMensualesLegales <= 0) {
            return BigDecimal.ZERO;
        }
        return sueldoMensualAcordado.divide(
            BigDecimal.valueOf(horasMensualesLegales), 2, RoundingMode.HALF_UP);
    }
    
    @Transient
    @Money
    public BigDecimal getValorHoraEfectivo() {
        if (valorHoraManual != null && valorHoraManual.compareTo(BigDecimal.ZERO) > 0) {
            return valorHoraManual;
        }
        return getValorHoraCalculado();
    }
    
    @Label
    @Money
    @Depends("sueldoMensualAcordado, horasMensualesLegales, valorHoraManual, porcentajeHoraExtra")
    public BigDecimal getValorHoraExtra() {
        BigDecimal base = getValorHoraEfectivo();
        if (base != null && porcentajeHoraExtra != null) {
            BigDecimal adicional = base.multiply(porcentajeHoraExtra)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return base.add(adicional);
        }
        return base;
    }
    
    @Label
    @Money
    @Depends("sueldoMensualAcordado, horasMensualesLegales, valorHoraManual, porcentajeHoraEspecial")
    public BigDecimal getValorHoraEspecial() {
        BigDecimal base = getValorHoraEfectivo();
        if (base != null && porcentajeHoraEspecial != null) {
            BigDecimal adicional = base.multiply(porcentajeHoraEspecial)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return base.add(adicional);
        }
        return base;
    }
    
    @Label
    @Depends("inicioActividades")
    public String getAntiguedadLaboral() {
        // Misma lógica actual de Personal
    }
}
```

---

## 🔗 Enums Propuestos

### CategoriaLaboral.java

```java
public enum CategoriaLaboral {
    OPERATIVO("Operativo"),
    ADMINISTRATIVO("Administrativo"),
    TECNICO("Técnico"),
    PROFESIONAL("Profesional"),
    SUPERVISORIO("Supervisorio"),
    GERENCIAL("Gerencial"),
    DIRECTIVO("Directivo");
    
    private final String descripcion;
}
```

### NivelJerarquico.java

```java
public enum NivelJerarquico {
    JUNIOR("Junior / Inicial"),
    SEMI_SENIOR("Semi-Senior"),
    SENIOR("Senior"),
    LIDER("Líder / Team Lead"),
    JEFE("Jefe"),
    GERENTE("Gerente"),
    DIRECTOR("Director"),
    C_LEVEL("C-Level (CEO, CFO, etc.)");
    
    private final String descripcion;
}
```

---

## 🔄 Cambios en Personal.java

### Nueva relación

```java
@AsEmbedded  // Muestra embebido en la vista de Personal
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "configuracion_laboral_id")
private ConfiguracionLaboral configuracionLaboral;
```

### Modificación de getters para mantener compatibilidad

```java
// Delega a ConfiguracionLaboral manteniendo la API pública
@Transient
public BigDecimal getValorHora() {
    return configuracionLaboral != null ? 
           configuracionLaboral.getValorHoraEfectivo() : 
           this.valorHora; // Fallback al campo legacy
}

@Transient
public BigDecimal getValorHoraExtra() {
    return configuracionLaboral != null ? 
           configuracionLaboral.getValorHoraExtra() : 
           calcularValorHoraExtraLegacy();
}
```

### Actualización de la Vista

```java
@View(members = "nombreCompleto, turnoActivoHoy;" +
    "InformacionPersonal { ... }; " +
    "InformacionLaboral { " +
    "   credenciales[...], " +
    "   configuracionLaboral; " +  // ← Nueva referencia embebida
    "}; " +
    // resto...
)
```

---

## 📊 Fases de Implementación

### Fase 1: Preparación (1-2 días)

- [ ] Crear enums `CategoriaLaboral` y `NivelJerarquico`
- [ ] Crear entidad `ConfiguracionLaboral.java`
- [ ] Agregar etiquetas i18n

### Fase 2: Integración Básica (2-3 días)

- [ ] Agregar relación en `Personal.java`
- [ ] Actualizar vistas de `Personal`
- [ ] Crear script de migración de datos existentes

### Fase 3: Lógica de Negocio (2-3 días)

- [ ] Implementar cálculo de valor hora desde sueldo mensual
- [ ] Modificar getters en `Personal` para delegación
- [ ] Actualizar `AuditoriaRegistros` para usar nuevos métodos
- [ ] Actualizar `LiquidacionJornadas` para capturar snapshots correctos

### Fase 4: Pruebas y Validación (2-3 días)

- [ ] Pruebas unitarias de cálculos
- [ ] Pruebas de integración con registros existentes
- [ ] Validar que liquidaciones y reportes funcionen correctamente
- [ ] Pruebas de migración en ambiente de prueba

### Fase 5: Migración de Datos (1-2 días)

- [ ] Script SQL para migrar datos de campos existentes
- [ ] Crear `ConfiguracionLaboral` para cada `Personal` existente
- [ ] Validar integridad de datos migrados

---

## 🗃️ Script de Migración SQL (Propuesto)

```sql
-- 1. Crear tabla configuracion_laboral
CREATE TABLE configuracion_laboral (
    id VARCHAR(32) PRIMARY KEY,
    puesto VARCHAR(100),
    categoria VARCHAR(30),
    nivel_jerarquico VARCHAR(30),
    descripcion_funciones VARCHAR(500),
    sucursal_id VARCHAR(32),
    inicio_actividades DATE,
    sueldo_mensual_acordado DECIMAL(12,2),
    horas_mensuales_legales INT DEFAULT 200,
    valor_hora_manual DECIMAL(10,2),
    porcentaje_hora_extra DECIMAL(4,1),
    porcentaje_hora_especial DECIMAL(4,1),
    fecha_vigencia_desde DATE,
    fecha_vigencia_hasta DATE,
    FOREIGN KEY (sucursal_id) REFERENCES sucursales(id)
);

-- 2. Migrar datos desde Personal
INSERT INTO configuracion_laboral (id, puesto, sucursal_id, inicio_actividades, 
                                   valor_hora_manual, porcentaje_hora_extra, 
                                   porcentaje_hora_especial, fecha_vigencia_desde)
SELECT UUID(), puesto, sucursal_id, inicio_actividades, 
       valor_hora, porcentaje_hora_extra, porcentaje_hora_especial,
       inicio_actividades
FROM personal
WHERE eliminado = false;

-- 3. Agregar columna FK en Personal
ALTER TABLE personal ADD COLUMN configuracion_laboral_id VARCHAR(32);

-- 4. Vincular registros
-- (script con UPDATE para vincular cada Personal con su ConfiguracionLaboral)
```

---

## ⚠️ Consideraciones Importantes

### 1. **Compatibilidad con Sistemas Existentes**

- `AuditoriaRegistros` ya guarda snapshots (`valorHoraSnapshot`) ✓
- `LiquidacionJornadas` ya captura valores en `capturarValoresSnapshot()` ✓
- Los reportes JasperReports seguirán funcionando ✓

### 2. **Valores por Defecto**

- `horasMensualesLegales = 200` (estándar en Argentina)
- Si no hay sueldo mensual, usar valor hora manual
- Si no hay ninguno, valor hora = 0

### 3. **Validaciones de Negocio**

- Al menos uno debe tener valor: `sueldoMensualAcordado` o `valorHoraManual`
- Los porcentajes deben estar entre 0 y 200%

### 4. **Interfaz de Usuario**

- Mostrar claramente si el valor hora es calculado o manual
- Indicador visual cuando se sobrescribe el valor calculado

---

## 📋 Checklist Pre-Implementación

- [ ] ¿Confirmar el nombre de la entidad: `ConfiguracionLaboral`?
- [ ] ¿Usar los enums propuestos o simplificar?
- [ ] ¿Mantener el campo `puesto` como String o crear catálogo de puestos?
- [ ] ¿Horas mensuales legales: 200 por defecto es correcto?
- [ ] ¿Se requiere historial de configuraciones (múltiples vigencias)?
- [ ] ¿Prioridad del valor hora: manual sobrescribe calculado?

---

## 🚀 Próximos Pasos

Una vez aprobado el plan:

1. **Definir decisiones pendientes** del checklist
2. **Comenzar con Fase 1**: Creación de enums y entidad
3. **Revisar la vista** antes de integrar en Personal
4. **Probar en entorno de desarrollo** antes de scripts de migración

---

*Documento generado el 2026-01-15 - STA.RH Sistema Biométrico*
