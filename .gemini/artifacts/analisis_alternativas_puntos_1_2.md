# Análisis de Alternativas: Puntos 1 y 2

## 📌 Punto 1: CategoriaLaboral y NivelJerarquico como Entidades

### Tu comentario: "Sí, parcialmente"

Necesito entender qué parte no te convence. Aquí presento las **opciones disponibles**:

---

### Opción 1A: Ambas como Entidades en `auxiliares/` (Plan V3 actual)

```
auxiliares/
├── CategoriaLaboral.java    # Entidad JPA
└── NivelJerarquico.java     # Entidad JPA
```

**Pros:**

- ✅ Administrable por el usuario (agregar/modificar categorías y niveles)
- ✅ Datos flexibles sin recompilar
- ✅ Puede incluir campos extra (descripción, código, orden)

**Contras:**

- ❌ Más complejidad (2 tablas adicionales en BD)
- ❌ Requiere ocultar del menú
- ❌ Datos iniciales deben cargarse

---

### Opción 1B: CategoriaLaboral como Entidad, NivelJerarquico como Enum

```
auxiliares/
└── CategoriaLaboral.java    # Entidad JPA (administrable)

enums/
└── NivelJerarquico.java     # Enum fijo (no administrable)
```

**Justificación:**

- Las **categorías** (Operativo, Administrativo, Técnico...) pueden cambiar según la empresa
- Los **niveles jerárquicos** (Junior, Senior, Gerente...) son más universales y raramente cambian

**Pros:**

- ✅ Categorización flexible
- ✅ Niveles predefinidos sin mantenimiento
- ✅ Menos complejidad

**Contras:**

- ❌ Si se necesita un nuevo nivel, hay que modificar código

---

### Opción 1C: Ambas como Enums

```
enums/
├── CategoriaLaboral.java    # Enum fijo
└── NivelJerarquico.java     # Enum fijo
```

**Pros:**

- ✅ Mínima complejidad
- ✅ Sin tablas adicionales
- ✅ Sin mantenimiento por parte del usuario

**Contras:**

- ❌ Requiere cambiar código para agregar valores
- ❌ No es flexible si la empresa tiene necesidades específicas

---

### Opción 1D: Separar en paquetes diferentes

```
auxiliares/
└── CategoriaLaboral.java    # Entidad de apoyo (catálogo)

modelo/
└── NivelJerarquico.java     # Entidad principal (con jerarquía @Tree)
```

**Justificación:**

- `CategoriaLaboral` es un catálogo simple (como `Sucursales`, `Nacionalidades`)
- `NivelJerarquico` es más complejo si incluye jerarquía auto-referenciada

---

### 🎯 Mi Recomendación: Opción 1B o 1D

**Si preferís simplicidad:** Opción 1B (Categoría como entidad, Nivel como enum)

**Si querés la jerarquía @Tree:** Opción 1D (separar en diferentes paquetes)

---

## 📌 Punto 2: Alternativas a @Tree para Organigrama

Dado que `@Tree` de OpenXava tiene limitaciones con entidades auto-referenciadas, aquí están las alternativas:

---

### Alternativa 2A: No usar @Tree - Solo jerarquía plana con `path`

**Concepto:** Mantener `NivelJerarquico` como entidad con `nivelPadre`, pero sin `@Tree`. Usar el campo `path` y `orden` para mostrar la jerarquía en forma tabulada.

```java
@Entity
public class NivelJerarquico extends Identifiable {
    private String codigo;           // "GER"
    private String nombre;           // "Gerente"
    private int orden;               // 3 (para ordenar)
    private String path;             // "/DIR/GER" (calculado)
    
    @ManyToOne
    private NivelJerarquico nivelPadre;  // Para relación
    
    // SIN @OneToMany subordinados - evita problemas
}
```

**Visualización en lista:**

```
| Orden | Código | Nombre       | Reporta a  | Path            |
|-------|--------|--------------|------------|-----------------|
| 1     | CEO    | C-Level      | -          | /CEO            |
| 2     | DIR    | Director     | C-Level    | /CEO/DIR        |
| 3     | GER    | Gerente      | Director   | /CEO/DIR/GER    |
| 4     | JEF    | Jefe         | Gerente    | /CEO/DIR/GER/JEF|
```

**Pros:**

- ✅ Simple y funcional
- ✅ El path permite queries jerárquicas (`LIKE '/CEO/DIR/%'`)
- ✅ No hay recursión infinita

**Contras:**

- ❌ No hay visualización de árbol expandible

---

### Alternativa 2B: Editor JSP Personalizado con JavaScript

**Concepto:** Crear un editor JSP custom que use una librería JavaScript para renderizar un organigrama interactivo.

**Librerías sugeridas:**

- **OrgChart.js** - Específica para organigramas
- **D3.js** - Muy potente para cualquier visualización
- **GoJS** - Profesional pero con licencia
- **treant-js** - Simple y gratuita

**Implementación:**

