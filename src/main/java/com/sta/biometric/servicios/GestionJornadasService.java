package com.sta.biometric.servicios;

import java.time.*;
import java.util.*;
import java.util.logging.*;

import javax.persistence.*;

import org.openxava.jpa.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

/**
 * Servicio de dominio para la gestión centralizada de jornadas laborales.
 * 
 * <p>
 * Unifica la lógica de negocio para:
 * <ul>
 * <li>Apertura diaria de jornadas (automáticas y manuales)</li>
 * <li>Cierre y consolidación de registros</li>
 * <li>Recuperación y re-evaluación de periodos pasados</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Patrón de uso:</strong>
 * </p>
 * <ul>
 * <li>Desde Jobs Quartz: Pasar el EntityManager del job explícitamente.</li>
 * <li>Desde Acciones OpenXava: Usar métodos sin parámetro EM (usa
 * XPersistence).</li>
 * </ul>
 * 
 * @author Sistema STARH
 * @version 2.1 - Thread-safe singleton, mejor manejo de errores, batch
 *          segmentado
 */
public class GestionJornadasService {

    private static final Logger logger = Logger.getLogger(GestionJornadasService.class.getName());

    // Singleton thread-safe con inicialización eager
    private static final GestionJornadasService INSTANCE = new GestionJornadasService();

    private GestionJornadasService() {
        // Constructor privado
    }

    public static GestionJornadasService getInstance() {
        return INSTANCE;
    }

    // =========================================================================
    // MÉTODOS PÚBLICOS - CON EntityManager EXPLÍCITO (para Jobs Quartz)
    // =========================================================================

    /**
     * Asegura que exista una jornada para el empleado y fecha indicados.
     * Si no existe, la crea. Si existe, la actualiza con datos básicos.
     * 
     * @param empleado Empleado objetivo
     * @param fecha    Fecha de la jornada
     * @param em       EntityManager del contexto llamante
     * @return La entidad AuditoriaRegistros gestionada
     */
    public AuditoriaRegistros abrirOActualizarJornada(Personal empleado, LocalDate fecha, EntityManager em) {
        AuditoriaRegistros asistencia = buscarAsistenciaDiaria(empleado, fecha, em);
        boolean nueva = false;

        if (asistencia == null) {
            asistencia = new AuditoriaRegistros();
            asistencia.setEmpleado(empleado);
            asistencia.setFecha(fecha);
            nueva = true;
        }

        // Actualizar estados base
        asistencia.setLicencia(Licencia.tieneLicenciaEnFecha(empleado, fecha));
        boolean esFeriado = Feriados.existeParaFecha(fecha);
        asistencia.setFeriado(esFeriado);

        // Inicializar lógica de negocio
        inicializarAsistencia(asistencia, empleado, fecha, esFeriado);

        if (nueva) {
            em.persist(asistencia);
        } else {
            em.merge(asistencia);
        }

        return asistencia;
    }

    /**
     * Consolida y cierra una jornada existente.
     * 
     * @param asistencia Jornada a cerrar
     * @param em         EntityManager del contexto llamante
     */
    public void cerrarJornada(AuditoriaRegistros asistencia, EntityManager em) {
        if (asistencia == null)
            return;

        asistencia.consolidarDesdeRegistros();
        em.merge(asistencia);
    }

    /**
     * Reprocesa un periodo completo para un empleado (o todos si empleado es null).
     * <p>
     * <strong>BATCH SEGMENTADO:</strong> Hace commit por cada día para evitar
     * transacciones demasiado largas y locks prolongados.
     * </p>
     * 
     * @param inicio   Fecha de inicio del periodo
     * @param fin      Fecha de fin del periodo
     * @param empleado Empleado específico o null para todos los activos
     * @param em       EntityManager del contexto llamante
     * @return Resultado con conteo de procesados y errores
     */
    public ReprocesarResultado reprocesarPeriodo(LocalDate inicio, LocalDate fin, Personal empleado, EntityManager em) {
        ReprocesarResultado resultado = new ReprocesarResultado();

        List<Personal> empleadosProcesar;
        if (empleado != null) {
            empleadosProcesar = Collections.singletonList(empleado);
        } else {
            empleadosProcesar = em.createQuery(
                    "SELECT e FROM Personal e WHERE e.activo = true AND e.eliminado = false", Personal.class)
                    .getResultList();
        }

        LocalDate actual = inicio;
        while (!actual.isAfter(fin)) {
            final LocalDate fechaProceso = actual;
            resultado.diasProcesados++;

            for (Personal emp : empleadosProcesar) {
                try {
                    AuditoriaRegistros jornada = abrirOActualizarJornada(emp, fechaProceso, em);
                    cerrarJornada(jornada, em);
                    resultado.jornadasProcesadas++;
                } catch (Exception e) {
                    resultado.errores++;
                    resultado.detallesErrores
                            .add(emp.getNombreCompleto() + " @ " + fechaProceso + ": " + e.getMessage());
                    logger.log(Level.WARNING,
                            "Error reprocesando " + emp.getNombreCompleto() + " fecha " + fechaProceso, e);
                    // Continuar con siguiente - no propagar para no abortar todo el batch
                }
            }

            // Flush por día para liberar memoria y reducir tamaño de TX
            em.flush();
            em.clear(); // Liberar entidades del contexto de persistencia

            actual = actual.plusDays(1);
        }

        logger.info("[GestionJornadasService] Reproceso completado: " + resultado);
        return resultado;
    }

