package com.sta.biometric.acciones;

import java.lang.reflect.*;
import java.util.*;

import org.openxava.actions.*;

import com.sta.biometric.embebidas.*;
import com.sta.biometric.servicios.*;
import com.sta.biometric.servicios.AsignarCoordenadasService.*;

/**
 * Acción para obtener coordenadas de una dirección usando geocodificación.
 * Requiere que todos los datos de dirección estén completos.
 */
public class ObtenerCoordenadasGenericaAction extends ViewBaseAction {

	@Override
	public void execute() throws Exception {
		Object entidad = getView().getEntity();

		if (entidad == null) {
			addError("No se pudo acceder a la entidad actual.");
			return;
		}

		// Buscar un método llamado "getDireccion"
		Method metodoGetDireccion;
		try {
			metodoGetDireccion = entidad.getClass().getMethod("getDireccion");
		} catch (NoSuchMethodException e) {
			addError("La entidad no tiene un método getDireccion(). No se puede obtener dirección.");
			return;
		}

		// Invocar getDireccion()
		Object direccionObj = metodoGetDireccion.invoke(entidad);
		if (!(direccionObj instanceof Direccion)) {
			addError("El método getDireccion() no devuelve un objeto de tipo Direccion.");
			return;
		}

		Direccion direccion = (Direccion) direccionObj;

		// Validar que TODOS los campos obligatorios estén completos
		List<String> camposFaltantes = validarCamposObligatorios(direccion);
		if (!camposFaltantes.isEmpty()) {
			addError("Complete los siguientes campos antes de obtener coordenadas: "
					+ String.join(", ", camposFaltantes));
			return;
		}

		try {
			// Usar el servicio mejorado con fallback
			GeoData geoData = AsignarCoordenadasService.obtenerGeoDataConFallback(direccion,
					ConfiguracionesPreferencias.getInstance().getProperties().getProperty("OPENCAGE_API_KEY"));

			if (geoData == null || geoData.getCoordenadas() == null) {
				addWarning("No se encontraron coordenadas para la dirección especificada.");
				return;
			}

			// Asignar coordenadas
			direccion.setUbicacion(geoData.getCoordenadas());
			getView().setValueNotifying("direccion.ubicacion", geoData.getCoordenadas());

			// Mensaje con nivel de precisión
			StringBuilder mensaje = new StringBuilder();
			mensaje.append("Coordenadas asignadas: ").append(geoData.getCoordenadas());

			if (geoData.getNivelPrecision() != null) {
				mensaje.append(" (Precisión: ").append(geoData.getNivelPrecision()).append(")");
			}

			if (geoData.esAproximado()) {
				addWarning(mensaje.toString() + " - UBICACIÓN APROXIMADA");
			} else {
				addMessage(mensaje.toString());
			}

			// Si la API devolvió un código postal y no tenemos uno, asignarlo
			if (geoData.getCodigoPostal() != null && !geoData.getCodigoPostal().isEmpty()) {
				String cpExistente = direccion.getCodigoPostal();
				if (cpExistente == null || cpExistente.isEmpty()) {
					direccion.setCodigoPostal(geoData.getCodigoPostal());
					getView().setValueNotifying("direccion.codigoPostal", geoData.getCodigoPostal());
					addMessage("Código postal detectado: " + geoData.getCodigoPostal());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			addError("Error al obtener las coordenadas: " + e.getMessage());
		}
	}

	/**
	 * Valida que todos los campos obligatorios para geocodificación estén
	 * completos.
	 * 
	 * @return Lista de nombres de campos faltantes (vacía si todos están completos)
	 */
	private List<String> validarCamposObligatorios(Direccion direccion) {
		List<String> faltantes = new ArrayList<>();

		if (direccion.getProvincia() == null) {
			faltantes.add("Provincia");
		}
		if (direccion.getPartido() == null) {
			faltantes.add("Partido");
		}
		if (direccion.getLocalidad() == null) {
			faltantes.add("Localidad");
		}
		if (direccion.getCalle() == null || direccion.getCalle().trim().isEmpty()) {
			faltantes.add("Calle");
		}
		if (direccion.getNumero() == null || direccion.getNumero().trim().isEmpty()) {
			faltantes.add("Número");
		}

		return faltantes;
	}
}