```java
// En la entidad
@Transient
@Editor("OrganigramaEditor")  // Editor custom
public Collection<NivelJerarquico> getOrganigrama() {
    // Retorna todos los niveles
}
```

```jsp
<!-- editors/OrganigramaEditor.jsp -->
<div id="organigrama"></div>
<script src="https://cdn.jsdelivr.net/npm/orgchart/dist/orgchart.min.js"></script>
<script>
    // Renderizar organigrama desde JSON
    var datasource = <%= request.getAttribute("organigramaData") %>;
    var orgchart = new OrgChart({ ... });
</script>
```

**Pros:**

- ✅ Visualización profesional de organigrama
- ✅ Interactivo (expandir, colapsar, zoom)
- ✅ Muy atractivo visualmente

**Contras:**

- ❌ Requiere desarrollo de editor custom
- ❌ Más complejidad de mantenimiento
- ❌ Dependencia de librería externa

---

### Alternativa 2C: Reporte PDF con JasperReports

**Concepto:** Generar un PDF del organigrama usando JasperReports.

**Pros:**

- ✅ Ya hay integración con JasperReports en el sistema
- ✅ Puede incluir fotos de empleados
- ✅ Imprimible/descargable

**Contras:**

- ❌ No es interactivo
- ❌ Estático (no se actualiza en tiempo real)

---

### Alternativa 2D: Simplificar - Sin jerarquía visual, solo datos

**Concepto:** NivelJerarquico es solo un catálogo ordenado sin auto-referencia. La jerarquía se infiere del campo `orden`.

```java
@Entity
public class NivelJerarquico extends Identifiable {
    private String codigo;
    private String nombre;
    private int orden;  // 1=CEO, 2=Director, 3=Gerente, etc.
    private String descripcion;
    
    // SIN nivelPadre - la jerarquía es implícita por el orden
}
```

**Pros:**

- ✅ Máxima simplicidad
- ✅ Sin problemas de recursión
- ✅ Suficiente para clasificar empleados

**Contras:**

- ❌ No se puede visualizar quién reporta a quién
- ❌ No es un organigrama real

---

### Alternativa 2E: Organigrama basado en Empleados (no en Niveles)

**Concepto:** En lugar de hacer jerarquía en `NivelJerarquico`, hacer la jerarquía en `Personal` (supervisor/supervisados).

```java
// En Personal.java
@ManyToOne
private Personal supervisor;  // "Reporta a"

@OneToMany(mappedBy = "supervisor")
@Tree  // Podría funcionar mejor porque representa personas reales
private Collection<Personal> supervisados;
```

**Pros:**

- ✅ Jerarquía real de empleados
- ✅ Puede visualizarse como organigrama real
- ✅ Más útil para reportes de gestión

**Contras:**

- ❌ @Tree puede seguir teniendo problemas
- ❌ Más complejo de mantener

---

## 🎯 Mi Recomendación para Punto 2

### Para empezar simple: **Alternativa 2A**

Mantener `NivelJerarquico` con `nivelPadre` y `path`, pero **sin @Tree**. La jerarquía se visualiza en la lista ordenada por `path`:

```
| Nivel          | Path                    |
|----------------|-------------------------|
| C-Level        | /C_LEVEL                |
| Director       | /C_LEVEL/DIRECTOR       |
| Gerente        | /C_LEVEL/DIRECTOR/GER   |
```

### Para futuro: **Alternativa 2B**

Si realmente necesitás un organigrama visual interactivo, desarrollar un **editor custom con OrgChart.js**. Pero esto puede hacerse en una fase posterior.

### Bonus: **Alternativa 2E**

Si además querés saber quién reporta a quién en términos de personas reales, agregar el campo `supervisor` en `Personal.java` es muy útil y complementa la clasificación por nivel.

---

## 📊 Resumen de Decisiones Pendientes

| Punto | Opciones | Mi Recomendación |
|-------|----------|------------------|
| **1: Entidades** | 1A (ambas entidades), 1B (categoría entidad, nivel enum), 1C (ambas enums), 1D (separar paquetes) | **1B** o **1D** |
| **2: Organigrama** | 2A (sin @Tree), 2B (editor custom), 2C (PDF), 2D (orden simple), 2E (en Personal) | **2A** ahora, **2B** o **2E** futuro |

---

## ❓ Preguntas para Definir

1. **¿Qué parte del Punto 1 no te convencía?** (categorías, niveles, o el paquete auxiliares/)

2. **Para el organigrama, ¿qué necesitás realmente?**
   - a) Solo clasificar empleados por nivel → Alternativa 2D
   - b) Ver estructura jerárquica en lista → Alternativa 2A
   - c) Visualización gráfica de árbol → Alternativa 2B (más complejo)
   - d) Saber quién reporta a quién por persona → Alternativa 2E

3. **¿La jerarquía de niveles es fija** (CEO → Director → Gerente...) o **cada empresa puede tener su propia estructura**?

Una vez me confirmes, ajusto el plan V4 final.

---

*Análisis de Alternativas - 2026-01-15*
