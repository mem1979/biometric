package com.sta.biometric.servicios;

import java.time.format.*;
import java.util.*;
import java.util.stream.*;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.sta.biometric.dto.*;
import com.sta.biometric.dto.DatosDesempenoDTO.NotaResumenDTO;
import com.sta.biometric.modelo.*;

/**
 * Servicio para generar análisis integral de desempeño anual.
 * Utiliza Google Gemini AI (modelo 2.5-flash) para análisis cualitativo,
 * con fallback a sistema experto local si la API no está disponible.
 */
public class AnalisisDesempenoService {

    private static final String SEPARADOR_SECCION = "===SECCION===";
    private static final String MODELO_GEMINI = "gemini-2.5-flash";

    // API Key configurada directamente (proporcionada por el usuario)
    private static final String DEFAULT_API_KEY = "AIzaSyBVhX0BPxR6FylAayyYphKKroJ0Crk1b9E";

    private final String apiKey;

    public AnalisisDesempenoService() {
        // Intentar obtener API Key de variable de entorno primero
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            // Usar la clave proporcionada por defecto
            key = DEFAULT_API_KEY;
        }
        this.apiKey = key;
    }

    /**
     * Constructor con API Key explícita (para testing).
     */
    public AnalisisDesempenoService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Realiza el análisis integral del desempeño anual.
     * Intenta usar Gemini AI; si falla, usa el sistema experto local.
     *
     * @param empleado Empleado a analizar
     * @param anio     Año del informe
     * @param datos    Datos de desempeño calculados
     * @return DTO con las 4 secciones del análisis
     */
    public AnalisisIntegralDTO realizarAnalisisIntegral(Personal empleado, int anio, DatosDesempenoDTO datos) {
        // Asegurar que tenemos el nombre del empleado en los datos
        if (datos.getNombreEmpleado() == null && empleado != null) {
            datos.setNombreEmpleado(empleado.getNombreCompleto());
            datos.setPuesto(empleado.getPuesto());
            datos.setAnio(anio);
        }

        // Intentar análisis con IA
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                return realizarAnalisisConGemini(datos);
            } catch (Exception e) {
                // Log del error y fallback
                System.err.println("[AnalisisDesempenoService] Error en Gemini API: " + e.getMessage());
                e.printStackTrace();
                AnalisisIntegralDTO fallbackResult = realizarAnalisisFallback(datos);
                fallbackResult.setMensajeEstado("Fallback activado por error: " + e.getMessage());
                return fallbackResult;
            }
        }

        // Sin API Key, usar fallback directamente
        AnalisisIntegralDTO fallback = realizarAnalisisFallback(datos);
        fallback.setMensajeEstado("API Key no configurada, usando análisis local");
        return fallback;
    }

    /**
     * Genera el análisis usando la API de Gemini.
     */
    private AnalisisIntegralDTO realizarAnalisisConGemini(DatosDesempenoDTO datos) throws Exception {
        // Construir el prompt
        String prompt = construirPrompt(datos);

        // Crear cliente Gemini con Builder y API Key explícita
        Client client = Client.builder()
                .apiKey(apiKey)
                .build();

        // Generar contenido
        GenerateContentResponse response = client.models.generateContent(
                MODELO_GEMINI,
                prompt,
                null // Sin configuración adicional
        );

        // Obtener texto de respuesta
        String respuestaCompleta = response.text();

        // Parsear las secciones
        return parsearRespuesta(respuestaCompleta);
    }

    /**
     * Construye el prompt estructurado para Gemini.
     */
    private String construirPrompt(DatosDesempenoDTO datos) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Actúa como un Gerente de Recursos Humanos Senior redactando una evaluación anual formal.\n\n");

        prompt.append("EMPLEADO: ").append(datos.getNombreEmpleado()).append("\n");
        if (datos.getPuesto() != null && !datos.getPuesto().isEmpty()) {
            prompt.append("PUESTO: ").append(datos.getPuesto()).append("\n");
        }
        prompt.append("AÑO EVALUADO: ").append(datos.getAnio()).append("\n\n");

        prompt.append("MÉTRICAS DE DESEMPEÑO:\n");
        prompt.append("- Presentismo: ").append(String.format("%.1f", datos.getPorcentajePresentismo())).append("%\n");
        prompt.append("- Días presentes: ").append(datos.getDiasPresentes()).append(" de ")
                .append(datos.getDiasLaborables()).append(" laborables\n");
        prompt.append("- Tardanzas: ").append(datos.getTotalTardanzas());
        if (datos.getMinutosTotalesTardanza() > 0) {
            prompt.append(" (").append(datos.getMinutosTotalesTardanza()).append(" minutos totales)");
        }
        prompt.append("\n");
        prompt.append("- Ausencias injustificadas: ").append(datos.getAusenciasInjustificadas()).append("\n");
        prompt.append("- Horas normales trabajadas: ").append(datos.getHorasNormalesFormateadas()).append("\n");
        prompt.append("- Horas extras: ").append(datos.getHorasExtrasFormateadas()).append("\n");
        if (datos.getMinutosEspeciales() > 0) {
            prompt.append("- Horas especiales: ").append(datos.getHorasEspecialesFormateadas()).append("\n");
        }
        prompt.append("- Licencias médicas: ").append(datos.getDiasLicenciaMedica()).append(" días\n");
        prompt.append("- Total días de licencia: ").append(datos.getTotalDiasLicencia()).append("\n");
        prompt.append("- Correcciones manuales realizadas: ").append(datos.getTotalCorrecciones()).append("\n\n");

        prompt.append("NOTAS DE DESEMPEÑO REGISTRADAS:\n");
        prompt.append(datos.getNotasFormateadas());
        if (datos.getEvaluacionNotas() != null && !datos.getEvaluacionNotas().isEmpty()) {
            prompt.append("\nEvaluación promedio de notas: ").append(datos.getEvaluacionNotas()).append("\n");
        }
        prompt.append("\n");

        prompt.append("INSTRUCCIONES:\n");
        prompt.append(
                "Genera un análisis profesional dividido en 4 secciones, separadas EXACTAMENTE por el marcador \"===SECCION===\" (sin comillas):\n\n");
        prompt.append(
                "1. RESUMEN EJECUTIVO: Dos párrafos formales con tono corporativo evaluando el desempeño general del empleado.\n");
        prompt.append(
                "2. FORTALEZAS: Lista con viñetas (•) de máximo 5 puntos destacando aspectos positivos basados en los datos.\n");
        prompt.append(
                "3. ÁREAS DE MEJORA: Lista con viñetas (•) de máximo 5 puntos identificando oportunidades de mejora.\n");
        prompt.append("4. RECOMENDACIONES: Acciones concretas y medibles para el próximo período.\n\n");
        prompt.append(
                "IMPORTANTE: Responde SOLO con las 4 secciones separadas por ===SECCION===. No incluyas encabezados de sección, solo el contenido.");

        return prompt.toString();
    }

    /**
     * Parsea la respuesta de Gemini y extrae las 4 secciones.
     */
    private AnalisisIntegralDTO parsearRespuesta(String respuesta) {
        AnalisisIntegralDTO dto = new AnalisisIntegralDTO();
        dto.setGeneradoPorIA(true);

        if (respuesta == null || respuesta.trim().isEmpty()) {
            dto.setMensajeEstado("Respuesta vacía de Gemini");
            return dto;
        }

        // Dividir por el separador
        String[] secciones = respuesta.split(SEPARADOR_SECCION);

        // Asignar cada sección (limpiar espacios)
        if (secciones.length >= 1) {
            dto.setResumenEjecutivo(limpiarTexto(secciones[0]));
        }
        if (secciones.length >= 2) {
            dto.setFortalezas(formatearListaHTML(secciones[1]));
        }
        if (secciones.length >= 3) {
            dto.setDebilidades(formatearListaHTML(secciones[2]));
        }
        if (secciones.length >= 4) {
            dto.setRecomendaciones(formatearListaHTML(secciones[3]));
        }

        // Si faltan secciones, completar con texto por defecto
        if (dto.getResumenEjecutivo() == null || dto.getResumenEjecutivo().isEmpty()) {
            dto.setResumenEjecutivo("No se pudo generar el resumen ejecutivo.");
        }
        if (dto.getFortalezas() == null || dto.getFortalezas().isEmpty()) {
            dto.setFortalezas("• Información no disponible");
        }
        if (dto.getDebilidades() == null || dto.getDebilidades().isEmpty()) {
            dto.setDebilidades("• Información no disponible");
        }
        if (dto.getRecomendaciones() == null || dto.getRecomendaciones().isEmpty()) {
            dto.setRecomendaciones("• Mantener seguimiento estándar");
        }

        return dto;
    }

    /**
     * Limpia el texto de espacios y caracteres innecesarios.
     */
    private String limpiarTexto(String texto) {
        if (texto == null)
            return "";
        return texto.trim()
                .replaceAll("(?m)^\\s*$[\n\r]{1,}", "\n") // Eliminar líneas vacías múltiples
                .replaceAll("^[\\n\\r]+", "") // Eliminar saltos de línea al inicio
                .replaceAll("[\\n\\r]+$", ""); // Eliminar saltos de línea al final
    }

    /**
     * Formatea una lista de viñetas para HTML.
     */
    /**
     * Formatea una lista de viñetas.
     * Nota: No se convierte a HTML ya que JasperReports maneja saltos de línea
     * nativamente.
     */
    private String formatearListaHTML(String texto) {
        if (texto == null || texto.trim().isEmpty())
            return "";

        String limpio = limpiarTexto(texto);
        // Convertir viñetas a formato consistente
        limpio = limpio.replaceAll("(?m)^\\s*[-•*]\\s*", "• ");
        // Mantener saltos de línea nativos (no convertir a HTML)
        // JasperReports con textAdjust="StretchHeight" maneja \n correctamente

        return limpio;
    }

    // =================== SISTEMA EXPERTO LOCAL (FALLBACK) ===================

    /**
     * Genera un análisis basado en reglas cuando Gemini no está disponible.
     * Mejora la lógica existente en
     * PersonalInformeAnualAction.agregarConclusiones()
     */
    private AnalisisIntegralDTO realizarAnalisisFallback(DatosDesempenoDTO datos) {
        AnalisisIntegralDTO dto = new AnalisisIntegralDTO();
        dto.setGeneradoPorIA(false);

        // 1. RESUMEN EJECUTIVO
        dto.setResumenEjecutivo(generarResumenEjecutivoFallback(datos));

        // 2. FORTALEZAS
        dto.setFortalezas(generarFortalezasFallback(datos));

        // 3. DEBILIDADES
        dto.setDebilidades(generarDebilidadesFallback(datos));

        // 4. RECOMENDACIONES
        dto.setRecomendaciones(generarRecomendacionesFallback(datos));

        return dto;
    }

    private String generarResumenEjecutivoFallback(DatosDesempenoDTO datos) {
        StringBuilder sb = new StringBuilder();
        double presentismo = datos.getPorcentajePresentismo();
        int tardanzas = datos.getTotalTardanzas();
        long ausencias = datos.getAusenciasInjustificadas();

        // Primer párrafo: evaluación general
        sb.append("Durante el año ").append(datos.getAnio()).append(", ");
        sb.append(datos.getNombreEmpleado());

        if (presentismo >= 95 && tardanzas < 5 && ausencias < 3) {
            sb.append(" demostró un desempeño EXCELENTE, ")
                    .append("destacándose por su compromiso y responsabilidad. ")
                    .append("Los indicadores de asistencia superan ampliamente los estándares ")
                    .append("establecidos por la organización.");
        } else if (presentismo >= 90 && tardanzas < 15) {
            sb.append(" mantuvo un desempeño BUENO, ")
                    .append("cumpliendo satisfactoriamente con las expectativas. ")
                    .append("Los indicadores de asistencia se encuentran dentro de los ")
                    .append("parámetros aceptables para el puesto.");
        } else if (presentismo >= 80) {
            sb.append(" presentó un desempeño REGULAR que requiere atención. ")
                    .append("Si bien cumple con requerimientos mínimos, existen ")
                    .append("oportunidades de mejora significativas.");
        } else {
            sb.append(" registró un desempeño por DEBAJO DE LAS EXPECTATIVAS. ")
                    .append("Los indicadores de asistencia requieren intervención ")
                    .append("inmediata y seguimiento cercano.");
        }

        // Segundo párrafo: métricas específicas
        sb.append("\n\n");
        sb.append("El análisis cuantitativo revela un índice de presentismo del ")
                .append(String.format("%.1f%%", presentismo));

        if (tardanzas > 0) {
            sb.append(", con ").append(tardanzas).append(" tardanzas registradas");
            if (datos.getMinutosTotalesTardanza() > 0) {
                sb.append(" (").append(datos.getMinutosTotalesTardanza()).append(" minutos acumulados)");
            }
        }

        if (ausencias > 0) {
            sb.append(" y ").append(ausencias).append(" ausencias injustificadas");
        }

        sb.append(". ");

        if (datos.getMinutosExtras() > 0) {
            sb.append("Se destaca la realización de ")
                    .append(datos.getHorasExtrasFormateadas())
                    .append(" horas extras durante el período. ");
        }

        if (datos.getEvaluacionNotas() != null && !datos.getEvaluacionNotas().isEmpty()) {
            sb.append("Las notas de desempeño califican al colaborador como '")
                    .append(datos.getEvaluacionNotas()).append("'.");
        }

        return sb.toString();
    }

    private String generarFortalezasFallback(DatosDesempenoDTO datos) {
        List<String> fortalezas = new ArrayList<>();
        double presentismo = datos.getPorcentajePresentismo();
        int tardanzas = datos.getTotalTardanzas();
        long ausencias = datos.getAusenciasInjustificadas();

        // Evaluar cada métrica
        if (presentismo >= 95) {
            fortalezas.add("Excelente índice de presentismo (" + String.format("%.1f%%", presentismo) + ")");
        } else if (presentismo >= 90) {
            fortalezas.add("Buen nivel de asistencia (" + String.format("%.1f%%", presentismo) + ")");
        }

        if (tardanzas < 5) {
            fortalezas.add("Puntualidad ejemplar con mínimas tardanzas");
        } else if (tardanzas < 10 && datos.getPromedioMinutosTardanza() < 10) {
            fortalezas.add("Tardanzas poco significativas en duración");
        }

        if (ausencias == 0) {
            fortalezas.add("Sin ausencias injustificadas durante todo el año");
        } else if (ausencias < 3) {
            fortalezas.add("Muy bajo nivel de ausentismo injustificado");
        }

        if (datos.getTotalLicencias() < 3) {
            fortalezas.add("Bajo uso de licencias, indicador de buena salud");
        }

        if (datos.getMinutosExtras() > 0) {
            fortalezas.add("Disposición para realizar horas extras cuando es necesario");
        }

        if (datos.getTotalCorrecciones() == 0) {
            fortalezas.add("Registros sin necesidad de correcciones manuales");
        }

        // Evaluación de notas
        if ("Excelente".equals(datos.getEvaluacionNotas()) || "Bueno".equals(datos.getEvaluacionNotas())) {
            fortalezas.add("Evaluación positiva en notas de desempeño");
        }

        // Si no hay fortalezas identificadas
        if (fortalezas.isEmpty()) {
            fortalezas.add("Cumplimiento básico de las responsabilidades asignadas");
        }

        return fortalezas.stream()
                .map(f -> "• " + f)
                .collect(Collectors.joining("<br>"));
    }

    private String generarDebilidadesFallback(DatosDesempenoDTO datos) {
        List<String> debilidades = new ArrayList<>();
        double presentismo = datos.getPorcentajePresentismo();
        int tardanzas = datos.getTotalTardanzas();
        long ausencias = datos.getAusenciasInjustificadas();

        if (presentismo < 80) {
            debilidades.add("Presentismo deficiente (" + String.format("%.1f%%", presentismo)
                    + "), muy por debajo del esperado");
        } else if (presentismo < 90) {
            debilidades.add("Presentismo por debajo del objetivo (" + String.format("%.1f%%", presentismo) + ")");
        }

        if (tardanzas > 30) {
            debilidades.add("Problemas graves de puntualidad con " + tardanzas + " tardanzas");
        } else if (tardanzas > 15) {
            debilidades.add("Frecuencia de tardanzas moderada-alta (" + tardanzas + ")");
        }

        if (datos.getPromedioMinutosTardanza() > 30) {
            debilidades.add("Duración promedio de tardanzas excesiva (" +
                    String.format("%.0f", datos.getPromedioMinutosTardanza()) + " minutos)");
        }

        if (ausencias > 10) {
            debilidades.add("Alto nivel de ausencias injustificadas (" + ausencias + ")");
        } else if (ausencias > 5) {
            debilidades.add("Ausencias injustificadas requieren seguimiento (" + ausencias + ")");
        }

        if (datos.getDiasLicenciaMedica() > 30) {
            debilidades.add("Alto uso de licencias médicas, posible problema de salud");
        } else if (datos.getDiasLicenciaMedica() > 15) {
            debilidades.add("Uso moderado-alto de licencias médicas");
        }

        if (datos.getTotalCorrecciones() > 10) {
            debilidades.add("Múltiples correcciones en registros (" + datos.getTotalCorrecciones() + ")");
        }

        if ("Requiere Mejora".equals(datos.getEvaluacionNotas())) {
            debilidades.add("Evaluación negativa en notas de desempeño");
        }

        // Si no hay debilidades identificadas
        if (debilidades.isEmpty()) {
            debilidades.add("No se detectaron debilidades significativas");
        }

        return debilidades.stream()
                .map(d -> "• " + d)
                .collect(Collectors.joining("<br>"));
    }

    private String generarRecomendacionesFallback(DatosDesempenoDTO datos) {
        List<String> recomendaciones = new ArrayList<>();
        double presentismo = datos.getPorcentajePresentismo();
        int tardanzas = datos.getTotalTardanzas();
        long ausencias = datos.getAusenciasInjustificadas();

        // Recomendaciones de reconocimiento
        if (presentismo >= 98 && tardanzas < 3 && ausencias == 0) {
            recomendaciones.add("Candidato para programa de reconocimiento por excelencia en asistencia");
            recomendaciones.add("Considerar como modelo/mentor para otros colaboradores");
        }

        // Recomendaciones de seguimiento
        if (tardanzas > 15) {
            recomendaciones.add("Evaluar posibilidad de ajuste de horario o turno");
            recomendaciones.add("Implementar seguimiento semanal de puntualidad");
        }

        if (ausencias > 5) {
            recomendaciones.add("Reunión de feedback para identificar causas de ausencias");
            if (ausencias > 10) {
                recomendaciones.add("Considerar inicio de proceso disciplinario progresivo");
            }
        }

        if (datos.getDiasLicenciaMedica() > 20) {
            recomendaciones.add("Sugerir revisión médica preventiva integral");
            recomendaciones.add("Evaluar ergonomía del puesto de trabajo");
        }

        if (presentismo < 85) {
            recomendaciones.add("Establecer metas mensuales de presentismo con seguimiento");
            recomendaciones.add("Implementar incentivos por mejora de asistencia");
        }

        if ("Requiere Mejora".equals(datos.getEvaluacionNotas())) {
            recomendaciones.add("Programar plan de desarrollo profesional individualizado");
        }

        // Si tiene buen desempeño y no hay otras recomendaciones
        if (recomendaciones.isEmpty()) {
            recomendaciones.add("Mantener seguimiento estándar del próximo período");
            recomendaciones.add("Continuar con el nivel de compromiso actual");
        }

        return recomendaciones.stream()
                .map(r -> "• " + r)
                .collect(Collectors.joining("<br>"));
    }

    // =================== MÉTODOS DE UTILIDAD ===================

    /**
     * Convierte la colección de notas de desempeño a formato DTO.
     */
    public static List<NotaResumenDTO> convertirNotas(Collection<NotaDesempeno> notas) {
        if (notas == null || notas.isEmpty()) {
            return Collections.emptyList();
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return notas.stream()
                .map(n -> NotaResumenDTO.builder()
                        .contenido(limpiarHTML(n.getContenido()))
                        .calificacion(n.getCalificacion().name())
                        .autor(n.getAutor())
                        .fecha(n.getFechaHora() != null ? n.getFechaHora().format(fmt) : "")
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Limpia tags HTML básicos del contenido de notas.
     */
    private static String limpiarHTML(String html) {
        if (html == null)
            return "";
        return html.replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
