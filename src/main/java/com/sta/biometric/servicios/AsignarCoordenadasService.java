package com.sta.biometric.servicios;
import java.io.*;
import java.net.*;

import org.json.*;

import com.sta.biometric.embebidas.*;

/**
 * Servicio para invocar la API de OpenCage y extraer coordenadas y/o codigo postal.
 */

public class AsignarCoordenadasService {

	private static final String apiKey = ConfiguracionesPreferencias.getInstance()
	        .getProperties().getProperty("OPENCAGE_API_KEY");

	    public static void asignarCoordenadasSiFaltan(Direccion direccion) throws Exception {
	        if (direccion == null || direccion.getUbicacion() != null) return;

	        String calle = direccion.getCalle();
	        String numero = direccion.getNumero();
	        String localidad = direccion.getLocalidad() != null ? direccion.getLocalidad().getNombre() : "";
	        String provincia = direccion.getProvincia() != null ? direccion.getProvincia().getNombre() : "";
	        String codigoPostal = direccion.getCodigoPostal() != null ? direccion.getCodigoPostal() : "";

	        String direccionCompleta = String.format("%s %s, %s, %s, %s",
	                calle != null ? calle : "",
	                numero != null ? numero : "",
	                localidad,
	                provincia,
	                codigoPostal);

	        GeoData geoData = AsignarCoordenadasService.obtenerGeoData(direccionCompleta, apiKey);
	        if (geoData != null && geoData.getCoordenadas() != null) {
	            direccion.setUbicacion(geoData.getCoordenadas());
	        }
	    }
	
    /**
     * Retorna un objeto con coordenadas y codigo postal.
     */
    public static GeoData obtenerGeoData(String direccionCompleta, String apiKey) throws Exception {
        String urlStr = String.format(
            "https://api.opencagedata.com/geocode/v1/json?q=%s&key=%s&language=es&countrycode=AR&limit=1&no_annotations=1",
            URLEncoder.encode(direccionCompleta, "UTF-8"), apiKey
        );

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "OpenXavaApp");

            if (conn.getResponseCode() != 200) {
                throw new IOException("HTTP error code: " + conn.getResponseCode());
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JSONObject json = new JSONObject(response.toString());
            JSONArray results = json.optJSONArray("results");
            if (results == null || results.isEmpty()) return null;

            JSONObject firstResult = results.getJSONObject(0);

            // Coordenadas
            JSONObject geometry = firstResult.optJSONObject("geometry");
            if (geometry == null) return null;
            double lat = geometry.optDouble("lat", 0.0);
            double lng = geometry.optDouble("lng", 0.0);

            // Código postal
            JSONObject components = firstResult.optJSONObject("components");
            String codigoPostal = (components != null) ? components.optString("postcode", null) : null;

            GeoData geoData = new GeoData();
            geoData.setCoordenadas(lat + "," + lng);
            geoData.setCodigoPostal(codigoPostal);
            return geoData;

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Clase para encapsular las coordenadas y el codigo postal.
     */
    public static class GeoData {
        private String coordenadas;
        private String codigoPostal;

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
    }
}
