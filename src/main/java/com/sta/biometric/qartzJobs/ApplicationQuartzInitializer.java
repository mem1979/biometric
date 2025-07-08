package com.sta.biometric.qartzJobs;
import javax.servlet.*;
import javax.servlet.annotation.*;

import org.quartz.*;
import org.quartz.impl.*;

/**
 * Inicializador de Quartz que se ejecuta automáticamente al arrancar Tomcat o el contenedor de Servlets.
 */
@WebListener
public class ApplicationQuartzInitializer implements ServletContextListener {

    private static boolean iniciado = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (iniciado) return;
        iniciado = true;

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            

            // Apertura diaria - 00:01 AM
            JobDetail aperturaJob = JobBuilder.newJob(AperturaJornadaJob.class)
                .withIdentity("aperturaJob", "asistencia")
                .build();

            Trigger aperturaTrigger = TriggerBuilder.newTrigger()
                .withIdentity("aperturaTrigger", "asistencia")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(22, 45))
                .build();

            scheduler.scheduleJob(aperturaJob, aperturaTrigger);
            System.out.println("Job de apertura de jornada programado a las 00:01");

            // Cierre diario - 23:59 PM
            JobDetail cierreJob = JobBuilder.newJob(CierreJornadaJob.class)
                .withIdentity("cierreJob", "asistencia")
                .build();

            Trigger cierreTrigger = TriggerBuilder.newTrigger()
                .withIdentity("cierreTrigger", "asistencia")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(22, 50))
                .build();

            scheduler.scheduleJob(cierreJob, cierreTrigger);
            System.out.println("Job de cierre de jornada programado a las 23:59");

            scheduler.start();

        } catch (SchedulerException e) {
            System.err.println("Error al iniciar Quartz: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
