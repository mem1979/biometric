package com.sta.biometric.servicios;

import java.math.*;
import java.time.*;
import java.util.*;
import javax.persistence.*;

import org.openxava.jpa.*;
import org.openxava.util.*;

import com.sta.biometric.modelo.*; // Personal, AuditoriaRegistros
import com.sta.biometric.modelo.rrhh.*; // Contrato, Periodo, Liquidacion, etc.
import com.sta.biometric.modelo.rrhh.enums.*;

import lombok.extern.apachecommons.*;

/**
 * Servicio central de Liquidación de Haberes.
 * 
 * Responsabilidades:
 * 1. Identificar empleados activos y sus contratos vigentes en el período.
 * 2. Calcular conceptos según el régimen (Mensual, Jornal, Servicios).
 * 3. Generar la Liquidación (Cabecera) y sus Items (Detalle).
 */
@CommonsLog
public class ProcesadorLiquidacionService {

    private static ProcesadorLiquidacionService instance;

    public static ProcesadorLiquidacionService getInstance() {
        if (instance == null) {
            instance = new ProcesadorLiquidacionService();
        }
        return instance;
    }

    /**
     * Ejecuta la liquidación para un período dado, procesando todos los contratos
     * activos.
     * 
     * @param periodo Período a liquidar.
     * @throws Exception Si ocurre algún error en el proceso.
     */
    public void liquidarPeriodo(PeriodoLiquidacion periodo) throws Exception {
        log.info("Iniciando liquidación para período: " + periodo.getDescripcion());

        // 1. Obtener contratos activos en el período
        List<Contrato> contratosActivos = obtenerContratosActivos(periodo.getFechaInicio(), periodo.getFechaFin());

        for (Contrato contrato : contratosActivos) {
            try {
                liquidarContrato(contrato, periodo);
            } catch (Exception e) {
                log.error("Error liquidando contrato de: " + contrato.getPersonal().getNombreCompleto(), e);
                // Podríamos continuar con el siguiente o detener, depende de la política. Por
                // ahora logueamos.
            }
        }

        log.info("Liquidación finalizada.");
    }

    /**
     * Liquida un contrato individual.
     */
    private void liquidarContrato(Contrato contrato, PeriodoLiquidacion periodo) {
        // Verificar si ya existe liquidación para este contrato y período (para no
        // duplicar)
        // Por simplicidad, asumimos que si se corre de nuevo, se borra la anterior o se
        // actualiza.
        // Aquí borramos la anterior si existe.
        borrarLiquidacionExistente(contrato, periodo);

        Liquidacion liquidacion = new Liquidacion();
        liquidacion.setPeriodo(periodo);
        liquidacion.setContrato(contrato);
        liquidacion.setFechaCalculo(LocalDate.now());

        // Determinar tipo de documento
        if (contrato.getRegimen() == RegimenContratacion.LOCACION_SERVICIOS) {
            liquidacion.setTipoDocumento(TipoDocumentoPago.ORDEN_PAGO);
        } else if (contrato.getRegimen() == RegimenContratacion.INFORMAL) {
            liquidacion.setTipoDocumento(TipoDocumentoPago.RECIBO_INTERNO);
        } else {
            liquidacion.setTipoDocumento(TipoDocumentoPago.RECIBO_LEY);
        }

        XPersistence.getManager().persist(liquidacion);

        // --- Cálculo de Conceptos ---
        List<ItemLiquidacion> items = new ArrayList<>();

        // A. Conceptos Fijos / Estructurales (Básico, Antigüedad)
        items.addAll(calcularConceptosEstructurales(contrato, periodo));

        // B. Conceptos Variables (Asistencia, Horas Extras - Integración con Biometric)
        items.addAll(calcularConceptosVariables(contrato, periodo));

        // C. Descuentos y Retenciones (Jubilación, Obra Social, Ganancias, Préstamos)
        items.addAll(calcularDeducciones(contrato, items)); // Pasamos items previos para calcular brutos

        // Persistir Items
        for (ItemLiquidacion item : items) {
            item.setLiquidacion(liquidacion);
            XPersistence.getManager().persist(item);
        }

        // Actualizar Totales Cabecera
        liquidacion.setItems(items);
        liquidacion.recalcularTotales();
        XPersistence.getManager().merge(liquidacion);
    }

