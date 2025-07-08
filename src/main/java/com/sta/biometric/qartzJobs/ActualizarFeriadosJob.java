package com.sta.biometric.qartzJobs;
import org.quartz.*;

import com.sta.biometric.servicios.*;

/**
 * Tarea que actualiza los feriados automáticamente el 1 de enero.
 */
@DisallowConcurrentExecution
public class ActualizarFeriadosJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            ImportadorFeriadosService.importarFeriadosDelAnioActual();
            System.out.println("Feriados actualizados automáticamente desde ArgentinaDatos.com");
        } catch (Exception e) {
            System.err.println("Error al actualizar feriados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
