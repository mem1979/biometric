package com.sta.biometric.servicios;

import java.io.*;
import java.net.*;

import org.json.*;

import com.sta.biometric.embebidas.*;

/**
 * Servicio para invocar la API de OpenCage y extraer coordenadas y/o código
 * postal.
 * 
 * Mejoras implementadas:
 * - Normalización de direcciones argentinas
 * - Estrategia de fallback con múltiples intentos
 * - Puntuación de confianza
 * - Validación de coordenadas dentro de Argentina
 */
public class AsignarCoordenadasService {

    private static final String apiKey = ConfiguracionesPreferencias.getInstance()
            .getProperties().getProperty("OPENCAGE_API_KEY");

    // Bounding box de Argentina (aproximado)
    private static final double AR_LAT_MIN = -55.0;
    private static final double AR_LAT_MAX = -21.7;
    private static final double AR_LNG_MIN = -73.5;
    private static final double AR_LNG_MAX = -53.6;

    /**
     * Asigna coordenadas a una dirección si no las tiene.
     * Usa estrategia de fallback para mayor precisión.
     */
    public static void asignarCoordenadasSiFaltan(Direccion direccion) throws Exception {
        if (direccion == null || direccion.getUbicacion() != null)
            return;

        GeoData geoData = obtenerGeoDataConFallback(direccion, apiKey);
        if (geoData != null && geoData.getCoordenadas() != null) {
            direccion.setUbicacion(geoData.getCoordenadas());
        }
    }

    /**
     * Estrategia de fallback: intenta múltiples formatos de dirección
     * para obtener la mejor precisión posible.
     * Incluye todos los datos disponibles: calle, número, localidad, partido,
     * provincia y CP.
     */
    public static GeoData obtenerGeoDataConFallback(Direccion direccion, String apiKey) throws Exception {
        String calle = normalizarDireccion(direccion.getCalle());
        String numero = direccion.getNumero() != null ? direccion.getNumero().trim() : "";
        String localidad = direccion.getLocalidad() != null ? direccion.getLocalidad().getNombre() : "";
        String partido = direccion.getPartido() != null ? direccion.getPartido().getNombre() : "";
        String provincia = direccion.getProvincia() != null ? direccion.getProvincia().getNombre() : "";
        String codigoPostal = direccion.getCodigoPostal() != null ? extraerDigitos(direccion.getCodigoPostal()) : "";

        GeoData mejorResultado = null;

        // Intento 1: Dirección COMPLETA con todos los datos (máxima precisión)
        if (calle != null && !calle.isEmpty() && !numero.isEmpty()) {
            // Formato optimizado: "Calle 1234, CP, Localidad, Partido, Provincia,
            // Argentina"
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(calle).append(" ").append(numero);

            // Agregar código postal si existe (ayuda mucho a la precisión)
            if (!codigoPostal.isEmpty()) {
                queryBuilder.append(", ").append(codigoPostal);
            }

            // Agregar localidad
            if (!localidad.isEmpty()) {
                queryBuilder.append(", ").append(localidad);
            }

            // Agregar partido
            if (!partido.isEmpty()) {
                queryBuilder.append(", ").append(partido);
            }

            // Agregar provincia y país
            if (!provincia.isEmpty()) {
                queryBuilder.append(", ").append(provincia);
            }
            queryBuilder.append(", Argentina");

            String query = queryBuilder.toString();
            System.out.println("[GeoService] Intento 1 (completo): " + query);
            GeoData result = obtenerGeoData(query, apiKey);
            if (result != null && result.getCoordenadas() != null) {
                result.setNivelPrecision("CALLE_NUMERO_CP");
                System.out.println("[GeoService] Resultado: confianza=" + result.getConfianza());
                // Si tiene confianza >= 5, usarlo directamente
                if (result.getConfianza() >= 5) {
                    return result;
                }
                mejorResultado = result;
            }
        }

        // Intento 2: Solo calle sin número (si no tenemos resultado aún o confianza
        // baja)
        if (calle != null && !calle.isEmpty() && (mejorResultado == null || mejorResultado.getConfianza() < 3)) {
            String query = String.format("%s, %s, %s, Argentina", calle, localidad, provincia);
            System.out.println("[GeoService] Intento 2 (calle): " + query);
            GeoData result = obtenerGeoData(query, apiKey);
            if (result != null && result.getCoordenadas() != null) {
                result.setNivelPrecision("CALLE");
                System.out.println("[GeoService] Resultado: confianza=" + result.getConfianza());
                if (mejorResultado == null || result.getConfianza() > mejorResultado.getConfianza()) {
                    mejorResultado = result;
                }
            }
        }

        // Si ya tenemos resultado con calle, usarlo
        if (mejorResultado != null && mejorResultado.getConfianza() >= 3) {
            return mejorResultado;
        }

        // Intento 3: Solo localidad y provincia
        if (!localidad.isEmpty()) {
            String query = String.format("%s, %s, Argentina", localidad, provincia);
            System.out.println("[GeoService] Intento 3 (localidad): " + query);
            GeoData result = obtenerGeoData(query, apiKey);
            if (result != null && result.getCoordenadas() != null) {
                result.setNivelPrecision("LOCALIDAD");
                System.out.println("[GeoService] Resultado: confianza=" + result.getConfianza());
                if (mejorResultado == null || result.getConfianza() > mejorResultado.getConfianza()) {
                    mejorResultado = result;
                }
            }
        }

        // Intento 4: Solo provincia (último recurso)
        if (mejorResultado == null && !provincia.isEmpty()) {
            String query = String.format("%s, Argentina", provincia);
            System.out.println("[GeoService] Intento 4 (provincia): " + query);
            GeoData result = obtenerGeoData(query, apiKey);
            if (result != null && result.getCoordenadas() != null) {
                result.setNivelPrecision("PROVINCIA");
                mejorResultado = result;
            }
        }

        return mejorResultado;
    }

