package com.sta.biometric.servicios;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;

import javax.json.*;
import javax.persistence.*;

import org.openxava.jpa.*;

import com.sta.biometric.auxiliares.*;

public class ImportadorFeriadosService {

    public static void importarFeriadosDelAnioActual() throws Exception {
        int anio = LocalDate.now().getYear();
        String apiUrl = "https://api.argentinadatos.com/v1/feriados/" + anio;

        // === Limpiar feriados fuera del año actual ===
        LocalDate desde = LocalDate.of(anio, 1, 1);
        LocalDate hasta = LocalDate.of(anio, 12, 31);

        EntityManager em = XPersistence.getManager();
        em.createQuery("DELETE FROM Feriados f WHERE f.fecha < :desde OR f.fecha > :hasta")
            .setParameter("desde", desde)
            .setParameter("hasta", hasta)
            .executeUpdate();

        // === Conexion a la API ===
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Error al obtener feriados: HTTP " + conn.getResponseCode());
        }

        try (InputStream is = conn.getInputStream();
             JsonReader reader = Json.createReader(is)) {

            JsonArray feriados = reader.readArray();

            for (JsonValue value : feriados) {
                JsonObject obj = value.asJsonObject();

                LocalDate fecha = LocalDate.parse(obj.getString("fecha"));

                // Verificar duplicado
                List<Feriados> existentes = em.createQuery("select f from Feriados f where f.fecha = :fecha", Feriados.class)
                        .setParameter("fecha", fecha)
                        .getResultList();

                if (!existentes.isEmpty()) continue;

                Feriados f = new Feriados();
                f.setFecha(fecha);
                f.setTipo(obj.getString("tipo", "SIN CLASIFICAR"));
                f.setMotivo(obj.getString("nombre", "Sin motivo"));
                f.setInfoAdicional("Importado automóticamente desde ArgentinaDatos.com");

                em.persist(f);
            }
        }
    }
}