    // --- Métodos Auxiliares de Consulta y Cálculo ---

    private List<Contrato> obtenerContratosActivos(LocalDate inicio, LocalDate fin) {
        String jpql = "SELECT c FROM Contrato c WHERE c.activo = true " +
                "AND c.fechaInicio <= :fin " +
                "AND (c.fechaFin IS NULL OR c.fechaFin >= :inicio)";
        return XPersistence.getManager().createQuery(jpql, Contrato.class)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();
    }

    private void borrarLiquidacionExistente(Contrato contrato, PeriodoLiquidacion periodo) {
        String jpql = "SELECT l FROM Liquidacion l WHERE l.contrato.id = :contratoId AND l.periodo.id = :periodoId";
        List<Liquidacion> existentes = XPersistence.getManager().createQuery(jpql, Liquidacion.class)
                .setParameter("contratoId", contrato.getId())
                .setParameter("periodoId", periodo.getId())
                .getResultList();

        for (Liquidacion l : existentes) {
            XPersistence.getManager().remove(l);
        }
    }

    // --- Stubs de Cálculo (A implementar detalle) ---

    private List<ItemLiquidacion> calcularConceptosEstructurales(Contrato contrato, PeriodoLiquidacion periodo) {
        List<ItemLiquidacion> items = new ArrayList<>();

        // Ejemplo simple: Sueldo Básico
        BigDecimal basico = BigDecimal.ZERO;

        if (contrato.getBasicoPactado() != null) {
            basico = contrato.getBasicoPactado();
        } else if (contrato.getCategoria() != null && contrato.getCategoria().getBasicoMensual() != null) {
            basico = contrato.getCategoria().getBasicoMensual();
        }

        if (basico.compareTo(BigDecimal.ZERO) > 0) {
            // Buscar concepto "SUELDO BASICO" (Hardcodeado por ahora, idealmente buscar por
            // código en DB)
            ConceptoRecibo conceptoBasico = buscarConcepto("100"); // Asumimos 100 es básico
            if (conceptoBasico != null) {
                ItemLiquidacion item = new ItemLiquidacion();
                item.setConcepto(conceptoBasico);
                item.setUnidad("Mes");
                item.setCantidad(new BigDecimal("30"));
                item.setImporte(basico);
                items.add(item);
            }
        }
        return items;
    }

