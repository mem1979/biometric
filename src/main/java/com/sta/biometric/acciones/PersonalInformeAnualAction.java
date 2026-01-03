package com.sta.biometric.acciones;

import java.math.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.dto.*;
import com.sta.biometric.dto.DatosDesempenoDTO.NotaResumenDTO;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

import net.sf.jasperreports.engine.*;

/**
 * Acción para generar el Informe Integrador Anual de un empleado.
 * 
 * Este informe consolida toda la información de asistencia, horas trabajadas,
 * tardanzas, ausencias, licencias, auditorías y costos laborales del año
 * calendario.
 */
public class PersonalInformeAnualAction extends JasperReportBaseAction {

    private static final int ANIO_ACTUAL = LocalDate.now().getYear();

    @Override
    protected JRDataSource getDataSource() throws Exception {
        return new JREmptyDataSource();
    }

    @Override
    protected String getJRXML() throws Exception {
        return "InformeAnualEmpleado.jrxml";
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map getParameters() throws Exception {
        Map params = new HashMap();

        // Obtener el empleado seleccionado
        Personal empleado = (Personal) MapFacade.findEntity(getModelName(), getView().getKeyValues());

        if (empleado == null) {
            addError("No se ha seleccionado ningún empleado.");
            return params;
        }

        // Definir rango del año
        LocalDate inicioAnio = LocalDate.of(ANIO_ACTUAL, 1, 1);
        LocalDate finAnio = LocalDate.of(ANIO_ACTUAL, 12, 31);

        // Obtener todos los registros del año
        List<AuditoriaRegistros> registrosAnuales = obtenerRegistrosAnuales(empleado, inicioAnio, finAnio);
        List<Licencia> licenciasAnuales = obtenerLicenciasAnuales(empleado);

        // ========== DETECTAR PERÍODO EFECTIVO DE TRABAJO ==========
        LocalDate primerRegistro = registrosAnuales.stream()
                .map(AuditoriaRegistros::getFecha)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(inicioAnio);

        LocalDate ultimoRegistro = registrosAnuales.stream()
                .map(AuditoriaRegistros::getFecha)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(finAnio);

        int mesesEfectivos = (int) ChronoUnit.MONTHS.between(
                primerRegistro.withDayOfMonth(1),
                ultimoRegistro.withDayOfMonth(1)) + 1;

        params.put("periodoInicio", primerRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("periodoFin", ultimoRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("mesesEfectivos", mesesEfectivos);

        // Calcular tardanzas una sola vez para usar en todo el informe (OPCIÓN B)
        int[] tardanzasData = calcularTardanzasUnificado(registrosAnuales);
        int totalTardanzas = tardanzasData[0];
        int minutosTotalesTardanza = tardanzasData[1];

        // 1. DATOS DEL EMPLEADO
        agregarDatosEmpleado(params, empleado);

        // 2. RESUMEN EJECUTIVO
        agregarResumenEjecutivo(params, empleado, registrosAnuales, licenciasAnuales,
                totalTardanzas, minutosTotalesTardanza, mesesEfectivos);

        // 3. ASISTENCIA CONSOLIDADA
        agregarAsistenciaConsolidada(params, registrosAnuales, mesesEfectivos);

        // 4. HORAS TRABAJADAS
        agregarHorasTrabajadas(params, empleado, registrosAnuales, mesesEfectivos);

        // 5. TARDANZAS Y PUNTUALIDAD (usa datos pre-calculados)
        agregarAnalisisTardanzas(params, registrosAnuales, totalTardanzas, minutosTotalesTardanza);

        // 6. AUSENCIAS Y LICENCIAS
        agregarAnalisisLicencias(params, licenciasAnuales, registrosAnuales);

        // 7. AUDITORÍAS Y CORRECCIONES
        agregarAuditorias(params, registrosAnuales);

        // 8. COSTO LABORAL
        agregarCostoLaboral(params, empleado, registrosAnuales);

        // 9. CONCLUSIONES Y RECOMENDACIONES
        agregarConclusiones(params, empleado, registrosAnuales, licenciasAnuales);

        // 10. DATOS PARA GRÁFICOS
        agregarDatosGraficos(params, registrosAnuales, licenciasAnuales);

        // Año del informe
        params.put("anioInforme", ANIO_ACTUAL);
        params.put("fechaGeneracion", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        return params;
    }

    // ==================== CÁLCULO UNIFICADO DE TARDANZAS (OPCIÓN B)
    // ====================

    /**
     * Calcula tardanzas con OPCIÓN B:
     * Solo cuenta como tardanza si el empleado entró tarde Y NO completó las horas
     * del turno.
     * Si entró tarde pero trabajó todas sus horas, NO se considera tardanza
     * evaluable.
     * 
     * @return int[2] donde [0]=totalTardanzas, [1]=minutosTotales
     */
    private int[] calcularTardanzasUnificado(List<AuditoriaRegistros> registros) {
        int totalTardanzas = 0;
        int minutosTotales = 0;

        for (AuditoriaRegistros reg : registros) {
            // Solo evaluar si hay hora esperada de entrada
            if (reg.getHoraEsperadaEntrada() == null)
                continue;

            // EXCLUIR jornadas incompletas (SIN SALIDA, EN CURSO, etc.)
            // Estas no son tardanzas reales, sino registros sin completar
            EvaluacionJornada eval = reg.getEvaluacion();
            if (eval == EvaluacionJornada.PENDIENTE ||
                    eval == EvaluacionJornada.EN_CURSO ||
                    eval == EvaluacionJornada.SIN_ENTRADA ||
                    eval == EvaluacionJornada.SIN_SALIDA) {
                continue;
            }

            // Buscar si hubo entrada tarde
            for (ColeccionRegistros fichada : reg.getRegistros()) {
                if (fichada.getTipoMovimiento() == TipoMovimiento.ENTRADA &&
                        "ENTRADA TARDE".equals(fichada.getEvaluacion())) {

                    // OPCIÓN B: Solo contar si la jornada NO se completó
                    // (es decir, si trabajó menos horas de las esperadas)
                    int minutosEsperados = reg.getMinutosEsperados();
                    int minutosTrabajados = reg.getMinutosTrabajados();

                    // Tolerancia: si trabajó al menos 95% de las horas esperadas, no es tardanza
                    // evaluable
                    boolean jornadaIncompleta = minutosTrabajados < (minutosEsperados * 0.95);

                    if (jornadaIncompleta) {
                        totalTardanzas++;
                        long minutosTardanza = ChronoUnit.MINUTES.between(
                                reg.getHoraEsperadaEntrada(), fichada.getHora());
                        // Limitar a máximo 120 minutos (2 horas) para evitar valores irreales
                        if (minutosTardanza > 0) {
                            minutosTotales += Math.min(minutosTardanza, 120);
                        }
                    }
                    break; // Solo evaluar primera entrada del día
                }
            }
        }
        return new int[] { totalTardanzas, minutosTotales };
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE DATOS ====================

    private List<AuditoriaRegistros> obtenerRegistrosAnuales(Personal empleado, LocalDate inicio, LocalDate fin) {
        return XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp " +
                        "AND a.fecha BETWEEN :inicio AND :fin " +
                        "ORDER BY a.fecha ASC", AuditoriaRegistros.class)
                .setParameter("emp", empleado)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();
    }

    private List<Licencia> obtenerLicenciasAnuales(Personal empleado) {
        return XPersistence.getManager()
                .createQuery("SELECT l FROM Licencia l " +
                        "WHERE l.empleado = :emp " +
                        "AND YEAR(l.fechaInicio) = :anio " +
                        "ORDER BY l.fechaInicio ASC", Licencia.class)
                .setParameter("emp", empleado)
                .setParameter("anio", ANIO_ACTUAL)
                .getResultList();
    }

    // ==================== 1. DATOS DEL EMPLEADO ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarDatosEmpleado(Map params, Personal empleado) {
        params.put("nombreCompleto", empleado.getNombreCompleto() != null ? empleado.getNombreCompleto() : "");
        params.put("userId", empleado.getUserId() != null ? empleado.getUserId() : "");

        String dniStr = "";
        if (empleado.getDni() != null && empleado.getDni().getNumero() != null) {
            dniStr = empleado.getDni().getNumero().trim();
        }
        params.put("dni", dniStr);

        params.put("puesto", empleado.getPuesto() != null ? empleado.getPuesto() : "");

        String sucursalStr = "";
        try {
            Object suc = empleado.getClass().getMethod("getSucursal").invoke(empleado);
            if (suc != null) {
                sucursalStr = (String) suc.getClass().getMethod("getNombre").invoke(suc);
            }
        } catch (Exception e) {
        }
        params.put("sucursal", sucursalStr);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        params.put("fechaIngreso",
                empleado.getInicioActividades() != null ? empleado.getInicioActividades().format(dateFmt) : "");
        params.put("antiguedad", empleado.getAntiguedadLaboral() != null ? empleado.getAntiguedadLaboral() : "");

        params.put("notasPersonale", empleado.getNotasPersonale() != null ? empleado.getNotasPersonale() : "");

        // Foto del empleado
        java.io.InputStream fotoStream = null;
        String fotoId = empleado.getFoto();
        if (fotoId != null && !fotoId.isEmpty()) {
            try {
                org.openxava.web.editors.IFilePersistor filePersistor = org.openxava.web.editors.FilePersistorFactory
                        .getInstance();
                org.openxava.web.editors.AttachedFile file = filePersistor.find(fotoId);
                if (file != null && file.getData() != null) {
                    fotoStream = new java.io.ByteArrayInputStream(file.getData());
                }
            } catch (Exception e) {
            }
        }
        params.put("fotoEmpleado", fotoStream);
    }

    // ==================== 2. RESUMEN EJECUTIVO ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarResumenEjecutivo(Map params, Personal empleado,
            List<AuditoriaRegistros> registros,
            List<Licencia> licencias,
            int totalTardanzas, int minutosTotalesTardanza, int mesesEfectivos) {

        // Calcular métricas principales
        long diasLaborables = registros.stream()
                .filter(r -> r.getEvaluacion() != EvaluacionJornada.FERIADO &&
                        r.getEvaluacion() != EvaluacionJornada.DIA_NO_LABORAL &&
                        r.getEvaluacion() != EvaluacionJornada.SIN_TURNO_ASIGNADO)
                .count();

        long diasPresentes = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.COMPLETA ||
                        r.getEvaluacion() == EvaluacionJornada.INCOMPLETA ||
                        r.getEvaluacion() == EvaluacionJornada.FERIADO_TRABAJADO)
                .count();

        long diasAusentes = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.AUSENTE)
                .count();

        // Calcular horas trabajadas usando campos directos
        int minutosNormales = registros.stream()
                .mapToInt(r -> r.getMinutosTrabajados())
                .sum();

        int minutosExtras = registros.stream()
                .mapToInt(r -> r.getMinutosExtras())
                .sum();

        // Minutos especiales no están disponibles como campo directo
        int minutosEspeciales = 0;

        // Calcular porcentaje de presentismo
        double presentismo = diasLaborables > 0 ? (diasPresentes * 100.0 / diasLaborables) : 0;

        // Agregar parámetros (usar tardanzas pre-calculadas con Opción B)
        params.put("presentismoAnual", String.format("%.1f%%", presentismo));
        params.put("totalAusencias", diasAusentes);
        params.put("totalTardanzas", (long) totalTardanzas); // Cast a Long para JRXML
        params.put("minutosTotalTardanza", (long) minutosTotalesTardanza); // Cast a Long
        params.put("horasNormalesAnual", formatearMinutosAHoras(minutosNormales));
        params.put("horasExtrasAnual", formatearMinutosAHoras(minutosExtras));
        params.put("horasEspecialesAnual", formatearMinutosAHoras(minutosEspeciales));

        // Texto ejecutivo (ya no se usa, reemplazado por IA)
        // Pero mantenemos compatibilidad
        String textoEjecutivo = generarTextoEjecutivo(presentismo, totalTardanzas, diasAusentes);
        params.put("textoEjecutivoBasico", textoEjecutivo);
    }

