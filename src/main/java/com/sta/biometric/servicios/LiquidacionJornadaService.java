package com.sta.biometric.servicios;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

/**
 * Servicio para la gestión de liquidaciones de jornadas.
 * 
 * <p>
 * Centraliza toda la lógica de cálculo y generación de liquidaciones,
 * consultando datos desde {@link AuditoriaRegistros} y almacenándolos
 * en {@link LiquidacionJornadas}.
 * </p>
 * 
 * @author Sistema STARH
 * @version 1.0
 * @since 2.0
 */
public class LiquidacionJornadaService {

    /**
     * Genera una nueva liquidación para un empleado en un período específico.
     * 
     * <p>
     * El método:
     * </p>
     * <ol>
     * <li>Verifica que no exista liquidación duplicada</li>
     * <li>Consulta todos los {@link AuditoriaRegistros} del período</li>
     * <li>Suma las horas normales, extras y especiales</li>
     * <li>Captura los valores monetarios actuales del empleado</li>
     * <li>Calcula los montos totales</li>
     * </ol>
     * 
     * @param empleado Empleado para el cual generar la liquidación
     * @param desde    Fecha de inicio del período
     * @param hasta    Fecha de fin del período
     * @return Nueva liquidación generada y persistida
     * @throws IllegalArgumentException si ya existe liquidación para el período
     */
    public static LiquidacionJornadas generarLiquidacion(Personal empleado, LocalDate desde, LocalDate hasta) {
        EntityManager em = XPersistence.getManager();

        // 1. Verificar que no exista liquidación duplicada
        Long existentes = em.createQuery(
                "SELECT COUNT(l) FROM LiquidacionJornadas l " +
                        "WHERE l.empleado = :emp AND l.periodoDesde = :desde AND l.periodoHasta = :hasta",
                Long.class)
                .setParameter("emp", empleado)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();

        if (existentes > 0) {
            throw new IllegalArgumentException(
                    "Ya existe una liquidación para el período " + desde + " - " + hasta);
        }

        // 2. Crear nueva liquidación
        LiquidacionJornadas liquidacion = new LiquidacionJornadas();
        liquidacion.setEmpleado(empleado);
        liquidacion.setPeriodoDesde(desde);
        liquidacion.setPeriodoHasta(hasta);
        liquidacion.setEstadoPeriodo(EstadoLiquidacion.ABIERTO);
        liquidacion.setFechaGeneracion(LocalDateTime.now());

        // 3. Capturar valores snapshot del empleado
        liquidacion.capturarValoresSnapshot();

        // 4. Calcular horas desde AuditoriaRegistros
        calcularHorasDesdeAuditoria(liquidacion, em);

        // 5. Calcular montos
        liquidacion.calcularMontos();

        // 6. Persistir
        em.persist(liquidacion);

        return liquidacion;
    }

    /**
     * Recalcula una liquidación existente desde AuditoriaRegistros.
     * 
     * <p>
     * Solo se permite recalcular liquidaciones con estado ABIERTO o RECALCULADO.
     * Si está CERRADO, lanzará una excepción.
     * </p>
     * 
     * @param liquidacion Liquidación a recalcular
     * @throws IllegalStateException si la liquidación está CERRADA
     */
    public static void recalcularLiquidacion(LiquidacionJornadas liquidacion) {
        if (liquidacion.getEstadoPeriodo() == EstadoLiquidacion.CERRADO) {
            throw new IllegalStateException(
                    "No se puede recalcular una liquidación cerrada. Período: " +
                            liquidacion.getPeriodoDesde() + " - " + liquidacion.getPeriodoHasta());
        }

        EntityManager em = XPersistence.getManager();

        // Recalcular horas
        calcularHorasDesdeAuditoria(liquidacion, em);

        // Actualizar valores snapshot (pueden haber cambiado)
        liquidacion.capturarValoresSnapshot();

        // Recalcular montos
        liquidacion.calcularMontos();

        // Marcar como recalculado
        liquidacion.marcarRecalculado();
    }

    /**
     * Obtiene la liquidación del mes actual para un empleado.
     * Si no existe, la crea.
     * 
     * @param empleado Empleado
     * @return Liquidación del mes actual
     */
    public static LiquidacionJornadas obtenerOCrearLiquidacionMesActual(Personal empleado) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        EntityManager em = XPersistence.getManager();