    private List<ItemLiquidacion> calcularConceptosVariables(Contrato contrato, PeriodoLiquidacion periodo) {
        List<ItemLiquidacion> items = new ArrayList<>();

        // 1. Obtener registros de asistencia del período
        List<AuditoriaRegistros> registros = obtenerAsistencia(contrato.getPersonal(), periodo.getFechaInicio(),
                periodo.getFechaFin());

        BigDecimal totalHorasNormales = BigDecimal.ZERO;
        BigDecimal totalHorasExtras = BigDecimal.ZERO; // Al 50%

        for (AuditoriaRegistros reg : registros) {
            if (reg.getMinutosTrabajados() > 0) {
                totalHorasNormales = totalHorasNormales.add(
                        new BigDecimal(reg.getMinutosTrabajados()).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP));
            }
            if (reg.getMinutosExtras() > 0) {
                totalHorasExtras = totalHorasExtras.add(
                        new BigDecimal(reg.getMinutosExtras()).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP));
            }
        }

        // CASO A: Jornalizado (Cobra por hora)
        if (contrato.getModalidadLiquidacion() == ModalidadLiquidacion.QUINCENAL ||
                contrato.getModalidadLiquidacion() == ModalidadLiquidacion.DIARIO) {

            BigDecimal valorHora = contrato.getValorHoraPactado();
            if (valorHora == null && contrato.getCategoria() != null) {
                valorHora = contrato.getCategoria().getValorHora();
            }
            if (valorHora == null)
                valorHora = BigDecimal.ZERO;

            if (totalHorasNormales.compareTo(BigDecimal.ZERO) > 0) {
                items.add(crearItem("101", "HS NORMALES", "Hs", totalHorasNormales,
                        valorHora.multiply(totalHorasNormales)));
            }
        }

        // CASO B: Extras (Para todos)
        if (totalHorasExtras.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal valorHora = (contrato.getValorHoraPactado() != null) ? contrato.getValorHoraPactado()
                    : (contrato.getCategoria() != null ? contrato.getCategoria().getValorHora() : BigDecimal.ZERO);

            if (valorHora.equals(BigDecimal.ZERO) && contrato.getBasicoPactado() != null) {
                valorHora = contrato.getBasicoPactado().divide(new BigDecimal(200), 2, RoundingMode.HALF_UP);
            }

            BigDecimal valorHoraExtra = valorHora.multiply(new BigDecimal("1.5")); // Al 50%
            items.add(crearItem("201", "HS EXTRAS 50%", "Hs", totalHorasExtras,
                    valorHoraExtra.multiply(totalHorasExtras)));
        }

        return items;
    }

    private List<ItemLiquidacion> calcularDeducciones(Contrato contrato, List<ItemLiquidacion> remunerativos) {
        List<ItemLiquidacion> items = new ArrayList<>();

        // Solo para Relación de Dependencia (LCT)
        if (contrato.getRegimen() != RegimenContratacion.RELACION_DEPENDENCIA) {
            return items;
        }

        // 1. Calcular Bruto Remunerativo
        BigDecimal brutoRemunerativo = BigDecimal.ZERO;
        for (ItemLiquidacion item : remunerativos) {
            if (item.getConcepto() != null && item.getConcepto().getTipo() == TipoConcepto.REMUNERATIVO) {
                brutoRemunerativo = brutoRemunerativo.add(item.getImporte());
            }
        }

        if (brutoRemunerativo.compareTo(BigDecimal.ZERO) <= 0) {
            return items;
        }

        // 2. Aplicar Retenciones de Ley (11% + 3% + 3% = 17%)

        // 501: Jubilación (11%)
        agregarDeduccion(items, "501", "JUBILACION (SIPA)", brutoRemunerativo, new BigDecimal("0.11"));

        // 502: Ley 19.032 (3%)
        agregarDeduccion(items, "502", "LEY 19.032 (INSSJP)", brutoRemunerativo, new BigDecimal("0.03"));

        // 503: Obra Social (3%)
        agregarDeduccion(items, "503", "OBRA SOCIAL", brutoRemunerativo, new BigDecimal("0.03"));

        // (Opcional) Sindicato si aplica
        if (contrato.getConvenio() != null && "CEC".equals(contrato.getConvenio().getSigla())) {
            agregarDeduccion(items, "504", "SINDICATO", brutoRemunerativo, new BigDecimal("0.02"));
            agregarDeduccion(items, "505", "FAECYS", brutoRemunerativo, new BigDecimal("0.005"));
        }

        return items;
    }

    private void agregarDeduccion(List<ItemLiquidacion> items, String codigo, String desc, BigDecimal base,
            BigDecimal porcentaje) {
        BigDecimal monto = base.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
        if (monto.compareTo(BigDecimal.ZERO) > 0) {
            ItemLiquidacion item = crearItem(codigo, desc, "%", porcentaje.multiply(new BigDecimal(100)), monto);
            items.add(item);
        }
    }

    private List<AuditoriaRegistros> obtenerAsistencia(Personal personal, LocalDate inicio, LocalDate fin) {
        String jpql = "SELECT a FROM AuditoriaRegistros a WHERE a.empleado.id = :personalId " +
                "AND a.fecha BETWEEN :inicio AND :fin ORDER BY a.fecha ASC";
        return XPersistence.getManager().createQuery(jpql, AuditoriaRegistros.class)
                .setParameter("personalId", personal.getId())
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();
    }

    private ItemLiquidacion crearItem(String codigoConcepto, String descripcionAux, String unidad, BigDecimal cantidad,
            BigDecimal importe) {
        ItemLiquidacion item = new ItemLiquidacion();
        ConceptoRecibo c = buscarConcepto(codigoConcepto);
        if (c == null) {
            c = new ConceptoRecibo();
            c.setCodigo(codigoConcepto);
            c.setDescripcion(descripcionAux);
        }
        item.setConcepto(c);
        item.setUnidad(unidad);
        item.setCantidad(cantidad);
        item.setImporte(importe);
        return item;
    }

    private ConceptoRecibo buscarConcepto(String codigo) {
        try {
            return XPersistence.getManager()
                    .createQuery("FROM ConceptoRecibo c WHERE c.codigo = :codigo", ConceptoRecibo.class)
                    .setParameter("codigo", codigo)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