    private String generarTextoEjecutivo(double presentismo, long tardanzas, long ausencias) {
        StringBuilder texto = new StringBuilder();

        if (presentismo >= 95) {
            texto.append("Desempeño EXCELENTE. ");
        } else if (presentismo >= 90) {
            texto.append("Desempeño BUENO. ");
        } else if (presentismo >= 80) {
            texto.append("Desempeño REGULAR. ");
        } else {
            texto.append("Desempeño DEFICIENTE. ");
        }

        if (tardanzas < 5) {
            texto.append("Puntualidad ejemplar. ");
        } else if (tardanzas < 15) {
            texto.append("Puntualidad aceptable. ");
        } else {
            texto.append("Problemas de puntualidad. ");
        }

        if (ausencias > 20) {
            texto.append("Alto ausentismo requiere atención.");
        } else if (ausencias > 10) {
            texto.append("Ausentismo moderado.");
        } else {
            texto.append("Ausentismo bajo.");
        }

        return texto.toString();
    }

    // ==================== 3. ASISTENCIA CONSOLIDADA ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarAsistenciaConsolidada(Map params, List<AuditoriaRegistros> registros, int mesesEfectivos) {
        // Arrays para datos mensuales (12 meses)
        int[] presentesMes = new int[12];
        int[] ausentesMes = new int[12];
        int[] tardanzasMes = new int[12];
        double[] cumplimientoMes = new double[12];

