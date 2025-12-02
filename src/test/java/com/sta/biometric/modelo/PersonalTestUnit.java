package com.sta.biometric.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sta.biometric.auxiliares.Dni;

/**
 * Tests unitarios para Personal (no requieren app corriendo)
 * 
 * NOTA: nombreCompleto es un campo que se llena en @PrePersist,
 * por lo tanto en tests sin DB usamos getApellidoNombre()
 */
class PersonalTestUnit {

    @Test
    void testApellidoNombreSeCalcula() {
        Personal empleado = new Personal();
        empleado.setApellido("García");
        empleado.setNombres("María");

        String apellidoNombre = empleado.getApellidoNombre();

        assertEquals("García, María", apellidoNombre);
    }

    @Test
    void testApellidoNombreConApellidoSolo() {
        Personal empleado = new Personal();
        empleado.setApellido("López");
        empleado.setNombres(null);

        String apellidoNombre = empleado.getApellidoNombre();

        // Debería concatenar apellido + ", " + null
        assertTrue(apellidoNombre.contains("López"));
    }

    @Test
    void testApellidoNombreConNombresSolo() {
        Personal empleado = new Personal();
        empleado.setApellido(null);
        empleado.setNombres("Carlos");

        String apellidoNombre = empleado.getApellidoNombre();

        // Debería concatenar null + ", " + Carlos
        assertTrue(apellidoNombre.contains("Carlos"));
    }

    @Test
    void testDNISeAsignaCorrectamente() {
        Personal empleado = new Personal();

        // Crear instancia de Dni
        Dni dni = new Dni();
        dni.setNumero("35123456");

        empleado.setDni(dni);

        assertNotNull(empleado.getDni());
        assertEquals("35123456", empleado.getDni().getNumero());
    }

    @Test
    void testCUILSeAsignaCorrectamente() {
        Personal empleado = new Personal();
        empleado.setCuil("27-35123456-8");

        assertEquals("27-35123456-8", empleado.getCuil());
    }

    @Test
    void testCreaUsuarioSeGeneraCorrectamente() {
        Personal empleado = new Personal();
        empleado.setApellido("García");
        empleado.setNombres("María");
        empleado.setUserId("EMP001");

        String usuario = empleado.getCreaUsuario();

        // Esperado: M + García + @ + EMP001
        assertEquals("MGarcía@EMP001", usuario);
    }
}
