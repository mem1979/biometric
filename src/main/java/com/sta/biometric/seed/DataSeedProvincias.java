package com.sta.biometric.seed;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.openxava.jpa.XPersistence;

import com.sta.biometric.auxiliares.Provincias;

/**
 * Servicio de carga inicial de datos para la entidad Provincias.
 * 
 * Este servicio es UNIVERSAL y funciona con cualquier base de datos
 * soportada por Hibernate (HSQLDB, MySQL, PostgreSQL, Oracle, etc.)
 * 
 * Uso:
 * - Llamar a DataSeedProvincias.seed() para cargar todas las provincias
 * - Es idempotente: no inserta duplicados si el registro ya existe
 * 
 * @author Generado automáticamente
 */
public class DataSeedProvincias {

    private static final Logger log = Logger.getLogger(DataSeedProvincias.class.getName());

    /** Mapa de provincias argentinas (numero -> nombre) */
    private static final Map<Integer, String> PROVINCIAS = new LinkedHashMap<>();

    static {
        PROVINCIAS.put(1, "BUENOS AIRES");
        PROVINCIAS.put(2, "CATAMARCA");
        PROVINCIAS.put(3, "CHACO");
        PROVINCIAS.put(4, "CHUBUT");
        PROVINCIAS.put(5, "CABA");
        PROVINCIAS.put(6, "CORDOBA");
        PROVINCIAS.put(7, "CORRIENTES");
        PROVINCIAS.put(8, "ENTRE RIOS");
        PROVINCIAS.put(9, "FORMOSA");
        PROVINCIAS.put(10, "JUJUY");
        PROVINCIAS.put(11, "LA PAMPA");
        PROVINCIAS.put(12, "LA RIOJA");
        PROVINCIAS.put(13, "MENDOZA");
        PROVINCIAS.put(14, "MISIONES");
        PROVINCIAS.put(15, "NEUQUEN");
        PROVINCIAS.put(16, "RIO NEGRO");
        PROVINCIAS.put(17, "SALTA");
        PROVINCIAS.put(18, "SAN JUAN");
        PROVINCIAS.put(19, "SAN LUIS");
        PROVINCIAS.put(20, "SANTA FE");
        PROVINCIAS.put(21, "SANTIAGO DEL ESTERO");
        PROVINCIAS.put(22, "TIERRA DEL FUEGO");
        PROVINCIAS.put(23, "TUCUMAN");
        PROVINCIAS.put(24, "SANTA CRUZ");
    }

    /**
     * Ejecuta la carga de todas las provincias.
     * Es idempotente: verifica si cada provincia existe antes de insertarla.
     * 
     * @return número de provincias insertadas (no cuenta las existentes)
     */
    public static int seed() {
        log.info("Iniciando carga de datos de Provincias...");

        EntityManager em = XPersistence.getManager();
        int insertadas = 0;
        int existentes = 0;

        for (Map.Entry<Integer, String> entry : PROVINCIAS.entrySet()) {
            int numero = entry.getKey();
            String nombre = entry.getValue();

            // Verificar si ya existe
            Provincias existente = em.find(Provincias.class, numero);

            if (existente == null) {
                // Crear nueva provincia
                Provincias nueva = new Provincias();
                nueva.setNumero(numero);
                nueva.setNombre(nombre);
                em.persist(nueva);
                insertadas++;
                log.fine("Insertada: " + numero + " - " + nombre);
            } else {
                existentes++;
                log.fine("Ya existe: " + numero + " - " + nombre);
            }
        }

        log.info("Carga de Provincias completada. Insertadas: " + insertadas + ", Existentes: " + existentes);
        return insertadas;
    }

    /**
     * Limpia todas las provincias y las recarga desde cero.
     * ¡PRECAUCIÓN! Esto eliminará TODAS las provincias existentes.
     * 
     * @return número de provincias insertadas
     */
    public static int resetAndSeed() {
        log.warning("Ejecutando reset de Provincias...");

        EntityManager em = XPersistence.getManager();

        // Eliminar todas las provincias existentes
        int eliminadas = em.createQuery("DELETE FROM Provincias").executeUpdate();
        log.info("Provincias eliminadas: " + eliminadas);

        // Insertar todas las provincias
        int insertadas = 0;
        for (Map.Entry<Integer, String> entry : PROVINCIAS.entrySet()) {
            Provincias nueva = new Provincias();
            nueva.setNumero(entry.getKey());
            nueva.setNombre(entry.getValue());
            em.persist(nueva);
            insertadas++;
        }

        log.info("Reset de Provincias completado. Total insertadas: " + insertadas);
        return insertadas;
    }

    /**
     * Verifica si todas las provincias están cargadas.
     * 
     * @return true si las 24 provincias existen
     */
    public static boolean isComplete() {
        EntityManager em = XPersistence.getManager();
        Long count = em.createQuery("SELECT COUNT(p) FROM Provincias p", Long.class)
                .getSingleResult();
        return count >= PROVINCIAS.size();
    }

    /**
     * Obtiene el mapa de provincias definidas (para uso externo).
     * 
     * @return mapa inmutable de provincias (numero -> nombre)
     */
    public static Map<Integer, String> getProvinciasDefinidas() {
        return new LinkedHashMap<>(PROVINCIAS);
    }
}
