package com.sta.biometric.dto;

public class InformeMensualDTO {
    private String mes;
    private int presentes;
    private int ausentes;
    private int tardanzas;
    private double horasNormales;
    private double horasExtras;
    private double horasEspeciales;
    private double cumplimiento;

    public InformeMensualDTO(String mes, int presentes, int ausentes, int tardanzas,
            double horasNormales, double horasExtras, double horasEspeciales, double cumplimiento) {
        this.mes = mes;
        this.presentes = presentes;
        this.ausentes = ausentes;
        this.tardanzas = tardanzas;
        this.horasNormales = horasNormales;
        this.horasExtras = horasExtras;
        this.horasEspeciales = horasEspeciales;
        this.cumplimiento = cumplimiento;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public int getPresentes() {
        return presentes;
    }

    public void setPresentes(int presentes) {
        this.presentes = presentes;
    }

    public int getAusentes() {
        return ausentes;
    }

    public void setAusentes(int ausentes) {
        this.ausentes = ausentes;
    }

    public int getTardanzas() {
        return tardanzas;
    }

    public void setTardanzas(int tardanzas) {
        this.tardanzas = tardanzas;
    }

    public double getHorasNormales() {
        return horasNormales;
    }

    public void setHorasNormales(double horasNormales) {
        this.horasNormales = horasNormales;
    }

    public double getHorasExtras() {
        return horasExtras;
    }

    public void setHorasExtras(double horasExtras) {
        this.horasExtras = horasExtras;
    }

    public double getHorasEspeciales() {
        return horasEspeciales;
    }

    public void setHorasEspeciales(double horasEspeciales) {
        this.horasEspeciales = horasEspeciales;
    }

    public double getCumplimiento() {
        return cumplimiento;
    }

    public void setCumplimiento(double cumplimiento) {
        this.cumplimiento = cumplimiento;
    }
}
