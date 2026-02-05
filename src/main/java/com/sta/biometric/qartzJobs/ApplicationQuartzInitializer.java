package com.sta.biometric.qartzJobs;

import javax.servlet.*;
import javax.servlet.annotation.*;

import org.quartz.*;
import org.quartz.impl.*;

/**
 * Inicializador de Quartz que se ejecuta automáticamente al arrancar Tomcat o
 * el contenedor de Servlets.
 * 
 * <p>
 * Jobs programados:
 * </p>
 * <ul>
 * <li><strong>AperturaJornadaJob</strong>: 00:01 AM - Crea jornadas para el
 * día</li>
 * <li><strong>CierreJornadaJob</strong>: 23:55 PM - Cierra jornadas
 * diurnas</li>
 * <li><strong>CierreJornadaNocturnaJob</strong>: 08:00 AM - Cierra jornadas
 * nocturnas del día anterior</li>
 * </ul>
 */
@WebListener
public class ApplicationQuartzInitializer implements ServletContextListener {

        private static boolean iniciado = false;
        private static Scheduler scheduler;

        @Override
        public void contextInitialized(ServletContextEvent sce) {
                if (iniciado)
                        return;
                iniciado = true;

                try {
                        scheduler = StdSchedulerFactory.getDefaultScheduler();

                        // =================================================================
                        // JOB 1: Apertura diaria - 00:01 AM
                        // =================================================================
                        JobDetail aperturaJob = JobBuilder.newJob(AperturaJornadaJob.class)
                                        .withIdentity("aperturaJob", "asistencia")
                                        .build();

                        Trigger aperturaTrigger = TriggerBuilder.newTrigger()
                                        .withIdentity("aperturaTrigger", "asistencia")
                                        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 1))
                                        .build();

                        scheduler.scheduleJob(aperturaJob, aperturaTrigger);
                        System.out.println("[Quartz] Job APERTURA programado: 00:01 AM");

                        // =================================================================
                        // JOB 2: Cierre diario - 23:55 PM (margen antes de medianoche)
                        // =================================================================
                        JobDetail cierreJob = JobBuilder.newJob(CierreJornadaJob.class)
                                        .withIdentity("cierreJob", "asistencia")
                                        .build();

                        Trigger cierreTrigger = TriggerBuilder.newTrigger()
                                        .withIdentity("cierreTrigger", "asistencia")
                                        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(23, 55))
                                        .build();

                        scheduler.scheduleJob(cierreJob, cierreTrigger);
                        System.out.println("[Quartz] Job CIERRE programado: 23:55 PM");

                        // =================================================================
                        // JOB 3: Cierre de jornadas nocturnas - 08:00 AM
                        // Cierra jornadas del día anterior que quedaron EN_CURSO por ser nocturnas
                        // =================================================================
                        JobDetail cierreNocturnoJob = JobBuilder.newJob(CierreJornadaNocturnaJob.class)
                                        .withIdentity("cierreNocturnoJob", "asistencia")
                                        .build();

                        Trigger cierreNocturnoTrigger = TriggerBuilder.newTrigger()
                                        .withIdentity("cierreNocturnoTrigger", "asistencia")
                                        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(8, 0))
                                        .build();

                        scheduler.scheduleJob(cierreNocturnoJob, cierreNocturnoTrigger);
                        System.out.println("[Quartz] Job CIERRE NOCTURNO programado: 08:00 AM");

                        // =================================================================
                        // Iniciar Scheduler
                        // =================================================================
                        scheduler.start();
                        System.out.println("[Quartz] ===== Scheduler iniciado correctamente =====");

                } catch (SchedulerException e) {
                        System.err.println("[Quartz] ERROR al iniciar: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
                try {
                        if (scheduler != null && scheduler.isStarted()) {
                                System.out.println("[Quartz] Apagando scheduler...");
                                scheduler.shutdown(true); // true = esperar a que terminen jobs en ejecución
                                System.out.println("[Quartz] Scheduler apagado correctamente.");
                        }
                } catch (SchedulerException e) {
                        System.err.println("[Quartz] Error al apagar: " + e.getMessage());
                        e.printStackTrace();
                }
        }
}
