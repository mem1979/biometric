package com.sta.biometric.servicios;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.openxava.jpa.*;
import org.openxava.util.Users;

import com.sta.biometric.enums.TipoRedondeo;
import com.sta.biometric.modelo.*;

/**
 * Servicio para aplicar redondeo automatico de horas.
 * 
 * <p>
 * Calcula el ajuste necesario para redondear el TOTAL de cada tipo de hora
 * (maximo 30 minutos), y luego distribuye ese ajuste al ultimo registro.
 * </p>
 * 
 * <p>
 * Los ajustes de redondeo se guardan en campos SEPARADOS de los ajustes
 * manuales:
 * - ajusteMinutosXXX = ajustes manuales del supervisor
 * - ajusteRedondeoXXX = ajustes de redondeo automatico
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class RedondeoHorasService {

    /** Ajuste maximo permitido: 30 minutos (media hora) */
    private static final int AJUSTE_MAXIMO = 30;

    /**
     * Calcula el ajuste de redondeo para un total de minutos.
     * 
     * @param minutos   Total de minutos a redondear
     * @param intervalo Intervalo de redondeo (ej: 30)
     * @param tipo      Estrategia de redondeo
     * @return Ajuste en minutos (positivo = suma, negativo = resta)
     */
    public static int calcularAjusteParaTotal(int minutos, int intervalo, TipoRedondeo tipo) {
        if (intervalo <= 0 || minutos <= 0)
            return 0;

        int residuo = minutos % intervalo;
        if (residuo == 0)
            return 0;

        int ajusteArriba = intervalo - residuo;
        int ajusteAbajo = -residuo;

        int ajuste;
        switch (tipo) {
            case A_FAVOR_EMPLEADO:
                ajuste = ajusteArriba;
                break;
            case A_FAVOR_EMPRESA:
                ajuste = ajusteAbajo;
                break;
            case MATEMATICO:
            default:
                ajuste = (ajusteArriba <= Math.abs(ajusteAbajo)) ? ajusteArriba : ajusteAbajo;
                break;
        }

        if (ajuste > AJUSTE_MAXIMO) {
            ajuste = AJUSTE_MAXIMO;
        } else if (ajuste < -AJUSTE_MAXIMO) {
            ajuste = -AJUSTE_MAXIMO;
        }

        return ajuste;
    }

    /**
     * Aplica redondeo masivo distribuyendo el ajuste entre los registros.
     */
    public static int aplicarRedondeoMasivo(LiquidacionJornadas liquidacion, ConfiguracionRedondeo config) {
        if (liquidacion == null || config == null)
            return 0;

        List<AuditoriaRegistros> registros = liquidacion.getJornadasDelPeriodo();
        if (registros == null || registros.isEmpty())
            return 0;

        int intervalo = config.getIntervaloMinutos();
        int registrosModificados = 0;
        StringBuilder resumenLiquidacion = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String usuario = Users.getCurrent();

        // === HORAS NORMALES ===
        int totalNormales = calcularTotalMinutos(registros, "normales");
        int ajusteN = calcularAjusteParaTotal(totalNormales, intervalo, config.getEstrategiaNormales());
        if (ajusteN != 0) {
            int distribuidos = distribuirAjuste(registros, "normales", ajusteN,
                    config.getEstrategiaNormales(), intervalo, timestamp, usuario);
            registrosModificados += distribuidos;
            resumenLiquidacion.append("Normales[").append(abreviarEstrategia(config.getEstrategiaNormales()))
                    .append("]: ")
                    .append(formatearMinutosHHMM(totalNormales)).append("->")
                    .append(formatearMinutosHHMM(totalNormales + ajusteN))
                    .append(" (").append(formatearConSigno(ajusteN)).append("); ");
        }

        // === HORAS EXTRAS ===
        int totalExtras = calcularTotalMinutos(registros, "extras");
        int ajusteE = calcularAjusteParaTotal(totalExtras, intervalo, config.getEstrategiaExtras());
        if (ajusteE != 0) {
            int distribuidos = distribuirAjuste(registros, "extras", ajusteE,
                    config.getEstrategiaExtras(), intervalo, timestamp, usuario);
            registrosModificados += distribuidos;
            resumenLiquidacion.append("Extras[").append(abreviarEstrategia(config.getEstrategiaExtras())).append("]: ")
                    .append(formatearMinutosHHMM(totalExtras)).append("->")
                    .append(formatearMinutosHHMM(totalExtras + ajusteE))
                    .append(" (").append(formatearConSigno(ajusteE)).append("); ");
        }

        // === HORAS ESPECIALES ===
        int totalEspeciales = calcularTotalMinutos(registros, "especiales");
        int ajusteS = calcularAjusteParaTotal(totalEspeciales, intervalo, config.getEstrategiaEspeciales());
        if (ajusteS != 0) {
            int distribuidos = distribuirAjuste(registros, "especiales", ajusteS,
                    config.getEstrategiaEspeciales(), intervalo, timestamp, usuario);
            registrosModificados += distribuidos;
            resumenLiquidacion.append("Especiales[").append(abreviarEstrategia(config.getEstrategiaEspeciales()))
                    .append("]: ")
                    .append(formatearMinutosHHMM(totalEspeciales)).append("->")
                    .append(formatearMinutosHHMM(totalEspeciales + ajusteS))
                    .append(" (").append(formatearConSigno(ajusteS)).append("); ");
        }

        // Registrar en observaciones de liquidacion
        if (resumenLiquidacion.length() > 0) {
            String lineaNota = String.format("[%s] %s | Redondeo (%dmin): %s",
                    timestamp, usuario, intervalo, resumenLiquidacion.toString().trim());
            String obsActual = liquidacion.getObservaciones();
            if (obsActual == null || obsActual.isBlank()) {
                liquidacion.setObservaciones(lineaNota);
            } else {
                liquidacion.setObservaciones(obsActual + "\n" + lineaNota);
            }
        }

        // Persistir registros modificados
        for (AuditoriaRegistros reg : registros) {
            if (reg.isRedondeoAutoAplicado()) {
                XPersistence.getManager().merge(reg);
            }
        }
        XPersistence.getManager().merge(liquidacion);

        return registrosModificados;
    }

    /**
     * Calcula el total de minutos de un tipo de hora sumando todos los registros.
     */
    private static int calcularTotalMinutos(List<AuditoriaRegistros> registros, String tipoHora) {
        int total = 0;
        for (AuditoriaRegistros reg : registros) {
            switch (tipoHora) {
                case "normales":
                    total += parsearHHMM(reg.getHorasBaseNormales()) + reg.getAjusteMinutosNormales();
                    break;
                case "extras":
                    total += parsearHHMM(reg.getHorasBaseExtras()) + reg.getAjusteMinutosExtras();
                    break;
                case "especiales":
                    total += parsearHHMM(reg.getHorasBaseEspeciales()) + reg.getAjusteMinutosEspeciales();
                    break;
            }
        }
        return total;
    }

    /**
     * Distribuye un ajuste al ultimo registro con horas del tipo especificado.
     */
    private static int distribuirAjuste(List<AuditoriaRegistros> registros, String tipoHora,
            int ajuste, TipoRedondeo estrategia, int intervalo, String timestamp, String usuario) {

        List<AuditoriaRegistros> registrosConHoras = new ArrayList<>();
        for (AuditoriaRegistros reg : registros) {
            int minutos = 0;
            switch (tipoHora) {
                case "normales":
                    minutos = parsearHHMM(reg.getHorasBaseNormales()) + reg.getAjusteMinutosNormales();
                    break;
                case "extras":
                    minutos = parsearHHMM(reg.getHorasBaseExtras()) + reg.getAjusteMinutosExtras();
                    break;
                case "especiales":
                    minutos = parsearHHMM(reg.getHorasBaseEspeciales()) + reg.getAjusteMinutosEspeciales();
                    break;
            }
            if (minutos > 0) {
                registrosConHoras.add(reg);
            }
        }

        if (registrosConHoras.isEmpty()) {
            return 0;
        }

        AuditoriaRegistros ultimoRegistro = registrosConHoras.get(registrosConHoras.size() - 1);

        StringBuilder cambios = new StringBuilder();
        String tipoDisplay = "";
        switch (tipoHora) {
            case "normales":
                ultimoRegistro.setAjusteRedondeoNormales(ultimoRegistro.getAjusteRedondeoNormales() + ajuste);
                tipoDisplay = "Normales";
                break;
            case "extras":
                ultimoRegistro.setAjusteRedondeoExtras(ultimoRegistro.getAjusteRedondeoExtras() + ajuste);
                tipoDisplay = "Extras";
                break;
            case "especiales":
                ultimoRegistro.setAjusteRedondeoEspeciales(ultimoRegistro.getAjusteRedondeoEspeciales() + ajuste);
                tipoDisplay = "Especiales";
                break;
        }
        cambios.append(tipoDisplay).append("[").append(abreviarEstrategia(estrategia)).append("]: ")
                .append(formatearConSigno(ajuste));

        // Registrar trazabilidad en nota del registro
        String lineaMotivo = String.format("[%s] %s | Redondeo masivo (%dmin) %s",
                timestamp, usuario, intervalo, cambios.toString());
        String notaActual = ultimoRegistro.getNota();
        if (notaActual == null || notaActual.isBlank()) {
            ultimoRegistro.setNota(lineaMotivo);
        } else {
            ultimoRegistro.setNota(notaActual + "\n" + lineaMotivo);
        }
        ultimoRegistro.setRedondeoAutoAplicado(true);

        return 1;
    }

    /**
     * Revierte los ajustes de redondeo de todos los registros de una liquidacion.
     */
    public static int revertirRedondeo(LiquidacionJornadas liquidacion) {
        if (liquidacion == null)
            return 0;

        List<AuditoriaRegistros> registros = liquidacion.getJornadasDelPeriodo();
        if (registros == null || registros.isEmpty())
            return 0;

        int revertidos = 0;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String usuario = Users.getCurrent();
        StringBuilder resumenLiquidacion = new StringBuilder();

        for (AuditoriaRegistros reg : registros) {
            if (reg.isRedondeoAutoAplicado()) {
                int ajusteN = reg.getAjusteRedondeoNormales();
                int ajusteE = reg.getAjusteRedondeoExtras();
                int ajusteS = reg.getAjusteRedondeoEspeciales();

                reg.setAjusteRedondeoNormales(0);
                reg.setAjusteRedondeoExtras(0);
                reg.setAjusteRedondeoEspeciales(0);
                reg.setRedondeoAutoAplicado(false);

                StringBuilder cambios = new StringBuilder();
                if (ajusteN != 0)
                    cambios.append("N:").append(ajusteN).append("->0 ");
                if (ajusteE != 0)
                    cambios.append("E:").append(ajusteE).append("->0 ");
                if (ajusteS != 0)
                    cambios.append("S:").append(ajusteS).append("->0 ");

                if (cambios.length() > 0) {
                    String lineaNota = String.format("[%s] %s | Redondeo revertido: %s",
                            timestamp, usuario, cambios.toString().trim());
                    String notaActual = reg.getNota();
                    if (notaActual == null || notaActual.isBlank()) {
                        reg.setNota(lineaNota);
                    } else {
                        reg.setNota(notaActual + "\n" + lineaNota);
                    }
                    resumenLiquidacion.append(cambios.toString().trim()).append("; ");
                }

                XPersistence.getManager().merge(reg);
                revertidos++;
            }
        }

        if (resumenLiquidacion.length() > 0) {
            String lineaNota = String.format("[%s] %s | Redondeo revertido: %s",
                    timestamp, usuario, resumenLiquidacion.toString().trim());
            String obsActual = liquidacion.getObservaciones();
            if (obsActual == null || obsActual.isBlank()) {
                liquidacion.setObservaciones(lineaNota);
            } else {
                liquidacion.setObservaciones(obsActual + "\n" + lineaNota);
            }
            XPersistence.getManager().merge(liquidacion);
        }

        return revertidos;
    }

    // ==================================================================================
    // METODOS UTILITARIOS
    // ==================================================================================

    private static String abreviarEstrategia(TipoRedondeo tipo) {
        if (tipo == null)
            return "?";
        switch (tipo) {
            case A_FAVOR_EMPLEADO:
                return "Empl";
            case A_FAVOR_EMPRESA:
                return "Emp";
            case MATEMATICO:
                return "Mat";
            default:
                return tipo.name();
        }
    }

    private static int parsearHHMM(String hhmm) {
        if (hhmm == null || hhmm.isBlank())
            return 0;
        try {
            String[] partes = hhmm.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = partes.length > 1 ? Integer.parseInt(partes[1]) : 0;
            return horas * 60 + minutos;
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formatearConSigno(int minutos) {
        String signo = minutos >= 0 ? "+" : "";
        int abs = Math.abs(minutos);
        int h = abs / 60;
        int m = abs % 60;
        if (h > 0) {
            return signo + String.format("%d:%02d", h, m);
        }
        return signo + m + "min";
    }

    private static String formatearMinutosHHMM(int minutos) {
        int h = minutos / 60;
        int m = minutos % 60;
        return String.format("%d:%02d", h, m);
    }
}
