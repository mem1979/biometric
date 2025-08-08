package com.sta.biometric.modelo;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import org.openxava.jpa.XPersistence;

/**
 * Listener de entidad para ColeccionRegistros que consolida la asistencia diaria
 * luego de insertar o actualizar un registro.
 */
public class ColeccionRegistrosListener {

    @PostPersist
    public void postPersist(ColeccionRegistros registro) {
        consolidar(registro);
    }

    @PostUpdate
    public void postUpdate(ColeccionRegistros registro) {
        consolidar(registro);
    }

    private void consolidar(ColeccionRegistros registro) {
        AuditoriaRegistros asistenciaDiaria = registro.getAsistenciaDiaria();
        if (asistenciaDiaria != null) {
            asistenciaDiaria.consolidarDesdeRegistros();
            XPersistence.getManager().merge(asistenciaDiaria);
        }
    }
}