        try {
            return em.createQuery(
                    "SELECT l FROM LiquidacionJornadas l " +
                            "WHERE l.empleado = :emp AND l.periodoDesde = :desde AND l.periodoHasta = :hasta",
                    LiquidacionJornadas.class)
                    .setParameter("emp", empleado)
                    .setParameter("desde", inicioMes)
                    .setParameter("hasta", finMes)
                    .getSingleResult();
        } catch (NoResultException e) {
            // No existe, crear nueva
            return generarLiquidacion(empleado, inicioMes, finMes);
        }
    }

    /**
     * Cierra una liquidación, marcándola como definitiva.
     * 
     * @param liquidacion Liquidación a cerrar
     */
    public static void cerrarPeriodo(LiquidacionJornadas liquidacion) {
        if (liquidacion.getEstadoPeriodo() == EstadoLiquidacion.CERRADO) {
            return; // Ya está cerrada
        }

        // Recalcular antes de cerrar para asegurar valores finales
        if (liquidacion.getEstadoPeriodo() == EstadoLiquidacion.ABIERTO) {
            EntityManager em = XPersistence.getManager();
            calcularHorasDesdeAuditoria(liquidacion, em);
            liquidacion.calcularMontos();
        }

        liquidacion.cerrar();
    }

    /**
     * Obtiene todas las liquidaciones de un empleado para un año.
     * 
     * @param empleado Empleado
     * @param anio     Año a consultar
     * @return Lista de liquidaciones ordenadas por período descendente
     */
    public static List<LiquidacionJornadas> obtenerLiquidacionesAnuales(Personal empleado, int anio) {
        EntityManager em = XPersistence.getManager();
        LocalDate inicioAnio = LocalDate.of(anio, 1, 1);
        LocalDate finAnio = LocalDate.of(anio, 12, 31);

        return em.createQuery(
                "SELECT l FROM LiquidacionJornadas l " +
                        "WHERE l.empleado = :emp AND l.periodoDesde >= :inicio AND l.periodoHasta <= :fin " +
                        "ORDER BY l.periodoDesde DESC",
                LiquidacionJornadas.class)
                .setParameter("emp", empleado)
                .setParameter("inicio", inicioAnio)
                .setParameter("fin", finAnio)
                .getResultList();
    }

    // ==================================================================================
    // MÉTODOS PRIVADOS DE CÁLCULO
    // ==================================================================================

    /**
     * Calcula las horas desde AuditoriaRegistros y las asigna a la liquidación.
     */
    private static void calcularHorasDesdeAuditoria(LiquidacionJornadas liquidacion, EntityManager em) {
        Personal empleado = liquidacion.getEmpleado();
        LocalDate desde = liquidacion.getPeriodoDesde();
        LocalDate hasta = liquidacion.getPeriodoHasta();

        // Consultar todos los registros del período
        List<AuditoriaRegistros> registros = em.createQuery(
                "SELECT a FROM AuditoriaRegistros a " +
                        "WHERE a.empleado = :emp AND a.fecha BETWEEN :desde AND :hasta",
                AuditoriaRegistros.class)
                .setParameter("emp", empleado)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        int totalNormales = 0;
        int totalExtras = 0;
        int totalEspeciales = 0;

        for (AuditoriaRegistros registro : registros) {
            // Sumar minutos normales (convertir de formato HH:MM)
            totalNormales += convertirHHMMaMinutos(registro.getHorasTrabajadasTurno());

            // Sumar minutos extras
            totalExtras += convertirHHMMaMinutos(registro.getHorasExtras());

            // Sumar minutos especiales
            totalEspeciales += convertirHHMMaMinutos(registro.getHorasEspeciales());
        }

        liquidacion.setTotalMinutosNormales(totalNormales);
        liquidacion.setTotalMinutosExtras(totalExtras);
        liquidacion.setTotalMinutosEspeciales(totalEspeciales);
    }

    /**
     * Convierte formato HH:MM a minutos totales.
     * 
     * @param hhMM String en formato "HH:MM"
     * @return Total de minutos
     */
    private static int convertirHHMMaMinutos(String hhMM) {
        if (hhMM == null || hhMM.isEmpty() || hhMM.equals("00:00")) {
            return 0;
        }
        try {
            String[] partes = hhMM.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = Integer.parseInt(partes[1]);
            return (horas * 60) + minutos;
        } catch (Exception e) {
            return 0;
        }
    }
}
