package com.sta.biometric.seed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.openxava.jpa.XPersistence;

import com.sta.biometric.auxiliares.Localidades;
import com.sta.biometric.auxiliares.Partidos;
import com.sta.biometric.auxiliares.Provincias;

/**
 * Servicio de carga inicial de datos para la entidad Localidades.
 * 
 * Este servicio carga las localidades desde el archivo CSV incluido en
 * resources.
 * Es UNIVERSAL y funciona con cualquier base de datos soportada por Hibernate.
 * 
 * IMPORTANTE: Requiere que Partidos y Provincias ya estén cargados.
 * 
 * Total de localidades: 3633
 * 
 * @author Generado automáticamente
 */
public class DataSeedLocalidades {

    private static final Logger log = Logger.getLogger(DataSeedLocalidades.class.getName());
    private static final String CSV_FILE = "/sql/localidades-data.csv";
    private static final int TOTAL_LOCALIDADES = 3633;

    /**
     * Ejecuta la carga de todas las localidades desde el archivo CSV.
     * IMPORTANTE: Partidos y Provincias deben estar cargados previamente.
     * Es idempotente: verifica si cada localidad existe antes de insertarla.
     * 
     * @return número de localidades insertadas (no cuenta las existentes)
     */
    public static int seed() {
        log.info("Iniciando carga de datos de Localidades (3633 registros)...");

        // Verificar dependencias
        if (!DataSeedProvincias.isComplete()) {
            log.warning("Provincias incompletas. Ejecutando DataSeedProvincias.seed()...");
            DataSeedProvincias.seed();
        }
        if (!DataSeedPartidos.isComplete()) {
            log.warning("Partidos incompletos. Ejecutando DataSeedPartidos.seed()...");
            DataSeedPartidos.seed();
        }

        EntityManager em = XPersistence.getManager();
        int insertados = 0;
        int existentes = 0;
        int errores = 0;

        try (InputStream is = DataSeedLocalidades.class.getResourceAsStream(CSV_FILE);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue; // Saltar líneas vacías y comentarios
                }

                String[] campos = linea.split(",");
                if (campos.length < 4) {
                    errores++;
                    continue;
                }

                try {
                    int numero = Integer.parseInt(campos[0].trim());
                    String nombre = campos[1].trim();
                    int partidoNumero = Integer.parseInt(campos[2].trim());
                    int provinciaNumero = Integer.parseInt(campos[3].trim());

                    // Verificar si ya existe
                    Localidades existente = em.find(Localidades.class, numero);
                    if (existente != null) {
                        existentes++;
                        continue;
                    }

                    // Obtener partido y provincia
                    Partidos partido = em.find(Partidos.class, partidoNumero);
                    Provincias provincia = em.find(Provincias.class, provinciaNumero);

                    if (partido == null || provincia == null) {
                        log.warning("Referencia no encontrada para localidad " + numero +
                                ": partido=" + partidoNumero + ", provincia=" + provinciaNumero);
                        errores++;
                        continue;
                    }

                    // Crear nueva localidad
                    Localidades nueva = new Localidades();
                    nueva.setNumero(numero);
                    nueva.setNombre(nombre);
                    nueva.setPartido(partido);
                    nueva.setProvincia(provincia);
                    em.persist(nueva);
                    insertados++;

                    // Flush cada 500 registros para evitar problemas de memoria
                    if (insertados % 500 == 0) {
                        em.flush();
                        log.info("Progreso: " + insertados + " localidades insertadas...");
                    }

                } catch (NumberFormatException e) {
                    errores++;
                }
            }

        } catch (Exception e) {
            log.severe("Error al leer archivo CSV: " + e.getMessage());
            throw new RuntimeException("Error cargando localidades desde CSV", e);
        }

        log.info("Carga de Localidades completada. Insertadas: " + insertados +
                ", Existentes: " + existentes + ", Errores: " + errores);
        return insertados;
    }

    /**
     * Verifica si todas las localidades están cargadas.
     * 
     * @return true si las 3633 localidades existen
     */
    public static boolean isComplete() {
        EntityManager em = XPersistence.getManager();
        Long count = em.createQuery("SELECT COUNT(l) FROM Localidades l", Long.class)
                .getSingleResult();
        return count >= TOTAL_LOCALIDADES;
    }

    /**
     * Obtiene la cantidad de localidades definidas.
     * 
     * @return número total de localidades (3633)
     */
    public static int getLocalidadesCount() {
        return TOTAL_LOCALIDADES;
    }
}