        for (AuditoriaRegistros reg : registros) {
            if (reg.getFecha() == null)
                continue;

            int mes = reg.getFecha().getMonthValue() - 1; // 0-11

            if (reg.getEvaluacion() == EvaluacionJornada.COMPLETA ||
                    reg.getEvaluacion() == EvaluacionJornada.INCOMPLETA ||
                    reg.getEvaluacion() == EvaluacionJornada.FERIADO_TRABAJADO) {
                presentesMes[mes]++;
            }

            if (reg.getEvaluacion() == EvaluacionJornada.AUSENTE) {
                ausentesMes[mes]++;
            }

            // Calcular tardanzas con OPCIÓN B
            // Primero: excluir jornadas incompletas
            EvaluacionJornada evalJornada = reg.getEvaluacion();
            if (evalJornada == EvaluacionJornada.PENDIENTE ||
                    evalJornada == EvaluacionJornada.EN_CURSO ||
                    evalJornada == EvaluacionJornada.SIN_ENTRADA ||
                    evalJornada == EvaluacionJornada.SIN_SALIDA) {
                continue;
            }

            if (reg.getHoraEsperadaEntrada() != null) {
                for (ColeccionRegistros fichada : reg.getRegistros()) {
                    if (fichada.getTipoMovimiento() == TipoMovimiento.ENTRADA &&
                            "ENTRADA TARDE".equals(fichada.getEvaluacion())) {
                        // OPCIÓN B: Solo contar si la jornada NO se completó
                        int minutosEsperados = reg.getMinutosEsperados();
                        int minutosTrabajados = reg.getMinutosTrabajados();
                        boolean jornadaIncompleta = minutosTrabajados < (minutosEsperados * 0.95);
                        if (jornadaIncompleta) {
                            tardanzasMes[mes]++;
                        }
                        break;
                    }
                }
            }
        }