    /**
     * Normaliza una dirección expandiendo abreviaturas comunes argentinas.
     */
    public static String normalizarDireccion(String direccion) {
        if (direccion == null || direccion.isEmpty()) {
            return "";
        }
        return direccion
                .replace("Av.", "Avenida")
                .replace("Av ", "Avenida ")
                .replace("Gral.", "General")
                .replace("Gral ", "General ")
                .replace("Cnel.", "Coronel")
                .replace("Cnel ", "Coronel ")
                .replace("Brig.", "Brigadier")
                .replace("Pte.", "Presidente")
                .replace("Dr.", "Doctor")
                .replace("Ing.", "Ingeniero")
                .replace("Sta.", "Santa")
                .replace("Sto.", "Santo")
                .replace("S. ", "San ")
                .replace("Cte.", "Comandante")
                .replace("Cap.", "Capitán")
                .replace("Tte.", "Teniente")
                .trim();
    }

    /**
     * Extrae solo los dígitos de un código postal alfanumérico.
     * Ejemplo: "B1708BXA" -> "1708"
     */
    public static String extraerDigitos(String codigoPostal) {
        if (codigoPostal == null || codigoPostal.isEmpty()) {
            return "";
        }
        return codigoPostal.replaceAll("[^0-9]", "");
    }

    /**
     * Verifica si las coordenadas están dentro del bounding box de Argentina.
     */
    public static boolean coordenadasEnArgentina(double lat, double lng) {
        return lat >= AR_LAT_MIN && lat <= AR_LAT_MAX
                && lng >= AR_LNG_MIN && lng <= AR_LNG_MAX;
    }

    /**
     * Retorna un objeto con coordenadas y código postal.
     */
    public static GeoData obtenerGeoData(String direccionCompleta, String apiKey) throws Exception {
        String urlStr = String.format(
                "https://api.opencagedata.com/geocode/v1/json?q=%s&key=%s&language=es&countrycode=AR&limit=1",
                URLEncoder.encode(direccionCompleta, "UTF-8"), apiKey);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "BiometricApp/1.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) {
                System.err.println("[GeoService] HTTP error: " + conn.getResponseCode());
                return null;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JSONObject json = new JSONObject(response.toString());
            JSONArray results = json.optJSONArray("results");
            if (results == null || results.isEmpty())
                return null;

            JSONObject firstResult = results.getJSONObject(0);

            // Coordenadas
            JSONObject geometry = firstResult.optJSONObject("geometry");
            if (geometry == null)
                return null;
            double lat = geometry.optDouble("lat", 0.0);
            double lng = geometry.optDouble("lng", 0.0);

            // Validar que las coordenadas estén en Argentina
            if (!coordenadasEnArgentina(lat, lng)) {
                System.err.println("[GeoService] Coordenadas fuera de Argentina: " + lat + "," + lng);
                return null;
            }

            // Código postal y confianza
            JSONObject components = firstResult.optJSONObject("components");
            String codigoPostal = (components != null) ? components.optString("postcode", null) : null;
            String tipoResultado = (components != null) ? components.optString("_type", "unknown") : "unknown";
            int confianza = firstResult.optInt("confidence", 0);

            GeoData geoData = new GeoData();
            geoData.setCoordenadas(lat + "," + lng);
            geoData.setCodigoPostal(codigoPostal);
            geoData.setConfianza(confianza);
            geoData.setTipoResultado(tipoResultado);

            return geoData;

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Clase para encapsular los datos de geocodificación.
     */
    public static class GeoData {
        private String coordenadas;
        private String codigoPostal;
        private int confianza;
        private String tipoResultado;
        private String nivelPrecision;

        public String getCoordenadas() {
            return coordenadas;
        }

        public void setCoordenadas(String coordenadas) {
            this.coordenadas = coordenadas;
        }

        public String getCodigoPostal() {
            return codigoPostal;
        }

        public void setCodigoPostal(String codigoPostal) {
            this.codigoPostal = codigoPostal;
        }

        public int getConfianza() {
            return confianza;
        }

        public void setConfianza(int confianza) {
            this.confianza = confianza;
        }

        public String getTipoResultado() {
            return tipoResultado;
        }

        public void setTipoResultado(String tipoResultado) {
            this.tipoResultado = tipoResultado;
        }

        public String getNivelPrecision() {
            return nivelPrecision;
        }

        public void setNivelPrecision(String nivelPrecision) {
            this.nivelPrecision = nivelPrecision;
        }

        /**
         * Indica si el resultado tiene buena precisión (confianza >= 7)
         */
        public boolean esConfiable() {
            return confianza >= 7;
        }

        /**
         * Indica si el resultado es aproximado (confianza < 5)
         */
        public boolean esAproximado() {
            return confianza < 5;
        }
    }
}