    /**
     * Versión batch con control de transacción externo.
     * <p>
     * Diseñado para Jobs Quartz que manejan transacciones largas.
     * Hace commit después de cada día procesado usando
     * XPersistence.createManager().
     * </p>
     */
    public ReprocesarResultado reprocesarPeriodoBatch(LocalDate inicio, LocalDate fin, Personal empleadoFiltro) {
        ReprocesarResultado resultado = new ReprocesarResultado();

        LocalDate actual = inicio;
        while (!actual.isAfter(fin)) {
            final LocalDate fechaProceso = actual;
            // Usar XPersistence.createManager() para reutilizar el EMFactory singleton
            EntityManager em = XPersistence.createManager();

            try {
                em.getTransaction().begin();

                List<Personal> empleadosProcesar;
                if (empleadoFiltro != null) {
                    empleadosProcesar = Collections.singletonList(em.merge(empleadoFiltro));
                } else {
                    empleadosProcesar = em.createQuery(
                            "SELECT e FROM Personal e WHERE e.activo = true AND e.eliminado = false", Personal.class)
                            .getResultList();
                }

                resultado.diasProcesados++;

                for (Personal emp : empleadosProcesar) {
                    try {
                        AuditoriaRegistros jornada = abrirOActualizarJornada(emp, fechaProceso, em);
                        cerrarJornada(jornada, em);
                        resultado.jornadasProcesadas++;
                    } catch (Exception e) {
                        resultado.errores++;
                        resultado.detallesErrores
                                .add(emp.getNombreCompleto() + " @ " + fechaProceso + ": " + e.getMessage());
                        logger.log(Level.WARNING, "Error en batch", e);
                    }
                }

                em.getTransaction().commit();
                logger.fine("[Batch] Día " + fechaProceso + " completado.");

            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                resultado.errores++;
                resultado.detallesErrores.add("Día " + fechaProceso + " falló completamente: " + e.getMessage());
                logger.log(Level.SEVERE, "Error crítico en batch día " + fechaProceso, e);
            } finally {
                em.close();
            }

            actual = actual.plusDays(1);
        }

        logger.info("[GestionJornadasService] Batch completado: " + resultado);
        return resultado;
    }

    // =========================================================================
    // MÉTODOS PÚBLICOS - SIN EntityManager (para Acciones OpenXava)
    // =========================================================================

    /**
     * Versión para uso desde acciones OpenXava (usa XPersistence).
     */
    public AuditoriaRegistros abrirOActualizarJornada(Personal empleado, LocalDate fecha) {
        return abrirOActualizarJornada(empleado, fecha, XPersistence.getManager());
    }

    /**
     * Versión para uso desde acciones OpenXava (usa XPersistence).
     */
    public void cerrarJornada(AuditoriaRegistros asistencia) {
        cerrarJornada(asistencia, XPersistence.getManager());
    }

    /**
     * Versión para uso desde acciones OpenXava (usa XPersistence).
     */
    public ReprocesarResultado reprocesarPeriodo(LocalDate inicio, LocalDate fin, Personal empleado) {
        return reprocesarPeriodo(inicio, fin, empleado, XPersistence.getManager());
    }

    // =========================================================================
    // MÉTODOS PRIVADOS
    // =========================================================================

    private AuditoriaRegistros buscarAsistenciaDiaria(Personal empleado, LocalDate fecha, EntityManager em) {
        try {
            return em.createQuery("SELECT a FROM AuditoriaRegistros a WHERE a.empleado = :emp AND a.fecha = :fecha",
                    AuditoriaRegistros.class)
                    .setParameter("emp", empleado)
                    .setParameter("fecha", fecha)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private void inicializarAsistencia(AuditoriaRegistros asistencia, Personal empleado, LocalDate fecha,
            boolean esFeriado) {
        // 1. Inicializar datos del turno
        asistencia.inicializarTurnoYCondiciones();

        // 2. Determinar Evaluación Inicial
        TurnosHorarios turno = empleado.getTurnoParaFecha(fecha);
        boolean esLaboral = turno != null && turno.esLaboral(fecha.getDayOfWeek());

        if (asistencia.isLicencia()) {
            asistencia.setEvaluacion(EvaluacionJornada.LICENCIA);
            asistencia.setJustificado(true);
        } else if (asistencia.isFeriado()) {
            asistencia.setEvaluacion(EvaluacionJornada.FERIADO);
            asistencia.setJustificado(true);
        } else if (!esLaboral) {
            asistencia.setEvaluacion(EvaluacionJornada.DIA_NO_LABORAL);
            asistencia.setJustificado(false);
        } else {
            if (asistencia.getEvaluacion() == null) {
                asistencia.setEvaluacion(EvaluacionJornada.PENDIENTE);
                asistencia.setJustificado(false);
            }
        }

        if (asistencia.getNota() == null || asistencia.getNota().isBlank()) {
            asistencia.setNota("Jornada inicializada/verificada.");
        }
    }

    // =========================================================================
    // CLASE INTERNA - Resultado de reprocesamiento
    // =========================================================================

    /**
     * Resultado del reprocesamiento de un periodo.
     */
    public static class ReprocesarResultado {
        public int diasProcesados = 0;
        public int jornadasProcesadas = 0;
        public int errores = 0;
        public List<String> detallesErrores = new ArrayList<>();

        @Override
        public String toString() {
            return String.format("Días=%d, Jornadas=%d, Errores=%d",
                    diasProcesados, jornadasProcesadas, errores);
        }

        public boolean tieneErrores() {
            return errores > 0;
        }
    }
}
