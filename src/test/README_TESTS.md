# Guía de Ejecución de Pruebas Automáticas

## Tests Creados

### 1. **PersonalTest** - 6 tests
- ✅ CRUD completo de empleados
- ✅ Validación de campos
- ✅ Búsqueda y filtrado
- ✅ Cálculo de nombre completo

### 2. **TurnosHorariosTest** - 7 tests
- ✅ Creación de turnos completos
- ✅ Validaciones de negocio
- ✅ Turnos nocturnos (cruza medianoche)
- ✅ Generación secuencial de códigos
- ✅ Agrupación de horarios

### 3. **AuditoriaRegistrosTest** - 17 tests
- ✅ Tests de actualización de notas (6 tests)
- ✅ Tests de getTurnoPlanificado (3 tests)
- ✅ Tests de cálculo de horas (6 tests)
- ✅ Tests getDiaSemana (2 tests)

## Cómo Ejecutar los Tests

### Desde Maven (Línea de Comandos)

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar un test específico
mvn test -Dtest=PersonalTest
mvn test -Dtest=TurnosHorariosTest
mvn test -Dtest=AuditoriaRegistrosTest

# Ejecutar un método específico
mvn test -Dtest=AuditoriaRegistrosTest#testGetTurnoPlanificado_ConTurnoYHorarios
```

### Desde IDE (Eclipse/IntelliJ)

1. **Eclipse**: Click derecho en el archivo de test → Run As → JUnit Test
2. **IntelliJ**: Click derecho en el archivo de test → Run 'NombreTest'

## Configuración Requerida

Los tests de tipo `ModuleTestBase` (PersonalTest, TurnosHorariosTest) requieren:

1. **Aplicación corriendo**: La aplicación debe estar desplegada y corriendo
2. **Usuario admin**: Debe existir usuario "admin" con password "admin"
3. **Base de datos**: Debe estar configurada y accesible

Los tests unitarios (AuditoriaRegistrosTest) no requieren la aplicación corriendo.

## Cobertura

```
Total Tests: 30
├── Unit Tests: 17 (AuditoriaRegistrosTest)
└── Module Tests: 13 (PersonalTest + TurnosHorariosTest)
```

## Próximos Pasos Recomendados

1. **Agregar más tests de módulos**:
   - FeriadosTest
   - LicenciaTest
   - ColeccionRegistrosTest

2. **Tests de integración**:
   - Tests de consolidación completa
   - Tests de importación de fichadas
   - Tests de cálculo de horas de rango

3. **Tests de acciones personalizadas**:
   - ConsolidarRegistrosActionTest
   - ImportarFichadasActionTest

## Notas Importantes

- Los tests de `ModuleTestBase` simulan un navegador usando HtmlUnit
- Requieren que la aplicación esté desplegada (con Tomcat u otro servidor)
- Los tests unitarios son más rápidos y no requieren despliegue
- Se recomienda separar tests unitarios de tests de integración

## Ejecución Continua (CI/CD)

Para ejecutar en un pipeline CI/CD, considera:

```yaml
# Ejemplo GitHub Actions
- name: Run Unit Tests
  run: mvn test -Dtest=*Test -DfailIfNoTests=false
  
- name: Run Integration Tests
  run: mvn test -Dtest=*IT -DfailIfNoTests=false
```
