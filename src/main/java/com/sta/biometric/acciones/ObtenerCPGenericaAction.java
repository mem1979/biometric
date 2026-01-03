package com.sta.biometric.acciones;

import java.lang.reflect.*;
import java.util.*;

import org.openxava.actions.*;

import com.sta.biometric.embebidas.*;
import com.sta.biometric.servicios.*;
import com.sta.biometric.servicios.AsignarCoordenadasService.*;

/**
 * Acción para obtener código postal de una dirección usando geocodificación.
 * Requiere que todos los datos de dirección estén completos.
 * Preserva el código postal existente si es más completo (formato CPA).
 */
public class ObtenerCPGenericaAction extends ViewBaseAction {

	@Override
	public void execute() throws Exception {
		Object entidad = getView().getEntity();

		if (entidad == null) {
			addError("No se pudo acceder a la entidad actual.");
			return;
		}

		// Buscar método getDireccion()
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
			addError("Complete los siguientes campos antes de obtener el código postal: "
					+ String.join(", ", camposFaltantes));
			return;
		}

		// Verificar si ya tiene un código postal completo (CPA de 8 caracteres)
		String cpExistente = direccion.getCodigoPostal();
		boolean tieneCPCompleto = cpExistente != null && cpExistente.length() == 8;

		if (tieneCPCompleto) {
			addMessage("Ya existe un código postal completo (CPA): " + cpExistente);
			return;
		}

		try {
			// Usar el servicio mejorado
			GeoData geoData = AsignarCoordenadasService.obtenerGeoDataConFallback(direccion,
					ConfiguracionesPreferencias.getInstance().getProperties().getProperty("OPENCAGE_API_KEY"));

			if (geoData == null || geoData.getCodigoPostal() == null || geoData.getCodigoPostal().isEmpty()) {
				addWarning("No se encontró un código postal para la dirección.");
				return;
			}

			String cpNuevo = geoData.getCodigoPostal();

			// Si ya tiene un CP parcial y el nuevo es más corto, conservar el existente
			if (cpExistente != null && !cpExistente.isEmpty() && cpNuevo.length() < cpExistente.length()) {
				addMessage("Se conserva el código postal existente: " + cpExistente +
						" (el detectado era más corto: " + cpNuevo + ")");
				return;
			}

			// Asignar el nuevo código postal
			direccion.setCodigoPostal(cpNuevo);
			getView().setValueNotifying("direccion.codigoPostal", cpNuevo);

			StringBuilder mensaje = new StringBuilder();
			mensaje.append("Código postal actualizado: ").append(cpNuevo);

			if (geoData.getNivelPrecision() != null) {
				mensaje.append(" (Precisión: ").append(geoData.getNivelPrecision()).append(")");
			}

			if (geoData.esAproximado()) {
				addWarning(mensaje.toString() + " - CÓDIGO POSTAL APROXIMADO");
			} else {
				addMessage(mensaje.toString());
			}

			// Si también se obtuvieron coordenadas y no las tenía, asignarlas
			if (geoData.getCoordenadas() != null) {
				String ubicacionExistente = direccion.getUbicacion();
				if (ubicacionExistente == null || ubicacionExistente.isEmpty()) {
					direccion.setUbicacion(geoData.getCoordenadas());
					getView().setValueNotifying("direccion.ubicacion", geoData.getCoordenadas());
					addMessage("Coordenadas detectadas: " + geoData.getCoordenadas());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			addError("Error al obtener el código postal: " + e.getMessage());
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
