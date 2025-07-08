package com.sta.biometric.dashboard.acciones;

import java.time.*;
import java.util.*;
import javax.persistence.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.dashboard.*;
import com.sta.biometric.dashboard.auxiliares.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

/**
 * Acción ejecutada automáticamente cuando el usuario cambia la sucursal
 * en el dashboard. Refresca los datos visibles.
 */
public class DashboardActualizarAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        DashboardAsistencia dashboard = (DashboardAsistencia) getView().getEntity();
        Sucursales sucursal = dashboard.getSucursalSeleccionada();

        // ============================
        // 1. Obtener empleados activos
        // ============================
        List<Personal> empleados;
        EntityManager em = XPersistence.getManager();

        if (sucursal != null && sucursal.getId() != null) {
            empleados = em.createQuery("SELECT e FROM Personal e WHERE e.activo = true AND e.sucursal.id = :id", Personal.class)
                .setParameter("id", sucursal.getId())
                .getResultList();
        } else {
            empleados = em.createQuery("SELECT e FROM Personal e WHERE e.activo = true", Personal.class)
                .getResultList();
        }

        // ============================
        // 2. Evaluar día actual
        // ============================
        LocalDate hoy = LocalDate.now();
        List<ResumenEmpleadoHoy> resumenes = ResumenAsistenciaHoyService.calcularResumen(empleados, hoy);

        // ============================
        // 3. Calcular métricas
        // ============================
        int cantidadAgentesHoy = (int) resumenes.stream().filter(ResumenEmpleadoHoy::isDebeTrabajar).count();
        int cantidadConLicenciaHoy = (int) resumenes.stream().filter(ResumenEmpleadoHoy::isConLicencia).count();
        int pendientesDeIngreso = (int) resumenes.stream()
            .filter(r -> r.isDebeTrabajar() && !r.isConLicencia() && !r.isIngresoRealizado())
            .count();
        int cantidadLlegadasTardeHoy = (int) resumenes.stream().filter(ResumenEmpleadoHoy::isLlegadaTarde).count();
        int cantidadSalidasAnticipadasHoy = (int) resumenes.stream().filter(ResumenEmpleadoHoy::isSalidaAnticipada).count();

        // ============================
        // 4. Refrescar vista
        // ============================
        getView().setValue("cantidadAgentesHoy", cantidadAgentesHoy);
        getView().setValue("cantidadConLicenciaHoy", cantidadConLicenciaHoy);
        getView().setValue("pendientesDeIngresoHoy", pendientesDeIngreso);
        getView().setValue("cantidadLlegadasTardeHoy", cantidadLlegadasTardeHoy);
        getView().setValue("cantidadSalidasAnticipadasHoy", cantidadSalidasAnticipadasHoy);

        getView().setValue("fechaHoraActual", dashboard.getFechaHoraActual());
        getView().refreshCollections(); // Refresca `registrosHoy` y `distribucionAsistenciaHoy`
    }
}
