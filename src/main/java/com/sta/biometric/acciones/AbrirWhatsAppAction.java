package com.sta.biometric.acciones;

import org.openxava.actions.*;

/**
 * Acción para abrir WhatsApp Web con el número de celular del empleado.
 * 
 * <p>
 * Formato del número:
 * <ul>
 * <li>Elimina paréntesis, espacios y guiones</li>
 * <li>Mantiene el código de país si está presente</li>
 * <li>Genera URL: https://wa.me/NUMERO</li>
 * </ul>
 * </p>
 * 
 * @author Sistema STARH - Mosquera, Marcelo
 * @since 2.0
 */
public class AbrirWhatsAppAction extends ViewBaseAction implements IForwardAction {

    private String celular;

    @Override
    public void execute() throws Exception {
        // Obtener el número de celular desde la vista
        Object celularValue = getView().getValue("contacto.celular");

        if (celularValue != null) {
            celular = celularValue.toString();
        }

        // Validar que el número no esté vacío
        if (celular == null || celular.trim().isEmpty()) {
            addError("No hay número de celular registrado");
            return;
        }
    }

    @Override
    public String getForwardURI() {
        if (celular == null || celular.trim().isEmpty()) {
            return null; // No redirigir si no hay número
        }

        // Formatear el número: eliminar paréntesis, espacios, guiones
        String numeroFormateado = formatearNumero(celular);

        // Generar URL de WhatsApp con wa.me
        return "https://wa.me/" + numeroFormateado;
    }

    @Override
    public boolean inNewWindow() {
        return true; // Abrir en nueva ventana/pestaña
    }

    /**
     * Formatea el número de celular para WhatsApp.
     * 
     * <p>
     * Ejemplo: "(0351)155123456" → "5493515123456"
     * <ul>
     * <li>Elimina paréntesis, espacios, guiones</li>
     * <li>Si comienza con 0 (Argentina), reemplaza por código de país 54</li>
     * <li>Si contiene 15 (prefijo móvil AR), lo elimina y agrega 9</li>
     * </ul>
     * </p>
     * 
     * @param numero Número de celular en formato original
     * @return Número formateado para wa.me (solo dígitos)
     */
    private String formatearNumero(String numero) {
        if (numero == null) {
            return "";
        }

        // Eliminar todos los caracteres no numéricos
        String soloDigitos = numero.replaceAll("[^0-9]", "");

        // Si está vacío después de limpiar, retornar vacío
        if (soloDigitos.isEmpty()) {
            return "";
        }

        // Lógica para números argentinos:
        // Formato típico: (0351)155123456 o 03515123456
        // Debe quedar: 5493515123456

        // Si comienza con 0 (código de área argentina)
        if (soloDigitos.startsWith("0")) {
            soloDigitos = soloDigitos.substring(1); // Quitar el 0
        }

        // Si contiene 15 después del código de área (3 o 4 dígitos de área)
        // Por ejemplo: 35115XXXXXXX → 549351XXXXXXX
        // 11 15XXXXXXX → 549 11 XXXXXXX
        if (soloDigitos.length() >= 10) {
            // Buscar el 15 y reemplazarlo por 9
            StringBuilder resultado = new StringBuilder();
            int posicion15 = -1;

            // Detectar posición del 15 (después del código de área de 2-4 dígitos)
            for (int i = 2; i <= 4 && i + 1 < soloDigitos.length(); i++) {
                if (soloDigitos.substring(i, i + 2).equals("15")) {
                    posicion15 = i;
                    break;
                }
            }

            if (posicion15 > 0) {
                // Código de área + 9 + resto del número (sin el 15)
                resultado.append("54");
                resultado.append("9");
                resultado.append(soloDigitos.substring(0, posicion15));
                resultado.append(soloDigitos.substring(posicion15 + 2));
                return resultado.toString();
            }
        }

        // Si no tiene el patrón típico argentino, agregar código de país 54
        if (!soloDigitos.startsWith("54")) {
            soloDigitos = "54" + soloDigitos;
        }

        return soloDigitos;
    }
}
