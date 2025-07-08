package com.sta.biometric.acciones;
import java.lang.reflect.*;

import org.openxava.actions.*;

import com.sta.biometric.embebidas.*;
import com.sta.biometric.servicios.*;
import com.sta.biometric.servicios.AsignarCoordenadasService.*;

public class ObtenerCPGenericaAction extends ViewBaseAction {

	private final String apiKey = ConfiguracionesPreferencias.getInstance()
		.getProperties().getProperty("OPENCAGE_API_KEY");

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

		String calle = direccion.getCalle();
		String numero = direccion.getNumero();
		String localidad = direccion.getLocalidad() != null ? direccion.getLocalidad().getNombre() : "";
		String provincia = direccion.getProvincia() != null ? direccion.getProvincia().getNombre() : "";

		// No incluir código postal porque aún no lo tenemos
		String direccionCompleta = String.format("%s %s, %s, %s",
				calle != null ? calle : "",
				numero != null ? numero : "",
				localidad,
				provincia);

		try {
			GeoData geoData = AsignarCoordenadasService.obtenerGeoData(direccionCompleta, apiKey);

			if (geoData == null || geoData.getCodigoPostal() == null || geoData.getCodigoPostal().isEmpty()) {
				addWarning("No se encontró código postal para la dirección.");
				return;
			}

			// Asignar código postal
			try {
				Method setCodigoPostal = direccion.getClass().getMethod("setCodigoPostal", String.class);
				setCodigoPostal.invoke(direccion, geoData.getCodigoPostal());
				getView().setValueNotifying("direccion.codigoPostal", geoData.getCodigoPostal());
				addMessage("Código postal actualizado: " + geoData.getCodigoPostal());
			} catch (NoSuchMethodException e) {
				addError("No se encontró un método setCodigoPostal(String) en la clase Direccion.");
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			addError("Error al obtener el código postal.");
		}
	}
}