        // Calcular porcentaje de cumplimiento por mes
        for (int i = 0; i < 12; i++) {
            int total = presentesMes[i] + ausentesMes[i];
            cumplimientoMes[i] = total > 0 ? (presentesMes[i] * 100.0 / total) : 0;
        }

        params.put("presentesMes", presentesMes);
        params.put("ausentesMes", ausentesMes);
        params.put("tardanzasMes", tardanzasMes);
        params.put("cumplimientoMes", cumplimientoMes);

        // Análisis de tendencias
        String analisisTendencias = analizarTendenciasAsistencia(presentesMes, ausentesMes, tardanzasMes);
        params.put("analisisTendencias", analisisTendencias);
    }

    private String analizarTendenciasAsistencia(int[] presentes, int[] ausentes, int[] tardanzas) {
        StringBuilder analisis = new StringBuilder();

        // Encontrar mes con más ausencias
        int mesMaxAusencias = 0;
        for (int i = 1; i < 12; i++) {
            if (ausentes[i] > ausentes[mesMaxAusencias]) {
                mesMaxAusencias = i;
            }
        }

        if (ausentes[mesMaxAusencias] > 5) {
            analisis.append(String.format("Pico de ausentismo en %s con %d ausencias. ",
                    obtenerNombreMes(mesMaxAusencias), ausentes[mesMaxAusencias]));
        }

        // Encontrar mes con más tardanzas
        int mesMaxTardanzas = 0;
        for (int i = 1; i < 12; i++) {
            if (tardanzas[i] > tardanzas[mesMaxTardanzas]) {
                mesMaxTardanzas = i;
            }
        }

        if (tardanzas[mesMaxTardanzas] > 5) {
            analisis.append(String.format("Mayor impuntualidad en %s con %d tardanzas. ",
                    obtenerNombreMes(mesMaxTardanzas), tardanzas[mesMaxTardanzas]));
        }

        return analisis.length() > 0 ? analisis.toString() : "No se detectaron patrones significativos.";
    }

    // ==================== 4. HORAS TRABAJADAS ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarHorasTrabajadas(Map params, Personal empleado, List<AuditoriaRegistros> registros,
            int mesesEfectivos) {
        // Arrays mensuales
        double[] horasNormalesMes = new double[12];
        double[] horasExtrasMes = new double[12];
        double[] horasEspecialesMes = new double[12];
        Set<Integer> mesesConActividad = new HashSet<>();

        for (AuditoriaRegistros reg : registros) {
            if (reg.getFecha() == null)
                continue;

            int mes = reg.getFecha().getMonthValue() - 1;
            mesesConActividad.add(mes);

            horasNormalesMes[mes] += reg.getMinutosTrabajados() / 60.0;
            horasExtrasMes[mes] += reg.getMinutosExtras() / 60.0;
            // Horas especiales no disponibles directamente
        }

        params.put("horasNormalesMes", horasNormalesMes);
        params.put("horasExtrasMes", horasExtrasMes);
        params.put("horasEspecialesMes", horasEspecialesMes);

        // Promedios y extremos
        // Usar meses con actividad para el promedio, o 1 si no hubo actividad para
        // evitar división por cero
        int divisorPromedio = mesesConActividad.isEmpty() ? 1 : mesesConActividad.size();
        double totalHoras = Arrays.stream(horasNormalesMes).sum();
        double promedioMensual = totalHoras / divisorPromedio;

        params.put("promedioHorasMensual", String.format("%.1f", promedioMensual));

        int mesMayorCarga = 0;
        for (int i = 1; i < 12; i++) {
            if (horasNormalesMes[i] > horasNormalesMes[mesMayorCarga]) {
                mesMayorCarga = i;
            }
        }
        params.put("mesMayorCarga", obtenerNombreMes(mesMayorCarga));
        params.put("horasMayorCarga", String.format("%.1f", horasNormalesMes[mesMayorCarga]));
    }

    // ==================== 5. TARDANZAS Y PUNTUALIDAD ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarAnalisisTardanzas(Map params, List<AuditoriaRegistros> registros,
            int totalTardanzasPreCalc, int minutosTotalesPreCalc) {
        // Usar los valores pre-calculados con Opción B
        int totalTardanzas = totalTardanzasPreCalc;
        int minutosTotales = minutosTotalesPreCalc;
        Map<DayOfWeek, Long> tardanzasPorDia = new HashMap<>();

        // Solo calcular distribución por día de semana (para análisis)
        for (AuditoriaRegistros reg : registros) {
            if (reg.getHoraEsperadaEntrada() == null)
                continue;

            // Buscar si hubo entrada tarde con jornada incompleta (Opción B)
            for (ColeccionRegistros fichada : reg.getRegistros()) {
                if (fichada.getTipoMovimiento() == TipoMovimiento.ENTRADA &&
                        "ENTRADA TARDE".equals(fichada.getEvaluacion())) {

                    // Solo contar para estadísticas de día si jornada fue incompleta
                    int minutosEsperados = reg.getMinutosEsperados();
                    int minutosTrabajados = reg.getMinutosTrabajados();
                    boolean jornadaIncompleta = minutosTrabajados < (minutosEsperados * 0.95);

                    if (jornadaIncompleta) {
                        DayOfWeek dia = reg.getFecha().getDayOfWeek();
                        tardanzasPorDia.merge(dia, 1L, (a, b) -> a + b);
                    }
                    break;
                }
            }
        }

        double promedioMinutos = totalTardanzas > 0 ? (minutosTotales * 1.0 / totalTardanzas) : 0;

        params.put("totalTardanzasDetalle", totalTardanzas);
        params.put("minutosTotalesTardanzaDetalle", minutosTotales);
        params.put("promedioMinutosTardanza", String.format("%.1f", promedioMinutos));

        // Análisis por día de semana
        DayOfWeek diaProblematico = tardanzasPorDia.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String analisisPuntualidad = generarAnalisisPuntualidad(totalTardanzas, promedioMinutos, diaProblematico);
        params.put("analisisPuntualidad", analisisPuntualidad);
    }

    private String generarAnalisisPuntualidad(int totalTardanzas, double promedioMinutos, DayOfWeek diaProblematico) {
        StringBuilder analisis = new StringBuilder();

        if (totalTardanzas == 0) {
            analisis.append("Puntualidad perfecta durante todo el año. ¡Felicitaciones! ");
            return analisis.toString();
        }

        // Analizar gravedad combinando frecuencia y duración
        boolean tardanzasGraves = promedioMinutos > 15;
        boolean tardanzasMuyGraves = promedioMinutos > 60;

        if (tardanzasMuyGraves) {
            analisis.append(
                    "Se detectan tardanzas de muy larga duración (promedio > 1 hora). Requiere revisión inmediata de horarios o justificaciones. ");
        } else if (totalTardanzas < 5 && !tardanzasGraves) {
            analisis.append("Puntualidad excelente. ");
        } else if (totalTardanzas < 15 && !tardanzasGraves) {
            analisis.append("Puntualidad aceptable con algunas tardanzas menores. ");
        } else if (tardanzasGraves) {
            analisis.append("Aunque la frecuencia es moderada, el tiempo promedio de demora es alto. ");
        } else if (totalTardanzas < 30) {
            analisis.append("Problemas moderados de puntualidad que requieren atención. ");
        } else {
            analisis.append("Problemas graves de puntualidad. Requiere intervención inmediata. ");
        }

        if (diaProblematico != null) {
            String nombreDia = diaProblematico.getDisplayName(
                    java.time.format.TextStyle.FULL, new Locale("es", "ES"));
            analisis.append(String.format("Mayor incidencia los días %s.", nombreDia));
        }

        return analisis.toString();
    }

    // ==================== 6. AUSENCIAS Y LICENCIAS ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarAnalisisLicencias(Map params, List<Licencia> licencias,
            List<AuditoriaRegistros> registros) {

        // Agrupar licencias por tipo
        Map<TipoLicenciaAR, Integer> diasPorTipo = new HashMap<>();

        for (Licencia lic : licencias) {
            TipoLicenciaAR tipo = lic.getTipo();
            int dias = lic.getDias() != null ? lic.getDias() : 0;
            diasPorTipo.merge(tipo, dias, (a, b) -> a + b);
        }

        params.put("totalLicencias", licencias.size());
        params.put("diasLicenciaMedica", diasPorTipo.getOrDefault(TipoLicenciaAR.ENFERMEDAD, 0));
        params.put("diasVacaciones", diasPorTipo.getOrDefault(TipoLicenciaAR.VACACIONES, 0));

        // Ausencias injustificadas
        long ausenciasInjustificadas = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.AUSENTE)
                .count();

        params.put("ausenciasInjustificadas", ausenciasInjustificadas);

        // Análisis de impacto
        int totalDiasLicencia = diasPorTipo.values().stream().mapToInt(Integer::intValue).sum();
        double porcentajeNoTrabajado = (totalDiasLicencia + ausenciasInjustificadas) * 100.0 / 365;

        params.put("porcentajeNoTrabajado", String.format("%.1f%%", porcentajeNoTrabajado));

        String analisisLicencias = generarAnalisisLicencias(totalDiasLicencia, ausenciasInjustificadas);
        params.put("analisisLicencias", analisisLicencias);
    }

    private String generarAnalisisLicencias(int diasLicencia, long ausenciasInjust) {
        StringBuilder analisis = new StringBuilder();

        if (diasLicencia > 30) {
            analisis.append("Alto nivel de licencias. Se recomienda revisión médica. ");
        } else if (diasLicencia > 15) {
            analisis.append("Nivel moderado de licencias. ");
        } else {
            analisis.append("Nivel bajo de licencias. ");
        }

        if (ausenciasInjust > 10) {
            analisis.append("Ausencias injustificadas excesivas. Requiere acción disciplinaria.");
        } else if (ausenciasInjust > 5) {
            analisis.append("Algunas ausencias injustificadas. Requiere seguimiento.");
        }

        return analisis.toString();
    }

    // ==================== 7. AUDITORÍAS Y CORRECCIONES ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarAuditorias(Map params, List<AuditoriaRegistros> registros) {
        // Verificar ajustes manuales usando campos directos
        List<AuditoriaRegistros> conAjustes = registros.stream()
                .filter(r -> r.getAjusteMinutosNormales() != 0 ||
                        r.getAjusteMinutosExtras() != 0 ||
                        r.getAjusteMinutosEspeciales() != 0)
                .collect(Collectors.toList());

        params.put("totalCorrecciones", conAjustes.size());

        // Análisis de trazabilidad
        String analisisAuditorias = conAjustes.isEmpty() ? "No se registraron correcciones manuales durante el año."
                : String.format("Se realizaron %d correcciones manuales. Revisar trazabilidad.", conAjustes.size());

        params.put("analisisAuditorias", analisisAuditorias);
    }

    // ==================== 8. COSTO LABORAL ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarCostoLaboral(Map params, Personal empleado, List<AuditoriaRegistros> registros) {
        BigDecimal valorHora = empleado.getValorHora();

        if (valorHora == null || valorHora.compareTo(BigDecimal.ZERO) == 0) {
            params.put("costoLaboralDisponible", false);
            return;
        }

        params.put("costoLaboralDisponible", true);

        // Calcular costos
        BigDecimal costoNormales = BigDecimal.ZERO;
        BigDecimal costoExtras = BigDecimal.ZERO;
        BigDecimal costoEspeciales = BigDecimal.ZERO;
        Set<Integer> mesesConActividad = new HashSet<>();

        for (AuditoriaRegistros reg : registros) {
            if (reg.getFecha() != null) {
                mesesConActividad.add(reg.getFecha().getMonthValue());
            }

            if (reg.getMontoTeoricoTurno() != null) {
                costoNormales = costoNormales.add(reg.getMontoTeoricoTurno());
            }
            if (reg.getMontoTeoricoExtras() != null) {
                costoExtras = costoExtras.add(reg.getMontoTeoricoExtras());
            }
            if (reg.getMontoTeoricoEspeciales() != null) {
                costoEspeciales = costoEspeciales.add(reg.getMontoTeoricoEspeciales());
            }
        }

        BigDecimal costoTotal = costoNormales.add(costoExtras).add(costoEspeciales);

        // Usar meses con actividad para el promedio
        int divisorPromedio = mesesConActividad.isEmpty() ? 1 : mesesConActividad.size();
        BigDecimal promedioMensual = costoTotal.divide(BigDecimal.valueOf(divisorPromedio), 2, RoundingMode.HALF_UP);

        params.put("costoNormales", costoNormales);
        params.put("costoExtras", costoExtras);
        params.put("costoEspeciales", costoEspeciales);
        params.put("costoTotal", costoTotal);
        params.put("costoMensualPromedio", promedioMensual);
    }

    // ==================== 9. CONCLUSIONES Y RECOMENDACIONES (IA GEMINI)
    // ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarConclusiones(Map params, Personal empleado,
            List<AuditoriaRegistros> registros,
            List<Licencia> licencias) {

        // Construir DTO con todos los datos para el análisis
        DatosDesempenoDTO datos = construirDatosDesempeno(empleado, registros, licencias);

        // Llamar al servicio de análisis (IA o fallback)
        AnalisisDesempenoService servicio = new AnalisisDesempenoService();
        AnalisisIntegralDTO analisis = servicio.realizarAnalisisIntegral(empleado, ANIO_ACTUAL, datos);

        // Inyectar resultados en los parámetros del reporte
        params.put("textoEjecutivo", analisis.getResumenEjecutivo());
        params.put("fortalezas", analisis.getFortalezas());
        params.put("debilidades", analisis.getDebilidades());
        params.put("recomendaciones", analisis.getRecomendaciones());
        params.put("generadoPorIA", analisis.isGeneradoPorIA());

        // Log para debugging
        if (analisis.getMensajeEstado() != null && !analisis.getMensajeEstado().isEmpty()) {
            System.out.println("[InformeAnual] Estado análisis: " + analisis.getMensajeEstado());
        }
    }

    /**
     * Construye el DTO con todos los datos necesarios para el análisis de IA.
     */
    private DatosDesempenoDTO construirDatosDesempeno(Personal empleado,
            List<AuditoriaRegistros> registros,
            List<Licencia> licencias) {

        // Calcular métricas de asistencia
        long diasLaborables = registros.stream()
                .filter(r -> r.getEvaluacion() != EvaluacionJornada.FERIADO &&
                        r.getEvaluacion() != EvaluacionJornada.DIA_NO_LABORAL &&
                        r.getEvaluacion() != EvaluacionJornada.SIN_TURNO_ASIGNADO)
                .count();

        long diasPresentes = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.COMPLETA ||
                        r.getEvaluacion() == EvaluacionJornada.INCOMPLETA ||
                        r.getEvaluacion() == EvaluacionJornada.FERIADO_TRABAJADO)
                .count();

        long ausencias = registros.stream()
                .filter(r -> r.getEvaluacion() == EvaluacionJornada.AUSENTE)
                .count();

        double presentismo = diasLaborables > 0 ? (diasPresentes * 100.0 / diasLaborables) : 0;

        // Calcular tardanzas usando OPCIÓN B (igual que calcularTardanzasUnificado)
        // Solo cuenta como tardanza si el empleado entró tarde Y NO completó las horas
        int totalTardanzas = 0;
        int minutosTardanza = 0;
        for (AuditoriaRegistros reg : registros) {
            if (reg.getHoraEsperadaEntrada() == null)
                continue;

            // Excluir jornadas incompletas (SIN SALIDA, EN CURSO, etc.)
            EvaluacionJornada evalJornada = reg.getEvaluacion();
            if (evalJornada == EvaluacionJornada.PENDIENTE ||
                    evalJornada == EvaluacionJornada.EN_CURSO ||
                    evalJornada == EvaluacionJornada.SIN_ENTRADA ||
                    evalJornada == EvaluacionJornada.SIN_SALIDA) {
                continue;
            }

            for (ColeccionRegistros fichada : reg.getRegistros()) {
                if (fichada.getTipoMovimiento() == TipoMovimiento.ENTRADA &&
                        "ENTRADA TARDE".equals(fichada.getEvaluacion())) {

                    // OPCIÓN B: Solo contar si la jornada NO se completó
                    int minutosEsperados = reg.getMinutosEsperados();
                    int minutosTrabajados = reg.getMinutosTrabajados();
                    boolean jornadaIncompleta = minutosTrabajados < (minutosEsperados * 0.95);

                    if (jornadaIncompleta) {
                        totalTardanzas++;
                        long mins = ChronoUnit.MINUTES.between(reg.getHoraEsperadaEntrada(), fichada.getHora());
                        if (mins > 0) {
                            // Limitar a máximo 120 minutos para evitar valores irreales
                            minutosTardanza += Math.min(mins, 120);
                        }
                    }
                    break;
                }
            }
        }
        double promedioTardanza = totalTardanzas > 0 ? (minutosTardanza * 1.0 / totalTardanzas) : 0;

        // Calcular horas
        int minutosNormales = registros.stream().mapToInt(r -> r.getMinutosTrabajados()).sum();
        int minutosExtras = registros.stream().mapToInt(r -> r.getMinutosExtras()).sum();
        int minutosEspeciales = 0; // No disponible directamente

        // Calcular licencias por tipo
        Map<TipoLicenciaAR, Integer> diasPorTipo = new HashMap<>();
        for (Licencia lic : licencias) {
            int dias = lic.getDias() != null ? lic.getDias() : 0;
            diasPorTipo.merge(lic.getTipo(), dias, (a, b) -> a + b);
        }
        int totalDiasLicencia = diasPorTipo.values().stream().mapToInt(Integer::intValue).sum();

        // Calcular correcciones
        int totalCorrecciones = (int) registros.stream()
                .filter(r -> r.getAjusteMinutosNormales() != 0 ||
                        r.getAjusteMinutosExtras() != 0 ||
                        r.getAjusteMinutosEspeciales() != 0)
                .count();

        // Obtener notas de desempeño
        List<NotaResumenDTO> notasDTO = AnalisisDesempenoService.convertirNotas(empleado.getNotasDesempeno());
        double promedioNotas = NotaDesempeno.calcularPromedio(empleado.getNotasDesempeno());
        String evaluacionNotas = NotaDesempeno.calcularEvaluacion(promedioNotas);

        // Construir el DTO
        return DatosDesempenoDTO.builder()
                .nombreEmpleado(empleado.getNombreCompleto())
                .puesto(empleado.getPuesto())
                .sucursal(empleado.getSucursal() != null ? empleado.getSucursal().getNombre() : "")
                .anio(ANIO_ACTUAL)
                .porcentajePresentismo(presentismo)
                .diasPresentes(diasPresentes)
                .diasLaborables(diasLaborables)
                .totalTardanzas(totalTardanzas)
                .minutosTotalesTardanza(minutosTardanza)
                .promedioMinutosTardanza(promedioTardanza)
                .totalAusencias(ausencias)
                .ausenciasInjustificadas(ausencias)
                .minutosNormales(minutosNormales)
                .minutosExtras(minutosExtras)
                .minutosEspeciales(minutosEspeciales)
                .totalLicencias(licencias.size())
                .diasLicenciaMedica(diasPorTipo.getOrDefault(TipoLicenciaAR.ENFERMEDAD, 0))
                .diasVacaciones(diasPorTipo.getOrDefault(TipoLicenciaAR.VACACIONES, 0))
                .totalDiasLicencia(totalDiasLicencia)
                .totalCorrecciones(totalCorrecciones)
                .notasDesempeno(notasDTO)
                .promedioCalificacionNotas(promedioNotas)
                .evaluacionNotas(evaluacionNotas)
                .build();
    }

    // ==================== 10. DATOS PARA GRÁFICOS ====================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void agregarDatosGraficos(Map params, List<AuditoriaRegistros> registros, List<Licencia> licencias) {
        // 1. Gráfico Mensual (Asistencia y Horas)
        List<InformeMensualDTO> listaMensual = new ArrayList<>();

        // Inicializar arrays
        int[] presentes = new int[12];
        int[] ausentes = new int[12];
        int[] tardanzas = new int[12];
        double[] horasNormales = new double[12];
        double[] horasExtras = new double[12];

        // Poblar arrays
        for (AuditoriaRegistros reg : registros) {
            if (reg.getFecha() == null)
                continue;

            int mes = reg.getFecha().getMonthValue() - 1;

            // Asistencia
            if (reg.getEvaluacion() == EvaluacionJornada.COMPLETA ||
                    reg.getEvaluacion() == EvaluacionJornada.INCOMPLETA ||
                    reg.getEvaluacion() == EvaluacionJornada.FERIADO_TRABAJADO) {
                presentes[mes]++;
            } else if (reg.getEvaluacion() == EvaluacionJornada.AUSENTE) {
                ausentes[mes]++;
            }

            // Tardanzas (lógica simplificada, idealmente usar la misma que en
            // agregarAnalisisTardanzas)
            // Aquí solo contamos si hubo "ENTRADA TARDE" en alguna fichada
            for (ColeccionRegistros fichada : reg.getRegistros()) {
                if (fichada.getTipoMovimiento() == TipoMovimiento.ENTRADA &&
                        "ENTRADA TARDE".equals(fichada.getEvaluacion())) {
                    tardanzas[mes]++;
                    break;
                }
            }

            // Horas
            horasNormales[mes] += reg.getMinutosTrabajados() / 60.0;
            horasExtras[mes] += reg.getMinutosExtras() / 60.0;
        }

        // Crear DTOs
        String[] nombresMeses = { "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
        for (int i = 0; i < 12; i++) {
            double cumplimiento = 0;
            int totalAsistencia = presentes[i] + ausentes[i];
            if (totalAsistencia > 0) {
                cumplimiento = (presentes[i] * 100.0) / totalAsistencia;
            }

            listaMensual.add(new InformeMensualDTO(
                    nombresMeses[i],
                    presentes[i],
                    ausentes[i],
                    tardanzas[i],
                    horasNormales[i],
                    horasExtras[i],
                    0.0, // horasEspeciales - no disponibles en el modelo actual
                    cumplimiento));
        }

        // Pasar la lista directamente para poder reutilizarla en múltiples gráficos
        params.put("listaMensual", listaMensual);

        // 2. Gráfico Licencias
        List<InformeLicenciaDTO> listaLicencias = new ArrayList<>();
        Map<String, Integer> mapaLicencias = new HashMap<>();

        for (Licencia lic : licencias) {
            String tipo = lic.getTipo().toString();
            int dias = lic.getDias() != null ? lic.getDias() : 0;
            mapaLicencias.merge(tipo, dias, (a, b) -> a + b);
        }

        for (Map.Entry<String, Integer> entry : mapaLicencias.entrySet()) {
            listaLicencias.add(new InformeLicenciaDTO(entry.getKey(), entry.getValue()));
        }

        // Si no hay licencias, agregar un dummy para que no falle el gráfico o se vea
        // vacío feo
        if (listaLicencias.isEmpty()) {
            listaLicencias.add(new InformeLicenciaDTO("Sin Licencias", 1));
        }

        params.put("listaLicencias", listaLicencias);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String formatearMinutosAHoras(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%d:%02d", horas, mins);
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };
        return meses[mes];
    }
}
