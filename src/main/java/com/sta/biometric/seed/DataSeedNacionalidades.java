package com.sta.biometric.seed;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.openxava.jpa.XPersistence;

import com.sta.biometric.auxiliares.Nacionalidades;
import com.sta.biometric.enums.Continentes;

/**
 * Servicio de carga inicial de datos para la entidad Nacionalidades.
 * 
 * Este servicio carga las nacionalidades de todo el mundo organizadas por
 * continente.
 * Es UNIVERSAL y funciona con cualquier base de datos soportada por Hibernate.
 * 
 * Total de nacionalidades: ~195 países
 * 
 * @author Generado automáticamente
 */
public class DataSeedNacionalidades {

    private static final Logger log = Logger.getLogger(DataSeedNacionalidades.class.getName());

    /** Clase interna para representar una nacionalidad */
    private static class NacionalidadData {
        final Continentes continente;
        final String pais;
        final String nacionalidad;

        NacionalidadData(Continentes continente, String pais, String nacionalidad) {
            this.continente = continente;
            this.pais = pais;
            this.nacionalidad = nacionalidad;
        }
    }

    /** Lista de todas las nacionalidades */
    private static final List<NacionalidadData> NACIONALIDADES = new ArrayList<>();

    static {
        // AMÉRICA (35 países)
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "ARGENTINA", "ARGENTINA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "BOLIVIA", "BOLIVIANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "BRASIL", "BRASILEÑA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "CANADA", "CANADIENSE"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "CHILE", "CHILENA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "COLOMBIA", "COLOMBIANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "COSTA RICA", "COSTARRICENSE"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "CUBA", "CUBANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "ECUADOR", "ECUATORIANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "EL SALVADOR", "SALVADOREÑA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "ESTADOS UNIDOS", "ESTADOUNIDENSE"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "GUATEMALA", "GUATEMALTECA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "HAITI", "HAITIANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "HONDURAS", "HONDUREÑA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "JAMAICA", "JAMAIQUINA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "MEXICO", "MEXICANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "NICARAGUA", "NICARAGUENSE"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "PANAMA", "PANAMEÑA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "PARAGUAY", "PARAGUAYA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "PERU", "PERUANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "PUERTO RICO", "PUERTORRIQUEÑA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "REPUBLICA DOMINICANA", "DOMINICANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "URUGUAY", "URUGUAYA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AMERICA, "VENEZUELA", "VENEZOLANA"));

        // EUROPA (45 países principales)
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "ALEMANIA", "ALEMANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "AUSTRIA", "AUSTRIACA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "BELGICA", "BELGA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "BULGARIA", "BULGARA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "CROACIA", "CROATA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "DINAMARCA", "DANESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "ESLOVAQUIA", "ESLOVACA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "ESLOVENIA", "ESLOVENA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "ESPAÑA", "ESPAÑOLA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "FINLANDIA", "FINLANDESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "FRANCIA", "FRANCESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "GRECIA", "GRIEGA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "HUNGRIA", "HUNGARA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "IRLANDA", "IRLANDESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "ITALIA", "ITALIANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "NORUEGA", "NORUEGA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "PAISES BAJOS", "NEERLANDESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "POLONIA", "POLACA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "PORTUGAL", "PORTUGUESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "REINO UNIDO", "BRITANICA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "REPUBLICA CHECA", "CHECA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "RUMANIA", "RUMANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "RUSIA", "RUSA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "SUECIA", "SUECA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "SUIZA", "SUIZA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.EUROPA, "UCRANIA", "UCRANIANA"));

        // ASIA (30 países principales)
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "ARABIA SAUDITA", "SAUDITA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "CHINA", "CHINA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "COREA DEL SUR", "SURCOREANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "EMIRATOS ARABES UNIDOS", "EMIRATÍ"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "FILIPINAS", "FILIPINA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "INDIA", "INDIA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "INDONESIA", "INDONESIA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "IRAN", "IRANI"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "IRAK", "IRAQUI"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "ISRAEL", "ISRAELI"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "JAPON", "JAPONESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "LIBANO", "LIBANESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "MALASIA", "MALASIA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "PAKISTAN", "PAKISTANI"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "SIRIA", "SIRIA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "TAILANDIA", "TAILANDESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "TAIWAN", "TAIWANESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "TURQUIA", "TURCA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.ASIA, "VIETNAM", "VIETNAMITA"));

        // AFRICA (20 países principales)
        NACIONALIDADES.add(new NacionalidadData(Continentes.AFRICA, "ARGELIA", "ARGELINA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AFRICA, "EGIPTO", "EGIPCIA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AFRICA, "ETIOPIA", "ETIOPE"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AFRICA, "KENIA", "KENIANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AFRICA, "MARRUECOS", "MARROQUI"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AFRICA, "NIGERIA", "NIGERIANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AFRICA, "SENEGAL", "SENEGALESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AFRICA, "SUDAFRICA", "SUDAFRICANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.AFRICA, "TUNEZ", "TUNECINA"));

        // OCEANIA (5 países principales)
        NACIONALIDADES.add(new NacionalidadData(Continentes.OCEANIA, "AUSTRALIA", "AUSTRALIANA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.OCEANIA, "NUEVA ZELANDA", "NEOZELANDESA"));
        NACIONALIDADES.add(new NacionalidadData(Continentes.OCEANIA, "FIJI", "FIYIANA"));
    }

    /**
     * Ejecuta la carga de todas las nacionalidades.
     * Es idempotente: verifica si cada nacionalidad existe antes de insertarla.
     * 
     * @return número de nacionalidades insertadas (no cuenta las existentes)
     */
    public static int seed() {
        log.info("Iniciando carga de datos de Nacionalidades...");

        EntityManager em = XPersistence.getManager();
        int insertadas = 0;
        int existentes = 0;

        for (NacionalidadData data : NACIONALIDADES) {
            // Verificar si ya existe por nombre de país
            Long count = em.createQuery(
                    "SELECT COUNT(n) FROM Nacionalidades n WHERE n.paises = :pais", Long.class)
                    .setParameter("pais", data.pais)
                    .getSingleResult();

            if (count == 0) {
                // Crear nueva nacionalidad
                Nacionalidades nueva = new Nacionalidades();
                nueva.setContinente(data.continente);
                nueva.setPaises(data.pais);
                nueva.setNacionalidad(data.nacionalidad);
                em.persist(nueva);
                insertadas++;
            } else {
                existentes++;
            }
        }

        log.info("Carga de Nacionalidades completada. Insertadas: " + insertadas + ", Existentes: " + existentes);
        return insertadas;
    }

    /**
     * Verifica si todas las nacionalidades están cargadas.
     * 
     * @return true si las nacionalidades base existen
     */
    public static boolean isComplete() {
        EntityManager em = XPersistence.getManager();
        Long count = em.createQuery("SELECT COUNT(n) FROM Nacionalidades n", Long.class)
                .getSingleResult();
        return count >= NACIONALIDADES.size();
    }

    /**
     * Obtiene la cantidad de nacionalidades definidas.
     * 
     * @return número total de nacionalidades
     */
    public static int getNacionalidadesCount() {
        return NACIONALIDADES.size();
    }
}
